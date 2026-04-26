import java.io.*;
import java.net.*;
import java.util.Scanner;


public class ChatClient {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Используйте: java ChatClient <host> <port> <userName>");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String userName = args[2];

        try (Socket socket = new Socket(host, port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException ignored) {}
            });
            reader.setDaemon(true);
            reader.start();

            out.println(userName);

            Scanner sc = new Scanner(System.in, "UTF-8");
            while (true) {
                if (!sc.hasNextLine()) break;
                String line = sc.nextLine();
                out.println(line);
                if (line.equalsIgnoreCase("@quit")) break;
            }
        } catch (IOException e) {
            System.err.println("Соединение прервано: " + e.getMessage());
        }
    }
}
