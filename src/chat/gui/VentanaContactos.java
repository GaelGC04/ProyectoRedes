package chat.gui;

import javax.swing.*;

public class VentanaContactos  {
    public VentanaContactos() {
    }

    // Que luego cambie de string a usuario para devolver la clase
    public static void cargarContactos(String nombreUsuarioActual) {
        // que aqui se obtnengan los usuarios de la listad e usuarios dinamica
        String[] clientes = {"Juan", "Nepomuseno", "Aldonso", "Sodel", "Solis"};
        
        String seleccionado = (String) JOptionPane.showInputDialog(
            null,
            "Bienvenido " + nombreUsuarioActual + "\n ¿Con quién deseas chatear?:",
            "Usuarios registrados",
            JOptionPane.QUESTION_MESSAGE,
            null,
            clientes,
            clientes[0]
        );

        cargarChat(nombreUsuarioActual, seleccionado);
    }

    private static void cargarChat(String nombreUsuarioActual, String seleccionado) {
        VentanaChat ventana = new VentanaChat(nombreUsuarioActual, seleccionado);
        ventana.cargarVentana();
        ventana.setVisible(true);
    }
}