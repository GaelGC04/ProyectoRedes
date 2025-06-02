package chat.gui;

import chat.datos.UsuarioCliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class ManejadorConexion {
    private static ManejadorConexion instancia;

    public static ManejadorConexion crearConexion(InetAddress ip, int puerto) throws Exception {
        instancia = new ManejadorConexion(ip, puerto);
        return instancia;
    }

    public static ManejadorConexion obtenerInstancia() {
        if (instancia == null) {
            throw new RuntimeException("Se debe crear primero una conexión");
        }

        return instancia;
    }

    private final Socket socketCliente;
    private final DataInputStream entrada;
    private final DataOutputStream salida;

    private ManejadorConexion(InetAddress ip, int puerto) throws Exception {
        socketCliente = new Socket(ip, puerto);
        entrada = new DataInputStream(socketCliente.getInputStream());
        salida = new DataOutputStream(socketCliente.getOutputStream());
    }

    public List<UsuarioCliente> obtenerUsuarios(UsuarioCliente usuarioActual) throws Exception {
        String protocolo = """
                tipo: obtenerUsuarios
                usuario: %s
                """.formatted(usuarioActual.getUuid());
        salida.writeUTF(protocolo);

        String respuesta = entrada.readUTF();
        return UsuarioCliente.convertirDeRespuestaLista(respuesta);
    }

    public static void cerrarConexion() throws Exception {
        if (instancia == null) {
            return;
        }

        instancia.socketCliente.close();
        instancia = null;
    }
}
