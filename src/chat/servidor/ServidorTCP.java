package chat.servidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// El servidor TCP manejará registro de usuarios
// Y mensajes de archivos
public class ServidorTCP implements Runnable {
    private ServerSocket socketServidor;

    public ServidorTCP(InetAddress ip, int puerto) throws Exception {
        socketServidor = new ServerSocket(puerto, 10, ip);
        System.out.printf("Servidor iniciado en %s:%d%n", socketServidor.getInetAddress(), puerto);
    }

    @Override
    public void run() {
        try {
            do {
                Socket socketCliente = socketServidor.accept();
                Thread procesoConexion = new Thread(new ManejadorTCP(socketCliente));
                procesoConexion.start();
            } while (true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
