package chat.datos;

import chat.gui.DialogoTransferenciaArchivo;
import chat.gui.ManejadorConexion;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

import javax.swing.JOptionPane;

public class ProcesoDescargaArchivo extends Thread {
    private final MensajeArchivo mensajeArchivo;

    public ProcesoDescargaArchivo(MensajeArchivo mensajeArchivo) {
        this.mensajeArchivo = mensajeArchivo;
    }

    @Override
    public void run() {
        try {
            var conexion = ManejadorConexion.obtenerInstancia();
            DataOutputStream salida = new DataOutputStream(conexion.getSocketTcp().getOutputStream());
            DataInputStream entrada = new DataInputStream(conexion.getSocketTcp().getInputStream());

            String protocolo = """
                    tipo: descargarArchivo
                    remitente: %s
                    destinatario: %s
                    idMensaje: %d""".formatted(mensajeArchivo.getRemitente().toString(), mensajeArchivo.getDestinatario().toString(), mensajeArchivo.getId());

            salida.writeUTF(protocolo);
            salida.flush();

            long tamanioArchivo = mensajeArchivo.getTamanio();
            long tiempoInicio = System.nanoTime();
            
            int tamanioBloque = 16 * 1024;
            long bytesRecibidos = 0;
            long tiempoRecienteAviso = System.nanoTime();

            DialogoTransferenciaArchivo dialogo = DialogoTransferenciaArchivo.mostrar(null, mensajeArchivo.getNombreArchivo(), tamanioArchivo);
            
            byte[] buffer = new byte[tamanioBloque];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (bytesRecibidos < tamanioArchivo) {
                int tamanio = (int)Math.min(tamanioBloque, tamanioArchivo - bytesRecibidos);
                int bytesLeidos = entrada.read(buffer, 0, tamanio);
                if (bytesLeidos == -1) break;
                baos.write(buffer, 0, bytesLeidos);
                bytesRecibidos += bytesLeidos;

                long momentoActual = System.nanoTime();
                double segundosTranscurridos = (momentoActual - tiempoInicio) / 1_000_000_000.0; // Esto es por que son nanosegundos
                double bps = (bytesRecibidos * 8) / segundosTranscurridos;
                double porcentaje = (bytesRecibidos * 100.0) / tamanioArchivo;
                double tiempoTotalEstimado = (tamanioArchivo * segundosTranscurridos) / bytesRecibidos;
                double tiempoRestante = tiempoTotalEstimado - segundosTranscurridos;

                dialogo.actualizar(porcentaje, bytesRecibidos, tamanioArchivo, bps, segundosTranscurridos, tiempoRestante);

                if ((momentoActual - tiempoRecienteAviso) > 500_000_000L || bytesRecibidos == tamanioArchivo) {
                    tiempoRecienteAviso = momentoActual;
                }
            }
            mensajeArchivo.setBytesArchivo(baos.toByteArray());
            baos.close();
            dialogo.cerrar();
            salida.flush();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al enviar el archivo: " + e.getMessage());
        }
    }
}
