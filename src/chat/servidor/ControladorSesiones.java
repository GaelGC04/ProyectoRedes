package chat.servidor;

import chat.datos.UsuarioServidor;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ControladorSesiones {
    private Map<UUID, UsuarioServidor> sesiones;

    private static ControladorSesiones controladorSesiones = null;

    public static ControladorSesiones getInstance() {
        if (controladorSesiones == null) {
            controladorSesiones = new ControladorSesiones();
        }

        return controladorSesiones;
    }

    private ControladorSesiones() {
        sesiones = new ConcurrentHashMap<>();
    }

    public void conectar(UsuarioServidor usuario) {
        sesiones.put(usuario.uuid(), usuario);
        System.out.println(sesiones);
    }

    public List<UsuarioServidor> obtenerUsuarios(UUID usuarioPropio) {
        ArrayList<UsuarioServidor> usuarios = new ArrayList<>(sesiones.size() - 1);
        for (var entradaUsuario : sesiones.entrySet()) {
            if (!entradaUsuario.getKey().equals(usuarioPropio)) {
                usuarios.add(entradaUsuario.getValue());
            }
        }

        return usuarios;
    }

    public UsuarioServidor obtenerUsuario(UUID usuario) {
        return sesiones.get(usuario);
    }

    public void desconectar(UUID usuario) {
        sesiones.remove(usuario);
    }

    public void desconectarPorSocket(Socket socket) {
        for(UsuarioServidor usuario : sesiones.values()) {
            if (usuario.socketCliente().equals(socket)) {
                sesiones.remove(usuario.uuid());
            }
        }
    }
}
