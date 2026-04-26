package com.tpprojects.servlets;

import com.tpprojects.model.Announcement;
import com.tpprojects.service.AnnouncementService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class AddAnnouncementServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String username = (String) session.getAttribute("username");
        
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Добавить объявление</title>");
        out.println("<link rel='stylesheet' type='text/css' href='" + request.getContextPath() + "/css/style.css'>");
        out.println("</head>");
        out.println("<body>");
        
        out.println("<div class='container'>");
        out.println("<h2>Добавить объявление</h2>");
        
        out.println("<form method='post' action='" + request.getContextPath() + "/add-announcement' class='announcement-form'>");
        
        out.println("<div class='form-group'>");
        out.println("<label for='title'>Заголовок:</label>");
        out.println("<input type='text' id='title' name='title' required>");
        out.println("</div>");
        
        out.println("<div class='form-group'>");
        out.println("<label for='text'>Текст объявления:</label>");
        out.println("<textarea id='text' name='text' rows='6' required></textarea>");
        out.println("</div>");
        
        out.println("<button type='submit' class='submit-button'>Добавить</button>");
        out.println("</form>");
        
        out.println("<a href='" + request.getContextPath() + "/' class='link-button'>Вернуться на главную</a>");
        
        out.println("</div>");
        
        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        request.setCharacterEncoding("UTF-8");
        
        String username = (String) session.getAttribute("username");
        String title = request.getParameter("title");
        String text = request.getParameter("text");
        
        Announcement announcement = new Announcement(title, text, username);
        
        AnnouncementService announcementService = 
            (AnnouncementService) getServletContext().getAttribute("announcementService");
        announcementService.addAnnouncement(announcement);
        
        response.sendRedirect(request.getContextPath() + "/");
    }
}
