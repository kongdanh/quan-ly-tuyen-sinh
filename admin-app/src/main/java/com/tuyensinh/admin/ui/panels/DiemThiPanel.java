package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.DiemThiFormDialog;
import com.tuyensinh.dao.DiemThiXetTuyenDAO;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.service.ImportService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DiemThiPanel extends BaseTablePanel<DiemThiXetTuyen> {

    private final DiemThiXetTuyenDAO diemDAO = new DiemThiXetTuyenDAO();

    public DiemThiPanel() {
        super("Điểm thi", "Quản lý bảng điểm xét tuyển của thí sinh");

        setupExtras();
        loadTableData();
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{
            "ID", "CCCD", "SBD", "Phương thức",
            "TO", "LI", "HO", "SI", "SU", "DI", "VA",
            "N1_THI", "N1_CC", "CNCN", "CNNN", "TI", "KTPL", "NL1", "NK1", "NK2",
            "Thao tác"
        };
    }

    @Override
    protected List<String> getSearchFields() {
        return Arrays.asList("thiSinh.cccd", "sobaodanh", "dPhuongthuc");
    }

    @Override
    protected int getTableAutoResizeMode() {
        return JTable.AUTO_RESIZE_OFF;
    }

    @Override
    protected int getHorizontalScrollBarPolicy() {
        return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
    }

    @Override
    protected void configureColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(95);

        for (int i = 4; i <= 19; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(70);
        }
    }

    @Override
    protected Object[] toTableRow(DiemThiXetTuyen d) {
        String cccd = d.getThiSinh() != null ? d.getThiSinh().getCccd() : valueOrEmpty(d.getCccd());
        return new Object[]{
            d.getId(),
            cccd,
            valueOrEmpty(d.getSobaodanh()),
            valueOrEmpty(DiemThiXetTuyenDAO.normalizeMethod(d.getDPhuongthuc())),
            scoreText(d.getTo()),
            scoreText(d.getLi()),
            scoreText(d.getHo()),
            scoreText(d.getSi()),
            scoreText(d.getSu()),
            scoreText(d.getDi()),
            scoreText(d.getVa()),
            scoreText(d.getN1Thi()),
            scoreText(d.getN1Cc()),
            scoreText(d.getCncn()),
            scoreText(d.getCnnn()),
            scoreText(d.getTi()),
            scoreText(d.getKtpl()),
            scoreText(d.getNl1()),
            scoreText(d.getNk1()),
            scoreText(d.getNk2()),
            ""
        };
    }

    @Override
    protected CompletableFuture<List<DiemThiXetTuyen>> fetchPage(String keyword, Map<String, Object> filters, int page, int pageSize) {
        return diemDAO.findPageForAdmin(keyword, filters, page, pageSize);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
        return diemDAO.countForAdmin(keyword, filters);
    }

    @Override
    protected void showAddDialog() {
        DiemThiFormDialog dialog = new DiemThiFormDialog(getParentFrame(), new DiemThiXetTuyen(), true);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadTableData();
            showSuccess("Thêm điểm thi thành công!");
        }
    }

    @Override
    protected void showEditDialog(int tableRow) {
        int id = getIdFromRow(tableRow);

        diemDAO.findByIdForAdmin(id).thenAccept(diem -> SwingUtilities.invokeLater(() -> {
            if (diem == null) {
                showError("Không tìm thấy bản ghi điểm thi.");
                return;
            }

            DiemThiFormDialog dialog = new DiemThiFormDialog(getParentFrame(), diem, false);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadTableData();
                showSuccess("Cập nhật điểm thi thành công!");
            }
        }));
    }

    @Override
    protected void deleteRecord(int tableRow) {
        String cccd = String.valueOf(getCellValue(tableRow, 1));
        if (!confirmDelete("Điểm thi CCCD " + cccd)) {
            return;
        }

        int id = getIdFromRow(tableRow);
        diemDAO.deleteByIdAsync(id).thenAccept(success -> SwingUtilities.invokeLater(() -> {
            if (success) {
                loadTableData();
                showSuccess("Đã xóa bản ghi điểm thi của " + cccd);
            } else {
                showError("Không thể xóa bản ghi điểm thi.");
            }
        }));
    }

    @Override
    protected void setupExtras() {
        toolbar.addDynamicFilterCategory(
            "Theo phương thức",
            3,
            Arrays.asList("Tất cả", "THPT", "VSAT", "DGNL"),
            (col, val) -> applyFilter("dPhuongthuc", val)
        );

        toolbar.getBtnImport().addActionListener(e -> handleImportExcel());
    }

    private void handleImportExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();
        toolbar.getBtnImport().setEnabled(false);
        toolbar.getBtnImport().setText("Đang xử lý...");

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return new ImportService().importDiemThi(file);
            }

            @Override
            protected void done() {
                toolbar.getBtnImport().setEnabled(true);
                toolbar.getBtnImport().setText("Nhập Excel");

                try {
                    List<String> errors = get();
                    if (errors.isEmpty()) {
                        loadTableData();
                        showSuccess("Import điểm thi thành công!");
                    } else {
                        showImportErrors(errors);
                    }
                } catch (Exception ex) {
                    showError("Lỗi không xác định khi import điểm thi.");
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void showImportErrors(List<String> errors) {
        StringBuilder sb = new StringBuilder();
        int maxPreview = Math.min(errors.size(), 12);
        for (int i = 0; i < maxPreview; i++) {
            sb.append("- ").append(errors.get(i)).append("\n");
        }
        if (errors.size() > maxPreview) {
            sb.append("... và ").append(errors.size() - maxPreview).append(" lỗi khác.");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(560, 260));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Import có " + errors.size() + " lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String scoreText(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    @Override
    protected String getModuleCode() {
        return com.tuyensinh.util.Constants.QUYEN_PHAN_QUYEN; 
    }
}
