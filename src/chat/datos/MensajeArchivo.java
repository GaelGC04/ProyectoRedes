package chat.datos;

import java.nio.charset.StandardCharsets;
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
        String bytesArchivo = new String(bytes, StandardCharsets.US_ASCII);
        return """
                tipo: msj
                remitente: %s
                destinatario: %s
                nombre: %s
                bytes: %s""".formatted(getRemitente(), getDestinatario(), nombre, bytesArchivo);
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
        String bytesArchivoString = lineas[4].split(": ")[1];
        bytes = bytesArchivoString.getBytes(StandardCharsets.US_ASCII);
    }

    public static MensajeArchivo construirConProtocolo(String protocolo) {
        MensajeArchivo mensaje = new MensajeArchivo();
        mensaje.convertirDeProtocolo(protocolo);

        return mensaje;
    }
}
