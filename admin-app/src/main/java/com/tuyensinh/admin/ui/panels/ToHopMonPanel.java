package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.ToHopMonDialog;
import com.tuyensinh.util.Constants;
import com.tuyensinh.dao.ToHopMonDAO;
import com.tuyensinh.model.ToHopMon;
import com.tuyensinh.service.ImportService;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ToHopMonPanel extends BaseTablePanel<ToHopMon> {

    private final ToHopMonDAO dao = new ToHopMonDAO();

    public ToHopMonPanel() {
        super("Quản lý Tổ Hợp Môn", "Danh sách các tổ hợp môn xét tuyển");
        setupExtras();
        loadTableData();
    }

    @Override
    protected String getModuleCode() {
        return Constants.QUYEN_TOHOP;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{
            "ID", "Mã Tổ Hợp", "Tên Tổ Hợp", "Môn 1", "Môn 2", "Môn 3", "Thao Tác"
        };
    }

    @Override
    protected Object[] toTableRow(ToHopMon entity) {
        return new Object[]{
            entity.getId(),
            entity.getMatohop(),
            entity.getTentohop(),
            entity.getMon1(),
            entity.getMon2(),
            entity.getMon3(),
            "..."
        };
    }

    @Override
    protected List<String> getSearchFields() {
        return List.of("matohop", "tentohop", "mon1", "mon2", "mon3");
    }

    @Override
    protected CompletableFuture<List<ToHopMon>> fetchPage(String keyword, Map<String, Object> filters, int page, int pageSize) {
        return dao.findPageWithFilters(keyword, getSearchFields(), filters, page, pageSize);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
        return dao.countWithFiltersAsync(keyword, getSearchFields(), filters);
    }

    @Override
    protected void setupExtras() {
        toolbar.getBtnImport().setVisible(true);
        toolbar.getBtnImport().addActionListener(e -> handleImportExcel());
    }

    @Override
    protected void showAddDialog() {
        ToHopMonDialog dialog = new ToHopMonDialog(getParentFrame(), "Tổ Hợp Môn", null, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadTableData();
        }
    }

    @Override
    protected void showEditDialog(int tableRow) {
        int id = getIdFromRow(tableRow);
        ToHopMon entity = dao.findById(id);
        if (entity != null) {
            ToHopMonDialog dialog = new ToHopMonDialog(getParentFrame(), "Tổ Hợp Môn", entity, dao);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadTableData();
            }
        }
    }

    @Override
    protected void deleteRecord(int tableRow) {
        int id = getIdFromRow(tableRow);
        String maToHop = getNameFromRow(tableRow); 
        if (confirmDelete("Tổ hợp môn " + maToHop)) {
            dao.deleteByIdAsync(id).thenAccept(success -> {
                if (success) {
                    showSuccess("Đã xóa tổ hợp môn thành công!");
                    loadTableData();
                } else {
                    showError("Lỗi: Không thể xóa tổ hợp môn (có thể đang được sử dụng ở nơi khác)!");
                }
            });
        }
    }

    private void handleImportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        toolbar.getBtnImport().setEnabled(false);
        toolbar.getBtnImport().setText("Đang xử lý...");

        new SwingWorker<List<String>, Void>() {
            @Override protected List<String> doInBackground() {
                return new ImportService().importToHopMon(file);
            }
            @Override protected void done() {
                toolbar.getBtnImport().setEnabled(true);
                toolbar.getBtnImport().setText("Nhập Excel");
                try {
                    List<String> errors = get();
                    if (errors.isEmpty()) { 
                        loadTableData(); 
                        showSuccess("Import tổ hợp môn thành công!"); 
                    } else {
                        showImportErrors(errors);
                    }
                } catch (Exception ex) {
                    showError("Lỗi không xác định khi import.");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void showImportErrors(List<String> errors) {
        StringBuilder sb = new StringBuilder();
        int preview = Math.min(errors.size(), 12);
        for (int i = 0; i < preview; i++) sb.append("• ").append(errors.get(i)).append("\n");
        if (errors.size() > preview) sb.append("... và ").append(errors.size() - preview).append(" lỗi khác.");
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false); ta.setLineWrap(true); ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new java.awt.Dimension(560, 260));
        JOptionPane.showMessageDialog(this, sp, "Import có " + errors.size() + " lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
