package com.tpprojects.service;

import com.tpprojects.model.Announcement;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnouncementService {
    private final List<Announcement> announcements = new ArrayList<>();
    private final String dataFilePath;

    public AnnouncementService(String dataFilePath) {
        this.dataFilePath = dataFilePath;
        loadAnnouncements();
    }

    public synchronized void addAnnouncement(Announcement announcement) {
        announcements.add(announcement);
        saveAnnouncements();
    }

    public synchronized List<Announcement> getAllAnnouncements() {
        List<Announcement> copy = new ArrayList<>(announcements);
        Collections.reverse(copy);
        return copy;
    }

    private void loadAnnouncements() {
        File file = new File(dataFilePath);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                announcements.clear();
                @SuppressWarnings("unchecked")
                List<Announcement> loaded = (List<Announcement>) obj;
                announcements.addAll(loaded);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка загрузки объявлений: " + e.getMessage());
        }
    }

    private void saveAnnouncements() {
        File file = new File(dataFilePath);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(announcements);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения объявлений: " + e.getMessage());
        }
    }
}
