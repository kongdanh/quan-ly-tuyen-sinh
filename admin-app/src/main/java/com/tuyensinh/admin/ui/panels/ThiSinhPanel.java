package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.components.*;
import com.tuyensinh.admin.ui.dialog.ThiSinhFormDialog;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.service.ImportService;
import com.tuyensinh.service.ThiSinhService;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

public class ThiSinhPanel extends BaseTablePanel<ThiSinh> {

    private final ThiSinhService thiSinhService = new ThiSinhService();
    private String currentGenderFilter = "";

    public ThiSinhPanel() {
        super("Quản lý thí sinh", "Danh sách hồ sơ đăng ký xét tuyển từ hệ thống");
    
        setupExtras();
        loadTableData();
    }

    // ----------------------------------------------------------------
    // CONFIG
    // ----------------------------------------------------------------

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Số Báo Danh", "Họ và Tên",
                            "Email", "Điện Thoại", "Ngày Sinh", "Giới Tính", "Thao tác"};
    }

    @Override
    protected List<String> getSearchFields() {
        return Arrays.asList("ho", "ten", "cccd", "sobaodanh");
    }

    @Override
    protected void configureColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
    }

    // ----------------------------------------------------------------
    // DATA
    // ----------------------------------------------------------------

    @Override
    protected Object[] toTableRow(ThiSinh ts) {
        return new Object[]{
            ts.getId(),
            ts.getSobaodanh() != null ? ts.getSobaodanh() : ts.getCccd(),
            ts.getHo() + " " + ts.getTen(),
            ts.getEmail(),
            ts.getDienThoai(),
            ts.getNgaySinh(),
            ts.getGioiTinh(),
            "" // placeholder cột Thao tác
        };
    }

    // ----------------------------------------------------------------
    // QUERY — delegate sang ThiSinhService
    // ----------------------------------------------------------------

    @Override
    protected CompletableFuture<List<ThiSinh>> fetchPage(String keyword, Map<String, Object> filters, int page, int pageSize) {
        return thiSinhService.findPageWithFilters(keyword, getSearchFields(), filters, page, pageSize);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
        return thiSinhService.countWithFiltersAsync(keyword, getSearchFields(), filters);
    }

    // ----------------------------------------------------------------
    // CRUD DIALOGS
    // ----------------------------------------------------------------

    @Override
    protected void showAddDialog() {
        ThiSinhFormDialog dialog = new ThiSinhFormDialog(getParentFrame(), new ThiSinh(), true);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadTableData();
            showSuccess("Thêm thí sinh thành công!");
        }
    }

    @Override
    protected void showEditDialog(int tableRow) {
        int id = getIdFromRow(tableRow);
        thiSinhService.findByIdAsync(id).thenAccept(ts ->
            SwingUtilities.invokeLater(() -> {
                ThiSinhFormDialog dialog = new ThiSinhFormDialog(getParentFrame(), ts, false);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    loadTableData();
                    showSuccess("Cập nhật thành công!");
                }
            })
        );
    }

    @Override
    protected void deleteRecord(int tableRow) {
        String name = getNameFromRow(tableRow);
        if (!confirmDelete(name)) return;

        int id = getIdFromRow(tableRow);
        thiSinhService.deleteByIdAsync(id).thenAccept(success ->
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    loadTableData();
                    showSuccess("Đã xóa hồ sơ: " + name);
                } else {
                    showError("Không thể xóa. Vui lòng thử lại.");
                }
            })
        );
    }

    // ----------------------------------------------------------------
    // EXTRAS — import Excel + filter giới tính
    // ----------------------------------------------------------------

    @Override
    protected void setupExtras() {
        toolbar.addDynamicFilterCategory(
            "Theo Giới Tính", 6, Arrays.asList("Tất cả", "Nam", "Nữ"),
            (col, val) -> applyFilter("gioiTinh", val)
        );

        toolbar.addDynamicFilterCategory(
            "Khu Vực", 7, Arrays.asList("Tất cả", "1", "2","2NT", "3"),
            (col, val) -> applyFilter("khuVuc", val)
        );

        toolbar.getBtnImport().addActionListener(e -> handleImportExcel());
    }

    // ----------------------------------------------------------------
    // PRIVATE HELPERS
    // ----------------------------------------------------------------

    private void handleImportExcel() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        toolbar.getBtnImport().setEnabled(false);
        toolbar.getBtnImport().setText("Đang xử lý...");

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return new ImportService().importThiSinh(file);
            }
            @Override
            protected void done() {
                toolbar.getBtnImport().setEnabled(true);
                toolbar.getBtnImport().setText("Nhập Excel");
                try {
                    List<String> errors = get();
                    if (errors.isEmpty()) {
                        loadTableData();
                        showSuccess("Import thành công!");
                    } else {
                        showError("Import có " + errors.size() + " lỗi. Kiểm tra lại file Excel.");
                    }
                } catch (Exception ex) {
                    showError("Lỗi không xác định khi import.");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
}