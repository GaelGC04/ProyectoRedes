package chat.datos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Conversacion {
    private List<Mensaje> conversacion;
    private UsuarioServidor usuario1;
    private UsuarioServidor usuario2;

    public Conversacion(UsuarioServidor usuario1, UsuarioServidor usuario2) {
        // Con esto: Conversacion(A, B) = Conversacion(B, A)
        if (usuario1.compareTo(usuario2) < 0) {
            this.usuario1 = usuario1;
            this.usuario2 = usuario2;
        } else {
            this.usuario1 = usuario2;
            this.usuario2 = usuario1;
        }

        conversacion = Collections.synchronizedList(new ArrayList<>());
    }

    public MensajeArchivo descargarArchivo(int idMensaje) {
        for (Mensaje mensaje : conversacion) {
            if (mensaje.getId() == idMensaje) {
                if (mensaje instanceof MensajeTexto texto) {
                    System.out.println(texto.getContenido());
                    System.out.println(texto.getContenido());
                    System.out.println(texto.getContenido());
                    System.out.println(texto.getContenido());
                }
                return (MensajeArchivo) mensaje;
            }
        }
        return null;
    }

    public void agregarMensaje(Mensaje mensaje) {
        conversacion.add(mensaje);
    }

    public List<Mensaje> obtenerMensajes() {
        return conversacion;
    }
}
