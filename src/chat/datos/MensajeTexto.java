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
        String contenidoFormateado = contenido.replace("\u001E", "\\x1E");
        return """
                tipo: msj
                id: %d
                remitente: %s
                destinatario: %s
                contenido: %s""".formatted(getId(), getRemitente(), getDestinatario(), contenidoFormateado);
    }

    @Override
    public boolean convertirDeProtocolo(String protocolo) {
        String[] lineas = protocolo.split("\n", 5);
        if (!lineas[0].equals("tipo: msj")) {
            return false;
        }
        int id = Integer.parseInt(lineas[1].split(": ")[1]);
        setId(id);
        String remitente = lineas[2].split(": ")[1];
        setRemitente(UUID.fromString(remitente));
        String destinatario = lineas[3].split(": ")[1];
        setDestinatario(UUID.fromString(destinatario));
        contenido = lineas[4].split(": ")[1];
        contenido = contenido.replace("\\x1E", "\u001E");
        return true;
    }

    public static MensajeTexto construirConProtocolo(String protocolo) {
        MensajeTexto mensaje = new MensajeTexto();
        mensaje.convertirDeProtocolo(protocolo);
        return mensaje;
    }
}
