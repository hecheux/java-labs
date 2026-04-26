import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private final int port;
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final java.util.Timer timer = new java.util.Timer(true);
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.List<String>> undelivered = new java.util.concurrent.ConcurrentHashMap<>();


    public ChatServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        int port = 5000;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        new ChatServer(port).start();
    }

    public void start() {
        System.out.println("Сервер запущен на порту: " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    public boolean registerClient(String name, ClientHandler handler) {
        if (name == null || name.isBlank() || clients.containsKey(name)) {
            return false;
        }
        //
        clients.put(name, handler);
        java.util.List<String> pending = undelivered.remove(name);
        if (pending != null) {
            ClientHandler h = clients.get(name);
            for (String msg : pending) if (h != null) h.sendLine(msg);
        }

        broadcast("[Server] " + name + " зашел в чат ", name);
        return true;
    }

    public void unregisterClient(String name) {
        if (name != null) {
            clients.remove(name);
            broadcast("[Server] " + name + " вышел из чата ", name);
        }
    }

    public void broadcast(String message, String senderName) {
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            String user = entry.getKey();
            ClientHandler ch = entry.getValue();
            if (senderName == null || !user.equals(senderName)) {
                ch.sendLine(message);
            }
        }
    }

    public boolean sendPrivate(String toUser, String fromUser, String message) {
        ClientHandler target = clients.get(toUser);
        if (target == null) return false;
        target.sendLine("[PM для " + fromUser + "] " + message);
        return true;
    }

    public List<String> listUsers() {
        return new ArrayList<>(clients.keySet());
    }

    //new
    static Long parseRel(String s) {
        try {
            if (!s.startsWith("+")) return null;
            long factor = 1000L;
            if (s.endsWith("s")) { factor = 1000L; s = s.substring(1, s.length()-1); }
            else if (s.endsWith("m")) { factor = 60_000L; s = s.substring(1, s.length()-1); }
            else if (s.endsWith("h")) { factor = 3_600_000L; s = s.substring(1, s.length()-1); }
            else { s = s.substring(1); }
            long n = Long.parseLong(s.trim());
            return Math.max(1, n * factor);
        } catch (Exception e) { return null; }
    }


    static class ClientHandler extends Thread {
        private final Socket socket;
        private final ChatServer server;
        private BufferedReader in;
        private PrintWriter out;
        private String userName;

        ClientHandler(Socket socket, ChatServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try (socket) {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                out.println("Введите имя пользователя: ");
                String nameAttempt = in.readLine();
                while (nameAttempt == null || !server.registerClient(nameAttempt, this)) {
                    out.println("Имя занято или неправильное, выберите другое: ");
                    nameAttempt = in.readLine();
                }
                userName = nameAttempt;
                out.println("Добро пожаловать, " + userName + "! \n Commands: @senduser <User> <Message>, @users, @help, @quit");

                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    if (line.equalsIgnoreCase("@quit")) {
                        out.println("Bye!");
                        break;



                    } else if (line.startsWith("@alarm ")) {
                        String spec = line.substring(7).trim();
                        Long delay = parseRel(spec);
                        if (delay == null) {
                            out.println("Usage: @alarm +30s | +5m | +1h");
                        } else {
                            server.timer.schedule(new java.util.TimerTask() {
                                @Override public void run() {
                                    ClientHandler ch = server.clients.get(userName);
                                    String msg = "Wake up!";
                                    if (ch != null) ch.sendLine(msg);
                                    else server.undelivered.computeIfAbsent(userName, k -> java.util.Collections.synchronizedList(new java.util.ArrayList<>())).add(msg);
                                }
                            }, delay);
                            out.println("Alarm set in " + delay/1000 + "s");
                        }



                    } else if (line.equalsIgnoreCase("@help")) {
                        out.println("Commands:");
                        out.println("  @senduser <User> <Message>  - send private message");
                        out.println("  @users                      - list online users");
                        out.println("  @quit                       - disconnect");
                    } else if (line.equalsIgnoreCase("@users")) {
                        out.println("Online: " + String.join(", ", server.listUsers()));
                    } else if (line.startsWith("@senduser ")) {
                        String rest = line.substring("@senduser ".length()).trim();
                        int sp = rest.indexOf(' ');
                        if (sp <= 0) {
                            out.println("Usage: @senduser <User> <Message>");
                        } else {
                            String toUser = rest.substring(0, sp);
                            String msg = rest.substring(sp + 1).trim();
                            boolean ok = server.sendPrivate(toUser, userName, msg);
                            if (!ok) {
                                out.println("User '" + toUser + "' not found");
                            } else {
                                out.println("[PM to " + toUser + "] " + msg);
                            }
                        }
                    } else {
                        String formatted = "[" + userName + "] " + line;
                        server.broadcast(formatted, userName);
                    }
                }
            } catch (IOException e) {
            } finally {
                server.unregisterClient(userName);
            }
        }

        void sendLine(String s) {
            if (out != null) out.println(s);
        }
    }
}
