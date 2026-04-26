package com.tpprojects.service;

import com.tpprojects.model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserService {
    private Map<String, User> users = new HashMap<>();
    
    public void loadUsers(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String username = parts[0].trim();
                        String password = parts[1].trim();
                        users.put(username, new User(username, password));
                    }
                }
            }
        }
    }
    
    public boolean authenticate(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }
}
