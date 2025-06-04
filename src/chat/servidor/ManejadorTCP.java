package chat.servidor;

import chat.datos.Mensaje;
import chat.datos.MensajeTexto;
import chat.datos.UsuarioServidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
                String protocolo = entrada.readUTF();
                String tipoPeticion = protocolo.split("\n", 2)[0];
                protocolo = protocolo.substring(protocolo.indexOf("\n") + 1);
                switch (tipoPeticion) {
                    case "tipo: registro" -> manejarRegistro(protocolo);
                    case "tipo: archivo" -> manejarMensajeArchivo(protocolo);
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

    private void manejarMensajeArchivo(String protocolo) {

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
        var respuesta = new StringBuilder();
        for (Mensaje mensaje : conversacion.obtenerMensajes()) {
            String protocoloMensaje = mensaje.convertirAProtocolo();
            respuesta.append(protocoloMensaje).append("\n");
            if (mensaje instanceof MensajeTexto) {
                respuesta.append("\u001E\n"); // Separador de mensajes de texto
            }
        }
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(this.socketCliente.getOutputStream());
            dataOutputStream.writeUTF(respuesta.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
