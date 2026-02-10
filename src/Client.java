import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {

    public void connDoWork(int port, String[] commands, int start) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            var itter = 10000;
            if (commands[0].contains("QUIT"))
                itter = 1;

            for (int i = start; i < itter; i++) {
                var str = commands[i % commands.length];
                out.println(str);
                //Thread.sleep(100);
                System.out.println(in.readLine());
                Thread.sleep(100);
            }

        } catch (InterruptedException | IndexOutOfBoundsException e) {
            Thread.currentThread().interrupt();
            e.getStackTrace();
        }
    }

}
