package com.tuyensinh.admin.util;

import com.tuyensinh.util.Constants;
import java.util.HashSet;
import java.util.Set;

public class AdminSession {
    private static AdminSession instance;
    private String currentUsername;
    private String currentRole;
    private Set<String> allowedModules;

    private AdminSession() {
        allowedModules = new HashSet<>();
    }

    public static synchronized AdminSession getInstance() {
        if (instance == null) instance = new AdminSession();
        return instance;
    }

    // Tạm thời truyền danh sách rỗng, ở bước sau kết nối DB ta sẽ truyền Set quyền thật vào đây
    public void login(String username, String role, Set<String> permissions) {
        this.currentUsername = username;
        this.currentRole = role;
        this.allowedModules = permissions != null ? permissions : new HashSet<>();
    }

    public void logout() {
        currentUsername = null;
        currentRole = null;
        allowedModules.clear();
    }

    public boolean hasPermission(String moduleCode) {
        if (moduleCode == null) return true;
        
        if (Constants.NHOM_ADMIN.equals(currentRole)) return true;
        
        return allowedModules.contains(moduleCode);
    }

    public String getCurrentUsername() { return currentUsername != null ? currentUsername : "Khách"; }
    public String getCurrentRole() { return currentRole != null ? currentRole : "Chưa đăng nhập"; }
}