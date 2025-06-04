package chat.checksum;

import java.util.ArrayList;

public class Checksum {
    private Checksum() {}
    public static short calcularChecksum(String datos) {
        var bloques = new ArrayList<Short>();
        short bloque = 0;
        for (int indiceDatoActual = 0; indiceDatoActual < datos.length(); indiceDatoActual += 2) {
            char caracterActual = datos.charAt(indiceDatoActual);
            bloque += (short) (caracterActual << 8);
            if (indiceDatoActual + 1 < datos.length()) {
                caracterActual = datos.charAt(++indiceDatoActual);
                bloque += (short) caracterActual;
            }
            bloques.add(bloque);
            bloque = 0;
        }

        short suma = 0;
        for (short bloqueActual : bloques) {
            suma = sumaBits(bloqueActual, suma);
        }

        suma = (short) ~suma;

        return suma;
    }

    public static boolean verificarChecksum(String datos, short checksum) {
        var bloques = new ArrayList<Short>();
        short bloque = 0;
        for (int indiceDatoActual = 0; indiceDatoActual < datos.length(); indiceDatoActual += 2) {
            char caracterActual = datos.charAt(indiceDatoActual);
            bloque += (short) (caracterActual << 8);
            if (indiceDatoActual + 1 < datos.length()) {
                caracterActual = datos.charAt(++indiceDatoActual);
                bloque += (short) caracterActual;
            }
            bloques.add(bloque);
            bloque = 0;
        }

        short suma = 0;
        for (short bloqueActual : bloques) {
            suma = sumaBits(bloqueActual, suma);
        }

        suma = sumaBits(checksum, suma);
        suma = (short) ~suma;
        return suma == 0;
    }

    private static short sumaBits(short bloque, short suma) {
        byte acarreo = 0;
        short nuevaSuma = 0;
        for (byte bitActual = 0; bitActual < 16; bitActual++) {
            byte bitSuma = (byte) ((suma >> bitActual) & 1);
            byte bitBloque = (byte) ((bloque >> bitActual) & 1);
            byte bitResultado = (byte) (bitSuma + bitBloque + acarreo);
            acarreo = (byte) (bitResultado >> 1);
            bitResultado &= 1;
            nuevaSuma |= (short) (bitResultado << bitActual);
        }

        nuevaSuma += acarreo;

        return nuevaSuma;
    }
}
