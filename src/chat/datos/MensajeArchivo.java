package chat.datos;

import java.util.UUID;

public final class MensajeArchivo extends Mensaje {
    private byte[] bytes;
    private String nombre;
    private int tamanio;

    private MensajeArchivo() {}

    public MensajeArchivo(int id, UUID remitente, UUID destinatario, byte[] bytes, String nombre) {
        super(id, remitente, destinatario);
        this.bytes = bytes;
        this.nombre = nombre;
        this.tamanio = bytes.length;
    }

    @Override
    public String convertirAProtocolo() {
        String bytesArchivo = new String(bytes);
        return """
                tipo: archivo
                id: %d
                remitente: %s
                destinatario: %s
                nombre: %s
                tamanio: %d""".formatted(getId(), getRemitente(), getDestinatario(), nombre, tamanio);
    }

    @Override
    public boolean convertirDeProtocolo(String protocolo) {
        String[] lineas = protocolo.split("\n", 6);
        if (!lineas[0].equals("tipo: archivo")) {
            return false;
        }
        int id = Integer.parseInt(lineas[1].split(": ")[1]);
        setId(id);
        String remitente = lineas[2].split(": ")[1];
        setRemitente(UUID.fromString(remitente));
        String destinatario = lineas[3].split(": ")[1];
        setDestinatario(UUID.fromString(destinatario));
        nombre = lineas[4].split(": ")[1];
        tamanio = Integer.parseInt(lineas[5].split(": ")[1]);
        return true;
    }

    public static MensajeArchivo construirConProtocolo(String protocolo) {
        MensajeArchivo mensaje = new MensajeArchivo();
        mensaje.convertirDeProtocolo(protocolo);

        return mensaje;
    }

    public String getNombreArchivo(){
        return this.nombre;
    }

    public byte[] getBytesArchivo() {
        return this.bytes;
    }

    public int getTamanio() {
        return tamanio;
    }

    public void setBytesArchivo(byte[] bytes) {
        this.bytes = bytes;
    }
}
