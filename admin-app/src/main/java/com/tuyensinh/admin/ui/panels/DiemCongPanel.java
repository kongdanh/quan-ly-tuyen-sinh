package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.DiemCongDialog;
import com.tuyensinh.model.DiemCong;
import com.tuyensinh.service.DiemCongService;
import com.tuyensinh.util.Constants;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Panel quản lý điểm cộng (IELTS, DGNL, VSAT, Ưu tiên).
 */
public class DiemCongPanel extends BaseTablePanel<DiemCong> {

    private final DiemCongService diemCongService = new DiemCongService();

    public DiemCongPanel() {
        super("Quản lý Điểm Cộng", "Danh sách các loại điểm ưu tiên, chứng chỉ của thí sinh");
        setupExtras();
        loadTableData();
    }

    @Override
    protected String getModuleCode() {
        return Constants.QUYEN_DIEM_CONG;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{
            "ID", "CCCD", "Tên Thí sinh", "Mã Ngành", "Tổ Hợp", "Phương Thức", 
            "Điểm Chứng Chỉ", "Điểm Ưu Tiên", "Tổng Điểm", "Ghi chú"
        };
    }

    @Override
    protected List<String> getSearchFields() {
        return Arrays.asList("thiSinh.cccd", "manganh", "dcKeys", "thiSinh.ho", "thiSinh.ten");
    }

    @Override
    protected void configureColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
    }

    @Override
    protected Object[] toTableRow(DiemCong entity) {
        String tenThiSinh = entity.getThiSinh() != null ? entity.getThiSinh().getHo() + " " + entity.getThiSinh().getTen() : "";
        return new Object[]{
            entity.getId(),
            entity.getThiSinh() != null ? entity.getThiSinh().getCccd() : "",
            tenThiSinh,
            entity.getManganh(),
            entity.getMatohop(),
            entity.getPhuongthuc(),
            entity.getDiemCC(),
            entity.getDiemUtxt(),
            entity.getDiemTong(),
            entity.getGhichu()
        };
    }

    @Override
    protected CompletableFuture<List<DiemCong>> fetchPage(String keyword, Map<String, Object> filters, int page, int pageSize) {
        return diemCongService.findPageWithFilters(keyword, getSearchFields(), filters, page, pageSize);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
        return diemCongService.countWithFiltersAsync(keyword, getSearchFields(), filters);
    }

    @Override
    protected void showAddDialog() {
        DiemCongDialog dialog = new DiemCongDialog(getParentFrame(), "Thêm Điểm Cộng", null);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadTableData();
    }

    @Override
    protected void showEditDialog(int tableRow) {
        int id = getIdFromRow(tableRow);
        diemCongService.findByIdAsync(id).thenAccept(dc -> SwingUtilities.invokeLater(() -> {
            if (dc != null) {
                DiemCongDialog dialog = new DiemCongDialog(getParentFrame(), "Chỉnh sửa Điểm Cộng", dc);
                dialog.setVisible(true);
                if (dialog.isSaved()) loadTableData();
            }
        }));
    }

    @Override
    protected void deleteRecord(int tableRow) {
        int id = getIdFromRow(tableRow);
        if (!confirmDelete("Bản ghi điểm cộng ID = " + id)) return;

        diemCongService.deleteByIdAsync(id).thenAccept(success -> SwingUtilities.invokeLater(() -> {
            if (success) {
                loadTableData();
                showSuccess("Đã xóa điểm cộng.");
            } else {
                showError("Không thể xóa. Vui lòng thử lại.");
            }
        }));
    }

    @Override
    protected void setupExtras() {
        toolbar.addClearFilterOption(() -> applyFilter("phuongthuc", null));
        toolbar.addDynamicFilterCategory("Phương Thức", -1, Arrays.asList("Tất cả", "THPT", "DGNL", "VSAT"), (col, val) -> {
            applyFilter("phuongthuc", val);
        });
    }
}
