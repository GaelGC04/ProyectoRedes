package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

import chat.datos.UsuarioCliente;

public class PruebaServidorTCPChat {
    public static void main(String[] args) throws Exception {
        UUID uuid = UUID.randomUUID();
        String protocolo = """
                tipo: registro
                uuid: %s
                nombre: Juan""".formatted(uuid);
        Socket socket = new Socket(InetAddress.getLocalHost(), 50000);
        DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
        salida.writeUTF(protocolo);

        Thread.sleep(10000);

        System.out.println("Enviando usuarios");

        String protocolo2 = """
                tipo: obtenerUsuarios
                usuario: %s
                """.formatted(uuid);
        salida.writeUTF(protocolo2);

        DataInputStream entrada = new DataInputStream(socket.getInputStream());

        String respuesta = entrada.readUTF();
        System.out.println(respuesta);

        System.out.println(UsuarioCliente.convertirDeRespuestaLista(respuesta));
    }
}
