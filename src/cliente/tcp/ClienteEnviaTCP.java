package cliente.tcp;
import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
// importar la libreria java.net
import java.io.*;
// importar la libreria java.io
 
// declararamos la clase clientetcp
public class ClienteEnviaTCP extends Thread{
    // declaramos un objeto socket para realizar la comunicación
    protected Socket socket; //TCP
    protected final int PUERTO_SERVER;
    protected final String SERVER;
    
    public ClienteEnviaTCP(String servidor, int puertoS)throws Exception{
        PUERTO_SERVER=puertoS;
        SERVER=servidor;
        
        // Instanciamos un socket con la dirección del destino y el
        // puerto que vamos a utilizar para la comunicación
        socket = new Socket(SERVER,PUERTO_SERVER); //se intenta hacer la conexión
    }
    
    public void run () {
        // Declaramos un bloque try y catch para controlar la ejecución del subprograma
        try {
            Mensaje mensajeObj=new Mensaje();
            EntradaSalida.mostrarMensaje("Cliente conectado con servidor "+
                    socket.getInetAddress()+ ":"+socket.getPort()+"...\n");
            EntradaSalida.mostrarMensaje("Cliente listo para mandar...\n");
            do {
                enviaMensaje(mensajeObj);
                // mientras el mensaje no encuentre la cadena fin, seguiremos ejecutando
                // el bucle do-while
            } while (!mensajeObj.getMensaje().startsWith("fin"));
        }
        // utilizamos el catch para capturar los errores que puedan surgir
        catch (Exception e) {
            // si existen errores los mostrará en la consola y después saldrá del
            // programa
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void enviaMensaje(Mensaje mensajeObj) throws Exception {
        // Creamos una instancia BuffererReader en la
        // que guardamos los datos introducido por el usuario
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        // Declaramos e instanciamos el objeto DataOutputStream
        // que nos valdrá para enviar datos al servidor destino
        DataOutputStream out =new DataOutputStream(socket.getOutputStream());

        // declaramos una variable de tipo string
        String mensaje="";

        // los datos que hemos obtenido despues de ejecutar la función
        // "readLine" en la instancia "in"
        mensaje = in.readLine();
        // enviamos el mensaje codificado en UTF
        out.writeUTF(mensaje);
        mensajeObj.setMensaje(mensaje);
        mensajeObj.setAddressServidor(socket.getInetAddress());
        mensajeObj.setPuertoServidor(socket.getPort());

        EntradaSalida.mostrarMensaje("Mensaje \""+ mensajeObj.getMensaje() +
                "\" enviado a servidor "+mensajeObj.getAddressServidor() + ":"+mensajeObj.getPuertoServidor()+"\n");
    }
}
