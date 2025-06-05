package chat;

import chat.servidor.ServidorTCP;
import chat.servidor.ServidorUDP;

import java.net.InetAddress;

public class Servidor {
    public static void main(String[] args) {
        InetAddress ip;
        try {
            try {
                ip = InetAddress.getByName(args[0]);
            } catch (Exception e1) {
                ip = InetAddress.getLocalHost();            
            }
            Thread servidorTcp = new Thread(new ServidorTCP(ip, 50000));
            Thread servidorUdp = new Thread(new ServidorUDP(ip, 50001));
            servidorTcp.start();
            servidorUdp.start();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}
