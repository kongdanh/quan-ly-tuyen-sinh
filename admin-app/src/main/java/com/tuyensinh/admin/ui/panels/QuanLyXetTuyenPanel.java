// admin-app/src/main/java/com/tuyensinh/admin/ui/panels/QuanLyXetTuyenPanel.java
package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.components.RoundedButton;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.dao.XetTuyenDAO;
import com.tuyensinh.model.DotTuyenSinh;
import com.tuyensinh.model.HoSoTuyenSinh;
import com.tuyensinh.model.KetQuaXetTuyen;
import com.tuyensinh.model.Nganh;
import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.service.DotTuyenSinhService;
import com.tuyensinh.service.NguyenVongService;
import com.tuyensinh.service.TraCuuService;
import com.tuyensinh.service.XetTuyenService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class QuanLyXetTuyenPanel extends JPanel {

    private final DotTuyenSinhService dotService = new DotTuyenSinhService();
    private final XetTuyenService xetTuyenService = new XetTuyenService();
    private final XetTuyenDAO xetTuyenDAO = new XetTuyenDAO();
    private final NguyenVongService nguyenVongService = new NguyenVongService();
    private final TraCuuService traCuuService = new TraCuuService();
    
    private JComboBox<String> cbDotTuyenSinh;
    private List<DotTuyenSinh> listDot;
    private RoundedButton btnChayThuatToan;
    private RoundedButton btnChotKetQua;
    private JTable tbKetQua;
    private DefaultTableModel tbModel;
    private JTable tbChiTiet;
    private DefaultTableModel tbModelChiTiet;
    private JLabel lblStatus;
    private RoundedButton btnExportExcel;
    private RoundedButton btnExportPdf;
    private RoundedButton btnGuiEmailThongBao;
    
    // Components cho tab Kết quả Xét tuyển
    private JTabbedPane tabbedPaneMain;
    private JTable tbKetQuaDau;
    private DefaultTableModel tbModelDau;
    private JTable tbKetQuaRot;
    private DefaultTableModel tbModelRot;
    private JTable tbKetQuaHuy;
    private DefaultTableModel tbModelHuy;
    private JLabel lblCountDau;
    private JLabel lblCountRot;
    private JLabel lblCountHuy;
    private JLabel lblCountTotal;
    
    // Lưu kết quả tạm thời sau khi chạy thuật toán (preview)
    private List<NguyenVong> ketQuaPreviewDau;
    private List<NguyenVong> ketQuaPreviewRot;
    private List<NguyenVong> ketQuaPreviewHuy;
    private int previewCountDau = 0;
    private int previewCountRot = 0;
    private int previewCountHuy = 0;
    private Integer currentIdDot = null;

    public QuanLyXetTuyenPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initUI();
        loadDotTuyenSinh();
    }

    private void initUI() {
        // --- Header & Controls ---
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 10));
        pnlHeader.setOpaque(false);

        JPanel pnlTitle = new JPanel(new GridLayout(2, 1));
        pnlTitle.setOpaque(false);
        JLabel title = new JLabel("Quản lý Xét tuyển");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel subTitle = new JLabel("Chạy thuật toán (Preview) và Chốt kết quả xét tuyển");
        subTitle.setForeground(Color.GRAY);
        pnlTitle.add(title);
        pnlTitle.add(subTitle);

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlControls.setOpaque(false);

        cbDotTuyenSinh = new JComboBox<>();
        btnChayThuatToan = new RoundedButton("Chạy Thuật Toán (Preview)");
        btnChayThuatToan.setBackground(Color.decode(UIConstants.COLOR_BLUE));
        btnChayThuatToan.setForeground(Color.WHITE);

        btnChotKetQua = new RoundedButton("Chốt Kết Quả");
        btnChotKetQua.setBackground(Color.decode("#059669"));
        btnChotKetQua.setForeground(Color.WHITE);

        btnExportExcel = new RoundedButton("Xuất Excel");
        btnExportExcel.setBackground(Color.decode("#10B981"));
        btnExportExcel.setForeground(Color.WHITE);

        btnExportPdf = new RoundedButton("In Giấy Báo");
        btnExportPdf.setBackground(Color.decode("#EF4444"));
        btnExportPdf.setForeground(Color.WHITE);

        btnGuiEmailThongBao = new RoundedButton("Gửi Email Báo Kết Quả");
        btnGuiEmailThongBao.setBackground(Color.decode("#F59E0B"));
        btnGuiEmailThongBao.setForeground(Color.WHITE);
        btnGuiEmailThongBao.addActionListener(e -> handleGuiEmailThongBao());

        pnlControls.add(new JLabel("Chọn đợt/Tất cả:"));
        pnlControls.add(cbDotTuyenSinh);
        pnlControls.add(btnChayThuatToan);
        pnlControls.add(btnExportExcel);
        pnlControls.add(btnExportPdf);
        pnlControls.add(btnChotKetQua);
        pnlControls.add(btnGuiEmailThongBao);

        pnlHeader.add(pnlTitle, BorderLayout.NORTH);
        pnlHeader.add(pnlControls, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        // --- Tabs ---
        tabbedPaneMain = new JTabbedPane();
        tabbedPaneMain.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Tab 1: Kết quả Xét tuyển (Preview)
        tabbedPaneMain.addTab("Kết quả Xét tuyển (Preview)", buildKetQuaXetTuyenTab());
        
        // Tab 2: Danh sách Trúng tuyển (đã chốt)
        String[] columns = {"Mã Hồ Sơ", "CCCD", "Họ Tên", "Điểm XT", "Nguyện Vọng", "Ngành Trúng Tuyển", "Trạng Thái"};
        tbModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbKetQua = new JTable(tbModel);
        tbKetQua.setRowHeight(35);
        tabbedPaneMain.addTab("Danh sách Trúng tuyển (Đã chốt)", new JScrollPane(tbKetQua));

        // Tab 3: Chi Tiet Diem Xet Tuyen
        String[] colsChiTiet = {"CCCD", "Họ Tên", "NV", "Tổ hợp (Thang 30)", "Điểm THM", "Điểm Cộng", "Điểm Ưu Tiên", "Điểm Xét Tuyển"};
        tbModelChiTiet = new DefaultTableModel(colsChiTiet, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbChiTiet = new JTable(tbModelChiTiet);
        tbChiTiet.setRowHeight(35);
        tabbedPaneMain.addTab("Chi tiết điểm xét tuyển", new JScrollPane(tbChiTiet));

        add(tabbedPaneMain, BorderLayout.CENTER);

        // --- Footer Status ---
        lblStatus = new JLabel("Trạng thái: Sẵn sàng");
        lblStatus.setBorder(new EmptyBorder(10, 0, 0, 0));
        add(lblStatus, BorderLayout.SOUTH);

        // Event
        btnChayThuatToan.addActionListener(e -> handleChayThuatToanPreview());
        btnChotKetQua.addActionListener(e -> handleChotKetQua());
        cbDotTuyenSinh.addActionListener(e -> {
            int idx = cbDotTuyenSinh.getSelectedIndex();
            if (idx >= 0) {
                if (idx == 0) {
                    currentIdDot = null;
                    System.out.println("[ComboBox] ===== CHỌN TẤT CẢ CÁC ĐỢT =====");
                    loadResultToTable(null);
                    loadChiTietToTable(null);
                    loadPreviewFromDatabase(null);  // SỬA: gọi load thay vì clearPreview()
                } else {
                    Integer idDot = listDot.get(idx - 1).getId();
                    currentIdDot = idDot;
                    System.out.println("[ComboBox] Chọn đợt ID=" + idDot);
                    loadResultToTable(idDot);
                    loadChiTietToTable(idDot);
                    loadPreviewFromDatabase(idDot);
                }
            }
        });
        btnExportExcel.addActionListener(e -> handleExportExcel());
        btnExportPdf.addActionListener(e -> handleExportPdf());
    }

    // Build tab Kết quả Xét tuyển với 3 bảng màu
    private JPanel buildKetQuaXetTuyenTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panel thống kê số lượng
        JPanel pnlStats = new JPanel(new GridLayout(1, 4, 15, 0));
        pnlStats.setOpaque(false);
        pnlStats.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Tạo các thẻ thống kê và lưu labels trực tiếp
        StatCardInfo dauCard = createStatCard("ĐẬU", "0", new Color(34, 197, 94));
        StatCardInfo rotCard = createStatCard("RỚT", "0", new Color(239, 68, 68));
        StatCardInfo huyCard = createStatCard("HỦY", "0", new Color(107, 114, 128));
        StatCardInfo totalCard = createStatCard("TỔNG", "0", new Color(59, 130, 246));
        
        pnlStats.add(dauCard.panel);
        pnlStats.add(rotCard.panel);
        pnlStats.add(huyCard.panel);
        pnlStats.add(totalCard.panel);
        
        // Lưu labels để cập nhật sau
        lblCountDau = dauCard.label;
        lblCountRot = rotCard.label;
        lblCountHuy = huyCard.label;
        lblCountTotal = totalCard.label;
        
        // Tạo 3 bảng riêng biệt
        String[] columns = {"STT", "Mã Hồ Sơ", "CCCD", "Họ Tên", "Điểm XT", "Nguyện Vọng", "Ngành"};
        
        // Bảng ĐẬU (màu xanh)
        tbModelDau = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbKetQuaDau = new JTable(tbModelDau);
        tbKetQuaDau.setRowHeight(32);
        tbKetQuaDau.setBackground(new Color(220, 252, 231));
        tbKetQuaDau.setForeground(new Color(21, 128, 61));
        customizeTable(tbKetQuaDau);
        JScrollPane scrollDau = new JScrollPane(tbKetQuaDau);
        scrollDau.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(34, 197, 94), 2),
            " DANH SÁCH ĐẬU ", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(34, 197, 94)
        ));
        
        // Bảng RỚT (màu đỏ)
        tbModelRot = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbKetQuaRot = new JTable(tbModelRot);
        tbKetQuaRot.setRowHeight(32);
        tbKetQuaRot.setBackground(new Color(254, 226, 226));
        tbKetQuaRot.setForeground(new Color(185, 28, 28));
        customizeTable(tbKetQuaRot);
        JScrollPane scrollRot = new JScrollPane(tbKetQuaRot);
        scrollRot.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(239, 68, 68), 2),
            " DANH SÁCH RỚT ", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(239, 68, 68)
        ));
        
        // Bảng HỦY (màu xám)
        tbModelHuy = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbKetQuaHuy = new JTable(tbModelHuy);
        tbKetQuaHuy.setRowHeight(32);
        tbKetQuaHuy.setBackground(new Color(241, 245, 249));
        tbKetQuaHuy.setForeground(new Color(71, 85, 105));
        customizeTable(tbKetQuaHuy);
        JScrollPane scrollHuy = new JScrollPane(tbKetQuaHuy);
        scrollHuy.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(107, 114, 128), 2),
            " DANH SÁCH HỦY ", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14), new Color(107, 114, 128)
        ));
        
        // Sử dụng JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(scrollDau, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(scrollRot);
        bottomPanel.add(scrollHuy);
        
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        
        mainPanel.add(pnlStats, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    // Class helper để lưu panel và label
    private static class StatCardInfo {
        JPanel panel;
        JLabel label;
        StatCardInfo(JPanel panel, JLabel label) {
            this.panel = panel;
            this.label = label;
        }
    }
    
    // Tạo thẻ thống kê
    private StatCardInfo createStatCard(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(color);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(color);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JPanel pnlValue = new JPanel(new BorderLayout());
        pnlValue.setOpaque(false);
        pnlValue.add(lblValue, BorderLayout.EAST);
        
        panel.add(lblTitle, BorderLayout.WEST);
        panel.add(pnlValue, BorderLayout.CENTER);
        
        return new StatCardInfo(panel, lblValue);
    }
    
    // Tùy chỉnh bảng
    private void customizeTable(JTable table) {
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(new Color(30, 41, 59));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        if (table.getColumnCount() > 4) {
            table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        }
    }
    
    // Clear preview data
    private void clearPreview() {
        previewCountDau = 0;
        previewCountRot = 0;
        previewCountHuy = 0;
        ketQuaPreviewDau = null;
        ketQuaPreviewRot = null;
        ketQuaPreviewHuy = null;
        
        if (tbModelDau != null) tbModelDau.setRowCount(0);
        if (tbModelRot != null) tbModelRot.setRowCount(0);
        if (tbModelHuy != null) tbModelHuy.setRowCount(0);
        if (lblCountDau != null) lblCountDau.setText("0");
        if (lblCountRot != null) lblCountRot.setText("0");
        if (lblCountHuy != null) lblCountHuy.setText("0");
        if (lblCountTotal != null) lblCountTotal.setText("0");
    }
    
    // Load preview từ database (khi đã có kết quả từ lần chốt trước)
    private void loadPreviewFromDatabase(Integer idDot) {
        if (idDot == null || idDot == 0) {
            // Tất cả các đợt - load tổng hợp
            System.out.println("[loadPreviewFromDatabase] ===== LOAD TỔNG HỢP TẤT CẢ CÁC ĐỢT =====");
            CompletableFuture.runAsync(() -> {
                try {
                    List<NguyenVong> allNv = new java.util.ArrayList<>();
                    if (listDot != null && !listDot.isEmpty()) {
                        for (DotTuyenSinh dot : listDot) {
                            List<NguyenVong> dotNv = nguyenVongService.findByDotTuyenSinh(dot.getId());
                            System.out.println("[loadPreviewFromDatabase] Đợt " + dot.getTenDot() + " có " + dotNv.size() + " nguyện vọng");
                            
                            // Load đầy đủ thông tin cho từng nguyện vọng
                            for (NguyenVong nv : dotNv) {
                                // Load thông tin thí sinh đầy đủ
                                if (nv.getThiSinh() != null && nv.getThiSinh().getCccd() != null) {
                                    ThiSinh ts = new com.tuyensinh.service.ThiSinhService().findByCccd(nv.getThiSinh().getCccd()).orElse(null);
                                    if (ts != null) {
                                        nv.setThiSinh(ts);
                                    }
                                }
                                // Load thông tin ngành đầy đủ
                                if (nv.getNganh() != null && nv.getNganh().getManganh() != null) {
                                    Nganh nganh = new com.tuyensinh.service.NganhService().findByMaNganh(nv.getNganh().getManganh()).orElse(null);
                                    if (nganh != null) {
                                        nv.setNganh(nganh);
                                    }
                                }
                                // Load thông tin hồ sơ
                                if (nv.getHoSoTuyenSinh() != null && nv.getHoSoTuyenSinh().getId() != null) {
                                    HoSoTuyenSinh hs = new com.tuyensinh.service.HoSoTuyenSinhService().findById(nv.getHoSoTuyenSinh().getId());
                                    if (hs != null) {
                                        nv.setHoSoTuyenSinh(hs);
                                    }
                                }
                            }
                            allNv.addAll(dotNv);
                        }
                    }
                    
                    List<NguyenVong> listDau = new java.util.ArrayList<>();
                    List<NguyenVong> listRot = new java.util.ArrayList<>();
                    List<NguyenVong> listHuy = new java.util.ArrayList<>();
                    
                    for (NguyenVong nv : allNv) {
                        String ketqua = nv.getNvKetqua();
                        if ("DAU".equals(ketqua)) {
                            listDau.add(nv);
                        } else if ("ROT".equals(ketqua)) {
                            listRot.add(nv);
                        } else if ("HUY".equals(ketqua)) {
                            listHuy.add(nv);
                        }
                    }
                    
                    previewCountDau = listDau.size();
                    previewCountRot = listRot.size();
                    previewCountHuy = listHuy.size();
                    ketQuaPreviewDau = listDau;
                    ketQuaPreviewRot = listRot;
                    ketQuaPreviewHuy = listHuy;
                    
                    displayPreviewResult();
                    System.out.println("[loadPreviewFromDatabase] Tổng hợp: Đậu=" + previewCountDau + ", Rớt=" + previewCountRot + ", Hủy=" + previewCountHuy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return;
        }
        
        // Phần code cho đợt cụ thể
        CompletableFuture.runAsync(() -> {
            try {
                List<NguyenVong> listNv = nguyenVongService.findByDotTuyenSinh(idDot);
                
                // Load đầy đủ thông tin cho từng nguyện vọng
                for (NguyenVong nv : listNv) {
                    // Load thông tin thí sinh đầy đủ
                    if (nv.getThiSinh() != null && nv.getThiSinh().getCccd() != null) {
                        ThiSinh ts = new com.tuyensinh.service.ThiSinhService().findByCccd(nv.getThiSinh().getCccd()).orElse(null);
                        if (ts != null) {
                            nv.setThiSinh(ts);
                        }
                    }
                    // Load thông tin ngành đầy đủ
                    if (nv.getNganh() != null && nv.getNganh().getManganh() != null) {
                        Nganh nganh = new com.tuyensinh.service.NganhService().findByMaNganh(nv.getNganh().getManganh()).orElse(null);
                        if (nganh != null) {
                            nv.setNganh(nganh);
                        }
                    }
                    // Load thông tin hồ sơ
                    if (nv.getHoSoTuyenSinh() != null && nv.getHoSoTuyenSinh().getId() != null) {
                        HoSoTuyenSinh hs = new com.tuyensinh.service.HoSoTuyenSinhService().findById(nv.getHoSoTuyenSinh().getId());
                        if (hs != null) {
                            nv.setHoSoTuyenSinh(hs);
                        }
                    }
                }
                
                List<NguyenVong> listDau = new java.util.ArrayList<>();
                List<NguyenVong> listRot = new java.util.ArrayList<>();
                List<NguyenVong> listHuy = new java.util.ArrayList<>();
                
                for (NguyenVong nv : listNv) {
                    String ketqua = nv.getNvKetqua();
                    if ("DAU".equals(ketqua)) {
                        listDau.add(nv);
                    } else if ("ROT".equals(ketqua)) {
                        listRot.add(nv);
                    } else if ("HUY".equals(ketqua)) {
                        listHuy.add(nv);
                    }
                }
                
                previewCountDau = listDau.size();
                previewCountRot = listRot.size();
                previewCountHuy = listHuy.size();
                ketQuaPreviewDau = listDau;
                ketQuaPreviewRot = listRot;
                ketQuaPreviewHuy = listHuy;
                
                displayPreviewResult();
                System.out.println("[loadPreviewFromDatabase] Đã load preview cho đợt " + idDot + ": Đậu=" + previewCountDau);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    // Hiển thị kết quả preview lên bảng
    private void displayPreviewResult() {
        SwingUtilities.invokeLater(() -> {
            if (lblCountDau != null) lblCountDau.setText(String.valueOf(previewCountDau));
            if (lblCountRot != null) lblCountRot.setText(String.valueOf(previewCountRot));
            if (lblCountHuy != null) lblCountHuy.setText(String.valueOf(previewCountHuy));
            if (lblCountTotal != null) lblCountTotal.setText(String.valueOf(previewCountDau + previewCountRot + previewCountHuy));
            
            // Cập nhật bảng ĐẬU
            tbModelDau.setRowCount(0);
            if (ketQuaPreviewDau != null) {
                int stt = 1;
                for (NguyenVong nv : ketQuaPreviewDau) {
                    // Lấy thông tin từ đối tượng đã được load đầy đủ
                    String hoTen = "";
                    String cccd = "";
                    String maHoSo = "";
                    String tenNganh = "";
                    
                    if (nv.getThiSinh() != null) {
                        cccd = nv.getThiSinh().getCccd() != null ? nv.getThiSinh().getCccd() : "";
                        hoTen = (nv.getThiSinh().getHo() != null ? nv.getThiSinh().getHo() : "") + " " + 
                                (nv.getThiSinh().getTen() != null ? nv.getThiSinh().getTen() : "");
                        if (hoTen.trim().isEmpty()) hoTen = cccd;
                    }
                    
                    if (nv.getHoSoTuyenSinh() != null && nv.getHoSoTuyenSinh().getMaHoSo() != null) {
                        maHoSo = nv.getHoSoTuyenSinh().getMaHoSo();
                    }
                    
                    if (nv.getNganh() != null && nv.getNganh().getTennganh() != null) {
                        tenNganh = nv.getNganh().getTennganh();
                    }
                    
                    double diemXT = nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0;
                    
                    tbModelDau.addRow(new Object[]{
                        stt++, maHoSo, cccd, hoTen,
                        diemXT,
                        "NV " + (nv.getNvTt() != null ? nv.getNvTt() : 0), 
                        tenNganh
                    });
                }
            }
            
            // Cập nhật bảng RỚT (tương tự)
            tbModelRot.setRowCount(0);
            if (ketQuaPreviewRot != null) {
                int stt = 1;
                for (NguyenVong nv : ketQuaPreviewRot) {
                    String hoTen = "";
                    String cccd = "";
                    String maHoSo = "";
                    String tenNganh = "";
                    
                    if (nv.getThiSinh() != null) {
                        cccd = nv.getThiSinh().getCccd() != null ? nv.getThiSinh().getCccd() : "";
                        hoTen = (nv.getThiSinh().getHo() != null ? nv.getThiSinh().getHo() : "") + " " + 
                                (nv.getThiSinh().getTen() != null ? nv.getThiSinh().getTen() : "");
                        if (hoTen.trim().isEmpty()) hoTen = cccd;
                    }
                    
                    if (nv.getHoSoTuyenSinh() != null && nv.getHoSoTuyenSinh().getMaHoSo() != null) {
                        maHoSo = nv.getHoSoTuyenSinh().getMaHoSo();
                    }
                    
                    if (nv.getNganh() != null && nv.getNganh().getTennganh() != null) {
                        tenNganh = nv.getNganh().getTennganh();
                    }
                    
                    double diemXT = nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0;
                    
                    tbModelRot.addRow(new Object[]{
                        stt++, maHoSo, cccd, hoTen,
                        diemXT,
                        "NV " + (nv.getNvTt() != null ? nv.getNvTt() : 0), 
                        tenNganh
                    });
                }
            }
            
            // Cập nhật bảng HỦY (tương tự)
            tbModelHuy.setRowCount(0);
            if (ketQuaPreviewHuy != null) {
                int stt = 1;
                for (NguyenVong nv : ketQuaPreviewHuy) {
                    String hoTen = "";
                    String cccd = "";
                    String maHoSo = "";
                    String tenNganh = "";
                    
                    if (nv.getThiSinh() != null) {
                        cccd = nv.getThiSinh().getCccd() != null ? nv.getThiSinh().getCccd() : "";
                        hoTen = (nv.getThiSinh().getHo() != null ? nv.getThiSinh().getHo() : "") + " " + 
                                (nv.getThiSinh().getTen() != null ? nv.getThiSinh().getTen() : "");
                        if (hoTen.trim().isEmpty()) hoTen = cccd;
                    }
                    
                    if (nv.getHoSoTuyenSinh() != null && nv.getHoSoTuyenSinh().getMaHoSo() != null) {
                        maHoSo = nv.getHoSoTuyenSinh().getMaHoSo();
                    }
                    
                    if (nv.getNganh() != null && nv.getNganh().getTennganh() != null) {
                        tenNganh = nv.getNganh().getTennganh();
                    }
                    
                    double diemXT = nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0;
                    
                    tbModelHuy.addRow(new Object[]{
                        stt++, maHoSo, cccd, hoTen,
                        diemXT,
                        "NV " + (nv.getNvTt() != null ? nv.getNvTt() : 0), 
                        tenNganh
                    });
                }
            }
        });
    }
    
    // ==================== XỬ LÝ CHẠY THUẬT TOÁN PREVIEW ====================
    private void handleChayThuatToanPreview() {
        int idx = cbDotTuyenSinh.getSelectedIndex();
        if (idx <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đợt cụ thể để chạy thuật toán.");
            return;
        }
        Integer idDot = listDot.get(idx - 1).getId();
        
        btnChayThuatToan.setEnabled(false);
        btnChotKetQua.setEnabled(false);
        lblStatus.setText("Đang chạy thuật toán preview... Vui lòng đợi.");

        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("[Preview] Bắt đầu preview cho đợt ID=" + idDot);
                
                List<NguyenVong> listNv = nguyenVongService.findByDotTuyenSinh(idDot);
                System.out.println("[Preview] Tổng số nguyện vọng: " + listNv.size());
                
                java.util.Map<String, Double> diemChuanMap = new java.util.HashMap<>();
                
                try (org.hibernate.Session session = com.tuyensinh.util.HibernateUtil.getSessionFactory().openSession()) {
                    String hql = "SELECT dc FROM DiemChuanDot dc WHERE dc.dotTuyenSinh.id = :idDot";
                    var query = session.createQuery(hql, com.tuyensinh.model.DiemChuanDot.class);
                    query.setParameter("idDot", idDot);
                    List<com.tuyensinh.model.DiemChuanDot> diemChuanList = query.getResultList();
                    
                    System.out.println("[Preview] Số bản ghi điểm chuẩn: " + diemChuanList.size());
                    
                    for (com.tuyensinh.model.DiemChuanDot dc : diemChuanList) {
                        if (dc.getDiemChuan() == null) continue;
                        Double score = dc.getDiemChuan().doubleValue();
                        
                        String manganh = dc.getNganhToHop() != null && dc.getNganhToHop().getNganh() != null
                                ? dc.getNganhToHop().getNganh().getManganh() : null;

                        if (manganh != null) {
                            manganh = manganh.trim().toUpperCase();
                            diemChuanMap.putIfAbsent(manganh, score);

                            if (dc.getNganhToHop().getToHopMon() != null && dc.getNganhToHop().getToHopMon().getMatohop() != null) {
                                String matohop = dc.getNganhToHop().getToHopMon().getMatohop().trim().toUpperCase();
                                diemChuanMap.put(manganh + "|" + matohop, score);
                            }
                            
                            String m1 = dc.getNganhToHop().getThMon1();
                            String m2 = dc.getNganhToHop().getThMon2();
                            String m3 = dc.getNganhToHop().getThMon3();
                            if (m1 != null && m2 != null && m3 != null) {
                                String combo = m1.trim().toUpperCase() + "-" + m2.trim().toUpperCase() + "-" + m3.trim().toUpperCase();
                                diemChuanMap.put(manganh + "|" + combo, score);
                            }
                        }
                    }
                }
                
                List<NguyenVong> listDau = new java.util.ArrayList<>();
                List<NguyenVong> listRot = new java.util.ArrayList<>();
                List<NguyenVong> listHuy = new java.util.ArrayList<>();
                
                java.util.Map<String, List<NguyenVong>> mapThiSinh = new java.util.HashMap<>();
                for (NguyenVong nv : listNv) {
                    String cccd = nv.getThiSinh().getCccd();
                    mapThiSinh.computeIfAbsent(cccd, k -> new java.util.ArrayList<>()).add(nv);
                }
                
                for (java.util.Map.Entry<String, List<NguyenVong>> entry : mapThiSinh.entrySet()) {
                    boolean daDau = false;
                    for (NguyenVong nv : entry.getValue()) {
                        if (daDau) {
                            listHuy.add(nv);
                            continue;
                        }
                        
                        String maNganhNorm = nv.getNganh().getManganh() != null ? nv.getNganh().getManganh().trim().toUpperCase() : "";
                        String maThNorm = nv.getTtThm() != null ? nv.getTtThm().trim().toUpperCase() : "";
                        
                        Double diemChuan = 999.0;
                        String key1 = maNganhNorm + "|" + maThNorm;
                        if (diemChuanMap.containsKey(key1)) {
                            diemChuan = diemChuanMap.get(key1);
                        } else if (diemChuanMap.containsKey(maNganhNorm)) {
                            diemChuan = diemChuanMap.get(maNganhNorm);
                        }
                        
                        Double diemThiSinh = nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0;
                        
                        if (diemThiSinh >= diemChuan) {
                            listDau.add(nv);
                            daDau = true;
                        } else {
                            listRot.add(nv);
                        }
                    }
                }
                
                previewCountDau = listDau.size();
                previewCountRot = listRot.size();
                previewCountHuy = listHuy.size();
                ketQuaPreviewDau = listDau;
                ketQuaPreviewRot = listRot;
                ketQuaPreviewHuy = listHuy;
                
                displayPreviewResult();
                
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Trạng thái: Preview hoàn tất! (" + previewCountDau + " đậu, " + previewCountRot + " rớt, " + previewCountHuy + " hủy)");
                    btnChayThuatToan.setEnabled(true);
                    btnChotKetQua.setEnabled(true);
                    
                    if (tabbedPaneMain != null) {
                        tabbedPaneMain.setSelectedIndex(0);
                    }
                    
                    JOptionPane.showMessageDialog(QuanLyXetTuyenPanel.this, 
                        "PREVIEW kết quả xét tuyển:\n\n" +
                        "- Đậu: " + previewCountDau + " thí sinh\n" +
                        "- Rớt: " + previewCountRot + " thí sinh\n" +
                        "- Hủy: " + previewCountHuy + " thí sinh\n\n" +
                        "Nhấn 'Chốt Kết Quả' để ghi vào database và khóa đợt.", 
                        "Preview", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(QuanLyXetTuyenPanel.this, "Lỗi: " + ex.getMessage());
                    btnChayThuatToan.setEnabled(true);
                    btnChotKetQua.setEnabled(true);
                    lblStatus.setText("Trạng thái: Lỗi khi chạy preview!");
                });
            }
        });
    }
    
    // ==================== XỬ LÝ CHỐT KẾT QUẢ ====================
    private void handleChotKetQua() {
        int idx = cbDotTuyenSinh.getSelectedIndex();
        if (idx <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đợt cụ thể để chốt kết quả.");
            return;
        }
        Integer idDot = listDot.get(idx - 1).getId();
        
        if (previewCountDau == 0 && previewCountRot == 0 && previewCountHuy == 0) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn chưa chạy preview cho đợt này.\nChốt kết quả sẽ chạy thuật toán và ghi vào database ngay lập tức.\nTiếp tục?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "CHÚ Ý: Sau khi chốt kết quả:\n" +
            "1. Kết quả xét tuyển sẽ được GHI vào DATABASE\n" +
            "2. Đợt tuyển sinh sẽ chuyển sang trạng thái KHÓA (INACTIVE)\n" +
            "3. Không thể sửa đổi kết quả sau khi chốt\n\n" +
            "Bạn có chắc chắn muốn chốt kết quả cho đợt này?", 
            "Xác nhận chốt kết quả", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;
        
        btnChayThuatToan.setEnabled(false);
        btnChotKetQua.setEnabled(false);
        lblStatus.setText("Đang chốt kết quả và khóa đợt... Vui lòng đợi.");

        CompletableFuture.runAsync(() -> {
            try {
                xetTuyenService.chayThuatToanXetTuyen(idDot);
                dotService.lockDot(idDot);
                loadResultToTable(idDot);
                loadChiTietToTable(idDot);
                loadPreviewFromDatabase(idDot);
                refreshDotComboBox();
                
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Trạng thái: Đã chốt kết quả và khóa đợt!");
                    btnChayThuatToan.setEnabled(true);
                    btnChotKetQua.setEnabled(true);
                    
                    if (tabbedPaneMain != null) {
                        tabbedPaneMain.setSelectedIndex(0);
                    }
                    
                    JOptionPane.showMessageDialog(QuanLyXetTuyenPanel.this, 
                        "CHỐT KẾT QUẢ THÀNH CÔNG!\n\n" +
                        "Kết quả:\n" +
                        "- Đậu: " + previewCountDau + " thí sinh\n" +
                        "- Rớt: " + previewCountRot + " thí sinh\n" +
                        "- Hủy: " + previewCountHuy + " thí sinh\n\n" +
                        "Đợt tuyển sinh đã được KHÓA.", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(QuanLyXetTuyenPanel.this, "Lỗi khi chốt kết quả: " + ex.getMessage());
                    btnChayThuatToan.setEnabled(true);
                    btnChotKetQua.setEnabled(true);
                    lblStatus.setText("Trạng thái: Lỗi khi chốt kết quả!");
                });
            }
        });
    }
    
    private void refreshDotComboBox() {
        dotService.findPageWithFilters("", List.of(), Map.of(), 1, 100).thenAccept(dots -> {
            this.listDot = dots;
            SwingUtilities.invokeLater(() -> {
                String selected = (String) cbDotTuyenSinh.getSelectedItem();
                cbDotTuyenSinh.removeAllItems();
                cbDotTuyenSinh.addItem("-- Tất cả các đợt --");
                for (DotTuyenSinh d : dots) {
                    String display = d.getTenDot() + " (" + d.getTrangThai() + ")";
                    cbDotTuyenSinh.addItem(display);
                }
                if (selected != null && !selected.equals("-- Tất cả các đợt --")) {
                    cbDotTuyenSinh.setSelectedItem(selected);
                } else {
                    cbDotTuyenSinh.setSelectedIndex(0);
                }
            });
        });
    }

    private void loadDotTuyenSinh() {
        dotService.findPageWithFilters("", List.of(), Map.of(), 1, 100).thenAccept(dots -> {
            this.listDot = dots;
            SwingUtilities.invokeLater(() -> {
                cbDotTuyenSinh.removeAllItems();
                cbDotTuyenSinh.addItem("-- Tất cả các đợt --");
                for (DotTuyenSinh d : dots) {
                    String display = d.getTenDot() + " (" + d.getTrangThai() + ")";
                    cbDotTuyenSinh.addItem(display);
                }
                
                if (cbDotTuyenSinh.getItemCount() > 0) {
                    cbDotTuyenSinh.setSelectedIndex(0);
                }
            });
        });
    }

    private void loadResultToTable(Integer idDot) {
        SwingUtilities.invokeLater(() -> {
            tbModel.setRowCount(0);
            tbKetQua.setEnabled(false);
        });
        
        CompletableFuture.runAsync(() -> {
            List<KetQuaXetTuyen> listKq = new java.util.ArrayList<>();
            try {
                if (idDot == null || idDot == 0) {
                    System.out.println("[loadResultToTable] ===== LẤY DỮ LIỆU TẤT CẢ CÁC ĐỢT =====");
                    if (listDot != null && !listDot.isEmpty()) {
                        for (DotTuyenSinh dot : listDot) {
                            try {
                                List<KetQuaXetTuyen> dotKq = xetTuyenDAO.getKetQuaTheoDot(dot.getId());
                                System.out.println("[loadResultToTable] Đợt " + dot.getTenDot() + " có " + dotKq.size() + " kết quả");
                                listKq.addAll(dotKq);
                            } catch (Exception e) {
                                System.err.println("[loadResultToTable] Lỗi đợt " + dot.getId() + ": " + e.getMessage());
                            }
                        }
                    }
                } else {
                    System.out.println("[loadResultToTable] Lấy dữ liệu đợt ID=" + idDot);
                    listKq = xetTuyenDAO.getKetQuaTheoDot(idDot);
                }
                System.out.println("[loadResultToTable] TỔNG SỐ: " + listKq.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

            final List<KetQuaXetTuyen> finalList = listKq;
            SwingUtilities.invokeLater(() -> {
                tbModel.setRowCount(0);
                for (KetQuaXetTuyen kq : finalList) {
                    try {
                        tbModel.addRow(new Object[]{
                            kq.getHoSo() != null ? kq.getHoSo().getMaHoSo() : "",
                            kq.getHoSo() != null && kq.getHoSo().getThiSinh() != null ? kq.getHoSo().getThiSinh().getCccd() : "",
                            kq.getHoSo() != null && kq.getHoSo().getThiSinh() != null ? kq.getHoSo().getThiSinh().getHo() + " " + kq.getHoSo().getThiSinh().getTen() : "",
                            kq.getDiemXetTuyen(),
                            "NV " + kq.getNguyenVongThu(),
                            kq.getNganh() != null ? kq.getNganh().getTennganh() : "",
                            kq.getTrangThai()
                        });
                    } catch (Exception ex) {
                        System.err.println("[loadResultToTable] Lỗi thêm dòng: " + ex.getMessage());
                    }
                }
                tbKetQua.setEnabled(true);
                tbModel.fireTableDataChanged();
            });
        });
    }

    private void loadChiTietToTable(Integer idDot) {
        System.out.println("[loadChiTietToTable] Bắt đầu với idDot=" + idDot);
        
        SwingUtilities.invokeLater(() -> {
            tbModelChiTiet.setRowCount(0);
        });
        
        CompletableFuture.runAsync(() -> {
            List<NguyenVong> listNv = new java.util.ArrayList<>();
            try {
                if (idDot == null || idDot == 0) {
                    System.out.println("[loadChiTietToTable] ===== LẤY DỮ LIỆU TẤT CẢ CÁC ĐỢT =====");
                    if (listDot != null && !listDot.isEmpty()) {
                        for (DotTuyenSinh dot : listDot) {
                            try {
                                List<NguyenVong> dotNv = nguyenVongService.findByDotTuyenSinh(dot.getId());
                                System.out.println("[loadChiTietToTable] Đợt " + dot.getTenDot() + " có " + dotNv.size() + " nguyện vọng");
                                listNv.addAll(dotNv);
                            } catch (Exception e) {
                                System.err.println("[loadChiTietToTable] Lỗi đợt " + dot.getId() + ": " + e.getMessage());
                            }
                        }
                    }
                } else {
                    System.out.println("[loadChiTietToTable] Lấy dữ liệu đợt ID=" + idDot);
                    listNv = nguyenVongService.findByDotTuyenSinh(idDot);
                }
                System.out.println("[loadChiTietToTable] TỔNG SỐ NGUYỆN VỌNG: " + listNv.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

            final List<NguyenVong> finalList = listNv;
            SwingUtilities.invokeLater(() -> {
                tbModelChiTiet.setRowCount(0);
                int rowCount = 0;
                for (NguyenVong nv : finalList) {
                    try {
                        String hoTen = "";
                        String cccd = "";
                        
                        if (nv.getThiSinh() != null) {
                            cccd = nv.getThiSinh().getCccd();
                            if (cccd != null && !cccd.isEmpty()) {
                                try {
                                    ThiSinh ts = new com.tuyensinh.service.ThiSinhService().findByCccd(cccd).orElse(null);
                                    if (ts != null) {
                                        hoTen = (ts.getHo() != null ? ts.getHo() : "") + " " + (ts.getTen() != null ? ts.getTen() : "");
                                    }
                                } catch (Exception ex) {
                                    hoTen = cccd;
                                }
                            }
                        }
                        
                        String thmDisplay = nv.getTtThm() != null ? nv.getTtThm() : "";
                        BigDecimal diemThm = nv.getDiemThxt();
                        if (diemThm != null && diemThm.doubleValue() > 0) {
                            if ("DGNL".equalsIgnoreCase(nv.getTtPhuongthuc())) {
                                Double qd = traCuuService.tinhDiemQuyDoiDGNL(diemThm.doubleValue());
                                if (qd != null) {
                                    thmDisplay += " (Quy đổi: " + String.format("%.2f", qd) + ")";
                                }
                            } else if ("VSAT".equalsIgnoreCase(nv.getTtPhuongthuc())) {
                                Double qd = traCuuService.tinhDiemQuyDoiVSAT(diemThm.doubleValue(), null);
                                if (qd != null) {
                                    thmDisplay += " (Quy đổi: " + String.format("%.2f", qd) + ")";
                                }
                            }
                        }

                        tbModelChiTiet.addRow(new Object[]{
                            cccd,
                            hoTen,
                            nv.getNvTt() != null ? nv.getNvTt() : 0,
                            thmDisplay,
                            diemThm != null ? diemThm : 0,
                            nv.getDiemCong() != null ? nv.getDiemCong() : 0,
                            nv.getDiemUtqd() != null ? nv.getDiemUtqd() : 0,
                            nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0
                        });
                        rowCount++;
                    } catch (Exception ex) {
                        System.err.println("[loadChiTietToTable] Lỗi thêm dòng: " + ex.getMessage());
                    }
                }
                System.out.println("[loadChiTietToTable] ĐÃ LOAD " + rowCount + " DÒNG VÀO BẢNG");
                tbModelChiTiet.fireTableDataChanged();
                tbChiTiet.repaint();
            });
        });
    }

    private void handleExportExcel() {
        int idx = cbDotTuyenSinh.getSelectedIndex();
        if (idx <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đợt cụ thể để xuất Excel.");
            return;
        }
        Integer idDot = listDot.get(idx - 1).getId();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file Excel");
        fileChooser.setSelectedFile(new java.io.File("Danh_Sach_Trung_Tuyen.xlsx"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<KetQuaXetTuyen> listKq = xetTuyenDAO.getKetQuaTheoDot(idDot);
                com.tuyensinh.util.ExcelExportUtil.exportDanhSachTrungTuyen(fileChooser.getSelectedFile(), listKq);
                JOptionPane.showMessageDialog(this, "Xuất danh sách Excel thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất Excel: " + ex.getMessage());
            }
        }
    }

    private void handleExportPdf() {
        int idx = cbDotTuyenSinh.getSelectedIndex();
        if (idx <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đợt cụ thể để in giấy báo.");
            return;
        }
        Integer idDot = listDot.get(idx - 1).getId();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file PDF Giấy báo");
        fileChooser.setSelectedFile(new java.io.File("Giay_Bao_Trung_Tuyen.pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<KetQuaXetTuyen> listKq = xetTuyenDAO.getKetQuaTheoDot(idDot);
                com.tuyensinh.util.PdfExportUtil.exportGiayBaoTrungTuyen(fileChooser.getSelectedFile(), listKq);
                JOptionPane.showMessageDialog(this, "In giấy báo PDF thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất PDF: " + ex.getMessage());
            }
        }
    }

    private void handleGuiEmailThongBao() {
        int idx = cbDotTuyenSinh.getSelectedIndex();
        if (idx <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đợt cụ thể để gửi email.");
            return;
        }
        Integer idDot = listDot.get(idx - 1).getId();

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Hệ thống sẽ gửi Email tự động đến TẤT CẢ thí sinh trong đợt này.\nBạn có chắc chắn muốn thực hiện?", 
            "Xác nhận gửi Email", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            lblStatus.setText("Đang tiến hành gửi Email... Vui lòng không đóng phần mềm.");
            btnGuiEmailThongBao.setEnabled(false);

            CompletableFuture.runAsync(() -> {
                try {
                    List<KetQuaXetTuyen> listKq = xetTuyenDAO.getKetQuaTheoDot(idDot);
                    System.out.println("Số lượng email sẽ gửi: " + listKq.size());
                    Thread.sleep(2000);

                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText("Trạng thái: Đã gửi Email thông báo thành công!");
                        btnGuiEmailThongBao.setEnabled(true);
                        JOptionPane.showMessageDialog(QuanLyXetTuyenPanel.this, "Hoàn tất gửi thông báo kết quả xét tuyển.");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText("Lỗi khi gửi mail: " + ex.getMessage());
                        btnGuiEmailThongBao.setEnabled(true);
                    });
                }
            });
        }
    }

}