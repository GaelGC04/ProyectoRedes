package chat.datos;

import java.util.UUID;

public class UsuarioCliente implements Protocolable {
    private String nombre;
    private UUID uuid;

    public UsuarioCliente(String nombre, UUID uuid) {
        this.nombre = nombre;
        this.uuid = uuid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void convertirDeProtocolo(String protocolo) {
        String[] lineas = protocolo.split("\n");
        uuid = UUID.fromString(lineas[0].split(": ")[1]);
        nombre = lineas[1].split(": ")[1];
    }

    @Override
    public String convertirAProtocolo() {
        return """
                uuid: %s
                nombre: %s""".formatted(uuid, nombre);
    }
}
