package chat.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import javax.swing.*;

import chat.datos.UsuarioCliente;

public class VentanaContactos  {
    public VentanaContactos() {
    }

    public static void cargarContactos(UsuarioCliente usuarioActual) throws Exception {
        Socket socket = new Socket(InetAddress.getLocalHost(), 50000);
        DataOutputStream salida = new DataOutputStream(socket.getOutputStream());

        String protocolo = """
                tipo: obtenerUsuarios
                usuario: %s
                """.formatted(usuarioActual.getUuid());
        salida.writeUTF(protocolo);

        DataInputStream entrada = new DataInputStream(socket.getInputStream());

        String respuesta = entrada.readUTF();
        List<UsuarioCliente> listaUsuarios = UsuarioCliente.convertirDeRespuestaLista(respuesta);
        
        if (listaUsuarios.size() != 0) {
            cargarListaContactos(listaUsuarios, usuarioActual);
        } else {
            cargarListaEspera(usuarioActual);
        }
    }

    // TODO hacer que mejor sea por UUID y obtener conversacion entre los dos UUIDs
    private static void cargarChat(UsuarioCliente usuarioActual, String seleccionado) {
        VentanaChat ventana = new VentanaChat(usuarioActual, seleccionado);
        ventana.cargarVentana();
        ventana.setVisible(true);
    }

    private static void cargarListaContactos(List<UsuarioCliente> listaUsuarios, UsuarioCliente usuarioActual) {
        final UsuarioCliente[] seleccionado = {null};

        DefaultListModel<UsuarioCliente> modeloUsuarios = new DefaultListModel<>();
        for (UsuarioCliente usuario : listaUsuarios) {
            modeloUsuarios.addElement(usuario);
        }

        JList<UsuarioCliente> lista = new JList<>(modeloUsuarios);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setPreferredSize(new Dimension(350, 220));

        JPanel panelBotones = new JPanel();
        JButton btnReiniciar = new JButton("Actualizar");
        JButton btnSalir = new JButton("Salir");
        panelBotones.add(btnReiniciar);
        panelBotones.add(btnSalir);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JLabel("Bienvenido " + usuarioActual.getNombre() + " ¿Con quién deseas chatear?"), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((JFrame) null, "Seleccionar chat", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);

        btnReiniciar.addActionListener(e -> {
            dialog.dispose();
            try {
                cargarContactos(usuarioActual);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        btnSalir.addActionListener(e -> {
            dialog.dispose();
            System.exit(0);
        });

        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && lista.getSelectedValue() != null) {
                seleccionado[0] = lista.getSelectedValue();
                dialog.dispose();
            }
        });

        dialog.setVisible(true);

        if (seleccionado[0] != null) {
            cargarChat(usuarioActual, seleccionado[0].getNombre());
        }
    }

    private static void cargarListaEspera(UsuarioCliente usuarioActual) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("En espera de que usuarios se conecten al servidor", SwingConstants.CENTER);
        JButton btnReiniciar = new JButton("Actualizar lista");
        panel.add(label, BorderLayout.CENTER);
        panel.add(btnReiniciar, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((JFrame) null, "No hay usuarios conectados", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(panel);
        dialog.setSize(350, 120);
        dialog.setLocationRelativeTo(null);

        btnReiniciar.addActionListener(e -> {
            dialog.dispose();
            try {
                cargarContactos(usuarioActual);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        dialog.setVisible(true);
    }
}