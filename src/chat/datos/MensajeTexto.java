package chat.datos;

import java.util.UUID;

public final class MensajeTexto extends Mensaje {
    private String contenido;

    private MensajeTexto() {}

    public MensajeTexto(int id, UUID remitente, UUID destinatario, String contenido) {
        super(id, remitente, destinatario);
        this.contenido = contenido;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    @Override
    public String convertirAProtocolo() {
        return "";
    }

    @Override
    public void convertirDeProtocolo(String protocolo) {

    }

    public static MensajeTexto construirConProtocolo(String protocolo) {
        MensajeTexto mensaje = new MensajeTexto();
        mensaje.convertirDeProtocolo(protocolo);
        return mensaje;
    }
}
