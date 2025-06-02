package chat.servidor;

import chat.datos.UsuarioServidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.UUID;

public class ManejadorTCP implements Runnable {
    private Socket socketCliente;

    public ManejadorTCP(Socket socketCliente) {
        this.socketCliente = socketCliente;
    }

    @Override
    public void run() {
        try {
            DataInputStream entrada = new DataInputStream(socketCliente.getInputStream());
            do {
                String protocolo = entrada.readUTF();
                String tipoPeticion = protocolo.split("\n", 2)[0];
                protocolo = protocolo.substring(protocolo.indexOf("\n") + 1);
                switch (tipoPeticion) {
                    case "tipo: registro" -> manejarRegistro(protocolo);
                    case "tipo: archivo" -> manejarMensajeArchivo(protocolo);
                    case "tipo: obtenerUsuarios" -> manejarListaUsuarios(protocolo);
                    default -> {
                        System.out.println("Petición desconocida:");
                        System.out.println(tipoPeticion);
                        System.out.println(protocolo);
                    }
                }
            } while (true);
        } catch (Exception e) {
            ControladorSesiones.getInstance().desconectarPorSocket(socketCliente);
            System.out.println("Desconectando usuario");
            e.printStackTrace();
        }
    }

    private void manejarRegistro(String protocolo) {
        System.out.println("Registrando usuario...");
        String[] lineas = protocolo.split("\n");
        UUID uid = UUID.fromString(lineas[0].split(": ")[1]);
        String nombre = lineas[1].split(": ")[1];
        UsuarioServidor usuarioServidor = new UsuarioServidor(nombre, uid, socketCliente);
        ControladorSesiones.getInstance().conectar(usuarioServidor);
    }

    private void manejarMensajeArchivo(String protocolo) {

    }

    private void manejarListaUsuarios(String protocolo) {
        System.out.println("Obteniendo usuarios...");
        String[] lineas = protocolo.split("\n");
        UUID uid = UUID.fromString(lineas[0].split(": ")[1]);

        var listaUsuarios = ControladorSesiones.getInstance().obtenerUsuarios(uid);
        var protocoloUsuarios = "";
        for (UsuarioServidor usuario : listaUsuarios) {
            protocoloUsuarios += (usuario.uuid()+","+usuario.nombre()+";\n");
        }

        try {
            DataOutputStream dataOutputStream = new DataOutputStream(this.socketCliente.getOutputStream());
            dataOutputStream.writeUTF(protocoloUsuarios);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
