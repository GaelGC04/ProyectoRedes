package chat.gui;

import chat.checksum.Checksum;
import chat.datos.Mensaje;
import chat.datos.MensajeArchivo;
import chat.datos.MensajeTexto;

import javax.swing.*;
import java.io.DataInputStream;
import java.net.DatagramPacket;

public class EscuchadorMensajes implements AutoCloseable, Runnable {
    private final Thread procesoMensajesTexto;
    private final Thread procesoMensajesArchivo;
    private final DefaultListModel<Mensaje> mensajes;

    public EscuchadorMensajes(DefaultListModel<Mensaje> mensajes) {
        this.mensajes = mensajes;
        this.procesoMensajesTexto = new Thread(() -> {

            var socketCliente = ManejadorConexion.obtenerInstancia().getSocketUdp();
            byte[] bytesEntrada = new byte[1024];
            DatagramPacket paqueteEntrada = new DatagramPacket(bytesEntrada, bytesEntrada.length);
            while (true) {
                try {
                    socketCliente.receive(paqueteEntrada);
                    short checksum = (short) ((bytesEntrada[0] << 8) | (bytesEntrada[1] & 0xFF));
                    int tamanioReal = paqueteEntrada.getLength();
                    String protocolo = new String(bytesEntrada, 2, tamanioReal - 2);
                    protocolo = protocolo.substring(0, protocolo.indexOf(0));
                    if (!protocolo.startsWith("tipo: msj")) {
                        continue;
                    }
                    if (!Checksum.verificarChecksum(protocolo, checksum)) {
                        System.out.println("Cliente: el checksum no es válido");
                        continue;
                    }
                    var mensaje = new MensajeTexto(0, null, null, null);
                    if (!mensaje.convertirDeProtocolo(protocolo)) {
                        continue;
                    }
                    this.mensajes.addElement(mensaje);
                } catch (Exception e) {
                    System.out.println("Error al recibir:");
                    e.printStackTrace();
                }
            }
        });
        this.procesoMensajesArchivo = new Thread(() -> {
            try {
                var socketCliente = ManejadorConexion.obtenerInstancia().getSocketTcp();
                var entrada = new DataInputStream(socketCliente.getInputStream());
                while (true) {
                    String respuesta = entrada.readUTF();
                    var mensaje = new MensajeArchivo(0, null, null, new byte[0], null);
                    if (!mensaje.convertirDeProtocolo(respuesta)) {
                        continue;
                    }
                    this.mensajes.addElement(mensaje);
                }
            } catch (Exception e) {
            }
        });
    }

    public void escucharMensajes() {
        procesoMensajesTexto.start();
        procesoMensajesArchivo.start();
    }

    @Override
    public void run() {
        escucharMensajes();
    }

    @Override
    public void close() throws Exception {
        procesoMensajesArchivo.interrupt();
        procesoMensajesTexto.interrupt();
    }
}
