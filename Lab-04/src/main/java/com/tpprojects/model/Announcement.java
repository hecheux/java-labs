package com.tpprojects.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Announcement implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String text;
    private String username;
    private LocalDateTime dateTime;

    public Announcement(String title, String text, String username) {
        this.title = title;
        this.text = text;
        this.username = username;
        this.dateTime = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getUsername() {
        return username;
    }

    public String getFormattedDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }
}
