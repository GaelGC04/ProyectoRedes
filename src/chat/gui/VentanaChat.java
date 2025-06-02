package chat.gui;

import javax.swing.*;

import chat.datos.UsuarioCliente;

import java.awt.*;
import java.awt.event.ActionEvent;

public class VentanaChat extends JFrame {
    private JTextArea areaMensajes;
    private JTextField campoTexto;
    private JButton botonAdjuntar;
    private JButton botonEnviar;

    private UsuarioCliente usuarioActual;
    private UsuarioCliente destinatario;
    
    public VentanaChat(UsuarioCliente usuarioActual, UsuarioCliente destinatario) {
        this.usuarioActual = usuarioActual;
        this.destinatario = destinatario;
    }

    public void cargarVentana(){
        setTitle("Chat - " + destinatario.getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(400, 600);
        setLocationRelativeTo(null);

        Color fondo = new Color(40, 40, 40);
        Color fondoVerde = new Color(40, 150, 40);
        Color texto = new Color(230, 230, 230);
        Color fondoPaneles = new Color(30, 30, 30);
        Color fondoBoton = new Color(60, 60, 60);

        getContentPane().setBackground(fondo);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(fondoPaneles);

        JButton botonContactos = new JButton("←");
        botonContactos.setForeground(texto);
        botonContactos.setBackground(fondoBoton);
        botonContactos.setFocusPainted(false);
        botonContactos.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        botonContactos.addActionListener(e -> {
            this.dispose();
            try {
                VentanaContactos.cargarContactos(usuarioActual);
            } catch (Exception err) {
                err.printStackTrace();
            }
        });
        panelSuperior.add(botonContactos, BorderLayout.WEST);

        JLabel etiquetaContacto = new JLabel(this.destinatario.getNombre());
        etiquetaContacto.setFont(new Font("Roboto", Font.BOLD, 18));
        etiquetaContacto.setForeground(texto);
        etiquetaContacto.setHorizontalAlignment(SwingConstants.RIGHT);
        etiquetaContacto.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        panelSuperior.add(etiquetaContacto, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);

        areaMensajes = new JTextArea();
        areaMensajes.setEditable(false);
        areaMensajes.setLineWrap(true);
        areaMensajes.setWrapStyleWord(true);
        areaMensajes.setBackground(fondo);
        areaMensajes.setForeground(texto);
        areaMensajes.setCaretColor(texto);
        JScrollPane scrollMensajes = new JScrollPane(areaMensajes);
        scrollMensajes.getViewport().setBackground(fondo);
        scrollMensajes.setBorder(null);
        add(scrollMensajes, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(fondoPaneles);

        JPanel panelInput = new JPanel(new BorderLayout());
        panelInput.setBackground(fondoPaneles);

        botonAdjuntar = new JButton("+");
        botonAdjuntar.setPreferredSize(new Dimension(50, 40));
        botonAdjuntar.setBackground(fondoBoton);
        botonAdjuntar.setForeground(texto);
        botonAdjuntar.addActionListener(e -> adjuntarArchivo());
        panelInput.add(botonAdjuntar, BorderLayout.WEST);

        campoTexto = new JTextField();
        campoTexto.setBackground(fondoBoton);
        campoTexto.setForeground(texto);
        campoTexto.setCaretColor(texto);
        panelInput.add(campoTexto, BorderLayout.CENTER);

        botonEnviar = new JButton("Enviar");
        botonEnviar.setPreferredSize(new Dimension(70, 40));
        botonEnviar.setBackground(fondoVerde);
        botonEnviar.setForeground(texto);
        botonEnviar.addActionListener(this::enviarMensaje);
        panelInput.add(botonEnviar, BorderLayout.EAST);

        panelInferior.add(panelInput, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private void adjuntarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "Archivo seleccionado: " + fileChooser.getSelectedFile().getName());
        }
    }

    private void enviarMensaje(ActionEvent e) {
        String texto = campoTexto.getText().trim();
        if (!texto.isEmpty()) {
            areaMensajes.append(this.usuarioActual.getNombre() + " (Tú): " + texto + "\n");
            campoTexto.setText("");
        }
    }
}