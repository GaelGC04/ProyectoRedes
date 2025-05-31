package cliente.udp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
import java.io.*;
 
//declaramos la clase udp envia
public class ClienteEnviaUDP extends Thread{
    //Definimos el sockets.
    protected final int PUERTO_SERVER;
    protected final String SERVER;
    protected DatagramSocket socket;

    public ClienteEnviaUDP(DatagramSocket nuevoSocket, String servidor, int puertoServidor){
        socket = nuevoSocket;
        SERVER=servidor;
        PUERTO_SERVER=puertoServidor;
    }
    
    public void run() {
        try {
            Mensaje mensajeObj=new Mensaje();
            EntradaSalida.mostrarMensaje("Cliente listo para mandar...\n");
            do {
                enviaMensaje(mensajeObj);
            } while (!mensajeObj.getMensaje().startsWith("fin"));
        }
        catch (Exception e) {
            System.err.println("Exception "+e.getMessage());
            System.exit(1);
        }
    }

    private void enviaMensaje(Mensaje mensajeObj) throws Exception{
        BufferedReader in= new BufferedReader(new InputStreamReader(System.in));
        byte[] mensaje_bytes;
        String mensaje="";
        DatagramPacket paquete;

        InetAddress addressServer=InetAddress.getByName(SERVER);
        mensaje = in.readLine();
        //mensaje_bytes=new byte[mensaje.length()];
        mensaje_bytes = mensaje.getBytes();
        paquete = new DatagramPacket(mensaje_bytes,mensaje.length(),addressServer,PUERTO_SERVER);
        socket.send(paquete);

        String mensajeMandado=new String(paquete.getData(),0,paquete.getLength()).trim();
        mensajeObj.setMensaje(mensajeMandado);
        mensajeObj.setAddressServidor(paquete.getAddress());
        mensajeObj.setPuertoServidor(paquete.getPort());

        EntradaSalida.mostrarMensaje("Mensaje \""+ mensajeObj.getMensaje() +
                "\" enviado a servidor "+mensajeObj.getAddressServidor() + ":"+mensajeObj.getPuertoServidor()+"\n");
    }
}
