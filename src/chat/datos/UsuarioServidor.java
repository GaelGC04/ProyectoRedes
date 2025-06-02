package chat.datos;

import java.util.UUID;

public record UsuarioServidor(
        String nombre,
        UUID uuid,
        int ip,
        short puerto
) {
}
