import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class ClientConn {
    final Socket socket;
    final PrintWriter out;

    ClientConn(Socket socket, PrintWriter out) {
        this.socket = socket;
        this.out = out;
    }
}

public class Server implements Closeable {
    public static final int TIMEOUT = 30000;
    private volatile boolean running = true;
    private final List<Thread> threads = new ArrayList<>();
    private final List<ServerSocket> serverSockets = new ArrayList<>();
    private final List<Integer> ports;
    private final List<ClientConn> clients = new CopyOnWriteArrayList<>();


    Server(List<Integer> ports) throws IOException {
        assert ports != null;
        assert !ports.isEmpty();
        this.ports = ports;
        init();
    }

    private void init() throws IOException {
        for (var p : ports) {

            System.out.println("Listening on port: " + p);
            serverSockets.add(new ServerSocket(p));
            System.out.println("Address: " + serverSockets.getLast().getInetAddress());
        }
    }

    public void StartListening() {


        for (var s : serverSockets) {
            threads.add(new Thread(() -> {
                try (s) {
                    while (running) {
                        Socket socket = s.accept();
                        new Thread(() -> {
                            try {
                                handleClient(socket);
                            } catch (SocketException e) {
                                if (running) e.printStackTrace();
                            }
                        }).start();
                    }
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }

            ));
        }

        threads.forEach(Thread::start);
        running = true;
    }

    private void handleClient(Socket socket) throws SocketException {
        ClientConn conn = null;
        socket.setSoTimeout(TIMEOUT);
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            conn = new ClientConn(socket, out);
            clients.add(conn);

            System.out.println("Started new socket with: " + socket.getInetAddress());

            String m;
            while (((m = in.readLine()) != null)) {
                var params = m.split(" ", 2);
                if (params.length > 1) {
                    switch (params[0]) {
                        case "UPPER":
                            out.println(params[1].toUpperCase());
                            break;
                        case "LOWER":
                            out.println(params[1].toLowerCase());
                            break;
                        case "BROADCAST":
                            broadcast("BROADCAST FROM " + socket.getInetAddress() + " : " + params[1], socket.getLocalPort(), socket.getPort());
                            break;
                        default:
                            out.println("ERROR: Unknown command: " + m);
                            break;
                    }
                } else {
                    switch (params[0]) {
                        case "TIME":
                            out.println("Time " + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));
                            break;
                        case "QUIT":
                            System.out.println("Ended socket: " + socket.getInetAddress().getHostAddress());
                            out.println("Bye");
                            return;
                        default:
                            out.println("ECHO: " + m);
                    }

                }
            }

        } catch (SocketException e) {
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress()
                    + " (" + e.getMessage() + ")");
        } catch (SocketTimeoutException e) {
            System.out.println("Client timed out: " + socket.getRemoteSocketAddress());
        } catch (IOException e) {
            if (running) e.printStackTrace();
        } finally {
            if (conn != null) clients.remove(conn);
        }
    }

    private void broadcast(String message, int port, int clientport) {
        clients.stream()
                .filter(c -> c.socket.getLocalPort() == port && !c.socket.isClosed())
                .forEach(c -> {
                    if (c.socket.getPort() == clientport) {
                        return;
                    }

                    c.out.println(message);
                    if (c.out.checkError()) {
                        clients.remove(c);
                        try {
                            c.socket.close();
                        } catch (IOException ignored) {
                        }
                    }
                });
    }

    @Override
    public void close() throws IOException {
        running = false;

        for (var t : threads) {
            try {
                t.join(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                for (var p : serverSockets) {
                    if (!p.isClosed()) p.close();
                }
            }
        }

        for (var c : clients) {
            if (!c.socket.isClosed()) c.socket.close();
        }


    }

    public boolean isRunning() {
        return running;
    }
}
