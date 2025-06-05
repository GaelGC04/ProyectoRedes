package chat.gui;

import chat.datos.Mensaje;
import chat.datos.MensajeArchivo;
import chat.datos.MensajeTexto;
import chat.datos.UsuarioCliente;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class EscuchadorMensajes implements AutoCloseable, Runnable {
    private final Thread procesoMensajesArchivo;
    private final DefaultListModel<Mensaje> mensajes;
    private volatile boolean escuchar;

    public EscuchadorMensajes(DefaultListModel<Mensaje> mensajes, UsuarioCliente usuarioActual) {
        this.mensajes = mensajes;
        escuchar = false;
        this.procesoMensajesArchivo = new Thread(() -> {
            Socket socketReferencia = ManejadorConexion.obtenerInstancia().getSocketTcp();
            try(Socket socketCliente = new Socket(socketReferencia.getInetAddress(), socketReferencia.getPort())) {
                socketCliente.setSoTimeout(2000);
                var salida = new DataOutputStream(socketCliente.getOutputStream());
                String protocolo = """
                        tipo: escucharChat
                        usuario: %s""".formatted(usuarioActual.getUuid());
                salida.writeUTF(protocolo);
                salida.flush();
                var entrada = new DataInputStream(socketCliente.getInputStream());
                String respuestaProtocolo = entrada.readUTF();
                if (!respuestaProtocolo.equals("ok")) {
                    return;
                }
                socketCliente.setSoTimeout(0);
                while (escuchar) {
                    String respuesta = entrada.readUTF();
                    System.out.println("Escuchador cliente TCP: mensaje: " + respuesta);
                    var mensaje = new MensajeArchivo(0, null, null, new byte[0], null);
                    var mensajeTexto = new MensajeTexto(0, null, null, null);
                    boolean esArchivo = mensaje.convertirDeProtocolo(respuesta);
                    boolean esTexto = mensajeTexto.convertirDeProtocolo(respuesta);
                    if (!esArchivo && !esTexto) {
                        continue;
                    }
                    if (esArchivo) {
                        this.mensajes.addElement(mensaje);
                    } else if (esTexto) {
                        this.mensajes.addElement(mensajeTexto);
                    }
                }
            } catch (Exception e) {
                System.out.println("Escuchador cliente: Error en TCP");
                e.printStackTrace();
            }
        });
    }

    public void escucharMensajes() {
        escuchar = true;
        procesoMensajesArchivo.start();
    }

    @Override
    public void run() {
        escucharMensajes();
    }

    @Override
    public void close() throws Exception {
        escuchar = false;
        procesoMensajesArchivo.interrupt();
    }
}
