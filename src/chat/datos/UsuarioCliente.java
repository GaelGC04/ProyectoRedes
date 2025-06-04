package chat.datos;

import java.util.ArrayList;
import java.util.List;
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
    public boolean convertirDeProtocolo(String protocolo) {
        String[] lineas = protocolo.split("\n");
        if (!lineas[0].equals("tipo: registro")) {
            return false;
        }
        uuid = UUID.fromString(lineas[1].split(": ")[1]);
        nombre = lineas[2].split(": ")[1];
        return true;
    }

    @Override
    public String convertirAProtocolo() {
        return """
                tipo: registro
                uuid: %s
                nombre: %s""".formatted(uuid, nombre);
    }

    public static List<UsuarioCliente> convertirDeRespuestaLista(String protocolo){
        List<UsuarioCliente> usuarios = new ArrayList<>();
        String[] lineas = protocolo.split(";\n");
        for (String linea : lineas) {
            try {
                UUID uuid = UUID.fromString(linea.split(",")[0]);
                String nombre = linea.split(",")[1];
                usuarios.add(new UsuarioCliente(nombre, uuid));
            } catch (Exception e) {
                continue;
            }
        }
        return usuarios;
    }

    @Override
    public String toString() {
        return this.nombre;
    }
}
