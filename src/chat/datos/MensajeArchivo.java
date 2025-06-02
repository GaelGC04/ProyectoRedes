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
                tipo: msj
                remitente: %s
                destinatario: %s
                nombre: %s
                tamanio: %d
                bytes: %s""".formatted(getRemitente(), getDestinatario(), nombre, tamanio, bytesArchivo);
    }

    @Override
    public void convertirDeProtocolo(String protocolo) {
        String[] lineas = protocolo.split("\n", 5);
        if (!lineas[0].equals("tipo: msj")) {
            return;
        }
        String remitente = lineas[1].split(": ")[1];
        setRemitente(UUID.fromString(remitente));
        String destinatario = lineas[2].split(": ")[1];
        setDestinatario(UUID.fromString(destinatario));
        nombre = lineas[3].split(": ")[1];
        tamanio = Integer.parseInt(lineas[4].split(": ")[1]);
        String bytesArchivoString = lineas[5].split(": ")[1].substring(0, tamanio);
        bytes = bytesArchivoString.getBytes();
    }

    public static MensajeArchivo construirConProtocolo(String protocolo) {
        MensajeArchivo mensaje = new MensajeArchivo();
        mensaje.convertirDeProtocolo(protocolo);

        return mensaje;
    }
}
