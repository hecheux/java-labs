package com.example.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PhoneBook implements Serializable {
    private String name;
    private List<String> phones;
    private String avatar;

    public PhoneBook() {
        this.phones = new ArrayList<>();
        this.avatar = "default.jpg";
    }

    public PhoneBook(String name) {
        this.name = name;
        this.phones = new ArrayList<>();
        this.avatar = "default.jpg";
    }

    public PhoneBook(String name, String avatar) {
        this.name = name;
        this.phones = new ArrayList<>();
        this.avatar = avatar != null ? avatar : "default.jpg";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones(List<String> phones) {
        this.phones = phones;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void addPhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            this.phones.add(phone);
        }
    }
}
