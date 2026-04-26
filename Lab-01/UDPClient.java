import java.net.*;
import java.io.*;
import java.util.Scanner;

public class UDPClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Использование: java UDPClient <адрес_сервера> <порт>");
            return;
        }

        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String userName = "Client";
        Scanner scanner = new Scanner(System.in);

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(serverHost);
            System.out.println("Подключение к серверу " + serverHost + ":" + serverPort);
            System.out.println("Введите команды:");
            System.out.println("@name <имя> — задать имя");
            System.out.println("@quit — выйти");
            System.out.println("@cat <имя_файла> — показать текстовый файл (TCP)");
            System.out.println("Любой текст — отправить сообщение");


            Thread receiveThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (true) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("\n" + message);
                        System.out.print("> ");
                    } catch (IOException e) {
                        break;
                    }
                }
            });
            receiveThread.start();


            Thread tcpThread = new Thread(() -> {
                try (ServerSocket tcpServer = new ServerSocket(serverPort + 1)) {
                    System.out.println("TCP сервер клиента запущен на порту " + (serverPort + 1));
                    while (true) {
                        try (Socket tcpSocket = tcpServer.accept();
                             BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                             PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true)) {

                            String fileName = in.readLine();
                            System.out.println("Сервер запросил файл: " + fileName);

                            File file = new File(fileName);
                            if (!file.exists()) {
                                out.println("Файл не найден: " + fileName);
                                continue;
                            }

                            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                                String line;
                                while ((line = fileReader.readLine()) != null) {
                                    out.println(line);
                                }
                            }
                            out.println("<<EOF>>");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка TCP у клиента: " + e.getMessage());
                }
            });
            tcpThread.start();


            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();

                if (input.startsWith("@name ")) {
                    userName = input.substring(6).trim();
                    System.out.println("Имя установлено: " + userName);

                } else if (input.equals("@quit")) {
                    System.out.println("Выход...");
                    socket.close();
                    break;

                } else if (input.startsWith("@cat ")) {


                    String fileName = input.substring(5).trim();

                    try (Socket tcpSocket = new Socket(serverAddress, serverPort + 1);
                         PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()))) {

                        out.println(fileName);
                        System.out.println("=== Содержимое файла с сервера ===");
                        String line;
                        while ((line = in.readLine()) != null) {
                            if (line.equals("<<EOF>>")) break;
                            System.out.println(line);
                        }
                        System.out.println("=== Конец файла ===");
                    } catch (IOException e) {
                        System.out.println("Ошибка TCP-запроса: " + e.getMessage());
                    }

                } else {

                    String message = userName + ": " + input;
                    byte[] data = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
                    socket.send(packet);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
