package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.util.Constants;
import com.tuyensinh.dao.NguyenVongDAO;
import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.service.NguyenVongService;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.tuyensinh.admin.ui.dialog.NguyenVongDialog;

public class NguyenVongPanel extends BaseTablePanel<NguyenVong> {

    private final NguyenVongDAO nvDAO = new NguyenVongDAO();
    private final NguyenVongService nvService = new NguyenVongService();

    public NguyenVongPanel() {
        super("Quản lý Nguyện vọng", "Xem danh sách và đăng ký nguyện vọng thí sinh");
        loadTableData();
    }

    @Override
    protected String getModuleCode() {
        return Constants.QUYEN_NGUYEN_VONG; 
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{
            "ID", "CCCD", "Họ Tên", "Ngành", "Tổ Hợp", "Thứ Tự", "Phương Thức", "Trạng Thái", "Thao Tác"
        };
    }

    @Override
    protected Object[] toTableRow(NguyenVong entity) {
        return new Object[]{
            entity.getId(),
            entity.getThiSinh() != null ? entity.getThiSinh().getCccd() : "",
            entity.getThiSinh() != null ? entity.getThiSinh().getHo() + " " + entity.getThiSinh().getTen() : "",
            entity.getNganh() != null ? entity.getNganh().getManganh() : "",
            entity.getTtThm(),
            entity.getNvTt(),
            entity.getTtPhuongthuc(),
            entity.getNvKetqua(),
            "..." // Thao tác
        };
    }

    @Override
    protected List<String> getSearchFields() {
        // Tìm kiếm theo CCCD hoặc Mã Ngành
        return List.of("thiSinh.cccd", "nganh.manganh");
    }

    @Override
    protected CompletableFuture<List<NguyenVong>> fetchPage(String keyword, Map<String, Object> filters, int page, int pageSize) {
        return nvDAO.findPageWithFilters(keyword, getSearchFields(), filters, page, pageSize);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
        return nvDAO.countWithFiltersAsync(keyword, getSearchFields(), filters);
    }

    @Override
    protected void showAddDialog() {
        // Mở Dialog thêm mới (đăng ký)
        NguyenVongDialog dialog = new NguyenVongDialog(getParentFrame(), "Đăng ký Nguyện Vọng", null, nvService);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadTableData();
        }
    }

    @Override
    protected void showEditDialog(int tableRow) {
        int id = getIdFromRow(tableRow);
        NguyenVong nv = nvDAO.findById(id);
        if (nv != null) {
            NguyenVongDialog dialog = new NguyenVongDialog(getParentFrame(), "Sửa Nguyện Vọng", nv, nvService);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadTableData();
            }
        }
    }

    @Override
    protected void deleteRecord(int tableRow) {
        int id = getIdFromRow(tableRow);
        String ten = getNameFromRow(tableRow); // Có thể là CCCD hoặc HoTen
        if (confirmDelete("Nguyện vọng ID " + id)) {
            NguyenVongService.KetQuaDangKy kq = nvService.huyNguyenVong(id);
            if (kq.isThanhCong()) {
                showSuccess(kq.getThongDiep());
                loadTableData();
            } else {
                showError(kq.getThongDiep());
            }
        }
    }

    @Override
    protected void configureColumns() {
        // Ẩn cột ID hoặc chỉnh width
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setMaxWidth(60); // Thứ tự
    }
}
