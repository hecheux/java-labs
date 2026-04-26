package com.tpprojects.servlets;

import com.tpprojects.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String error = request.getParameter("error");
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Вход в систему</title>");
        out.println("<link rel='stylesheet' type='text/css' href='" + request.getContextPath() + "/css/style.css'>");
        out.println("</head>");
        out.println("<body>");
        
        out.println("<div class='container'>");
        out.println("<h2>Вход в систему</h2>");
        
        if ("true".equals(error)) {
            out.println("<p class='error'>Неверный логин или пароль!</p>");
        }
        
        out.println("<form method='post' action='" + request.getContextPath() + "/login' class='login-form'>");
        out.println("<div class='form-group'>");
        out.println("<label for='username'>Логин:</label>");
        out.println("<input type='text' id='username' name='username' required>");
        out.println("</div>");
        
        out.println("<div class='form-group'>");
        out.println("<label for='password'>Пароль:</label>");
        out.println("<input type='password' id='password' name='password' required>");
        out.println("</div>");
        
        out.println("<button type='submit' class='submit-button'>Войти</button>");
        out.println("</form>");
        
        out.println("<a href='" + request.getContextPath() + "/' class='link-button'>Вернуться на главную</a>");
        
        out.println("</div>");
        
        out.println("</body>");
        out.println("</html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        UserService userService = 
            (UserService) getServletContext().getAttribute("userService");
        
        if (userService.authenticate(username, password)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("username", username);
            
            response.sendRedirect(request.getContextPath() + "/");
        } else {
            response.sendRedirect(request.getContextPath() + "/login?error=true");
        }
    }
}
