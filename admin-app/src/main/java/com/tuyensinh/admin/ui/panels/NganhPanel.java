package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.NganhFormDialog;
import com.tuyensinh.model.Nganh;
import com.tuyensinh.service.NganhService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NganhPanel extends BaseTablePanel<Nganh> {

//    private static final String[] COLUMN_NAMES = {
//            "STT", "M\u00e3 ng\u00e0nh", "T\u00ean ng\u00e0nh", "Khoa",
//            "Ch\u1ec9 ti\u00eau", "\u0110\u00e3 \u0111\u0103ng k\u00fd", "T\u1ec9 l\u1ec7", "Tr\u1ea1ng th\u00e1i", "H\u00e0nh \u0111\u1ed9ng"
//    };
private static final String[] COLUMN_NAMES = {
        "STT", "M\u00e3 ng\u00e0nh", "T\u00ean ng\u00e0nh", "Khoa",
        "Ch\u1ec9 ti\u00eau", "Tuy\u1ec3n th\u1eb3ng", "\u0110GNL", "THPT", "VSAT",
        "\u0110\u00e3 \u0111\u0103ng k\u00fd", "T\u1ec9 l\u1ec7",
        "Tr\u1ea1ng th\u00e1i", "H\u00e0nh \u0111\u1ed9ng"
};
    private static final int[] COLUMN_WIDTHS =
            {50, 110, 220, 120, 80, 90, 80, 80, 80, 100, 160, 130, 90};

    private final NganhService nganhService = new NganhService();
    private static final List<String> SEARCH_FIELDS = List.of("manganh", "tennganh");

    public NganhPanel() {
        super("Ng\u00e0nh tuy\u1ec3n sinh", "Qu\u1ea3n l\u00fd danh s\u00e1ch ng\u00e0nh v\u00e0 ch\u1ec9 ti\u00eau tuy\u1ec3n sinh");
        setupExtras();
        loadTableData();
    }

    // =========================================================================
    // ABSTRACT IMPLEMENTATIONS
    // =========================================================================

    @Override protected String[] getColumnNames()  { return COLUMN_NAMES; }
    @Override protected List<String> getSearchFields() { return SEARCH_FIELDS; }
    @Override protected String getToolbarTitle()   { return "Danh s\u00e1ch ng\u00e0nh"; }
    @Override protected int getActionColumnIndex() { return COLUMN_NAMES.length - 1; }
    @Override protected int getActionColumnWidth() { return 90; }
    private Map<String, Long> dangKyCountCache = new java.util.HashMap<>();
    @Override
    protected Object[] toTableRow(Nganh n) {
        int chiTieu  = n.getNChitieu()   != null ? n.getNChitieu()   : 0;
        int daDangKy = n.getSlDadangky() != null ? n.getSlDadangky() : 0;
        int phanTram = chiTieu == 0 ? 0 : (int) Math.round(daDangKy * 100.0 / chiTieu);

        return new Object[]{
                null,
                n.getManganh(),
                n.getTennganh(),
                resolveKhoa(n.getManganh()),
                chiTieu,
                "1".equals(n.getNTuyenthang()) ? "Có" : "Không",
                "1".equals(n.getNDgnl()) ? "Có" : "Không",
                "1".equals(n.getNThpt()) ? "Có" : "Không",
                "1".equals(n.getNVsat()) ? "Có" : "Không",
                daDangKy,
                phanTram,
                resolveTrangThai(n, phanTram),
                n.getId()
        };
    }

    @Override
    protected void showAddDialog() {
        NganhFormDialog dlg = new NganhFormDialog(getParentFrame(), null);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadTableData(); showSuccess("Th\u00eam ng\u00e0nh th\u00e0nh c\u00f4ng!"); }
    }

    @Override
    protected void showEditDialog(int tableRow) {
        Object idObj = getCellValue(tableRow, 12);
        if (idObj == null) return;
        int id = (idObj instanceof Integer) ? (Integer) idObj : Integer.parseInt(idObj.toString());
        nganhService.findByIdAsync(id).thenAccept(nganh -> {
            if (nganh == null) { SwingUtilities.invokeLater(() -> showError("Kh\u00f4ng t\u00ecm th\u1ea5y ng\u00e0nh.")); return; }
            SwingUtilities.invokeLater(() -> {
                NganhFormDialog dlg = new NganhFormDialog(getParentFrame(), nganh);
                dlg.setVisible(true);
                if (dlg.isSaved()) { loadTableData(); showSuccess("C\u1eadp nh\u1eadt ng\u00e0nh th\u00e0nh c\u00f4ng!"); }
            });
        }).exceptionally(ex -> { SwingUtilities.invokeLater(() -> showError("L\u1ed7i: " + ex.getMessage())); return null; });
    }

    // =========================================================================
    // DELETE - xu ly foreign key constraint
    // =========================================================================

    @Override
    protected void deleteRecord(int tableRow) {
        String tenNganh = getNameFromRow(tableRow);
        Object idObj    = getCellValue(tableRow, 12);
        Object maObj    = getCellValue(tableRow, 1);
        if (idObj == null || maObj == null) return;
        int    id      = (idObj instanceof Integer) ? (Integer) idObj : Integer.parseInt(idObj.toString());
        String maNganh = maObj.toString();

        // Buoc 1: Xac nhan co muon xoa khong
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n x\u00f3a ng\u00e0nh \"" + tenNganh + "\"?",
                "X\u00e1c nh\u1eadn x\u00f3a",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // Buoc 2: Kiem tra FK truoc khi xoa
        nganhService.checkDeleteDependencies(id, maNganh).thenAccept(deps ->
                SwingUtilities.invokeLater(() -> {
                    if (deps.isEmpty()) {
                        // Khong co du lieu con -> xoa truc tiep an toan
                        doSimpleDelete(id, tenNganh);
                    } else {
                        // Co du lieu con -> canh bao, hoi co dong y xoa cascade khong
                        showCascadeConfirmDialog(id, maNganh, tenNganh, deps);
                    }
                })
        ).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> showError("L\u1ed7i ki\u1ec3m tra: " + ex.getMessage()));
            return null;
        });
    }

    /** Xoa don gian khi khong co du lieu con */
    private void doSimpleDelete(int id, String tenNganh) {
        nganhService.deleteByIdAsync(id).thenAccept(ok ->
                SwingUtilities.invokeLater(() -> {
                    if (ok) { loadTableData(); showSuccess("\u0110\u00e3 x\u00f3a ng\u00e0nh: " + tenNganh); }
                    else      showError("Kh\u00f4ng th\u1ec3 x\u00f3a ng\u00e0nh n\u00e0y.");
                })
        ).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> showError("L\u1ed7i x\u00f3a: " + ex.getMessage()));
            return null;
        });
    }

    /**
     * Dialog canh bao khi co du lieu con.
     * Liet ke ro rang bao nhieu ban ghi se bi anh huong.
     * Neu user dong y -> xoa cascade toan bo.
     */
    private void showCascadeConfirmDialog(int id, String maNganh, String tenNganh, List<String> deps) {
        // Xay dung noi dung canh bao
        StringBuilder sb = new StringBuilder();
        sb.append("\u0110\u1ec3 x\u00f3a ng\u00e0nh \"").append(tenNganh).append("\",\n");
        sb.append("c\u00e1c d\u1eef li\u1ec7u sau c\u0169ng s\u1ebd b\u1ecb x\u00f3a theo:\n\n");
        for (String dep : deps) {
            sb.append("    \u2022  ").append(dep).append("\n");
        }
        sb.append("\nH\u00e0nh \u0111\u1ed9ng n\u00e0y kh\u00f4ng th\u1ec3 ho\u00e0n t\u00e1c.\nB\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n ti\u1ebfp t\u1ee5c?");

        // Custom panel: icon canh bao + text area
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JLabel iconLbl = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
        iconLbl.setVerticalAlignment(SwingConstants.TOP);
        panel.add(iconLbl, BorderLayout.WEST);

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setOpaque(false);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ta.setForeground(new Color(0x374151));
        panel.add(ta, BorderLayout.CENTER);

        int choice = JOptionPane.showConfirmDialog(
                this, panel,
                "C\u1ea3nh b\u00e1o: C\u00f3 d\u1eef li\u1ec7u li\u00ean quan",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null
        );
        if (choice != JOptionPane.YES_OPTION) return;

        // User dong y -> xoa cascade
        nganhService.deleteCascadeAsync(id, maNganh).thenAccept(ok ->
                SwingUtilities.invokeLater(() -> {
                    if (ok) {
                        loadTableData();
                        showSuccess("\u0110\u00e3 x\u00f3a ng\u00e0nh v\u00e0 to\u00e0n b\u1ed9 d\u1eef li\u1ec7u li\u00ean quan.");
                    } else {
                        showError("X\u00f3a th\u1ea5t b\u1ea1i.");
                    }
                })
        ).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> showError("L\u1ed7i x\u00f3a: " + ex.getMessage()));
            return null;
        });
    }
    @Override
    protected int getTableAutoResizeMode() {
        return JTable.AUTO_RESIZE_OFF;
    }

    @Override
    protected int getHorizontalScrollBarPolicy() {
        return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
    }
    // =========================================================================
    // COLUMN CONFIG
    // =========================================================================

    @Override
    protected void configureColumns() {
        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < COLUMN_WIDTHS.length && i < cm.getColumnCount(); i++) {
            TableColumn col = cm.getColumn(i);
            col.setPreferredWidth(COLUMN_WIDTHS[i]);
            if (i == 0 || i == 4) col.setMaxWidth(COLUMN_WIDTHS[i] + 20);
        }
        cm.getColumn(0).setCellRenderer(new SttRenderer());
        cm.getColumn(10).setCellRenderer(new ProgressBarRenderer()); // Tỉ lệ
        cm.getColumn(11).setCellRenderer(new StatusBadgeRenderer()); // Trạng thái
    }

    // =========================================================================
    // EXTRAS
    // =========================================================================

    @Override
    protected void setupExtras() {
        toolbar.addClearFilterOption(() -> { activeFilters.clear(); currentPage = 1; loadTableData(); });

        toolbar.addDynamicFilterCategory("Trạng thái", -1,
                Arrays.asList("Tất cả", "Đang tuyển", "Đã đủ chỉ tiêu", "Tạm dừng"),
                (col, val) -> { if ("Tất cả".equals(val)) activeFilters.remove("trangThai"); else activeFilters.put("trangThai", val); currentPage = 1; loadTableData(); });

        toolbar.addDynamicFilterCategory("Chỉ tiêu", -1,
                Arrays.asList("Tất cả", "< 100", "100 - 150", "150 - 200", "> 200"),
                (col, val) -> { if ("Tất cả".equals(val)) activeFilters.remove("nChitieuRange"); else activeFilters.put("nChitieuRange", val); currentPage = 1; loadTableData(); });

        toolbar.addDynamicFilterCategory("Xét tuyển thẳng", -1,
                Arrays.asList("Tất cả", "Có", "Không"),
                (col, val) -> { if ("Tất cả".equals(val)) activeFilters.remove("nTuyenthang"); else applyFilter("nTuyenthang", "Có".equals(val) ? "1" : "0"); });

        toolbar.getBtnImport().addActionListener(e -> importExcel());

        // Nút xem Tổ hợp môn của ngành
        JButton btnXemToHop = new JButton("Xem Tổ hợp môn");
        btnXemToHop.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một ngành để xem tổ hợp môn.");
                return;
            }
            String maNganh = (String) getCellValue(row, 1);
            String tenNganh = (String) getCellValue(row, 2);
            
            new com.tuyensinh.service.NganhToHopService().findByMaNganhAsync(maNganh).thenAccept(list -> {
                SwingUtilities.invokeLater(() -> {
                    if (list == null || list.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Ngành này chưa được gán tổ hợp môn nào.");
                        return;
                    }
                    StringBuilder sb = new StringBuilder("Các tổ hợp môn của ngành " + tenNganh + ":\n\n");
                    for (com.tuyensinh.model.NganhToHop nt : list) {
                        sb.append(" • ").append(nt.getToHopMon().getMatohop())
                          .append(": ").append(nt.getToHopMon().getTentohop()).append("\n");
                    }
                    JOptionPane.showMessageDialog(this, sb.toString(), "Tổ hợp môn theo ngành", JOptionPane.INFORMATION_MESSAGE);
                });
            });
        });
        toolbar.add(btnXemToHop);
    }

    // =========================================================================
    // FETCH
    // =========================================================================

    // Bỏ field dangKyCountCache đi, không cần nữa

    @Override
    protected CompletableFuture<List<Nganh>> fetchPage(
            String keyword, Map<String, Object> filters, int page, int pageSize) {

        Map<String, Object> dbFilters = buildDbFilters(filters);

        CompletableFuture<List<Nganh>> dataFuture =
                nganhService.findPageWithFilters(keyword, SEARCH_FIELDS, dbFilters, page, pageSize)
                        .thenApply(list -> applyClientFilters(list, filters));

        CompletableFuture<Map<String, Long>> countFuture =
                nganhService.fetchDangKyCountMap();

        // Chờ cả 2 xong rồi gắn count thực tế vào từng Nganh
        return dataFuture.thenCombine(countFuture, (list, countMap) -> {
            for (Nganh n : list) {
                long realCount = countMap.getOrDefault(n.getManganh(), 0L);
                n.setSlDadangky((int) realCount);
            }
            return list;
        });
    }

    @Override
    protected CompletableFuture<Long> fetchCount(
            String keyword, Map<String, Object> filters) {
        Map<String, Object> dbFilters = buildDbFilters(filters);
        return nganhService.findPageWithFilters(keyword, SEARCH_FIELDS, dbFilters, 1, Integer.MAX_VALUE)
                .thenApply(list -> (long) applyClientFilters(list, filters).size());
    }

    private Map<String, Object> buildDbFilters(Map<String, Object> filters) {
        Map<String, Object> db = new HashMap<>(filters);
        db.remove("trangThai");
        db.remove("nChitieuRange");
        return db;
    }

    private List<Nganh> applyClientFilters(List<Nganh> list, Map<String, Object> filters) {
        String trangThaiFilter    = (String) filters.get("trangThai");
        String chiTieuRangeFilter = (String) filters.get("nChitieuRange");
        if (trangThaiFilter == null && chiTieuRangeFilter == null) return list;

        List<Nganh> result = new ArrayList<>();
        for (Nganh n : list) {
            int chiTieu  = n.getNChitieu()   != null ? n.getNChitieu()   : 0;
            int daDangKy = n.getSlDadangky() != null ? n.getSlDadangky() : 0;
            int pct      = chiTieu == 0 ? 0 : (int) Math.round(daDangKy * 100.0 / chiTieu);

            if (trangThaiFilter != null && !resolveTrangThai(n, pct).equals(trangThaiFilter)) continue;

            if (chiTieuRangeFilter != null) {
                boolean inRange = switch (chiTieuRangeFilter) {
                    case "< 100"     -> chiTieu < 100;
                    case "100 - 150" -> chiTieu >= 100 && chiTieu <= 150;
                    case "150 - 200" -> chiTieu > 150  && chiTieu <= 200;
                    case "> 200"     -> chiTieu > 200;
                    default          -> true;
                };
                if (!inRange) continue;
            }
            result.add(n);
        }
        return result;
    }

    // =========================================================================
    // IMPORT EXCEL
    // =========================================================================

    private void importExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Ch\u1ecdn file Excel ng\u00e0nh tuy\u1ec3n sinh");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel files (*.xlsx, *.xls)", "xlsx", "xls"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        toolbar.getBtnImport().setEnabled(false);
        toolbar.getBtnImport().setText("\u0110ang x\u1eed l\u00fd...");

        CompletableFuture.runAsync(() -> processExcelImport(file))
                .thenRun(() -> SwingUtilities.invokeLater(() -> {
                    toolbar.getBtnImport().setEnabled(true);
                    toolbar.getBtnImport().setText("Nh\u1eadp Excel");
                    loadTableData();
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        toolbar.getBtnImport().setEnabled(true);
                        toolbar.getBtnImport().setText("Nh\u1eadp Excel");
                        showError("L\u1ed7i nh\u1eadp Excel: " + ex.getCause().getMessage());
                    });
                    return null;
                });
    }

    private void processExcelImport(File file) {
        try {
            org.apache.poi.ss.usermodel.Workbook wb = org.apache.poi.ss.usermodel.WorkbookFactory.create(file);
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
            List<Nganh> batch = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                if (row == null) continue;
                Nganh n = new Nganh();
                n.setManganh(cellStr(row, 0));    n.setTennganh(cellStr(row, 1));
                n.setNTohopgoc(cellStr(row, 2));  n.setNChitieu(cellInt(row, 3));
                n.setNDiemsan(cellBD(row, 4));    n.setNDiemtrungtuyen(cellBD(row, 5));
                n.setNTuyenthang(String.valueOf(cellInt(row, 6)));
                n.setNDgnl(String.valueOf(cellInt(row, 7)));
                n.setNThpt(String.valueOf(cellInt(row, 8)));
                n.setNVsat(String.valueOf(cellInt(row, 9)));
                n.setSlDadangky(cellInt(row, 10));
                if (n.getManganh() != null && !n.getManganh().isEmpty()
                        && n.getTennganh() != null && !n.getTennganh().isEmpty())
                    batch.add(n);
            }
            wb.close();
            if (!batch.isEmpty()) {
                nganhService.saveOrUpdateAll(batch).join();
                final int count = batch.size();
                SwingUtilities.invokeLater(() -> showSuccess("Nh\u1eadp th\u00e0nh c\u00f4ng " + count + " ng\u00e0nh t\u1eeb Excel."));
            } else {
                SwingUtilities.invokeLater(() -> showError("File Excel kh\u00f4ng c\u00f3 d\u1eef li\u1ec7u h\u1ee3p l\u1ec7."));
            }
        } catch (Exception e) { throw new RuntimeException(e.getMessage(), e); }
    }

    private String cellStr(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return null;
        cell.setCellType(org.apache.poi.ss.usermodel.CellType.STRING);
        String v = cell.getStringCellValue().trim();
        return v.isEmpty() ? null : v;
    }
    private int cellInt(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return 0;
        try { return (int) cell.getNumericCellValue(); } catch (Exception e) { return 0; }
    }
    private java.math.BigDecimal cellBD(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return null;
        try { return new java.math.BigDecimal(cell.getNumericCellValue()).setScale(2, java.math.RoundingMode.HALF_UP); }
        catch (Exception e) { return null; }
    }

    // =========================================================================
    // BUSINESS HELPERS
    // =========================================================================

    private String resolveKhoa(String ma) {
        if (ma == null) return "\u2014";
        String m = ma.toUpperCase();
        if (m.startsWith("748") || m.startsWith("746") || m.startsWith("751") || m.startsWith("752")) return "C\u00f4ng ngh\u1ec7";
        if (m.startsWith("734"))   return "Kinh t\u1ebf";
        if (m.startsWith("722") || m.startsWith("714023")) return "Ngo\u1ea1i ng\u1eef";
        if (m.startsWith("714"))   return "S\u01b0 ph\u1ea1m";
        if (m.startsWith("738"))   return "Lu\u1eadt";
        if (m.startsWith("731"))   return "X\u00e3 h\u1ed9i";
        if (m.startsWith("781"))   return "Du l\u1ecbch";
        return "\u2014";
    }

    private String resolveTrangThai(Nganh n, int pct) {
        boolean allOff = !"1".equals(n.getNTuyenthang()) && !"1".equals(n.getNDgnl())
                && !"1".equals(n.getNThpt()) && !"1".equals(n.getNVsat());
        if (allOff)     return "T\u1ea1m d\u1eebng";
        if (pct >= 100) return "\u0110\u00e3 \u0111\u1ee7 ch\u1ec9 ti\u00eau";
        return "\u0110ang tuy\u1ec3n";
    }

    // =========================================================================
    // RENDERERS
    // =========================================================================

    private static class SttRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, row + 1, sel, foc, row, col);
            setHorizontalAlignment(CENTER);
            return this;
        }
    }

    private static class ProgressBarRenderer implements TableCellRenderer {
        private static final int TRACK_W = 100, TRACK_H = 7, LABEL_W = 50, GAP = 6;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            int pct     = (value instanceof Integer) ? (Integer) value : 0;
            int clamped = Math.min(pct, 100);
            Color fillColor = pct >= 100 ? new Color(0xEF4444) : new Color(0x1E3A8A);
            Color bgColor   = isSelected ? new Color(0xEEF2FF) : Color.WHITE;

            JPanel cell = new JPanel(new GridBagLayout());
            cell.setOpaque(true);
            cell.setBackground(bgColor);
            cell.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));

            JPanel inner = new JPanel(null) {
                @Override public Dimension getPreferredSize() { return new Dimension(TRACK_W + GAP + LABEL_W, TRACK_H); }
                @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
                @Override public Dimension getMaximumSize()   { return getPreferredSize(); }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    g2.setColor(new Color(0xE5E7EB));
                    g2.fillRoundRect(0, 0, TRACK_W, TRACK_H, TRACK_H, TRACK_H);

                    int fillW = (int) Math.round(TRACK_W * clamped / 100.0);
                    if (fillW > 0) {
                        g2.setColor(fillColor);
                        if (fillW >= TRACK_W) {
                            g2.fillRoundRect(0, 0, fillW, TRACK_H, TRACK_H, TRACK_H);
                        } else {
                            g2.setClip(0, 0, fillW, TRACK_H);
                            g2.fillRoundRect(0, 0, TRACK_W, TRACK_H, TRACK_H, TRACK_H);
                            g2.setClip(null);
                        }
                    }

                    g2.setColor(new Color(0x374151));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(pct + "%", TRACK_W + GAP, TRACK_H / 2 + fm.getAscent() / 2 - 1);
                    g2.dispose();
                }
            };
            inner.setOpaque(false);
            cell.add(inner);
            return cell;
        }
    }

    private static class StatusBadgeRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String status = value != null ? value.toString() : "";
            final Color bg, fg;
            if      (status.equals("\u0110ang tuy\u1ec3n"))             { bg = new Color(0xDCFCE7); fg = new Color(0x16A34A); }
            else if (status.equals("\u0110\u00e3 \u0111\u1ee7 ch\u1ec9 ti\u00eau")) { bg = new Color(0xFEE2E2); fg = new Color(0xDC2626); }
            else if (status.equals("T\u1ea1m d\u1eebng"))               { bg = new Color(0xFEF9C3); fg = new Color(0xCA8A04); }
            else                                                         { bg = new Color(0xF3F4F6); fg = new Color(0x6B7280); }

            JLabel badge = new JLabel(status) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setOpaque(false);
            badge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            badge.setForeground(fg);
            badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
            badge.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
            wrapper.setOpaque(true);
            wrapper.setBackground(isSelected ? new Color(0xF0F4FF) : Color.WHITE);
            wrapper.add(badge);
            return wrapper;
        }
    }

    @Override
    protected String getModuleCode() {
        return com.tuyensinh.util.Constants.QUYEN_PHAN_QUYEN; 
    }
}