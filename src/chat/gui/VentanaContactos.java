package chat.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.InetAddress;
import java.util.List;

import javax.swing.*;
import chat.datos.UsuarioCliente;

public class VentanaContactos  {
    private static ManejadorConexion manejadorConexion;
    public VentanaContactos() {
    }

    public static void cargarContactos(UsuarioCliente usuarioActual) throws Exception {
        manejadorConexion = ManejadorConexion.obtenerInstancia();
        List<UsuarioCliente> listaUsuarios = manejadorConexion.obtenerUsuarios(usuarioActual);
        if (listaUsuarios.size() != 0) {
            cargarListaContactos(listaUsuarios, usuarioActual);
        } else {
            cargarListaEspera(usuarioActual);
        }
    }

    private static void cargarChat(UsuarioCliente usuarioActual, UsuarioCliente destinatario) {
        VentanaChat ventana = new VentanaChat(usuarioActual, destinatario);
        ventana.cargarVentana();
        ventana.setVisible(true);
    }

    private static void cargarListaContactos(List<UsuarioCliente> listaUsuarios, UsuarioCliente usuarioActual) throws Exception {
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
        JButton botonReiniciar = new JButton("Actualizar");
        JButton botonSalir = new JButton("Salir");
        panelBotones.add(botonReiniciar);
        panelBotones.add(botonSalir);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JLabel("Bienvenido " + usuarioActual.getNombre() + " ¿Con quién deseas chatear?"), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        JDialog ventanaLista = new JDialog((JFrame) null, "Seleccionar chat", true);
        ventanaLista.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        ventanaLista.getContentPane().add(panel);
        ventanaLista.pack();
        ventanaLista.setLocationRelativeTo(null);

        botonReiniciar.addActionListener(listener -> {
            ventanaLista.dispose();
            try {
                cargarContactos(usuarioActual);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        botonSalir.addActionListener(listener -> {
            ventanaLista.dispose();
            System.exit(0);
        });

        lista.addListSelectionListener(listener -> {
            if (!listener.getValueIsAdjusting() && lista.getSelectedValue() != null) {
                seleccionado[0] = lista.getSelectedValue();
                ventanaLista.dispose();
            }
        });

        ventanaLista.setVisible(true);

        if (seleccionado[0] != null) {
            listaUsuarios = manejadorConexion.obtenerUsuarios(usuarioActual);
            if (listaUsuarios.size() != 0) {
                cargarChat(usuarioActual, seleccionado[0]);
            } else {
                JOptionPane.showMessageDialog(null, seleccionado[0].getNombre() + " se ha desconectado", "Usuario desconectado", JOptionPane.WARNING_MESSAGE);
                cargarListaEspera(usuarioActual);
            }
        }
    }

    private static void cargarListaEspera(UsuarioCliente usuarioActual) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("En espera de que usuarios se conecten al servidor", SwingConstants.CENTER);
        JButton botonReiniciar = new JButton("Actualizar");
        panel.add(label, BorderLayout.CENTER);
        panel.add(botonReiniciar, BorderLayout.SOUTH);

        JDialog ventanaAviso = new JDialog((JFrame) null, "No hay usuarios conectados", true);
        ventanaAviso.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        ventanaAviso.getContentPane().add(panel);
        ventanaAviso.setSize(350, 120);
        ventanaAviso.setLocationRelativeTo(null);

        botonReiniciar.addActionListener(listener -> {
            ventanaAviso.dispose();
            try {
                cargarContactos(usuarioActual);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ventanaAviso.setVisible(true);
    }
}
