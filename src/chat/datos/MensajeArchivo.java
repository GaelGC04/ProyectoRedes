package chat.datos;

import java.util.UUID;

public final class MensajeArchivo extends Mensaje {
    private byte[] bytes;
    private String nombre;

    private MensajeArchivo() {}

    public MensajeArchivo(int id, UUID remitente, UUID destinatario, byte[] bytes, String nombre) {
        super(id, remitente, destinatario);
        this.bytes = bytes;
        this.nombre = nombre;
    }

    @Override
    public String convertirAProtocolo() {
        return "";
    }

    @Override
    public void convertirDeProtocolo(String protocolo) {

    }

    public static MensajeArchivo construirConProtocolo(String protocolo) {
        MensajeArchivo mensaje = new MensajeArchivo();
        mensaje.convertirDeProtocolo(protocolo);

        return mensaje;
    }
}
