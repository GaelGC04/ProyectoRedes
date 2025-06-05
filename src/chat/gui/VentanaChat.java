package chat.gui;

import javax.swing.*;

import chat.datos.Mensaje;
import chat.datos.MensajeArchivo;
import chat.datos.MensajeTexto;
import chat.datos.UsuarioCliente;

import java.io.DataOutputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.awt.*;
import java.awt.event.*;

public class VentanaChat extends JFrame {
    private DefaultListModel<Mensaje> modeloMensajes;
    private JList<Mensaje> listaMensajes;
    private JTextField campoTexto;
    private JButton botonAdjuntar;
    private JButton botonEnviar;
    private int idActual;
    private Thread procesoEscucha;
    private EscuchadorMensajes escuchadorMensajes;

    private UsuarioCliente usuarioActual;
    private UsuarioCliente destinatario;
    
    public VentanaChat(UsuarioCliente usuarioActual, UsuarioCliente destinatario) {
        this.usuarioActual = usuarioActual;
        this.destinatario = destinatario;
        this.idActual = 0;
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

        // Botón de salida del chat
        JButton botonContactos = new JButton("←");
        botonContactos.setForeground(texto);
        botonContactos.setBackground(fondoBoton);
        botonContactos.setFocusPainted(false);
        botonContactos.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        botonContactos.addActionListener(listener -> {
            this.dispose();
            try {
                VentanaContactos.cargarContactos(usuarioActual);
                escuchadorMensajes.close();
                procesoEscucha.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        panelSuperior.add(botonContactos, BorderLayout.WEST);

        // Etiqueta de contacto del chat
        JLabel etiquetaContacto = new JLabel(this.destinatario.getNombre());
        etiquetaContacto.setFont(new Font("Roboto", Font.BOLD, 18));
        etiquetaContacto.setForeground(texto);
        etiquetaContacto.setHorizontalAlignment(SwingConstants.RIGHT);
        etiquetaContacto.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        panelSuperior.add(etiquetaContacto, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);

        // Lista de mensajes
        modeloMensajes = new DefaultListModel<>();
        this.listaMensajes = new JList<>(modeloMensajes);
        this.listaMensajes.setBackground(fondoBoton);
        this.cargarListaMensajes();
        JScrollPane scrollMensajes = new JScrollPane(this.listaMensajes);
        scrollMensajes.getViewport().setBackground(fondo);
        scrollMensajes.setBorder(null);
        add(scrollMensajes, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(fondoPaneles);

        JPanel panelInput = new JPanel(new BorderLayout());
        panelInput.setBackground(fondoPaneles);

        // Botón para añadir archivos
        this.botonAdjuntar = new JButton("+");
        this.botonAdjuntar.setPreferredSize(new Dimension(50, 40));
        this.botonAdjuntar.setBackground(fondoBoton);
        this.botonAdjuntar.setForeground(texto);
        this.botonAdjuntar.addActionListener(listener -> adjuntarArchivo());
        panelInput.add(this.botonAdjuntar, BorderLayout.WEST);
        
        // Campo de texto para escribir los mensajes
        campoTexto = new JTextField();
        campoTexto.setBackground(fondoBoton);
        campoTexto.setForeground(texto);
        campoTexto.setCaretColor(texto);
        panelInput.add(campoTexto, BorderLayout.CENTER);

        // Botón para enviar mensaje al destinatario
        this.botonEnviar = new JButton("Enviar");
        this.botonEnviar.setPreferredSize(new Dimension(90, 40));
        this.botonEnviar.setBackground(fondoVerde);
        this.botonEnviar.setForeground(texto);
        this.botonEnviar.addActionListener(this::enviarMensaje);
        panelInput.add(this.botonEnviar, BorderLayout.EAST);

        panelInferior.add(panelInput, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);
        escucharMensajes();
    }

    private void adjuntarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();

            if (archivo == null) {
                return;
            }
            byte[] bytesArchivo = null;
            try {
                bytesArchivo = Files.readAllBytes(archivo.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
         
            this.idActual += 1;
            MensajeArchivo mensajeArchivo = new MensajeArchivo(
                this.idActual,
                usuarioActual.getUuid(),
                destinatario.getUuid(),
                bytesArchivo,
                archivo.getName()
            );
            var conexion = ManejadorConexion.obtenerInstancia();
            try {
                conexion.enviarArchivo(mensajeArchivo);
            } catch (Exception e) {
                e.printStackTrace();
            }

            modeloMensajes.addElement(mensajeArchivo);
            this.listaMensajes.ensureIndexIsVisible(modeloMensajes.size() - 1);
        }
    }

    private void enviarMensaje(ActionEvent evento) {
        String texto = campoTexto.getText().trim();
        if (!texto.isEmpty()) {
            this.idActual += 1;
            MensajeTexto mensajeTexto = new MensajeTexto(this.idActual, usuarioActual.getUuid(), destinatario.getUuid(), texto);

            var conexion = ManejadorConexion.obtenerInstancia();
            boolean enviado = false;
            try {
                enviado = conexion.enviarMensajeTexto(mensajeTexto);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (enviado){
                modeloMensajes.addElement(mensajeTexto);
                this.listaMensajes.ensureIndexIsVisible(modeloMensajes.size() - 1);
                campoTexto.setText("");
            } else {
                JOptionPane.showMessageDialog(null, "Mensaje no enviado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cargarListaMensajes() {
        this.listaMensajes.setCellRenderer((lista, elemento, indice, esSeleccionado, celdaEnfocada) -> {
            JPanel panelFlujo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

            int anchoMaximo = lista.getWidth() > 0 ? lista.getWidth() - 60 : 320;
            String nombreRemitente = elemento.getRemitente().equals(this.usuarioActual.getUuid()) ? this.usuarioActual.getNombre() : this.destinatario.getNombre();

            JTextArea areaMensaje = new JTextArea();
            areaMensaje.setLineWrap(true);
            areaMensaje.setWrapStyleWord(true);
            areaMensaje.setEditable(false);
            areaMensaje.setOpaque(false);
            areaMensaje.setForeground(new Color(255, 255, 255));
            areaMensaje.setFont(lista.getFont());
            areaMensaje.setBorder(null);
            areaMensaje.setMaximumSize(new Dimension(anchoMaximo, Integer.MAX_VALUE));
            areaMensaje.setPreferredSize(null);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            
            panel.setBackground(new Color(60, 40, 40));
            panelFlujo.setBackground(new Color(60, 40, 40));
            if (elemento.getRemitente().equals(this.usuarioActual.getUuid())) {
                panelFlujo.setBackground(new Color(40, 60, 40));
                panel.setBackground(new Color(40, 60, 40));
            }

            if (elemento instanceof MensajeTexto) {
                // En ese caso se parsea a MensajeTexto y se envía como formato de puro texto
                MensajeTexto mensajeTexto = (MensajeTexto) elemento;
                areaMensaje.setText(nombreRemitente + ": " + mensajeTexto.getContenido());
                areaMensaje.setSize(anchoMaximo, Short.MAX_VALUE);
                areaMensaje.setPreferredSize(new Dimension(anchoMaximo, areaMensaje.getPreferredSize().height));
                panel.add(areaMensaje);
            } else if (elemento instanceof MensajeArchivo) {
                // En ese caso se parsea a MensajeArchivo y el botón de descarga del archivo
                MensajeArchivo mensajeArchivo = (MensajeArchivo) elemento;
                areaMensaje.setText(nombreRemitente + " envió un archivo: " + mensajeArchivo.getNombreArchivo());
                panel.add(areaMensaje);

                JButton botonDescargar = new JButton("Descargar " + mensajeArchivo.getNombreArchivo());
                botonDescargar.setBackground(new Color(20, 35, 20));
                botonDescargar.setForeground(Color.WHITE);
                botonDescargar.setFocusPainted(false);
                botonDescargar.setAlignmentX(Component.LEFT_ALIGNMENT);
                botonDescargar.setMaximumSize(new Dimension(anchoMaximo - 24, 40));
                botonDescargar.setPreferredSize(new Dimension(anchoMaximo - 24, 40));

                panel.add(Box.createVerticalStrut(4));
                panel.add(botonDescargar);
            }

            panelFlujo.add(panel);
            return panelFlujo;
        });

        listaMensajes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                // Como no funciona el botón de descarga por estar dentro del modelo entonces en vez de hacer que
                // se descargue el archivo por el botón mejor que se descargue por hacer click en todo el mensaje
                int indice = listaMensajes.locationToIndex(evento.getPoint());
                if (indice != -1 && listaMensajes.getModel().getElementAt(indice) instanceof MensajeArchivo) {
                    MensajeArchivo mensajeArchivo = (MensajeArchivo) listaMensajes.getModel().getElementAt(indice);
                    JFileChooser chooser = new JFileChooser();
                    chooser.setSelectedFile(new File(mensajeArchivo.getNombreArchivo()));
                    int respuesta = chooser.showSaveDialog(listaMensajes);

                    if (respuesta == JFileChooser.APPROVE_OPTION) {
                        try {
                            Files.write(chooser.getSelectedFile().toPath(), mensajeArchivo.getBytesArchivo());
                            JOptionPane.showMessageDialog(listaMensajes, "El archivo ha sido guardado con éxito");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void escucharMensajes() {
        escuchadorMensajes = new EscuchadorMensajes(modeloMensajes, usuarioActual);
        procesoEscucha = new Thread(escuchadorMensajes);
        procesoEscucha.start();
    }
}
