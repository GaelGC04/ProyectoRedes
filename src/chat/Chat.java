package chat;

import chat.datos.UsuarioCliente;
import chat.gui.ManejadorConexion;
import chat.gui.VentanaContactos;

import java.net.InetAddress;
import java.util.UUID;

import javax.swing.*;

public class Chat {
    private static ManejadorConexion manejadorConexion;

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
                try {
                    manejadorConexion = ManejadorConexion.obtenerInstancia();
                } catch (Exception e) {
                    e.printStackTrace();
                    InetAddress ip;
                    try {
                        try {
                            ip = InetAddress.getByName(args[0]);
                        } catch (Exception e1) {
                            ip = InetAddress.getLocalHost();            
                        }
                        manejadorConexion = ManejadorConexion.crearConexion(ip, ManejadorConexion.PUERTO_TCP, ManejadorConexion.PUERTO_UDP);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                UUID uuid = UUID.randomUUID();
                UsuarioCliente usuarioActual = new UsuarioCliente(nombre, uuid);

                try {
                    manejadorConexion.conectarUsuario(usuarioActual);
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    VentanaContactos.cargarContactos(usuarioActual);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
