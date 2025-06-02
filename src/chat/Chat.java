package chat;

import chat.gui.VentanaContactos;

import javax.swing.*;

public class Chat {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            String nombre = "";
            do {
                nombre = JOptionPane.showInputDialog(null, "Ingresa tu nombre:", "Nombre de usuario", JOptionPane.QUESTION_MESSAGE);
            } while (nombre == null || nombre.trim().isEmpty());

            VentanaContactos.cargarContactos(nombre);
        });
    }
}