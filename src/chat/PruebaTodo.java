package chat;

public class PruebaTodo {
    public static void main(String[] args) {
        Servidor.main(args);
        Thread cliente1 = new Thread(() -> {
            Chat.main(args);
        });
        cliente1.start();
    }
}
