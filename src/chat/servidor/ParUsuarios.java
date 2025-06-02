package chat.servidor;

import chat.datos.UsuarioServidor;

public record ParUsuarios (
        UsuarioServidor usuario1,
        UsuarioServidor usuario2
) {
    public static ParUsuarios nuevoPar(UsuarioServidor usuario1, UsuarioServidor usuario2) {
        if (usuario1.compareTo(usuario2) < 0) {
            return new ParUsuarios(usuario1, usuario2);
        } else {
            return new ParUsuarios(usuario2, usuario1);
        }
    }
}
