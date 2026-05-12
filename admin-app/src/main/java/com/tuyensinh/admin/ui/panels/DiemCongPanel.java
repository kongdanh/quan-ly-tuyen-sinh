package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.model.DiemCong;
import com.tuyensinh.service.DiemCongService;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DiemCongPanel extends BaseTablePanel<DiemCong> {

    private final DiemCongService diemCongService = new DiemCongService();

    public DiemCongPanel() {
        super("Quản lý điểm cộng", "Danh sách điểm cộng của thí sinh (Tiếng Anh, HSG, Ưu tiên...)");
        loadTableData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "CCCD Thí sinh", "Tên Thí sinh", "SBD", "Điểm Chứng chỉ", "Điểm Ưu tiên XT", "Tổng Điểm Cộng", "Ghi chú"};
    }

    @Override
    protected List<String> getSearchFields() {
        return Arrays.asList("tsCccd", "thiSinh.cccd", "thiSinh.ho", "thiSinh.ten", "thiSinh.sobaodanh");
    }

    @Override
    protected void configureColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
    }

    @Override
    protected Object[] toTableRow(DiemCong dc) {
        String tenThiSinh = dc.getThiSinh() != null ? dc.getThiSinh().getHo() + " " + dc.getThiSinh().getTen() : "";
        String sbd = dc.getThiSinh() != null ? dc.getThiSinh().getSobaodanh() : "";
        String cccd = dc.getTsCccd();
        return new Object[]{
            dc.getId(),
            cccd,
            tenThiSinh,
            sbd,
            dc.getDiemCC(),
            dc.getDiemUtxt(),
            dc.getDiemTong(),
            dc.getGhichu()
        };
    }

    @Override
    protected CompletableFuture<List<DiemCong>> fetchPage(
            String keyword, Map<String, Object> filters, int page, int pageSize) {
        return diemCongService.findPageWithFilters(keyword, getSearchFields(), filters, page, pageSize);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(
            String keyword, Map<String, Object> filters) {
        return diemCongService.countWithFiltersAsync(keyword, getSearchFields(), filters);
    }

    @Override
    protected void showAddDialog() {
        com.tuyensinh.admin.ui.dialog.DiemCongDialog dialog = new com.tuyensinh.admin.ui.dialog.DiemCongDialog(
            getParentFrame(), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadTableData();
    }

    @Override
    protected void showEditDialog(int tableRow) {
        int id = getIdFromRow(tableRow);
        diemCongService.findByIdAsync(id).thenAccept(dc -> SwingUtilities.invokeLater(() -> {
            if (dc != null) {
                com.tuyensinh.admin.ui.dialog.DiemCongDialog dialog = new com.tuyensinh.admin.ui.dialog.DiemCongDialog(
                    getParentFrame(), dc);
                dialog.setVisible(true);
                if (dialog.isSaved()) loadTableData();
            }
        }));
    }

    @Override
    protected void deleteRecord(int tableRow) {
        int id = getIdFromRow(tableRow);
        if (!confirmDelete("Bản ghi điểm cộng ID = " + id)) return;

        diemCongService.deleteByIdAsync(id).thenAccept(success ->
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    loadTableData();
                    showSuccess("Đã xóa điểm cộng.");
                } else {
                    showError("Không thể xóa. Vui lòng thử lại.");
                }
            })
        );
    }

    @Override
    protected void setupExtras() {
    }

    @Override
    protected String getModuleCode() {
        return com.tuyensinh.util.Constants.QUYEN_DIEM_THI; // Hoặc quyền khác phù hợp
    }
}
