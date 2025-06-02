package chat.datos;

import java.net.Socket;
import java.util.UUID;

public record UsuarioServidor(
        String nombre,
        UUID uuid,
        Socket socketCliente
) implements Comparable<UsuarioServidor> {

    @Override
    public int compareTo(UsuarioServidor o) {
        return uuid.compareTo(o.uuid);
    }
}
