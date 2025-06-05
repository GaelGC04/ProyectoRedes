package chat.servidor;

import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ControladorEscuchadores {
    private static ControladorEscuchadores instance;

    public static ControladorEscuchadores getInstance() {
        if (instance == null) {
            instance = new ControladorEscuchadores();
        }
        return instance;
    }

    private Map<UUID, Socket> escuchadores;

    private ControladorEscuchadores() {
        escuchadores = new ConcurrentHashMap<>();
    }

    public void agregarEscuchador(UUID usuario, Socket socket) {
        escuchadores.putIfAbsent(usuario, socket);
    }

    public Socket obtenerSocketEscucha(UUID usuario) {
        return escuchadores.get(usuario);
    }

    public void quitarSocket(UUID usuario) {
        escuchadores.remove(usuario);
    }

    public void quitarPorSocket(Socket socket) {
        for (var entrada : escuchadores.entrySet()) {
            if (entrada.getValue().equals(socket)) {
                escuchadores.remove(entrada.getKey());
                break;
            }
        }
    }
}
