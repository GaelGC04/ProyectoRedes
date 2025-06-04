package chat.servidor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServidorUDP implements Runnable {
    private DatagramSocket socket;

    public ServidorUDP(InetAddress ip, int puerto) throws Exception {
        socket = new DatagramSocket(puerto, ip);
    }

    @Override
    public void run() {
        try {
            do {
                byte[] datos = new byte[1024];
                DatagramPacket paqueteEntrada = new DatagramPacket(datos, datos.length);
                socket.receive(paqueteEntrada);

            } while (true);
        } catch (Exception e) {

        }
    }
}
