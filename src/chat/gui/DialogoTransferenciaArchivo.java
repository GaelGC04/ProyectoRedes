package chat.gui;

import javax.swing.*;
import java.awt.*;

public class DialogoTransferenciaArchivo extends JDialog {
    private final JProgressBar barraProgreso;
    private final JLabel labelEstado;
    private final JLabel labelTasa;
    private final JLabel labelTiempoTranscurrido;
    private final JLabel labelTiempoRestante;

    public DialogoTransferenciaArchivo(Frame parent, String nombreArchivo, long tamArchivo) {
        super(parent, "Transfiriendo el archivo...", false);
        setLayout(new BorderLayout(10, 10));
        setSize(600, 200);
        setLocationRelativeTo(parent);

        labelEstado = new JLabel("Iniciando");
        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        labelTasa = new JLabel("Tasa: 0 bps");
        labelTiempoTranscurrido = new JLabel("Tiempo transcurrido: 0s");
        labelTiempoRestante = new JLabel("Tiempo restante: 0s");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Archivo: " + nombreArchivo + " (" + tamArchivo + " bytes)"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(labelEstado);
        panel.add(barraProgreso);
        panel.add(labelTasa);
        panel.add(labelTiempoTranscurrido);
        panel.add(labelTiempoRestante);

        add(panel, BorderLayout.CENTER);
    }

    public void actualizar(double porcentaje, long enviados, long total, double bps, double transcurrido, double restante) {
        labelEstado.setText("Progreso (bytes enviados): " + porcentaje + "% (" + enviados + "/" + total + " bytes)");
        barraProgreso.setValue((int) porcentaje);
        labelTasa.setText("Tasa de transferencia: " + bps + " bps");
        labelTiempoTranscurrido.setText("Tiempo transcurrido: " + transcurrido + "s");
        labelTiempoRestante.setText("Tiempo aproximado restante: " + restante + "s");
    }

    public static DialogoTransferenciaArchivo mostrar(Frame parent, String nombreArchivo, long tamArchivo) {
        DialogoTransferenciaArchivo dialogo = new DialogoTransferenciaArchivo(parent, nombreArchivo, tamArchivo);
        dialogo.setAlwaysOnTop(true);
        SwingUtilities.invokeLater(() -> {
            dialogo.setVisible(true);
            dialogo.toFront();
            dialogo.requestFocus();
        });
        return dialogo;
    }

    public void cerrar() {
        SwingUtilities.invokeLater(this::dispose);
    }
}
