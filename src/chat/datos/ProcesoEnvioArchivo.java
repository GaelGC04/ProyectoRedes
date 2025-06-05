package chat.datos;

import chat.gui.DialogoTransferenciaArchivo;
import chat.gui.ManejadorConexion;

import java.io.DataOutputStream;

import javax.swing.JOptionPane;

public class ProcesoEnvioArchivo extends Thread {
    private final MensajeArchivo mensajeArchivo;

    public ProcesoEnvioArchivo(MensajeArchivo mensajeArchivo) {
        this.mensajeArchivo = mensajeArchivo;
    }

    @Override
    public void run() {
        try {
            var conexion = ManejadorConexion.obtenerInstancia();
            DataOutputStream salida = new DataOutputStream(conexion.getSocketTcp().getOutputStream());

            // Envía encabezado
            String protocolo = mensajeArchivo.convertirAProtocolo();
            salida.writeUTF(protocolo);

            // Envía archivo en bloques y muestra el progreso
            long tamanioArchivo = mensajeArchivo.getBytesArchivo().length; // Se obtiene el tamaño del archivo enviado
            long tiempoInicio = System.nanoTime(); // Se obtiene tiempo preciso actual
            
            int tamanioBloque = 16 * 1024;
            long bytesEnviados = 0;
            long tiempoRecienteAviso = System.nanoTime();

            // Se hace la instancia del jdialog para que se muestre el envío para el usuario que envie el archivo
            DialogoTransferenciaArchivo dialogo = DialogoTransferenciaArchivo.mostrar(null, mensajeArchivo.getNombreArchivo(), tamanioArchivo);
            
            for (int iteradorArchivo = 0; iteradorArchivo < tamanioArchivo; iteradorArchivo += tamanioBloque) {
                int tamanio = Math.min(tamanioBloque, (int)(tamanioArchivo - iteradorArchivo));
                salida.write(mensajeArchivo.getBytesArchivo(), iteradorArchivo, tamanio);
                bytesEnviados += tamanio;

                // Se calcula el tiempo y la tasa de transferencia
                long momentoActual = System.nanoTime();
                double segundosTranscurridos = (momentoActual - tiempoInicio) / 1_000_000_000.0; // Esto es por que son nanosegundos
                double bps = (bytesEnviados * 8) / segundosTranscurridos;
                double porcentaje = (bytesEnviados * 100.0) / tamanioArchivo;
                double tiempoTotalEstimado = (tamanioArchivo * segundosTranscurridos) / bytesEnviados;
                double tiempoRestante = tiempoTotalEstimado - segundosTranscurridos;

                // Se actualiza el jdialog mostrando el progreso nuevo
                dialogo.actualizar(porcentaje, bytesEnviados, tamanioArchivo, bps, segundosTranscurridos, tiempoRestante);

                // Cada vez que pase medio segundo se muestra el avance del archivo y como va y los bps
                if ((momentoActual - tiempoRecienteAviso) > 500_000_000L || bytesEnviados == tamanioArchivo) {
                    tiempoRecienteAviso = momentoActual;
                }
            }
            dialogo.cerrar();
            salida.flush();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al enviar el archivo: " + e.getMessage());
        }
    }
}
