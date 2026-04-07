package com.tuyensinh.service;

/**
 * AuthService — xác thực đăng nhập
 *
 * DEMO: dùng tài khoản cứng để chạy thử khi chưa có bảng users trong DB
 * TODO: sau khi có bảng users → thay doLogin() bằng truy vấn DB thật
 */
public class AuthService {

    private static AuthService instance;
    private String currentUsername;
    private String currentRole; // ADMIN / USER

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    public boolean login(String username, String password) {
        // ── DEMO accounts ──────────────────────────────────────
        // Tài khoản admin: admin / admin123
        // Tài khoản user:  user  / user123
        // TODO: xóa phần này, thay bằng query bảng users
        // ────────────────────────────────────────────────────────
        if ("admin".equals(username) && "admin123".equals(password)) {
            currentUsername = username;
            currentRole = "ADMIN";
            return true;
        }
        if ("user".equals(username) && "user123".equals(password)) {
            currentUsername = username;
            currentRole = "USER";
            return true;
        }
        return false;
    }

    public void logout() {
        currentUsername = null;
        currentRole = null;
    }

    public boolean isLoggedIn()          { return currentUsername != null; }
    public boolean isAdmin()             { return "ADMIN".equals(currentRole); }
    public String  getCurrentUsername()  { return currentUsername; }
    public String  getCurrentRole()      { return currentRole; }
}