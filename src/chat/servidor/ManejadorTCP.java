package chat.servidor;

import chat.datos.Mensaje;
import chat.datos.MensajeArchivo;
import chat.datos.MensajeTexto;
import chat.datos.UsuarioServidor;
import chat.gui.DialogoTransferenciaArchivo;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ManejadorTCP implements Runnable {
    private Socket socketCliente;

    public ManejadorTCP(Socket socketCliente) {
        this.socketCliente = socketCliente;
    }

    @Override
    public void run() {
        try(Socket cliente = this.socketCliente) {
            DataInputStream entrada = new DataInputStream(cliente.getInputStream());
            do {
                System.out.println("Servidor TCP: esperando peticion");
                String protocolo = entrada.readUTF();
                String tipoPeticion = protocolo.split("\n", 2)[0];
                System.out.println("Nueva peticion: " + tipoPeticion);
                protocolo = protocolo.substring(protocolo.indexOf("\n") + 1);
                switch (tipoPeticion) {
                    case "tipo: registro" -> manejarRegistro(protocolo);
                    case "tipo: archivo" -> manejarMensajeArchivo(protocolo, entrada);
                    case "tipo: obtenerUsuarios" -> manejarListaUsuarios(protocolo);
                    case "tipo: obtenerChat" -> manejarObtenerChat(protocolo);
                    default -> {
                        System.out.println("Petición desconocida:");
                        System.out.println(tipoPeticion);
                        System.out.println(protocolo);
                    }
                }
            } while (true);
        } catch (Exception e) {
            ControladorSesiones.getInstance().desconectarPorSocket(socketCliente);
            System.out.println("Desconectando usuario");
            e.printStackTrace();
        }
    }

    private void manejarRegistro(String protocolo) {
        System.out.println("Registrando usuario...");
        String[] lineas = protocolo.split("\n");
        UUID uid = UUID.fromString(lineas[0].split(": ")[1]);
        String nombre = lineas[1].split(": ")[1];
        UsuarioServidor usuarioServidor = new UsuarioServidor(nombre, uid, socketCliente);
        ControladorSesiones.getInstance().conectar(usuarioServidor);
    }

    private void manejarMensajeArchivo(String protocolo, DataInputStream entrada) throws Exception {
        System.out.println("Procesando archivo...");
        String protocoloCompleto = "tipo: archivo\n" + protocolo;
        MensajeArchivo mensajeArchivo = MensajeArchivo.construirConProtocolo(protocoloCompleto);
        byte[] bytesArchivo = new byte[mensajeArchivo.getTamanio()];
        entrada.readFully(bytesArchivo);
        mensajeArchivo.setBytesArchivo(bytesArchivo);
        UUID uidRemitente = mensajeArchivo.getRemitente();
        UUID uidDestinatario = mensajeArchivo.getDestinatario();
        var sesiones = ControladorSesiones.getInstance();
        var conversaciones = ControladorConversaciones.getInstance();
        UsuarioServidor remitente = sesiones.obtenerUsuario(uidRemitente);
        UsuarioServidor destinatario = sesiones.obtenerUsuario(uidDestinatario);
        var conversacion = conversaciones.obtenerConversacion(remitente, destinatario);
        conversacion.agregarMensaje(mensajeArchivo);

        // Esta parte es la que se usa para poder medir la tasa de transferencia bps
        long tamanioArchivo = bytesArchivo.length; // Se obtiene el tamaño del archivo enviado
        long tiempoInicio = System.nanoTime(); // Se obtiene tiempo preciso actual

        try {
            DataOutputStream mensajeDestinatario = new DataOutputStream(destinatario.socketCliente().getOutputStream());
            mensajeDestinatario.writeUTF(protocoloCompleto);

            // Se envia entre bloques de 16 kB para poder medir el avance
            int tamanioBloque = 16 * 1024;
            long bytesEnviados = 0;
            long tiempoRecienteAviso = System.nanoTime();
            
            // Se hace la instancia del jdialog para que se muestre el envío para el usuario que envie el archivo
            DialogoTransferenciaArchivo dialogo = DialogoTransferenciaArchivo.mostrar(null, mensajeArchivo.getNombreArchivo(), tamanioArchivo);
            
            for (int iteradorArchivo = 0; iteradorArchivo < tamanioArchivo; iteradorArchivo += tamanioBloque) {
                int tamanio = Math.min(tamanioBloque, (int)(tamanioArchivo - iteradorArchivo));
                mensajeDestinatario.write(bytesArchivo, iteradorArchivo, tamanio);
                bytesEnviados += tamanio;

                // Se calcula el tiempo y la tasa de transferencia
                long momentoActual = System.nanoTime();
                double segundosTranscurridos = (momentoActual - tiempoInicio) / 1_000_000_000.0; // Esto es por que son nanosegundos
                double bps = (bytesEnviados * 8) / segundosTranscurridos;
                double porcentaje = (bytesEnviados * 100.0) / tamanioArchivo;
                double tiempoTotalEstimado = (tamanioArchivo * segundosTranscurridos) / bytesEnviados;
                double tiempoRestante = tiempoTotalEstimado - segundosTranscurridos;

                // Se actualiza el jdialog mostrando el progreso nuevo
                dialogo.actualizar(porcentaje, bytesEnviados, tamanioArchivo, bps, segundosTranscurridos, tiempoRestante);

                // Cada vez que pase medio segundo se muestra el avance del archivo y como va y los bps
                if ((momentoActual - tiempoRecienteAviso) > 500_000_000L || bytesEnviados == tamanioArchivo) {
                    System.out.println(
                        "------------------------------------------------------\n" +
                        "Archivo: " + String.format("%.2f", porcentaje) + "%,\n" +
                        "Bytes enviados: " + bytesEnviados + " de " + tamanioArchivo + " bytes,\n" +
                        "Tasa de transferencia: " + String.format("%.2f", bps) + " bps,\n" +
                        "Tiempo transcurrido: " + String.format("%.2f", segundosTranscurridos) + "s,\n" +
                        "Tiempo restante estimado: " + String.format("%.2f", tiempoRestante) + "s\n" +
                        "------------------------------------------------------"
                    );
                    tiempoRecienteAviso = momentoActual;
                }
                Thread.sleep(500);
            }
            // Se duerme el jdialgo
            dialogo.cerrar();
            mensajeDestinatario.flush();

            long momentoCierre = System.nanoTime();
            double tiempoTotal = (momentoCierre - tiempoInicio) / 1_000_000_000.0;
            double tasaFinal = (tamanioArchivo * 8) / tiempoTotal;
            System.out.println(
                "++++++++++++++++++++++++++++++++++++++++++++++++++++++\n"+
                "Transferencia terminada\n" +
                "Tiempo total: " + String.format("%.2f", tiempoTotal) + "s,\n" +
                "Tasa promedio de: " + String.format("%.2f", tasaFinal) + " bps" +
                "++++++++++++++++++++++++++++++++++++++++++++++++++++++"
            );
        } catch (Exception e) {
            System.out.println("Servidor TCP: error al enviar mensaje de archivo");
            e.printStackTrace();
        }
    }

    private void manejarListaUsuarios(String protocolo) {
        System.out.println("Obteniendo usuarios...");
        String[] lineas = protocolo.split("\n");
        UUID uid = UUID.fromString(lineas[0].split(": ")[1]);

        var listaUsuarios = ControladorSesiones.getInstance().obtenerUsuarios(uid);
        StringBuilder protocoloUsuarios = new StringBuilder();
        for (UsuarioServidor usuario : listaUsuarios) {
            protocoloUsuarios.append(usuario.uuid()).append(",").append(usuario.nombre()).append(";\n");
        }

        try {
            DataOutputStream dataOutputStream = new DataOutputStream(this.socketCliente.getOutputStream());
            dataOutputStream.writeUTF(protocoloUsuarios.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param protocolo
     */
    private void manejarObtenerChat(String protocolo) {
        String[] lineas = protocolo.split("\n");
        UUID remitente = UUID.fromString(lineas[0].split(": ")[1]);
        UUID destinatario = UUID.fromString(lineas[1].split(": ")[1]);
        var controlador = ControladorSesiones.getInstance();
        var usuario1 = controlador.obtenerUsuario(remitente);
        var usuario2 = controlador.obtenerUsuario(destinatario);
        var conversacion = ControladorConversaciones.getInstance().obtenerConversacion(usuario1, usuario2);
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(this.socketCliente.getOutputStream());
            for (Mensaje mensaje : conversacion.obtenerMensajes()) {
                String protocoloMensaje = mensaje.convertirAProtocolo();
                dataOutputStream.writeUTF(protocoloMensaje);
                if (mensaje instanceof MensajeTexto) {
                    dataOutputStream.writeUTF("\u001E\n"); // Separador de mensajes de texto
                } else if (mensaje instanceof MensajeArchivo archivo) {
                    dataOutputStream.write(archivo.getBytesArchivo());
                }
            }
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
