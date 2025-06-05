package chat.gui;

import chat.datos.Mensaje;
import chat.datos.MensajeArchivo;
import chat.datos.MensajeTexto;

import javax.swing.*;
import java.io.DataInputStream;

public class EscuchadorMensajes implements AutoCloseable, Runnable {
    private final Thread procesoMensajesArchivo;
    private final DefaultListModel<Mensaje> mensajes;

    public EscuchadorMensajes(DefaultListModel<Mensaje> mensajes) {
        this.mensajes = mensajes;
        this.procesoMensajesArchivo = new Thread(() -> {
            try {
                var socketCliente = ManejadorConexion.obtenerInstancia().getSocketTcp();
                var entrada = new DataInputStream(socketCliente.getInputStream());
                while (true) {
                    System.out.println("Escuchador cliente TCP: esperando mensaje...");
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
                        byte[] archivo = new byte[mensaje.getTamanio()];
                        entrada.readFully(archivo);
                        mensaje.setBytesArchivo(archivo);
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
        procesoMensajesArchivo.start();
    }

    @Override
    public void run() {
        escucharMensajes();
    }

    @Override
    public void close() throws Exception {
        procesoMensajesArchivo.interrupt();
    }
}
