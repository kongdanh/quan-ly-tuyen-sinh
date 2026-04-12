package com.tuyensinh.admin.util;

public class AdminSession {
    private static AdminSession instance;
    private String currentUsername;
    private String currentRole;

    private AdminSession() {}

    public static AdminSession getInstance() {
        if (instance == null) {
            instance = new AdminSession();
        }
        return instance;
    }

    public void login(String username, String role) {
        this.currentUsername = username;
        this.currentRole = role;
    }

    public void logout() {
        this.currentUsername = null;
        this.currentRole = null;
    }

    public String getCurrentUsername() {
        return currentUsername != null ? currentUsername : "Guest";
    }

    public String getCurrentRole() {
        return currentRole != null ? currentRole : "Unknown";
    }
}