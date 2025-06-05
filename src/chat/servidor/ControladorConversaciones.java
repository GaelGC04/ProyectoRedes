package chat.servidor;

import chat.datos.Conversacion;
import chat.datos.UsuarioServidor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControladorConversaciones {
    private Map<ParUsuarios, Conversacion>  conversaciones;

    private static ControladorConversaciones instance;

    public static ControladorConversaciones getInstance() {
        if (instance == null) {
            instance = new ControladorConversaciones();
        }
        return instance;
    }

    private ControladorConversaciones() {
        conversaciones = new ConcurrentHashMap<>();
    }

    public Conversacion obtenerConversacion(UsuarioServidor usuario1, UsuarioServidor usuario2) {
        ParUsuarios par = ParUsuarios.nuevoPar(usuario1, usuario2);
        Conversacion conversacion = conversaciones
                .computeIfAbsent(par, (parActual) -> new Conversacion(parActual.usuario1(), parActual.usuario2()));
        conversaciones.putIfAbsent(par, conversacion);
        return conversacion;
    }
}
