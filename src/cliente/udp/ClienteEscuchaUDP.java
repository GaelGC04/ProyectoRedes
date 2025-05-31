package cliente.udp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
//import java.io.*;
 
//declaramos la clase udp escucha
public class ClienteEscuchaUDP extends Thread{
    //Definimos el sockets.
    protected final int PUERTO_CLIENTE;
    protected DatagramSocket socket;

    public  ClienteEscuchaUDP(DatagramSocket socketNuevo){
        socket=socketNuevo;
        PUERTO_CLIENTE=socket.getLocalPort();
    }

    public void run() { // se ejecuta cuando se invoca al mètodo start
        try {
            Mensaje mensajeObj=new Mensaje();
            do {
                mensajeObj=recibeMensaje();
            } while (!mensajeObj.getMensaje().startsWith("fin"));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excepcion C: "+e.getMessage());
            System.exit(1);
        }
    }

    private Mensaje recibeMensaje() throws Exception{
        Mensaje mensajeObj=new Mensaje();
        byte[] recogerServidor_bytes;
        final int MAX_BUFFER=256;
        DatagramPacket servPaquete;

        recogerServidor_bytes = new byte[MAX_BUFFER];

        //Esperamos a recibir un paquete
        servPaquete = new DatagramPacket(recogerServidor_bytes,MAX_BUFFER);
        socket.receive(servPaquete);
        mensajeObj.setAddressServidor(servPaquete.getAddress());
        mensajeObj.setPuertoServidor(servPaquete.getPort());

        //Convertimos el mensaje recibido en un string
        String cadenaMensaje = new String(recogerServidor_bytes).trim();
        mensajeObj.setMensaje(cadenaMensaje);

        //Imprimimos el paquete recibido
        EntradaSalida.mostrarMensaje("Mensaje recibido \""+mensajeObj.getMensaje() +"\" de servidor "+
                mensajeObj.getAddressServidor()+":"+mensajeObj.getPuertoServidor()+"\n");

        return mensajeObj;
    }
}
