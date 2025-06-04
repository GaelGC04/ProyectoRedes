package chat.servidor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServidorUDP implements Runnable {
    private DatagramSocket socket;
    private int puerto;

    public ServidorUDP(InetAddress ip, int puerto) throws Exception {
        socket = new DatagramSocket(puerto, ip);
        this.puerto = puerto;
        System.out.printf("Servidor UDP iniciado en %s:%d%n", ip, puerto);
    }

    @Override
    public void run() {
        try {
            do {
                byte[] datos = new byte[1024];
                DatagramPacket paqueteEntrada = new DatagramPacket(datos, datos.length);
                socket.receive(paqueteEntrada);
                System.out.println("UDP: paquete recibido");
                new Thread(new ManejadorUDP(paqueteEntrada, socket, datos)).start();
            } while (true);
        } catch (Exception e) {

        }
    }
}
