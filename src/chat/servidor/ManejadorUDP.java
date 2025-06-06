package chat.servidor;

import chat.checksum.Checksum;
import chat.datos.Conversacion;
import chat.datos.MensajeTexto;
import chat.datos.UsuarioServidor;

import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Arrays;

public class ManejadorUDP implements Runnable {
    private DatagramPacket paqueteEntrada;
    private DatagramSocket socket;
    private byte[] datos;

    public ManejadorUDP(DatagramPacket paqueteEntrada, DatagramSocket socket, byte[] datos) {
        this.paqueteEntrada = paqueteEntrada;
        this.socket = socket;
        this.datos = datos;
    }

    @Override
    public void run() {
        short checksum = (short)(((datos[0] << 8) | (datos[1] & 0xFF)));
        int tamanioReal = paqueteEntrada.getLength();
        String entrada = new String(datos, 2, tamanioReal - 2);
        System.out.println("UDP: Nuevo mensaje: " + entrada);
        if (!checksumValido(entrada, checksum)) {
            System.out.println("UDP: el checksum no es válido. Checksum recibido: " + checksum + ", checksum mensaje: " + Checksum.calcularChecksum(entrada));
            System.out.println("Bytes del mensaje recibido: " + Arrays.toString(datos));
            byte[] bytes = "No".getBytes();
            DatagramPacket salida = new DatagramPacket(bytes, bytes.length, paqueteEntrada.getAddress(), paqueteEntrada.getPort());
            try {
                socket.send(salida);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        String tipoPeticion = entrada.split("\n", 2)[0];
        if (tipoPeticion.equals("tipo: msj")) {
            manejarMensajeTexto(entrada, checksum);
        }
    }

    private boolean checksumValido(String entrada, short checksum) {
        boolean checksumValido = Checksum.verificarChecksum(entrada, checksum);
        if (!checksumValido) {
            byte[] respuesta = "No".getBytes();
            DatagramPacket paqueteSalida = new DatagramPacket(respuesta, respuesta.length, paqueteEntrada.getSocketAddress());
            try {
                int intentos = 0;
                do {
                    intentos++;
                    socket.send(paqueteSalida);
                    break;
                } while (intentos < 10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private void manejarMensajeTexto(String protocolo, short checksum) {
        System.out.println("Mensaje " + protocolo);
        String[] lineas = protocolo.split("\n", 5);

        MensajeTexto mensaje = MensajeTexto.construirConProtocolo(protocolo);
        var sesiones = ControladorSesiones.getInstance();
        var conversaciones = ControladorConversaciones.getInstance();
        UsuarioServidor usuario1 = sesiones.obtenerUsuario(mensaje.getRemitente());
        UsuarioServidor usuario2 = sesiones.obtenerUsuario(mensaje.getDestinatario());
        Conversacion conversacion = conversaciones.obtenerConversacion(usuario1, usuario2);
        conversacion.agregarMensaje(mensaje);
        byte[] respuestaRemitente = "Ok".getBytes();
        DatagramPacket paqueteRespuesta = new DatagramPacket(respuestaRemitente, respuestaRemitente.length, paqueteEntrada.getSocketAddress());
        try {
            System.out.println("Enviando confirmación");
            socket.send(paqueteRespuesta);
            System.out.println("Enviando mensaje");
            Socket socketUsuario2 = ControladorEscuchadores.getInstance().obtenerSocketEscucha(usuario2.uuid());
            DataOutputStream salida = new DataOutputStream(socketUsuario2.getOutputStream());
            salida.writeUTF(protocolo);
            System.out.println("Enviado");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
