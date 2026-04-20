package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.components.RoundedButton;
import com.tuyensinh.admin.ui.dialog.DanhSachYeuCauDialog;
import com.tuyensinh.admin.ui.dialog.ThiSinhFormDialog;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.service.ImportService;
import com.tuyensinh.service.ThiSinhService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ThiSinhPanel extends BaseTablePanel<ThiSinh> {

    private final ThiSinhService    thiSinhService = new ThiSinhService();

    private RoundedButton btnYeuCauSua;
    private Timer         timerYeuCau;

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
            ""
        };
    }

    // ----------------------------------------------------------------
    // QUERY
    // ----------------------------------------------------------------

    @Override
    protected CompletableFuture<List<ThiSinh>> fetchPage(
            String keyword, Map<String, Object> filters, int page, int pageSize) {
        return thiSinhService.findPageWithFilters(keyword, getSearchFields(), filters, page, pageSize);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(
            String keyword, Map<String, Object> filters) {
        return thiSinhService.countWithFiltersAsync(keyword, getSearchFields(), filters);
    }

    // ----------------------------------------------------------------
    // CRUD
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
    // EXTRAS
    // ----------------------------------------------------------------

    @Override
    protected void setupExtras() {
        // Filter giới tính
        toolbar.addDynamicFilterCategory(
            "Theo Giới Tính", 6,
            Arrays.asList("Tất cả", "Nam", "Nữ"),
            (col, val) -> applyFilter("gioiTinh", "Tất cả".equals(val) ? "" : val)
        );

        // Filter khu vực
        toolbar.addDynamicFilterCategory(
            "Khu Vực", 7,
            Arrays.asList("Tất cả", "1", "2", "2NT", "3"),
            (col, val) -> applyFilter("khuVuc", "Tất cả".equals(val) ? "" : val)
        );

        // Import Excel
        toolbar.getBtnImport().addActionListener(e -> handleImportExcel());

        // btn cập nhật thông tin thí sinh ADMIN
        btnYeuCauSua = new RoundedButton("Yêu cầu sửa (0)");
        btnYeuCauSua.setBackground(Color.decode(UIConstants.COLOR_BG));
        btnYeuCauSua.setForeground(Color.decode(UIConstants.COLOR_TEXT_MUTED));
        btnYeuCauSua.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btnYeuCauSua.addActionListener(e -> {
            new DanhSachYeuCauDialog(getParentFrame()).setVisible(true);
            loadTableData();
            checkYeuCauCount();
        });

        // add btn
        Container actionPanel = toolbar.getBtnImport().getParent();
        if (actionPanel != null) {
            actionPanel.add(Box.createHorizontalStrut(10));
            actionPanel.add(btnYeuCauSua);
            
            actionPanel.revalidate();
            actionPanel.repaint();
        } else {
            // Backup trong trường hợp không lấy được parent
            toolbar.add(btnYeuCauSua);
            toolbar.revalidate();
        }

        // polling
        startYeuCauPolling();
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

    private void checkYeuCauCount() {
        com.tuyensinh.service.YeuCauCapNhatService.getInstance().demYeuCauChoDuyet().thenAccept(countLong -> {
            SwingUtilities.invokeLater(() -> {
                int count = countLong != null ? countLong.intValue() : 0; 
                
                if (count > 0) {
                    btnYeuCauSua.setText("Yêu cầu sửa (" + count + ")");
                    btnYeuCauSua.setBackground(Color.decode(com.tuyensinh.admin.util.UIConstants.COLOR_DANGER)); 
                    btnYeuCauSua.setForeground(Color.WHITE);
                } else {
                    btnYeuCauSua.setText("Yêu cầu sửa (0)");
                    btnYeuCauSua.setBackground(Color.decode(com.tuyensinh.admin.util.UIConstants.COLOR_BG)); 
                    btnYeuCauSua.setForeground(Color.decode(com.tuyensinh.admin.util.UIConstants.COLOR_TEXT_MUTED));
                }
            });
        }).exceptionally(ex -> {
            System.err.println("Lỗi gọi Service đếm yêu cầu: " + ex.getMessage());
            return null;
        });
    }

    /** Polling mỗi 30 giây kiểm tra yêu cầu mới */
    private void startYeuCauPolling() {
        checkYeuCauCount();
        timerYeuCau = new Timer(30_000, e -> checkYeuCauCount());
        timerYeuCau.setRepeats(true);
        timerYeuCau.start();
    }
}