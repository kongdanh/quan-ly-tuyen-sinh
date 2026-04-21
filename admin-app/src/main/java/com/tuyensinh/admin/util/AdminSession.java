package com.tuyensinh.admin.util;

import com.tuyensinh.model.QuyenChucNang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminSession {
    private static AdminSession instance;
    private String currentUsername;
    private String currentRole;
    
    // Lưu trữ toàn bộ object quyền để check cả Xem/Thêm/Sửa/Xóa
    private final Map<String, QuyenChucNang> permissionsMap;

    private AdminSession() {
        permissionsMap = new HashMap<>();
    }

    public static synchronized AdminSession getInstance() {
        if (instance == null) instance = new AdminSession();
        return instance;
    }

    public void login(String username, String role, List<QuyenChucNang> permissions) {
        this.currentUsername = username;
        this.currentRole = role;
        this.permissionsMap.clear();
        
        if (permissions != null) {
            for (QuyenChucNang q : permissions) {
                permissionsMap.put(q.getMaChucNang(), q);
            }
        }
    }

    public void logout() {
        currentUsername = null;
        currentRole = null;
        permissionsMap.clear();
    }

    public boolean hasPermission(String moduleCode) {
        if (moduleCode == null) return true;
        
        if ("ADMIN".equals(currentRole)) return true; 
        
        return canView(moduleCode);
    }

    // --- CÁC HÀM KIỂM TRA QUYỀN CHI TIẾT DÀNH CHO BASE TABLE PANEL ---

    public boolean canView(String module) {
        if ("ADMIN".equals(currentRole)) return true;
        return permissionsMap.containsKey(module) && permissionsMap.get(module).getCoXem();
    }

    public boolean canAdd(String module) {
        if ("ADMIN".equals(currentRole)) return true;
        return permissionsMap.containsKey(module) && permissionsMap.get(module).getCoThem();
    }

    public boolean canEdit(String module) {
        if ("ADMIN".equals(currentRole)) return true;
        return permissionsMap.containsKey(module) && permissionsMap.get(module).getCoSua();
    }

    public boolean canDelete(String module) {
        if ("ADMIN".equals(currentRole)) return true;
        return permissionsMap.containsKey(module) && permissionsMap.get(module).getCoXoa();
    }

    public String getCurrentUsername() { return currentUsername != null ? currentUsername : "Khách"; }
    public String getCurrentRole() { return currentRole != null ? currentRole : "Chưa đăng nhập"; }
}