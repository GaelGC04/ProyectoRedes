package servidor.udp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
//import java.io.*;

public class ServidorEscuchaUDP extends Thread{
    protected DatagramSocket socket; //UDP
    protected final int PUERTO_SERVER;

    public ServidorEscuchaUDP(int puertoS) throws Exception{
        //Creamos el socket
        PUERTO_SERVER=puertoS;
        socket = new DatagramSocket(puertoS);
    }

    public void run() { //se invoca cuando se llama al mètodo start()
        try {
            Mensaje mensajeObj=new Mensaje();
            //Iniciamos el bucle
            EntradaSalida.mostrarMensaje("Servidor listo...\n");
            do {
                mensajeObj=recibeMensaje();
                procesaMensaje(mensajeObj);

            } while (!mensajeObj.getMensaje().startsWith("fin"));
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void procesaMensaje(Mensaje mensajeObj) throws Exception{
        String mensajeComp ="";

        if (mensajeObj.getMensaje().startsWith("fin")) {
            mensajeComp="Transmisión con el servidor finalizada...";
            mensajeObj.setMensaje(mensajeComp);
            enviaMensaje(mensajeObj);
        }
        else if (mensajeObj.getMensaje().startsWith("hola")) {
            mensajeComp="¿Cómo estas?";

            //formateamos el mensaje de salida
            mensajeObj.setMensaje(mensajeComp);
            enviaMensaje(mensajeObj);
        }
        else if (mensajeObj.getMensaje().startsWith("bien y tú")) {
            mensajeComp="También estoy bien, gracias";

            //formateamos el mensaje de salida
            mensajeObj.setMensaje(mensajeComp);
            enviaMensaje(mensajeObj);
        }
        else{
            mensajeComp="...";
        }
    }

    private Mensaje recibeMensaje() throws Exception{
        Mensaje mensajeObj=new Mensaje();
        String mensaje="";
        byte[] mensaje_bytes;
        //byte[] mensaje2_bytes;
        final int MAX_BUFFER=256;
        DatagramPacket paquete;

        // Recibimos el paquete
        mensaje_bytes=new byte[MAX_BUFFER];
        paquete = new DatagramPacket(mensaje_bytes,MAX_BUFFER);
        socket.receive(paquete);

        // Lo formateamos
        //mensaje_bytes=new byte[paquete.getLength()];
        mensaje_bytes=paquete.getData();
        mensaje = new String(mensaje_bytes,0,paquete.getLength()).trim();
        mensajeObj.setMensaje(mensaje);

        //Obtenemos IP Y PUERTO
        mensajeObj.setPuertoCliente(paquete.getPort());
        mensajeObj.setAddressCliente(paquete.getAddress());

        // Lo mostramos por pantalla
        EntradaSalida.mostrarMensaje("Mensaje recibido \""+mensajeObj.getMensaje()+"\" del cliente "+
                mensajeObj.getAddressCliente()+":"+mensajeObj.getPuertoCliente()+"\n");

        return mensajeObj;
    }
    private void enviaMensaje(Mensaje mensajeObj) throws Exception{
        byte[] mensaje2_bytes = new byte[mensajeObj.getMensaje().length()];
        DatagramPacket envPaquete;

        mensaje2_bytes = mensajeObj.getMensaje().getBytes();

        //Preparamos el paquete que queremos enviar
        envPaquete = new DatagramPacket(mensaje2_bytes,mensaje2_bytes.length,mensajeObj.getAddressCliente(),mensajeObj.getPuertoCliente());

        // realizamos el envio
        socket.send(envPaquete);

        EntradaSalida.mostrarMensaje("Mensaje saliente del servidor \""+
                mensajeObj.getMensaje()+"\" al cliente " + mensajeObj.getAddressCliente() + ":"+mensajeObj.getPuertoCliente()+"\n");
    }
}
