package chat;

import chat.datos.UsuarioCliente;
import chat.gui.VentanaContactos;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

import javax.swing.*;

public class Chat {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            String nombre = "";
            do {
                nombre = JOptionPane.showInputDialog(null, "Ingresa tu nombre:", "Nombre de usuario", JOptionPane.QUESTION_MESSAGE);
                if (nombre == null) {
                    break;
                }
            } while (nombre.contains(",") || nombre.contains(";") || nombre.trim().isEmpty());

            if (nombre != null) {
                UUID uuid = UUID.randomUUID();
                String protocolo = """
                        tipo: registro
                        uuid: %s
                        nombre: %s""".formatted(uuid, nombre);

                Socket socket;
                try {
                    socket = new Socket(InetAddress.getLocalHost(), 50000);
                    DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                    salida.writeUTF(protocolo);
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                UsuarioCliente usuarioActual = new UsuarioCliente(nombre, uuid);
                try {
                    VentanaContactos.cargarContactos(usuarioActual);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}