package com.tpprojects.servlets;

import com.tpprojects.model.Announcement;
import com.tpprojects.service.AnnouncementService;
import com.tpprojects.service.UserService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class MainServlet extends HttpServlet {

    private UserService userService;
    private AnnouncementService announcementService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        userService = new UserService();

        ServletContext context = config.getServletContext();
        String usersFilePath = context.getInitParameter("usersFile");
        String realUsersPath = context.getRealPath(usersFilePath);

        try {
            userService.loadUsers(realUsersPath);
            System.out.println("Пользователи загружены из: " + realUsersPath);
        } catch (IOException e) {
            throw new ServletException("Ошибка загрузки пользователей", e);
        }

        // new
        String announcementsFileName = "/WEB-INF/announcements.ser";
        String realAnnouncementsPath = context.getRealPath(announcementsFileName);

        announcementService = new AnnouncementService(realAnnouncementsPath);

        context.setAttribute("userService", userService);
        context.setAttribute("announcementService", announcementService);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        String username = null;
        boolean isLoggedIn = false;

        if (session != null && session.getAttribute("username") != null) {
            username = (String) session.getAttribute("username");
            isLoggedIn = true;
        }

        AnnouncementService announcementService = 
            (AnnouncementService) getServletContext().getAttribute("announcementService");
        List<Announcement> announcements = announcementService.getAllAnnouncements();

        // new
        long timestamp = System.currentTimeMillis();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Доска объявлений</title>");
        // Добавляем параметр версии к CSS, чтобы браузер всегда загружал новую версию
        out.println("<link rel='stylesheet' type='text/css' href='" + request.getContextPath() + "/css/style.css?v=" + timestamp + "'>");
        out.println("</head>");
        out.println("<body>");

        out.println("<div class='container'>");
        out.println("<h1>Доска объявлений</h1>");

        out.println("<div class='control-panel'>");

        if (isLoggedIn) {
            out.println("<p>Вы вошли как: <strong>" + username + "</strong></p>");
            out.println("<a href='" + request.getContextPath() + "/add-announcement' class='link-button'>Добавить объявление</a>");
            out.println("<a href='" + request.getContextPath() + "/logout' class='link-button'>Выйти из системы</a>");
        } else {
            out.println("<a href='" + request.getContextPath() + "/login' class='link-button'>Войти в систему</a>");
        }

        out.println("</div>");

        out.println("<div class='announcements'>");

        if (announcements.isEmpty()) {
            out.println("<p class='no-announcements'>Объявлений пока нет</p>");
        } else {
            for (Announcement announcement : announcements) {
                out.println("<div class='announcement'>");
                out.println("<h2 class='announcement-title'>" + escapeHtml(announcement.getTitle()) + "</h2>");
                out.println("<p class='announcement-text'>" + escapeHtml(announcement.getText()) + "</p>");
                out.println("<div class='announcement-info'>");
                out.println("<span>Автор: " + escapeHtml(announcement.getUsername()) + "</span>");
                out.println("<span>Дата: " + announcement.getFormattedDateTime() + "</span>");
                out.println("</div>");
                out.println("</div>");
            }
        }

        out.println("</div>");
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
}
