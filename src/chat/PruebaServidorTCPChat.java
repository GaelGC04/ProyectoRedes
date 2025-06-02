package chat;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

public class PruebaServidorTCPChat {
    public static void main(String[] args) throws Exception {
        String protocolo = """
                tipo: registro
                uuid: %s
                nombre: Juan""".formatted(UUID.randomUUID());
        Socket socket = new Socket(InetAddress.getLocalHost(), 50000);
        DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
        salida.writeUTF(protocolo);
    }
}
