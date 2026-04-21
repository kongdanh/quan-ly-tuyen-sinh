package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.UserFormDialog;
import com.tuyensinh.model.User;
import com.tuyensinh.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UserPanel extends BaseTablePanel<User> {

    private final UserService userService = new UserService();

    public UserPanel() {
        super("Quản lý Người Dùng", "Danh sách tài khoản Admin, Giảng viên, Giáo vụ");
        loadTableData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Tên Đăng Nhập", "Họ Tên", "Bộ Phận", "Nhóm Quyền", "Trạng Thái", "Thao tác"};
    }

    @Override
    protected Object[] toTableRow(User u) {
        return new Object[]{
            u.getId(),
            u.getUsername(),
            u.getHoTen(),
            u.getBoPhan() != null ? u.getBoPhan() : "",
            u.getNhomQuyen() != null ? u.getNhomQuyen().getTenNhom() : "Chưa có",
            u.getTrangThai(),
            "" // Cột thao tác (Action Cell) do BaseTablePanel tự render
        };
    }

    @Override
    protected CompletableFuture<List<User>> fetchPage(String keyword, Map<String, Object> filters, int page, int pageSize) {
        return userService.findPageWithFilters(keyword, List.of("username", "hoTen", "boPhan"), filters, page, pageSize);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
        return userService.countWithFiltersAsync(keyword, List.of("username", "hoTen", "boPhan"), filters);
    }

    @Override
    protected void showAddDialog() {
        UserFormDialog dialog = new UserFormDialog(getParentFrame(), new User(), true);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadTableData();
    }

    @Override
    protected void showEditDialog(int tableRow) {
        int id = getIdFromRow(tableRow);
        User user = userService.findById(id); 
        if (user != null) {
            UserFormDialog dialog = new UserFormDialog(getParentFrame(), user, false);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadTableData();
        }
    }

    @Override
    protected void deleteRecord(int tableRow) {
        int id = getIdFromRow(tableRow);
        String name = getNameFromRow(tableRow);
        if (confirmDelete(name)) {
            userService.deleteByIdAsync(id).thenAccept(success -> {
                if (success) {
                    showSuccess("Đã xóa nhân viên!");
                    loadTableData();
                } else {
                    showError("Không thể xóa user này vì đang liên kết với dữ liệu khác.");
                }
            });
        }
    }

    @Override
    protected String getModuleCode() {
        return com.tuyensinh.util.Constants.QUYEN_PHAN_QUYEN; 
    }

}