import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    static final List<Integer> PORTS = List.of(8081, 8082, 8083, 8084, 9090);
    static final String[] listOfCommands = new String[]{"TIME", "LOWER HELLO WORLD LOWER", "UPPER Hello world upper!", "BROADCAST Hello world!"};
    static final Random rand= new Random();


    void main() throws IOException {
        System.out.println("Server started.");
        Server s = new Server(PORTS);
        try (s) {

            AtomicBoolean mainRunning = new AtomicBoolean(true);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nCtrl+C detected. Stopping...");
                mainRunning.set(false);
            }));

            s.StartListening();
            createClients();

            while (s.isRunning() && mainRunning.get()) {
                Thread.sleep(5000);
            }

            System.out.println("Server stopped.");
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            s.close();
        }
        var a = 1;
    }

    private static void createClients() {

        new Thread(() -> {
            var c = new Client();
            var c1 = new Client();
            try {
                int i = 0;
                while (true) {
                    c.connDoWork(9090, listOfCommands, rand.nextInt(100));
                    Thread.sleep(1000);
                    c1.connDoWork(8082, listOfCommands,rand.nextInt(102));
                    i++;
                }
            } catch (InterruptedException | IOException | IndexOutOfBoundsException e) {
                e.getStackTrace();
            } finally {
                try {
                    c.connDoWork(9090, new String[]{"QUIT"},0);
                    c1.connDoWork(8082,  new String[]{"QUIT"},0);
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            var c = new Client();
            var c1 = new Client();
            try {
                int i = 0;
                while (true) {
                    c.connDoWork(9090, listOfCommands,rand.nextInt(103));
                    Thread.sleep(1000);
                    c1.connDoWork(8081, listOfCommands,rand.nextInt(100));
                    i++;
                }
            } catch (InterruptedException | IOException | IndexOutOfBoundsException e) {
                e.getStackTrace();
            } finally {
                try {
                    c.connDoWork(9090,  new String[]{"QUIT"},0);
                    c1.connDoWork(8081,  new String[]{"QUIT"}, 0);
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            var c = new Client();
            var c1 = new Client();
            try {
                int i = 0;
                while (true) {
                    c.connDoWork(9090, listOfCommands,rand.nextInt(102));
                    Thread.sleep(1000);
                    c1.connDoWork(8083, listOfCommands,rand.nextInt(103));
                    i++;
                }
            } catch (InterruptedException | IOException e) {
                e.getStackTrace();
            } finally {
                try {
                    c.connDoWork(9090,  new String[]{"QUIT"},0);
                    c1.connDoWork(8083,  new String[]{"QUIT"},0);
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        }).start();
    }
}
