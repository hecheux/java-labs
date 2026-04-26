package com.tplabs.webservletv3;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@WebServlet("/hierlist")
public class HierarchicalListServlet extends HttpServlet {
    private static final String FILE_PATH = "/Users/plushavey/Desktop/TP_Projects/WebServletV3/my-webapp/list.txt";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LinkedHashMap<String, List<String>> itemMap = readListFile(FILE_PATH);

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"ru\">");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<title>Двухуровневый список</title>");
        out.println("<link rel=\"stylesheet\" href=\"/WebServletV3/static/style.css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Двухуровневый список</h2>");
        
        out.println("<button id=\"addViewBtn\">Add view</button>");
        out.println("<hr>");
        
        out.println("<div id=\"viewsContainer\">");
        
        // Первое представление
        out.println("<div class=\"view\" data-view-id=\"0\">");
        out.println("<ol id=\"mainList-0\">");

        int idx = 0;
        for (Map.Entry<String, List<String>> entry : itemMap.entrySet()) {
            String group = entry.getKey();
            List<String> items = entry.getValue();
            out.printf("<li><span class=\"toggle\" data-idx=\"%d\" data-view=\"0\">[+]</span>%s", idx, group);
            out.printf("<ul class=\"sublist\" id=\"sublist-0-%d\">", idx);
            for (String item : items) {
                out.printf("<li class=\"subitem\">%s</li>", item);
            }
            out.println("</ul></li>");
            idx++;
        }
        out.println("</ol>");
        out.println("</div>");
        
        out.println("</div>");
        
        // Передаём данные в JavaScript как массив объектов
        out.println("<script>");
        out.println("window.listDataArray = [");
        boolean firstEntry = true;
        for (Map.Entry<String, List<String>> entry : itemMap.entrySet()) {
            if (!firstEntry) out.println(",");
            out.println("  {");
            out.print("    \"name\": \"");
            out.print(entry.getKey().replace("\\", "\\\\").replace("\"", "\\\""));
            out.println("\",");
            out.print("    \"items\": [");
            boolean firstItem = true;
            for (String item : entry.getValue()) {
                if (!firstItem) out.print(", ");
                out.print("\"");
                out.print(item.replace("\\", "\\\\").replace("\"", "\\\""));
                out.print("\"");
                firstItem = false;
            }
            out.println("]");
            out.println("  }");
            firstEntry = false;
        }
        out.println("];");
        out.println("console.log('listDataArray загружен:', window.listDataArray);");
        out.println("</script>");
        
        out.println("<script src=\"/WebServletV3/static/scripts.js\"></script>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    private LinkedHashMap<String, List<String>> readListFile(String filepath) throws IOException {
        LinkedHashMap<String, List<String>> map = new LinkedHashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(filepath));
        String currentParent = null;
        for (String line : lines) {
            if (line.startsWith("    * ")) {
                if (currentParent != null)
                    map.get(currentParent).add(line.substring(6).trim());
            } else if (line.startsWith("* ")) {
                currentParent = line.substring(2).trim();
                map.put(currentParent, new ArrayList<>());
            }
        }
        return map;
    }
}
