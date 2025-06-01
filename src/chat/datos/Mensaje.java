package chat.datos;

import java.util.UUID;

public abstract class Mensaje {
    protected int id;
    protected UUID remitente;
    protected UUID destinatario;

    public Mensaje() {}

    public Mensaje(int id, UUID remitente, UUID destinatario) {
        this.id = id;
        this.remitente = remitente;
        this.destinatario = destinatario;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getRemitente() {
        return remitente;
    }

    public void setRemitente(UUID remitente) {
        this.remitente = remitente;
    }

    public UUID getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(UUID destinatario) {
        this.destinatario = destinatario;
    }

    public abstract String convertirAProtocolo();
    public abstract void convertirDeProtocolo(String protocolo);
}
