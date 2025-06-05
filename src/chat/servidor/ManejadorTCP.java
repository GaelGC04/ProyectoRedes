package chat.servidor;

import chat.datos.Conversacion;
import chat.datos.Mensaje;
import chat.datos.MensajeArchivo;
import chat.datos.MensajeTexto;
import chat.datos.UsuarioServidor;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ManejadorTCP implements Runnable {
    private Socket socketCliente;
    private volatile boolean esManejadorEscucha = false;

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
                if (esManejadorEscucha) {
                    return;
                }
                String tipoPeticion = protocolo.split("\n", 2)[0];
                System.out.println("Nueva peticion: " + tipoPeticion);
                protocolo = protocolo.substring(protocolo.indexOf("\n") + 1);
                switch (tipoPeticion) {
                    case "tipo: registro" -> manejarRegistro(protocolo);
                    case "tipo: archivo" -> manejarMensajeArchivo(protocolo, entrada);
                    case "tipo: obtenerUsuarios" -> manejarListaUsuarios(protocolo);
                    case "tipo: obtenerChat" -> manejarObtenerChat(protocolo);
                    case "tipo: escucharChat" -> manejarEscucharChat(protocolo);
                    case "tipo: descargarArchivo" -> manejarDescargarArchivo(protocolo);
                    default -> {
                        System.out.println("Petición desconocida:");
                        System.out.println(tipoPeticion);
                        System.out.println(protocolo);
                    }
                }
            } while (true);
        } catch (Exception e) {
            if (!esManejadorEscucha){
                ControladorSesiones.getInstance().desconectarPorSocket(socketCliente);
                System.out.println("Desconectando usuario");
                e.printStackTrace();
            } else {
                ControladorEscuchadores.getInstance().quitarPorSocket(socketCliente);
                System.out.println("Escuchador removido");
            }
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

        int tamanioArchivo = mensajeArchivo.getTamanio();
        byte[] bytesArchivo = new byte[tamanioArchivo];
        int tamanioBloque = 16 * 1024;
        int bytesLeidos = 0;

        while (bytesLeidos < tamanioArchivo) {
            int bytesRestantes = tamanioArchivo - bytesLeidos;
            int bytesALeer = Math.min(tamanioBloque, bytesRestantes);
            int leidos = entrada.read(bytesArchivo, bytesLeidos, bytesALeer);
            if (leidos == -1) break; // Fin de stream inesperado
            bytesLeidos += leidos;
        }

        mensajeArchivo.setBytesArchivo(bytesArchivo);
        UUID uidRemitente = mensajeArchivo.getRemitente();
        UUID uidDestinatario = mensajeArchivo.getDestinatario();
        var sesiones = ControladorSesiones.getInstance();
        var conversaciones = ControladorConversaciones.getInstance();
        UsuarioServidor remitente = sesiones.obtenerUsuario(uidRemitente);
        UsuarioServidor destinatario = sesiones.obtenerUsuario(uidDestinatario);
        var conversacion = conversaciones.obtenerConversacion(remitente, destinatario);
        conversacion.agregarMensaje(mensajeArchivo);

        try {
            Socket socketDestinatario = ControladorEscuchadores.getInstance().obtenerSocketEscucha(uidDestinatario);
            DataOutputStream mensajeDestinatario = new DataOutputStream(socketDestinatario.getOutputStream());
            mensajeDestinatario.writeUTF(protocoloCompleto);
            mensajeDestinatario.flush();
        } catch (Exception e) {
            System.out.println("Servidor TCP: error al enviar mensaje de archivo");
            e.printStackTrace();
        }
    }

    public void manejarDescargarArchivo(String protocolo) {
        String[] lineas = protocolo.split("\n", 3);
        UUID remitente = UUID.fromString(lineas[0].split(": ")[1]);
        UUID destinatario = UUID.fromString(lineas[1].split(": ")[1]);
        int idMensaje = Integer.parseInt(lineas[2].split(": ")[1]);
        
        var sesiones = ControladorSesiones.getInstance();
        var conversaciones = ControladorConversaciones.getInstance();

        UsuarioServidor usuario1 = sesiones.obtenerUsuario(remitente);
        UsuarioServidor usuario2 = sesiones.obtenerUsuario(destinatario);

        Conversacion conversacion = conversaciones.obtenerConversacion(usuario1, usuario2);
        MensajeArchivo archivo = conversacion.descargarArchivo(idMensaje);
        
        int tamanioBloque = 16 * 1024;
        byte[] bytesArchivo = archivo.getBytesArchivo();
        long tamanioArchivo = bytesArchivo.length;

        try {
            DataOutputStream salida = new DataOutputStream(socketCliente.getOutputStream());
            for (int offset = 0; offset < tamanioArchivo; offset += tamanioBloque) {
                int tamanio = Math.min(tamanioBloque, (int)(tamanioArchivo - offset));
                salida.write(bytesArchivo, offset, tamanio);
                salida.flush();
            }
        } catch (Exception e) {
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

    private void manejarEscucharChat(String protocolo) {
        UUID usuario = UUID.fromString(protocolo.split(": ")[1]);
        ControladorEscuchadores.getInstance().agregarEscuchador(usuario, socketCliente);
        esManejadorEscucha = true;
        try {
            var salida = new DataOutputStream(socketCliente.getOutputStream());
            salida.writeUTF("ok");
            salida.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
