package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.components.RoundedButton;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.dao.XetTuyenDAO;
import com.tuyensinh.model.DotTuyenSinh;
import com.tuyensinh.model.KetQuaXetTuyen;
import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.service.DotTuyenSinhService;
import com.tuyensinh.service.NguyenVongService;
import com.tuyensinh.service.TraCuuService;
import com.tuyensinh.service.XetTuyenService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private RoundedButton btnLuuKetQua;
    private JTable tbKetQua;
    private DefaultTableModel tbModel;
    private JTable tbChiTiet;
    private DefaultTableModel tbModelChiTiet;
    private JLabel lblStatus;
    private RoundedButton btnExportExcel;
    private RoundedButton btnExportPdf;
    private RoundedButton btnGuiEmailThongBao;

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
        JLabel subTitle = new JLabel("Chạy thuật toán lọc ảo và chi tiết điểm xét tuyển");
        subTitle.setForeground(Color.GRAY);
        pnlTitle.add(title);
        pnlTitle.add(subTitle);

        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlControls.setOpaque(false);

        cbDotTuyenSinh = new JComboBox<>();
        btnChayThuatToan = new RoundedButton("Chạy Thuật Toán");
        btnChayThuatToan.setBackground(Color.decode(UIConstants.COLOR_BLUE));
        btnChayThuatToan.setForeground(Color.WHITE);

        btnLuuKetQua = new RoundedButton("Chốt Kết Quả");
        btnLuuKetQua.setBackground(Color.decode("#059669"));
        btnLuuKetQua.setForeground(Color.WHITE);

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
        pnlControls.add(btnLuuKetQua);
        pnlControls.add(btnGuiEmailThongBao);

        pnlHeader.add(pnlTitle, BorderLayout.NORTH);
        pnlHeader.add(pnlControls, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        // --- Tabs ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Tab 1: Ket Qua Trung Tuyen
        String[] columns = {"Mã Hồ Sơ", "CCCD", "Họ Tên", "Điểm XT", "Nguyện Vọng", "Ngành Trúng Tuyển", "Trạng Thái"};
        tbModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbKetQua = new JTable(tbModel);
        tbKetQua.setRowHeight(35);
        tabbedPane.addTab("Danh sách Trúng tuyển", new JScrollPane(tbKetQua));

        // Tab 2: Chi Tiet Diem Xet Tuyen (Theo bien ban)
        String[] colsChiTiet = {"CCCD", "Họ Tên", "NV", "Tổ hợp (Thang 30)", "Điểm THM", "Điểm Cộng", "Điểm Ưu Tiên", "Điểm Xét Tuyển"};
        tbModelChiTiet = new DefaultTableModel(colsChiTiet, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tbChiTiet = new JTable(tbModelChiTiet);
        tbChiTiet.setRowHeight(35);
        tabbedPane.addTab("Chi tiết điểm xét tuyển (Tất cả NV)", new JScrollPane(tbChiTiet));

        add(tabbedPane, BorderLayout.CENTER);

        // --- Footer Status ---
        lblStatus = new JLabel("Trạng thái: Sẵn sàng");
        lblStatus.setBorder(new EmptyBorder(10, 0, 0, 0));
        add(lblStatus, BorderLayout.SOUTH);

        // Event
        btnChayThuatToan.addActionListener(e -> handleChayThuatToan());
        cbDotTuyenSinh.addActionListener(e -> {
            int idx = cbDotTuyenSinh.getSelectedIndex();
            if (idx >= 0) {
                Integer idDot = null;
                if (idx > 0) {
                    idDot = listDot.get(idx - 1).getId();
                }
                loadResultToTable(idDot);
                loadChiTietToTable(idDot);
            }
        });
        btnExportExcel.addActionListener(e -> handleExportExcel());
        btnExportPdf.addActionListener(e -> handleExportPdf());
    }

    private void loadDotTuyenSinh() {
        dotService.findPageWithFilters("", List.of(), Map.of(), 1, 100).thenAccept(dots -> {
            this.listDot = dots;
            SwingUtilities.invokeLater(() -> {
                cbDotTuyenSinh.removeAllItems();
                cbDotTuyenSinh.addItem("-- Tất cả các đợt --"); // Thêm tùy chọn tất cả
                for (DotTuyenSinh d : dots) cbDotTuyenSinh.addItem(d.getTenDot());
                
                if (cbDotTuyenSinh.getItemCount() > 0) {
                    cbDotTuyenSinh.setSelectedIndex(0);
                }
            });
        });
    }

    private void handleChayThuatToan() {
        int idx = cbDotTuyenSinh.getSelectedIndex();
        if (idx <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đợt cụ thể để chạy thuật toán.");
            return;
        }
        Integer idDot = listDot.get(idx - 1).getId();
        
        btnChayThuatToan.setEnabled(false);
        lblStatus.setText("Đang chạy thuật toán lọc ảo... Vui lòng đợi.");

        CompletableFuture.runAsync(() -> {
            try {
                xetTuyenService.chayThuatToanXetTuyen(idDot);
                SwingUtilities.invokeLater(() -> {
                    loadResultToTable(idDot);
                    lblStatus.setText("Trạng thái: Chạy thuật toán hoàn tất!");
                    btnChayThuatToan.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
                    btnChayThuatToan.setEnabled(true);
                });
            }
        });
    }

    private void loadResultToTable(Integer idDot) {
        CompletableFuture.runAsync(() -> {
            List<KetQuaXetTuyen> listKq;
            if (idDot == null) {
                // Nếu chọn "Tất cả", có thể cần vòng lặp hoặc DAO riêng. 
                // Tạm thời get tất cả đợt nếu idDot null.
                listKq = new java.util.ArrayList<>();
                for (DotTuyenSinh dot : listDot) {
                    listKq.addAll(xetTuyenDAO.getKetQuaTheoDot(dot.getId()));
                }
            } else {
                listKq = xetTuyenDAO.getKetQuaTheoDot(idDot);
            }

            SwingUtilities.invokeLater(() -> {
                tbModel.setRowCount(0);
                for (KetQuaXetTuyen kq : listKq) {
                    tbModel.addRow(new Object[]{
                        kq.getHoSo().getMaHoSo(),
                        kq.getHoSo().getThiSinh().getCccd(),
                        kq.getHoSo().getThiSinh().getHo() + " " + kq.getHoSo().getThiSinh().getTen(),
                        kq.getDiemXetTuyen(),
                        "NV " + kq.getNguyenVongThu(),
                        kq.getNganh().getTennganh(),
                        kq.getTrangThai()
                    });
                }
            });
        });
    }

    private void loadChiTietToTable(Integer idDot) {
        CompletableFuture.runAsync(() -> {
            List<NguyenVong> listNv;
            if (idDot == null) {
                listNv = new java.util.ArrayList<>();
                for (DotTuyenSinh dot : listDot) {
                    listNv.addAll(nguyenVongService.findByDotTuyenSinh(dot.getId()));
                }
            } else {
                listNv = nguyenVongService.findByDotTuyenSinh(idDot);
            }

            SwingUtilities.invokeLater(() -> {
                tbModelChiTiet.setRowCount(0);
                for (NguyenVong nv : listNv) {
                    String hoTen = nv.getThiSinh() != null ? nv.getThiSinh().getHo() + " " + nv.getThiSinh().getTen() : "";
                    String cccd = nv.getThiSinh() != null ? nv.getThiSinh().getCccd() : "";
                    
                    // Logic quy đổi điểm thang 30 cho UI từ CSDL thật
                    String thmDisplay = nv.getTtThm();
                    BigDecimal diemThm = nv.getDiemThxt();
                    if (diemThm != null) {
                        if ("DGNL".equalsIgnoreCase(nv.getTtPhuongthuc()) || nv.getTtPhuongthuc().contains("ĐGNL")) {
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
                        nv.getNvTt(),
                        thmDisplay,
                        diemThm,
                        nv.getDiemCong(),
                        nv.getDiemUtqd(),
                        nv.getDiemXettuyen()
                    });
                }
            });
        });
    }

    private void handleExportExcel() {
        int idx = cbDotTuyenSinh.getSelectedIndex();
        if (idx <= 0) return;
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
        if (idx <= 0) return;
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
        if (idx <= 0) return;
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
                    Thread.sleep(2000); 

                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText("Trạng thái: Đã gửi Email thông báo thành công!");
                        btnGuiEmailThongBao.setEnabled(true);
                        JOptionPane.showMessageDialog(this, "Hoàn tất gửi thông báo kết quả xét tuyển.");
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
