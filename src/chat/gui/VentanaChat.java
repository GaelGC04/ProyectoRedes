package chat.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

// Esta clase se encarga de mostrar el chat del cliente
public class VentanaChat extends JFrame {
    private JTextArea areaMensajes;
    private JTextField campoTexto;
    private JButton botonAdjuntar;
    private JButton botonEnviar;

    private String nombreUsuarioActual;
    private String destinatario;
    
    public VentanaChat(String nombreUsuarioActual, String destinatario) {
        this.nombreUsuarioActual = nombreUsuarioActual;
        this.destinatario = destinatario;
    }

    public void cargarVentana(){
        // Aqui se signa el nombre de la ventana indicando el chat con la persona
        setTitle("Chat - " + destinatario);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Se le asignan dimensiones similares a las de un teléfono
        setSize(400, 600);
        setLocationRelativeTo(null);

        // Aqui hay colores como para un tema oscuro
        Color fondo = new Color(40, 40, 40);
        Color fondoVerde = new Color(40, 150, 40);
        Color texto = new Color(230, 230, 230);
        Color fondoPaneles = new Color(30, 30, 30);
        Color fondoBoton = new Color(60, 60, 60);

        getContentPane().setBackground(fondo);

        // Sección donde se encuentra el nombre del destinatario y botón de salir del chat
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(fondoPaneles);

        // Botón de volver a contactos a la izquierda en un botón chiquito
        JButton botonContactos = new JButton("←");
        botonContactos.setForeground(texto);
        botonContactos.setBackground(fondoBoton);
        botonContactos.setFocusPainted(false);
        botonContactos.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        // En caso de presionarlo se cierra el chat actual y se abren los contactos
        botonContactos.addActionListener(e -> {
            this.dispose();
            VentanaContactos.cargarContactos(nombreUsuarioActual);
        });
        panelSuperior.add(botonContactos, BorderLayout.WEST);

        // Nombre del destinatario
        JLabel etiquetaContacto = new JLabel(this.destinatario);
        etiquetaContacto.setFont(new Font("Roboto", Font.BOLD, 18));
        etiquetaContacto.setForeground(texto);
        etiquetaContacto.setHorizontalAlignment(SwingConstants.RIGHT);
        etiquetaContacto.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        panelSuperior.add(etiquetaContacto, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);


        // Mensajes en texto de momento que no se puede editar y se puede scrollear
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

        // Parte de texto y botón de añadir archivos y enviar mensaje
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(fondoPaneles);

        // Panel para adjuntar, texto y enviar
        JPanel panelInput = new JPanel(new BorderLayout());
        panelInput.setBackground(fondoPaneles);

        // Botón para añadir los archivos
        botonAdjuntar = new JButton("+");
        botonAdjuntar.setPreferredSize(new Dimension(50, 40));
        botonAdjuntar.setBackground(fondoBoton);
        botonAdjuntar.setForeground(texto);
        // Se llama a JFileChooser para seleccionar el archivo, se envía automaticamente una vez seleccionado
        botonAdjuntar.addActionListener(e -> adjuntarArchivo());
        panelInput.add(botonAdjuntar, BorderLayout.WEST);

        // Campo de texto
        campoTexto = new JTextField();
        campoTexto.setBackground(fondoBoton);
        campoTexto.setForeground(texto);
        campoTexto.setCaretColor(texto);
        panelInput.add(campoTexto, BorderLayout.CENTER);

        // Botón para enviar lo que sea que esté en el campo de texto
        botonEnviar = new JButton("Enviar");
        botonEnviar.setPreferredSize(new Dimension(70, 40));
        botonEnviar.setBackground(fondoVerde);
        botonEnviar.setForeground(texto);
        // Se envía el evento para poder enviar el mensaje
        botonEnviar.addActionListener(this::enviarMensaje);
        panelInput.add(botonEnviar, BorderLayout.EAST);

        panelInferior.add(panelInput, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);
    }

    // Método para cargar los archivos con JFileChooser
    private void adjuntarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "Archivo seleccionado: " + fileChooser.getSelectedFile().getName());
        }
    }

    // Método usado para enviar mensaje
    private void enviarMensaje(ActionEvent e) {
        String texto = campoTexto.getText().trim();
        if (!texto.isEmpty()) {
            areaMensajes.append(this.nombreUsuarioActual + " (Tú): " + texto + "\n");
            campoTexto.setText("");
        }
    }
}