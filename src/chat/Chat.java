package chat;

import chat.gui.VentanaContactos;

import javax.swing.*;

public class Chat {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            String nombre = "";
            do {
                nombre = JOptionPane.showInputDialog(null, "Ingresa tu nombre:", "Nombre de usuario", JOptionPane.QUESTION_MESSAGE);
                if (nombre.contains(",") || nombre.contains(";")) {
                    JOptionPane.showMessageDialog(null, "Favor de no ingresar ',' ni ';'", "Valores inválidos", 0);
                    nombre = "";
                }
            } while (nombre == null || nombre.trim().isEmpty());

            VentanaContactos.cargarContactos(nombre);
        });
    }
}