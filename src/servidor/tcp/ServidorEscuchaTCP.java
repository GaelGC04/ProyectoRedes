package servidor.tcp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
//importar la libreria java.net
 
import java.io.*;
//importar la libreria java.io
// declaramos la clase servidortcp
 
public class ServidorEscuchaTCP extends Thread {
    // declaramos un objeto ServerSocket para realizar la comunicación
    protected ServerSocket socket; //TCP
    protected Socket socket_cli;
    protected final int PUERTO_SERVER;
    
    public ServidorEscuchaTCP(int puertoS)throws Exception{
        PUERTO_SERVER=puertoS;
        // Instanciamos un ServerSocket con la dirección del destino y el
        // puerto que vamos a utilizar para la comunicación

        socket = new ServerSocket(PUERTO_SERVER); //ejecuta la primitva de LISTEN
    }
    // método principal main de la clase
    public void run() { //invocación de start
        // Declaramos un bloque try y catch para controlar la ejecución del subprograma
        try {
            // Creamos un socket_cli al que le pasamos el contenido del objeto socket después
            // de ejecutar la función accept que nos permitirá aceptar conexiones de clientes
            EntradaSalida.mostrarMensaje("Servidor escuchando...\n");
            socket_cli = socket.accept();

            // Creamos un bucle do while en el que recogemos el mensaje
            // que nos ha enviado el cliente y después lo mostramos
            // por consola
            EntradaSalida.mostrarMensaje("Servidor conectado con cliente "+
                    socket_cli.getInetAddress()+ ":"+socket_cli.getPort()+"...\n");
            do {
                Mensaje mensajeObj=recibeMensaje();
            } while (true);
        }
        // utilizamos el catch para capturar los errores que puedan surgir
        catch (Exception e) {

            // si existen errores los mostrará en la consola y después saldrá del
            // programa
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    private Mensaje recibeMensaje() throws Exception{
        // Declaramos e instanciamos el objeto DataInputStream
        // que nos valdrá para recibir datos del cliente
        Mensaje mensajeObj=new Mensaje();

        DataInputStream in =new DataInputStream(socket_cli.getInputStream());

        String mensaje ="";

        mensaje = in.readUTF();
        mensajeObj.setMensaje(mensaje);
        mensajeObj.setAddressCliente(socket_cli.getInetAddress());
        mensajeObj.setPuertoCliente(socket_cli.getPort());

        //Imprimimos el mensaje recibido
        EntradaSalida.mostrarMensaje("Mensaje recibido \""+mensajeObj.getMensaje() +"\" del cliente "+
                mensajeObj.getAddressCliente()+":"+mensajeObj.getPuertoCliente()+"\n");
        return mensajeObj;
    }
}
