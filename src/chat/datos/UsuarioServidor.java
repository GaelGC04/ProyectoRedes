package chat.datos;

import java.net.Socket;
import java.util.UUID;

public record UsuarioServidor(
        String nombre,
        UUID uuid,
        Socket socketCliente
) {
}
