import java.net.*;
import java.io.*;
import java.util.Scanner;

public class UDPServer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Использование: java UDPServer <порт>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String userName = "Server";
        Scanner scanner = new Scanner(System.in);

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
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

            InetAddress clientAddress = null;
            int clientPort = -1;

            Thread tcpThread = new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(port + 1)) {
                    System.out.println("TCP сервер для @cat запущен на порту " + (port + 1));
                    while (true) {
                        try (Socket clientSocket = serverSocket.accept();
                             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                            String fileName = in.readLine();
                            System.out.println("TCP-запрос на файл: " + fileName);

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
                            out.println("<<EOF>>"); // сигнал конца файла
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка TCP: " + e.getMessage());
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
  

                    if (clientAddress == null) {
                        System.out.println("Нет подключенного клиента для TCP-запроса.");
                        continue;
                    }
                    String fileName = input.substring(5).trim();

                    try (Socket tcpSocket = new Socket(clientAddress, port + 1);
                         PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()))) {

                        out.println(fileName);
                        System.out.println("=== Содержимое файла с клиента ===");
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


                    if (clientAddress == null) {
                        System.out.println("Ожидание первого сообщения от клиента...");
                        continue;
                    }
                    String message = userName + ": " + input;
                    byte[] data = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
                    socket.send(packet);
                }


                if (clientAddress == null) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    clientAddress = packet.getAddress();
                    clientPort = packet.getPort();
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Клиент подключился: " + clientAddress + ":" + clientPort);
                    System.out.println("Сообщение: " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
