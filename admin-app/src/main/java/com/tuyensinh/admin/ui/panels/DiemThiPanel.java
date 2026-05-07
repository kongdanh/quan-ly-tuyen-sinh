package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.components.*;
import com.tuyensinh.admin.ui.dialog.DiemThiFormDialog;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.dao.DiemThiXetTuyenDAO;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.service.ImportService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Panel Quản lý Điểm Thi – thiết kế Master-Detail.
 *
 * Giải pháp UX:
 *  1. Cột động theo phương thức (THPT / VSAT / DGNL / Tất cả).
 *  2. Tiêu đề cột tiếng Việt rõ nghĩa.
 *  3. Cột điểm căn phải, format 2 chữ số thập phân.
 *  4. Panel chi tiết bên dưới tự cập nhật khi chọn dòng.
 */
public class DiemThiPanel extends JPanel {

    // ----------------------------------------------------------------
    // Mapping: key nội bộ -> tiêu đề tiếng Việt
    // ----------------------------------------------------------------
    private static final Map<String, String> VI_LABEL = new LinkedHashMap<>();
    static {
        VI_LABEL.put("ID",          "ID");
        VI_LABEL.put("CCCD",        "CCCD");
        VI_LABEL.put("SBD",         "Số báo danh");
        VI_LABEL.put("PT",          "Phương thức");
        VI_LABEL.put("TO",          "Toán");
        VI_LABEL.put("LI",          "Vật lý");
        VI_LABEL.put("HO",          "Hóa học");
        VI_LABEL.put("SI",          "Sinh học");
        VI_LABEL.put("SU",          "Lịch sử");
        VI_LABEL.put("DI",          "Địa lý");
        VI_LABEL.put("VA",          "Ngữ văn");
        VI_LABEL.put("N1_THI",      "Ngoại ngữ (Thi)");
        VI_LABEL.put("N1_CC",       "Ngoại ngữ (CC)");
        VI_LABEL.put("CNCN",        "CN Công nghệ");
        VI_LABEL.put("CNNN",        "CN Nông nghiệp");
        VI_LABEL.put("TI",          "Tin học");
        VI_LABEL.put("KTPL",        "Kinh tế Pháp luật");
        VI_LABEL.put("NL1",         "Đánh giá NL");
        VI_LABEL.put("NK1",         "Năng khiếu 1");
        VI_LABEL.put("NK2",         "Năng khiếu 2");
        VI_LABEL.put("ACTION",      "Thao tác");
    }

    // ----------------------------------------------------------------
    // Cột hiển thị theo chế độ lọc
    // ----------------------------------------------------------------
    // Cột chung (luôn hiển thị)
    private static final String[] COLS_BASE = {"ID", "CCCD", "SBD", "PT"};

    // Cột điểm theo từng phương thức
    private static final String[] COLS_THPT  = {"TO","LI","HO","SI","SU","DI","VA","N1_THI","N1_CC","KTPL","TI"};
    private static final String[] COLS_DGNL  = {"NL1","TI","KTPL","CNCN","CNNN"};
    private static final String[] COLS_VSAT  = {"NK1","NK2","N1_THI","N1_CC","TI"};
    private static final String[] COLS_ALL   = {"TO","VA","N1_THI","NL1","NK1"};

    // ----------------------------------------------------------------
    // State
    // ----------------------------------------------------------------
    private final DiemThiXetTuyenDAO diemDAO = new DiemThiXetTuyenDAO();

    private List<DiemThiXetTuyen> currentPage = new ArrayList<>();
    private String currentFilter  = "Tất cả";   // "Tất cả" | "THPT" | "VSAT" | "DGNL"
    private String currentKeyword = "";
    private int    page           = 1;
    private int    pageSize       = UIConstants.PAGE_SIZE;

    // ----------------------------------------------------------------
    // UI – Master
    // ----------------------------------------------------------------
    private final DefaultTableModel tableModel = new DefaultTableModel() {
        @Override public boolean isCellEditable(int r, int c) {
            return c == getColumnCount() - 1;
        }
    };
    private final CustomTable   table        = new CustomTable();
    private final PaginationPanel pagination;
    private final TableToolbar  toolbar      = new TableToolbar("Danh sách điểm thi 2026");

    // ----------------------------------------------------------------
    // UI – Detail panel
    // ----------------------------------------------------------------
    private final Map<String, JLabel> detailValues = new LinkedHashMap<>();
    private final JLabel              detailTitle   = new JLabel("Chọn một thí sinh để xem chi tiết điểm");

    // ----------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------
    public DiemThiPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBackground(Color.decode(UIConstants.DASH_CONTENT_BG));
        setBorder(new EmptyBorder(
            UIConstants.SECTION_GAP,
            UIConstants.SECTION_GAP + 10,
            UIConstants.SECTION_GAP,
            UIConstants.SECTION_GAP + 10
        ));

        add(new HeaderPanel("Điểm thi", "Quản lý bảng điểm xét tuyển của thí sinh"), BorderLayout.NORTH);

        // Card chứa toàn bộ nội dung
        RoundPanel card = new RoundPanel(UIConstants.DASH_CARD_RADIUS, true);
        card.setLayout(new BorderLayout());

        // Toolbar
        setupToolbar();
        card.add(toolbar, BorderLayout.NORTH);

        // Split: table (trên) + detail (dưới)
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.68);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setOpaque(false);

        // --- Master pane ---
        JPanel masterPane = new JPanel(new BorderLayout());
        masterPane.setOpaque(false);

        setupTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0,0,1,0,
                Color.decode(UIConstants.COLOR_BORDER)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        masterPane.add(scrollPane, BorderLayout.CENTER);

        pagination = new PaginationPanel((newPage, newSize) -> {
            page     = newPage;
            pageSize = newSize;
            loadData();
        });
        masterPane.add(pagination, BorderLayout.SOUTH);
        split.setTopComponent(masterPane);

        // --- Detail pane ---
        split.setBottomComponent(buildDetailPane());
        card.add(split, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        // Load lần đầu
        applyMode("Tất cả");
        loadData();
    }

    // ================================================================
    // TOOLBAR SETUP
    // ================================================================

    private void setupToolbar() {
        toolbar.addRealtimeSearchListener(() -> {
            currentKeyword = toolbar.getSearchField().getText().trim();
            page = 1;
            loadData();
        });

        if (toolbar.getBtnAdd() != null) {
            toolbar.getBtnAdd().addActionListener(e -> showAddDialog());
        }

        // Filter theo phương thức – đổi cột hiển thị và reload data
        toolbar.addDynamicFilterCategory(
            "Theo phương thức", 3,
            Arrays.asList("Tất cả", "THPT", "VSAT", "DGNL"),
            (col, val) -> {
                applyMode(val);   // đổi cột
                page = 1;         // reset về trang đầu
                loadData();       // reload data theo filter mới
            }
        );

        toolbar.getBtnImport().addActionListener(e -> handleImportExcel());
    }


    // ================================================================
    // TABLE SETUP
    // ================================================================

    private void setupTable() {
        table.setModel(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Selection listener → cập nhật detail panel
        table.getSelectionModel().addListSelectionListener(this::onRowSelected);
    }

    /**
     * Rebuild cột của bảng theo chế độ lọc hiện tại.
     */
    private void applyMode(String mode) {
        currentFilter = mode;

        // Xác định cột điểm cần hiển thị
        String[] scoreCols = switch (mode) {
            case "THPT"   -> COLS_THPT;
            case "DGNL"   -> COLS_DGNL;
            case "VSAT"   -> COLS_VSAT;
            default       -> COLS_ALL;
        };

        // Gộp cột
        List<String> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(COLS_BASE));
        keys.addAll(Arrays.asList(scoreCols));
        keys.add("ACTION");

        // Rebuild model
        String[] headers = keys.stream()
            .map(k -> VI_LABEL.getOrDefault(k, k))
            .toArray(String[]::new);

        tableModel.setColumnIdentifiers(headers);
        tableModel.setRowCount(0);

        // Cấu hình renderer + width
        configureDynamicColumns(keys);

        // Gắn lại action cell renderer/editor (cột cuối)
        int actionCol = keys.size() - 1;
        TableActionCell.TableActionEvent event = new TableActionCell.TableActionEvent() {
            @Override public void onEdit(int row)   { showEditDialog(row); }
            @Override public void onDelete(int row) { deleteRecord(row); }
        };
        table.getColumnModel().getColumn(actionCol)
             .setCellRenderer(new TableActionCell.Renderer());
        table.getColumnModel().getColumn(actionCol)
             .setCellEditor(new TableActionCell.Editor(event));

        // Tag mỗi cột với key để dùng khi fill row
        for (int i = 0; i < keys.size(); i++) {
            table.getColumnModel().getColumn(i).setIdentifier(keys.get(i));
        }
    }

    private void configureDynamicColumns(List<String> keys) {
        // Tự động giãn cột nếu số lượng ít (để không bị trắng 1 bên), cuộn ngang nếu nhiều cột
        if (keys.size() <= 10) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }

        // Right-aligned renderer cho cột điểm
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIConstants.TABLE_CELL_PADDING_X));
                return this;
            }
        };

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Renderers cho Header để khớp với dữ liệu
        TableCellRenderer defaultHeaderRenderer = table.getTableHeader().getDefaultRenderer();
        TableCellRenderer rightHeaderRenderer = (tbl, val, sel, focus, row, col) -> {
            Component c = defaultHeaderRenderer.getTableCellRendererComponent(tbl, val, sel, focus, row, col);
            if (c instanceof JLabel) {
                ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIConstants.TABLE_CELL_PADDING_X));
            }
            return c;
        };
        TableCellRenderer centerHeaderRenderer = (tbl, val, sel, focus, row, col) -> {
            Component c = defaultHeaderRenderer.getTableCellRendererComponent(tbl, val, sel, focus, row, col);
            if (c instanceof JLabel) {
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            }
            return c;
        };

        Set<String> scoreKeys = new HashSet<>(Arrays.asList(
            "TO","LI","HO","SI","SU","DI","VA","N1_THI","N1_CC",
            "CNCN","CNNN","TI","KTPL","NL1","NK1","NK2"
        ));

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            TableColumn col = table.getColumnModel().getColumn(i);

            if ("ACTION".equals(key)) {
                col.setPreferredWidth(UIConstants.COL_ACTION_WIDTH);
                col.setHeaderRenderer(centerHeaderRenderer);
            } else if ("ID".equals(key)) {
                col.setPreferredWidth(50);
                col.setCellRenderer(centerRenderer);
                col.setHeaderRenderer(centerHeaderRenderer);
            } else if ("CCCD".equals(key)) {
                col.setPreferredWidth(125);
                col.setCellRenderer(centerRenderer);
                col.setHeaderRenderer(centerHeaderRenderer);
            } else if ("SBD".equals(key)) {
                col.setPreferredWidth(100);
                col.setCellRenderer(centerRenderer);
                col.setHeaderRenderer(centerHeaderRenderer);
            } else if ("PT".equals(key)) {
                col.setPreferredWidth(100);
                col.setCellRenderer(centerRenderer);
                col.setHeaderRenderer(centerHeaderRenderer);
            } else if (scoreKeys.contains(key)) {
                col.setPreferredWidth(110); // Đủ rộng cho "Ngoại ngữ (Thi)"
                col.setCellRenderer(rightRenderer);
                col.setHeaderRenderer(rightHeaderRenderer);
            } else {
                col.setPreferredWidth(100);
            }
        }
    }

    // ================================================================
    // DATA LOADING
    // ================================================================

    private void loadData() {
        pagination.setEnabled(false);

        // activeFilters
        Map<String, Object> filters = new HashMap<>();
        if (!currentFilter.equals("Tất cả")) {
            filters.put("dPhuongthuc", currentFilter);
        }

        CompletableFuture<List<DiemThiXetTuyen>> dataFut =
            diemDAO.findPageForAdmin(currentKeyword, filters, page, pageSize);
        CompletableFuture<Long> countFut =
            diemDAO.countForAdmin(currentKeyword, filters);

        CompletableFuture.allOf(dataFut, countFut).thenAccept(v -> {
            try {
                List<DiemThiXetTuyen> list = dataFut.get();
                long total = countFut.get();
                SwingUtilities.invokeLater(() -> {
                    currentPage = list;
                    fillTable(list);
                    pagination.updatePagination((int) total, page);
                    pagination.setEnabled(true);
                    clearDetail();
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> pagination.setEnabled(true));
            }
        });
    }

    private void fillTable(List<DiemThiXetTuyen> list) {
        tableModel.setRowCount(0);
        for (DiemThiXetTuyen d : list) {
            tableModel.addRow(buildRow(d));
        }
    }

    /**
     * Xây dựng dòng dữ liệu dựa trên các cột đang active.
     */
    private Object[] buildRow(DiemThiXetTuyen d) {
        int colCount = tableModel.getColumnCount();
        Object[] row = new Object[colCount];
        for (int i = 0; i < colCount; i++) {
            String key = columnKey(i);
            row[i] = cellValue(d, key);
        }
        return row;
    }

    private String columnKey(int colIndex) {
        Object id = table.getColumnModel().getColumn(colIndex).getIdentifier();
        return id != null ? id.toString() : "";
    }

    private Object cellValue(DiemThiXetTuyen d, String key) {
        return switch (key) {
            case "ID"     -> d.getId();
            case "CCCD"   -> d.getThiSinh() != null ? d.getThiSinh().getCccd() : nvl(d.getCccd());
            case "SBD"    -> nvl(d.getSobaodanh());
            case "PT"     -> nvl(DiemThiXetTuyenDAO.normalizeMethod(d.getDPhuongthuc()));
            case "TO"     -> fmt(d.getTo());
            case "LI"     -> fmt(d.getLi());
            case "HO"     -> fmt(d.getHo());
            case "SI"     -> fmt(d.getSi());
            case "SU"     -> fmt(d.getSu());
            case "DI"     -> fmt(d.getDi());
            case "VA"     -> fmt(d.getVa());
            case "N1_THI" -> fmt(d.getN1Thi());
            case "N1_CC"  -> fmt(d.getN1Cc());
            case "CNCN"   -> fmt(d.getCncn());
            case "CNNN"   -> fmt(d.getCnnn());
            case "TI"     -> fmt(d.getTi());
            case "KTPL"   -> fmt(d.getKtpl());
            case "NL1"    -> fmt(d.getNl1());
            case "NK1"    -> fmt(d.getNk1());
            case "NK2"    -> fmt(d.getNk2());
            case "ACTION" -> "";
            default       -> "";
        };
    }

    // ================================================================
    // DETAIL PANEL
    // ================================================================

    private JPanel buildDetailPane() {
        JPanel pane = new JPanel(new BorderLayout());
        pane.setBackground(Color.decode(UIConstants.DASH_CONTENT_BG));
        pane.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, Color.decode(UIConstants.COLOR_BORDER)),
            new EmptyBorder(10, 14, 10, 14)
        ));

        // Title row
        detailTitle.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 13f));
        detailTitle.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));
        pane.add(detailTitle, BorderLayout.NORTH);

        // Grid of score fields
        JPanel grid = new JPanel(new GridLayout(0, 4, 10, 6));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Tất cả các môn cần hiển thị trong detail
        String[] allScoreKeys = {
            "TO","LI","HO","SI","SU","DI","VA",
            "N1_THI","N1_CC","CNCN","CNNN","TI","KTPL","NL1","NK1","NK2"
        };

        for (String key : allScoreKeys) {
            String label = VI_LABEL.getOrDefault(key, key);
            JPanel cell = new JPanel(new BorderLayout(4, 0));
            cell.setOpaque(false);

            JLabel lbl = new JLabel(label + ":");
            lbl.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 11f));
            lbl.setForeground(Color.decode(UIConstants.COLOR_TEXT_MUTED));
            lbl.setPreferredSize(new Dimension(130, 22));

            JLabel val = new JLabel("—");
            val.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 12f));
            val.setForeground(Color.decode(UIConstants.COLOR_TEXT));
            val.setHorizontalAlignment(SwingConstants.RIGHT);

            detailValues.put(key, val);
            cell.add(lbl, BorderLayout.WEST);
            cell.add(val, BorderLayout.CENTER);
            grid.add(cell);
        }

        JScrollPane detailScroll = new JScrollPane(grid);
        detailScroll.setBorder(null);
        detailScroll.getViewport().setOpaque(false);
        detailScroll.setOpaque(false);
        pane.add(detailScroll, BorderLayout.CENTER);

        return pane;
    }

    private void onRowSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentPage.size()) {
            clearDetail();
            return;
        }
        DiemThiXetTuyen d = currentPage.get(row);
        updateDetail(d);
    }

    private void updateDetail(DiemThiXetTuyen d) {
        String cccd = d.getThiSinh() != null ? d.getThiSinh().getCccd() : nvl(d.getCccd());
        String pt   = nvl(DiemThiXetTuyenDAO.normalizeMethod(d.getDPhuongthuc()));
        detailTitle.setText("Chi tiết: " + cccd + "  |  Phương thức: " + pt);
        detailTitle.setForeground(Color.decode(UIConstants.DASH_TEXT_DARK));

        detailValues.get("TO")    .setText(fmt(d.getTo()));
        detailValues.get("LI")    .setText(fmt(d.getLi()));
        detailValues.get("HO")    .setText(fmt(d.getHo()));
        detailValues.get("SI")    .setText(fmt(d.getSi()));
        detailValues.get("SU")    .setText(fmt(d.getSu()));
        detailValues.get("DI")    .setText(fmt(d.getDi()));
        detailValues.get("VA")    .setText(fmt(d.getVa()));
        detailValues.get("N1_THI").setText(fmt(d.getN1Thi()));
        detailValues.get("N1_CC") .setText(fmt(d.getN1Cc()));
        detailValues.get("CNCN")  .setText(fmt(d.getCncn()));
        detailValues.get("CNNN")  .setText(fmt(d.getCnnn()));
        detailValues.get("TI")    .setText(fmt(d.getTi()));
        detailValues.get("KTPL")  .setText(fmt(d.getKtpl()));
        detailValues.get("NL1")   .setText(fmt(d.getNl1()));
        detailValues.get("NK1")   .setText(fmt(d.getNk1()));
        detailValues.get("NK2")   .setText(fmt(d.getNk2()));

        // Tô màu cho điểm ĐGNL (thang khác)
        colorScore(detailValues.get("NL1"), d.getNl1(), new BigDecimal("1200"), true);
        colorScore(detailValues.get("TO"),  d.getTo(),  new BigDecimal("10"),   false);
        colorScore(detailValues.get("VA"),  d.getVa(),  new BigDecimal("10"),   false);
    }

    private void colorScore(JLabel lbl, BigDecimal val, BigDecimal max, boolean isNl) {
        if (val == null) { lbl.setForeground(Color.decode(UIConstants.COLOR_TEXT_MUTED)); return; }
        double ratio = val.doubleValue() / max.doubleValue();
        if (ratio >= 0.8)      lbl.setForeground(Color.decode(UIConstants.COLOR_SUCCESS));
        else if (ratio >= 0.5) lbl.setForeground(Color.decode(UIConstants.COLOR_TEXT));
        else                   lbl.setForeground(Color.decode(UIConstants.COLOR_DANGER));
    }

    private void clearDetail() {
        detailTitle.setText("Chọn một thí sinh để xem chi tiết điểm");
        detailTitle.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));
        detailValues.values().forEach(lbl -> {
            lbl.setText("—");
            lbl.setForeground(Color.decode(UIConstants.COLOR_TEXT));
        });
    }

    // ================================================================
    // CRUD ACTIONS
    // ================================================================

    private void showAddDialog() {
        DiemThiFormDialog dlg = new DiemThiFormDialog(getParentFrame(), new DiemThiXetTuyen(), true);
        dlg.setVisible(true);
        if (dlg.isSaved()) { loadData(); showSuccess("Thêm điểm thi thành công!"); }
    }

    private void showEditDialog(int row) {
        Object idObj = tableModel.getValueAt(row, 0);
        if (idObj == null) return;
        int id = (int) idObj;
        diemDAO.findByIdForAdmin(id).thenAccept(diem -> SwingUtilities.invokeLater(() -> {
            if (diem == null) { showError("Không tìm thấy bản ghi."); return; }
            DiemThiFormDialog dlg = new DiemThiFormDialog(getParentFrame(), diem, false);
            dlg.setVisible(true);
            if (dlg.isSaved()) { loadData(); showSuccess("Cập nhật điểm thi thành công!"); }
        }));
    }

    private void deleteRecord(int row) {
        Object cccdObj = tableModel.getValueAt(row, 1);
        String cccd = cccdObj != null ? cccdObj.toString() : "?";
        if (!confirmDelete("Điểm thi CCCD " + cccd)) return;
        Object idObj = tableModel.getValueAt(row, 0);
        if (idObj == null) return;
        int id = (int) idObj;
        diemDAO.deleteByIdAsync(id).thenAccept(ok -> SwingUtilities.invokeLater(() -> {
            if (ok) { loadData(); showSuccess("Đã xóa bản ghi của " + cccd); }
            else      showError("Không thể xóa bản ghi.");
        }));
    }

    // ================================================================
    // IMPORT EXCEL
    // ================================================================

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
                return new ImportService().importDiemThi(file);
            }
            @Override protected void done() {
                toolbar.getBtnImport().setEnabled(true);
                toolbar.getBtnImport().setText("Nhập Excel");
                try {
                    List<String> errors = get();
                    if (errors.isEmpty()) { loadData(); showSuccess("Import điểm thi thành công!"); }
                    else showImportErrors(errors);
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
        sp.setPreferredSize(new Dimension(560, 260));
        JOptionPane.showMessageDialog(this, sp, "Import có " + errors.size() + " lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private String fmt(BigDecimal v) {
        return v == null ? "—" : String.format("%.2f", v);
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private Frame getParentFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    private boolean confirmDelete(String name) {
        return JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn xóa: " + name + "?", "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

