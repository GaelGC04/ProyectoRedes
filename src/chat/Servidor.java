package chat;

import chat.servidor.ServidorTCP;

public class Servidor {
    public static void main(String[] args) {
        try {
            Thread servidorTcp = new Thread(new ServidorTCP(50000));
            servidorTcp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
