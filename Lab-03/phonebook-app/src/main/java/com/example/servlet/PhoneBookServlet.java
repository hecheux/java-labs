package com.example.servlet;

import com.example.model.PhoneBook;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet(name = "PhoneBookServlet", urlPatterns = {"/phonebook"}, loadOnStartup = 1)
public class PhoneBookServlet extends HttpServlet {

    private Map<String, PhoneBook> phoneBookMap;
    private String dataFilePath;

    private static final String[] AVAILABLE_AVATARS = {
            "avatar1.jpg",
            "avatar2.jpg",
            "avatar3.jpg",
            "avatar4.jpg",
            "avatar5.jpg",
            "default.jpg"
    };

    @Override
    public void init() throws ServletException {
        super.init();
        phoneBookMap = new ConcurrentHashMap<>();
        dataFilePath = getServletContext().getRealPath("/WEB-INF/phonebook.txt");
        loadDataFromFile();
        log("PhoneBookServlet initialized. Data loaded from: " + dataFilePath);
    }

    private void loadDataFromFile() {
        File file = new File(dataFilePath);

        if (!file.exists()) {
            log("Data file not found, starting with empty phonebook");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            PhoneBook currentEntry = null;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.equals("---")) {
                    if (currentEntry != null && currentEntry.getName() != null) {
                        phoneBookMap.put(currentEntry.getName(), currentEntry);
                    }
                    currentEntry = null;
                    lineNumber = 0;
                } else if (!line.isEmpty()) {
                    if (lineNumber == 0) {
                        currentEntry = new PhoneBook(line);
                        lineNumber++;
                    } else if (lineNumber == 1) {
                        if (currentEntry != null) {
                            currentEntry.setAvatar(line);
                        }
                        lineNumber++;
                    } else {
                        if (currentEntry != null) {
                            currentEntry.addPhone(line);
                        }
                    }
                }
            }

            if (currentEntry != null && currentEntry.getName() != null) {
                phoneBookMap.put(currentEntry.getName(), currentEntry);
            }

            log("Loaded " + phoneBookMap.size() + " entries from file");

        } catch (IOException e) {
            log("Error loading data from file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void saveDataToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(dataFilePath))) {
            for (PhoneBook entry : phoneBookMap.values()) {
                writer.println(entry.getName());
                writer.println(entry.getAvatar());
                for (String phone : entry.getPhones()) {
                    writer.println(phone);
                }
                writer.println("---");
            }
            log("Data saved to file");
        } catch (IOException e) {
            log("Error saving data to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        if ("addUser".equals(action)) {
            showAddUserForm(response);
        } else if ("addPhone".equals(action)) {
            showAddPhoneForm(response);
        } else {
            showMainPage(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String action = request.getParameter("action");

        if ("addUser".equals(action)) {
            addNewUser(request, response);
        } else if ("addPhone".equals(action)) {
            addPhoneToUser(request, response);
        } else {
            response.sendRedirect("phonebook");
        }
    }

    private void showMainPage(HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Телефонная книга</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 800px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #333; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }");
        out.println(".links { margin: 20px 0; }");
        out.println(".links a { display: inline-block; margin-right: 15px; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px; }");
        out.println(".links a:hover { background-color: #45a049; }");
        out.println(".entry { border: 1px solid #ddd; margin: 10px 0; padding: 15px; border-radius: 4px; background-color: #fafafa; display: flex; align-items: flex-start; }");
        out.println(".avatar { width: 80px; height: 80px; border-radius: 50%; margin-right: 20px; object-fit: cover; border: 2px solid #2196F3; }");
        out.println(".info { flex: 1; }");
        out.println(".name { font-weight: bold; font-size: 18px; color: #2196F3; margin-bottom: 8px; }");
        out.println(".phone { margin-left: 20px; color: #666; padding: 3px 0; }");
        out.println(".no-phones { margin-left: 20px; color: #999; font-style: italic; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1> Телефонная книга</h1>");

        out.println("<div class='links'>");
        out.println("<a href='phonebook?action=addUser'>Добавить пользователя</a>");
        out.println("<a href='phonebook?action=addPhone'>Добавить телефон</a>");
        out.println("</div>");

        out.println("<h2>Список записей:</h2>");

        if (phoneBookMap.isEmpty()) {
            out.println("<p>Записная книжка пуста</p>");
        } else {
            synchronized (phoneBookMap) {
                for (PhoneBook entry : phoneBookMap.values()) {
                    out.println("<div class='entry'>");

                    out.println("<img src='avatars/" + escapeHtml(entry.getAvatar()) + "' alt='Avatar' class='avatar' />");

                    out.println("<div class='info'>");
                    out.println("<div class='name'>" + escapeHtml(entry.getName()) + "</div>");

                    if (entry.getPhones().isEmpty()) {
                        out.println("<div class='no-phones'>Нет телефонов</div>");
                    } else {
                        for (String phone : entry.getPhones()) {
                            out.println("<div class='phone'> " + escapeHtml(phone) + "</div>");
                        }
                    }
                    out.println("</div>");

                    out.println("</div>");
                }
            }
        }

        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    private void showAddUserForm(HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Добавить пользователя</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #333; }");
        out.println("form { margin-top: 20px; }");
        out.println("label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; }");
        out.println("input[type='text'] { width: 100%; padding: 10px; margin-bottom: 15px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }");
        out.println(".avatar-selection { margin-bottom: 20px; }");
        out.println(".avatar-option { display: inline-block; margin: 10px; text-align: center; cursor: pointer; }");
        out.println(".avatar-option input[type='radio'] { display: none; }");
        out.println(".avatar-option img { width: 80px; height: 80px; border-radius: 50%; border: 3px solid #ddd; transition: border-color 0.3s; }");
        out.println(".avatar-option input[type='radio']:checked + img { border-color: #4CAF50; box-shadow: 0 0 10px rgba(76, 175, 80, 0.5); }");
        out.println(".avatar-option:hover img { border-color: #2196F3; }");
        out.println("input[type='submit'] { background-color: #4CAF50; color: white; padding: 12px 30px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }");
        out.println("input[type='submit']:hover { background-color: #45a049; }");
        out.println("a { display: inline-block; margin-top: 15px; color: #2196F3; text-decoration: none; }");
        out.println("a:hover { text-decoration: underline; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>Добавить нового пользователя</h1>");
        out.println("<form method='post' action='phonebook'>");
        out.println("<input type='hidden' name='action' value='addUser' />");

        out.println("<label for='username'>Имя пользователя:</label>");
        out.println("<input type='text' id='username' name='username' required />");

        out.println("<label>Выберите аватарку:</label>");
        out.println("<div class='avatar-selection'>");

        for (int i = 0; i < AVAILABLE_AVATARS.length; i++) {
            String avatar = AVAILABLE_AVATARS[i];
            boolean isDefault = avatar.equals("default.jpg");

            out.println("<label class='avatar-option'>");
            out.println("<input type='radio' name='avatar' value='" + avatar + "' " +
                    (isDefault ? "checked" : "") + " />");
            out.println("<img src='avatars/" + avatar + "' alt='" + avatar + "' />");
            out.println("</label>");
        }

        out.println("</div>");

        out.println("<input type='submit' value='Добавить' />");
        out.println("</form>");
        out.println("<a href='phonebook'>← Вернуться к списку</a>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    private void showAddPhoneForm(HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Добавить телефон</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 500px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        out.println("h1 { color: #333; }");
        out.println("form { margin-top: 20px; }");
        out.println("label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; }");
        out.println("input[type='text'], select { width: 100%; padding: 10px; margin-bottom: 15px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }");
        out.println("input[type='submit'] { background-color: #4CAF50; color: white; padding: 12px 30px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }");
        out.println("input[type='submit']:hover { background-color: #45a049; }");
        out.println("a { display: inline-block; margin-top: 15px; color: #2196F3; text-decoration: none; }");
        out.println("a:hover { text-decoration: underline; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>Добавить телефон</h1>");
        out.println("<form method='post' action='phonebook'>");
        out.println("<input type='hidden' name='action' value='addPhone' />");

        out.println("<label for='username'>Выберите пользователя:</label>");
        out.println("<select id='username' name='username' required>");
        out.println("<option value=''>-- Выберите --</option>");

        synchronized (phoneBookMap) {
            for (String name : phoneBookMap.keySet()) {
                out.println("<option value='" + escapeHtml(name) + "'>" + escapeHtml(name) + "</option>");
            }
        }

        out.println("</select>");

        out.println("<label for='phone'>Телефон:</label>");
        out.println("<input type='text' id='phone' name='phone' required />");
        out.println("<input type='submit' value='Добавить телефон' />");
        out.println("</form>");
        out.println("<a href='phonebook'>← Вернуться к списку</a>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    private void addNewUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String username = request.getParameter("username");
        String avatar = request.getParameter("avatar");

        if (username != null && !username.trim().isEmpty()) {
            username = username.trim();

            if (phoneBookMap.containsKey(username)) {
                showMessage(response, "Ошибка", "Пользователь с именем '" + username + "' уже существует!");
            } else {
                if (avatar == null || avatar.trim().isEmpty()) {
                    avatar = "default.jpg";
                }

                PhoneBook newEntry = new PhoneBook(username, avatar);
                phoneBookMap.put(username, newEntry);
                saveDataToFile();
                showMessage(response, "Успех", "Пользователь '" + username + "' успешно добавлен!");
            }
        } else {
            showMessage(response, "Ошибка", "Имя пользователя не может быть пустым!");
        }
    }

    private void addPhoneToUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String username = request.getParameter("username");
        String phone = request.getParameter("phone");

        if (username != null && !username.trim().isEmpty() &&
                phone != null && !phone.trim().isEmpty()) {

            username = username.trim();
            phone = phone.trim();

            PhoneBook entry = phoneBookMap.get(username);

            if (entry != null) {
                synchronized (entry) {
                    entry.addPhone(phone);
                }
                saveDataToFile();
                showMessage(response, "Успех", "Телефон успешно добавлен пользователю '" + username + "'!");
            } else {
                showMessage(response, "Ошибка", "Пользователь '" + username + "' не найден!");
            }
        } else {
            showMessage(response, "Ошибка", "Все поля должны быть заполнены!");
        }
    }

    private void showMessage(HttpServletResponse response, String title, String message)
            throws IOException {

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>" + title + "</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        out.println(".container { max-width: 500px; margin: 50px auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); text-align: center; }");
        out.println("h1 { color: #333; }");
        out.println("p { font-size: 16px; color: #666; margin: 20px 0; }");
        out.println("a { display: inline-block; margin-top: 20px; padding: 10px 20px; background-color: #2196F3; color: white; text-decoration: none; border-radius: 4px; }");
        out.println("a:hover { background-color: #0b7dda; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>" + title + "</h1>");
        out.println("<p>" + escapeHtml(message) + "</p>");
        out.println("<a href='phonebook'>Вернуться к главной странице</a>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    @Override
    public void destroy() {
        saveDataToFile();
        log("PhoneBookServlet destroyed. Data saved.");
        super.destroy();
    }
}
