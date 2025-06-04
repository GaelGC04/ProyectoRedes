package chat.gui;

import chat.checksum.Checksum;
import chat.datos.Mensaje;
import chat.datos.MensajeArchivo;
import chat.datos.MensajeTexto;
import chat.datos.UsuarioCliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ManejadorConexion {
    private static ManejadorConexion instancia;

    public static ManejadorConexion crearConexion(InetAddress ip, int puerto) throws Exception {
        instancia = new ManejadorConexion(ip, puerto);
        return instancia;
    }

    public static ManejadorConexion obtenerInstancia() {
        if (instancia == null) {
            throw new RuntimeException("Se debe crear primero una conexión");
        }

        return instancia;
    }

    private final Socket socketCliente;
    private final DatagramSocket socketUdpCliente;
    private final DataInputStream entrada;
    private final DataOutputStream salida;

    private ManejadorConexion(InetAddress ip, int puerto) throws Exception {
        socketCliente = new Socket(ip, puerto);
        socketUdpCliente = new DatagramSocket(puerto, ip);
        entrada = new DataInputStream(socketCliente.getInputStream());
        salida = new DataOutputStream(socketCliente.getOutputStream());
    }

    public List<UsuarioCliente> obtenerUsuarios(UsuarioCliente usuarioActual) throws Exception {
        String protocolo = """
                tipo: obtenerUsuarios
                usuario: %s
                """.formatted(usuarioActual.getUuid());
        salida.writeUTF(protocolo);

        String respuesta = entrada.readUTF();
        return UsuarioCliente.convertirDeRespuestaLista(respuesta);
    }

    public List<Mensaje> obtenerMensajes(UsuarioCliente usuario1, UsuarioCliente usuario2) throws Exception {
        String protocoloMensajes = """
                tipo: obtenerChat
                remitente: %s
                destinatario: %s""".formatted(usuario1.getUuid(), usuario2.getUuid());
        salida.writeUTF(protocoloMensajes);
        List<Mensaje> mensajes = new ArrayList<>();
        String respuesta = entrada.readUTF();
        String mensajeActual = respuesta;
        do {
            if (mensajeActual.startsWith("tipo: msj")) {
                MensajeTexto mensaje = MensajeTexto.construirConProtocolo(mensajeActual);
                mensajes.add(mensaje);
                mensajeActual = mensajeActual.substring(mensajeActual.indexOf("\u001E")).split("\n", 2)[1];
            } else if (mensajeActual.startsWith("tipo: archivo")) {
                MensajeArchivo archivo = MensajeArchivo.construirConProtocolo(mensajeActual);
                mensajes.add(archivo);
                int indicePosicionFin = archivo.convertirAProtocolo().length();
                mensajeActual = mensajeActual.substring(indicePosicionFin);
                mensajeActual = mensajeActual.split("\n", 2)[1];
            }
        } while (!mensajeActual.isBlank());

        return mensajes;
    }

    public void enviarMensajeTexto(MensajeTexto mensaje) throws Exception {
        String protocoloMensaje = mensaje.convertirAProtocolo();
        short checksum = Checksum.calcularChecksum(protocoloMensaje);
        byte[] bytesChecksum = {(byte)(checksum >> 8), (byte)checksum};
        String mensajePaquete = new String(bytesChecksum) + protocoloMensaje;
        byte[] bytesProtocolo = mensajePaquete.getBytes();
        byte[] bytesRespuesta = new byte[2];
        String respuesta;
        do {
            DatagramPacket paquete = new DatagramPacket(bytesProtocolo, 0, bytesProtocolo.length, socketUdpCliente.getInetAddress(), socketCliente.getPort());
            socketUdpCliente.send(paquete);
            DatagramPacket paqueteRespuesta = new DatagramPacket(bytesRespuesta, 0, bytesRespuesta.length);
            socketUdpCliente.receive(paqueteRespuesta);
            respuesta = new String(bytesRespuesta);
        } while ("No".equals(respuesta));
    }

    public static void cerrarConexion() throws Exception {
        if (instancia == null) {
            return;
        }

        instancia.socketCliente.close();
        instancia.socketUdpCliente.close();
        instancia = null;
    }
}
