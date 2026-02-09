import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    void main() throws IOException {
        System.out.println("Server started.");
        Server s = new Server(List.of(8081, 8082, 8083, 8084, 9090));
        try (s) {

            AtomicBoolean mainRunning = new AtomicBoolean(true);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nCtrl+C detected. Stopping...");
                mainRunning.set(false);
            }));

            s.StartListening();

//            Thread.sleep(5000);
            createClients();

            while (s.isRunning() && mainRunning.get()) {Thread.sleep(5000);}

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
            try {
                int i = 0;
                while (i != 100) {
                    var str = i % 2 == 0 ? "UPPER Hello new world!" : "BROADCAST Hello world!";
                    var c = new Client();
                    c.connDoWork(8081 + i % 2, str);
                    Thread.sleep(1000);
                    i++;
                }
            } catch (InterruptedException | IOException e) {
                e.getStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                int i = 0;
                while (i != 100) {
                    var str = i % 2 == 0 ? "UPPER Hello new world!" : "BROADCAST Hello world!";
                    var c = new Client();
                    c.connDoWork(8081 + i % 2, str);
                    Thread.sleep(1000);
                    i++;
                }
            } catch (InterruptedException | IOException e) {
                e.getStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                int i = 0;
                while (i != 100) {
                    var str = i % 2 == 0 ? "UPPER Hello new world!" : "BROADCAST Hello world!";
                    var c = new Client();
                    c.connDoWork(8082 + i % 2, str);
                    Thread.sleep(1000);
                    i++;
                }
            } catch (InterruptedException | IOException e) {
                e.getStackTrace();
            }
        }).start();
    }
}
