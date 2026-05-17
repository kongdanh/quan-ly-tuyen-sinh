package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.components.CustomTable;
import com.tuyensinh.admin.ui.components.HeaderPanel;
import com.tuyensinh.admin.ui.components.RoundedButton;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.model.NhomQuyen;
import com.tuyensinh.model.QuyenChucNang;
import com.tuyensinh.service.NhomQuyenService;
import com.tuyensinh.service.QuyenChucNangService;
import com.tuyensinh.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class NhomQuyenPanel extends JPanel {

    // Services
    private final NhomQuyenService nhomQuyenService = new NhomQuyenService();
    private final QuyenChucNangService quyenService = new QuyenChucNangService();

    // UI Components - Master (Left)
    private CustomTable tableNhomQuyen;
    private DefaultTableModel modelNhomQuyen;
    private RoundedButton btnAddNhom, btnEditNhom;

    // UI Components - Detail (Right)
    private CustomTable tableQuyen;
    private DefaultTableModel modelQuyen;
    private RoundedButton btnSaveQuyen;
    private JLabel lblDetailTitle;

    // State
    private NhomQuyen selectedNhomQuyen = null;

    // Danh sách các chức năng hệ thống cần phân quyền
    private final String[] DANH_SACH_CHUC_NANG = {
        Constants.QUYEN_CAU_HINH,      // Hệ thống
        Constants.QUYEN_BANG_QUY_DOI,
        Constants.QUYEN_DIEM_CONG,

        Constants.QUYEN_DIEM_CHUAN,    // Điểm chuẩn
        Constants.QUYEN_NGANH,         
        Constants.QUYEN_TOHOP, 
        Constants.QUYEN_NGANH_TOHOP,
        
        Constants.QUYEN_HO_SO,         // Hồ sơ
        Constants.QUYEN_DIEM_THI, 
        
        Constants.QUYEN_NGUYEN_VONG,   // Kết quả
        Constants.QUYEN_THONG_KE,
        
        Constants.QUYEN_NGUOI_DUNG,   // Phân quyền
        Constants.QUYEN_PHAN_QUYEN
    };

    public NhomQuyenPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, UIConstants.SECTION_GAP));
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Header
        add(new HeaderPanel("Quản lý Phân Quyền", "Thiết lập nhóm người dùng và phân quyền chi tiết"), BorderLayout.NORTH);

        // 2. Chia đôi màn hình bằng JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(10);
        splitPane.setLeftComponent(createMasterPanel());
        splitPane.setRightComponent(createDetailPanel());
        splitPane.setDividerLocation(450); // Chiều rộng mặc định bên trái

        add(splitPane, BorderLayout.CENTER);

        // 3. Load dữ liệu khởi tạo ban đầu
        loadDataNhomQuyen();
    }

    // ==================== PANEL BÊN TRÁI (MASTER - NHÓM QUYỀN) ====================
    private JPanel createMasterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Toolbar: Thêm, Sửa
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        btnAddNhom = new RoundedButton("Thêm Nhóm");
        btnEditNhom = new RoundedButton("Sửa");
        btnEditNhom.setEnabled(false); // Disable khi chưa chọn dòng nào

        boolean canAdd = com.tuyensinh.admin.util.AdminSession.getInstance().canAdd(Constants.QUYEN_PHAN_QUYEN);
        boolean canEdit = com.tuyensinh.admin.util.AdminSession.getInstance().canEdit(Constants.QUYEN_PHAN_QUYEN);

        btnAddNhom.setVisible(canAdd); // Ẩn nút nếu không có quyền Thêm
        btnEditNhom.setVisible(canEdit);

        btnAddNhom.addActionListener(e -> {
            com.tuyensinh.admin.ui.dialog.NhomQuyenFormDialog dialog = 
                new com.tuyensinh.admin.ui.dialog.NhomQuyenFormDialog((Frame) SwingUtilities.getWindowAncestor(this), new NhomQuyen(), true);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadDataNhomQuyen(); // Load lại bảng bên trái
        });

        btnEditNhom.addActionListener(e -> {
            if (selectedNhomQuyen != null) {
                com.tuyensinh.admin.ui.dialog.NhomQuyenFormDialog dialog = 
                    new com.tuyensinh.admin.ui.dialog.NhomQuyenFormDialog((Frame) SwingUtilities.getWindowAncestor(this), selectedNhomQuyen, false);
                dialog.setVisible(true);
                if (dialog.isSaved()) loadDataNhomQuyen(); // Load lại bảng bên trái
            }
        });
        toolbar.add(btnAddNhom);
        toolbar.add(btnEditNhom);

        // Table
        String[] cols = {"ID", "Mã Nhóm", "Tên Nhóm", "Trạng Thái"};
        modelNhomQuyen = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableNhomQuyen = new CustomTable();
        tableNhomQuyen.setModel(modelNhomQuyen);

        // Bắt sự kiện Click vào 1 dòng Nhóm quyền -> Load dấu tick bên phải
        tableNhomQuyen.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableNhomQuyen.getSelectedRow();
                if (row >= 0) {
                    Integer id = (Integer) modelNhomQuyen.getValueAt(row, 0);
                    selectedNhomQuyen = nhomQuyenService.findById(id); // Bạn nhớ thêm hàm findById bên Service nhé
                    btnEditNhom.setEnabled(true);
                    lblDetailTitle.setText("Đang phân quyền cho: " + selectedNhomQuyen.getTenNhom());
                    loadDataQuyenChucNang(selectedNhomQuyen.getId());
                    btnSaveQuyen.setEnabled(true);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableNhomQuyen);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Color.decode(UIConstants.COLOR_BORDER)));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ==================== PANEL BÊN PHẢI (DETAIL - QUYỀN CHỨC NĂNG) ====================
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 10, 0, 0));

        // Tiêu đề
        lblDetailTitle = new JLabel("Vui lòng chọn 1 nhóm quyền bên trái...");
        lblDetailTitle.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 14f));
        lblDetailTitle.setForeground(Color.decode(UIConstants.COLOR_NAVY));
        panel.add(lblDetailTitle, BorderLayout.NORTH);

        // Table Checkbox
        String[] cols = {"Mã Chức Năng", "Xem", "Thêm", "Sửa", "Xóa"};
        modelQuyen = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return String.class;
                return Boolean.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
        };
        tableQuyen = new CustomTable();
        tableQuyen.setModel(modelQuyen);
        tableQuyen.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tableQuyen.rowAtPoint(e.getPoint());
                int col = tableQuyen.columnAtPoint(e.getPoint());
                
                // Chỉ xử lý nếu click vào các cột: Xem, Thêm, Sửa, Xóa (Index > 0)
                if (row >= 0 && col > 0) { 
                    Boolean currentVal = (Boolean) modelQuyen.getValueAt(row, col);
                    modelQuyen.setValueAt(currentVal == null || !currentVal, row, col);
                    tableQuyen.repaint();
                    
                    btnSaveQuyen.setEnabled(true); 
                }
            }
        });
        tableQuyen.setRowHeight(35);

        JScrollPane scroll = new JScrollPane(tableQuyen);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Color.decode(UIConstants.COLOR_BORDER)));

        // Nút Lưu phân quyền
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        btnSaveQuyen = new RoundedButton("Lưu Phân Quyền");
        btnSaveQuyen.setEnabled(false);
        btnSaveQuyen.setBackground(Color.decode(UIConstants.DASH_ACCENT_SUCCESS));
        btnSaveQuyen.addActionListener(e -> saveQuyenChucNang());

        boolean canEdit = com.tuyensinh.admin.util.AdminSession.getInstance().canEdit(Constants.QUYEN_PHAN_QUYEN);
        btnSaveQuyen.setVisible(canEdit); 
        
        if (!canEdit) {
            tableQuyen.setEnabled(false); 
        }

        bottom.add(btnSaveQuyen);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    // ==================== XỬ LÝ DỮ LIỆU ====================
    private void loadDataNhomQuyen() {
        modelNhomQuyen.setRowCount(0);
        List<NhomQuyen> list = nhomQuyenService.findAllSync();
        for (NhomQuyen nq : list) {
            modelNhomQuyen.addRow(new Object[]{
                nq.getId(), nq.getMaNhom(), nq.getTenNhom(), nq.getTrangThai()
            });
        }
    }

    private void loadDataQuyenChucNang(Integer idNhom) {
        modelQuyen.setRowCount(0);
        List<QuyenChucNang> quyenCu = quyenService.findByNhomQuyenId(idNhom); 

        Map<String, QuyenChucNang> mapQuyen = new HashMap<>();
        for (QuyenChucNang q : quyenCu) {
            mapQuyen.put(q.getMaChucNang(), q);
        }

        for (String maCN : DANH_SACH_CHUC_NANG) {
            QuyenChucNang q = mapQuyen.get(maCN);
            modelQuyen.addRow(new Object[]{
                maCN,
                q != null && q.getCoXem(),
                q != null && q.getCoThem(),
                q != null && q.getCoSua(),
                q != null && q.getCoXoa()
            });
        }
    }

    private void saveQuyenChucNang() {
        if (selectedNhomQuyen == null) return;

        btnSaveQuyen.setEnabled(false);
        btnSaveQuyen.setText("Đang lưu...");

        // Đọc dữ liệu từ Table
        List<QuyenChucNang> listToSave = new java.util.ArrayList<>();
        for (int i = 0; i < modelQuyen.getRowCount(); i++) {
            QuyenChucNang q = new QuyenChucNang();
            q.setNhomQuyen(selectedNhomQuyen);
            q.setMaChucNang((String) modelQuyen.getValueAt(i, 0));
            q.setCoXem((Boolean) modelQuyen.getValueAt(i, 1));
            q.setCoThem((Boolean) modelQuyen.getValueAt(i, 2));
            q.setCoSua((Boolean) modelQuyen.getValueAt(i, 3));
            q.setCoXoa((Boolean) modelQuyen.getValueAt(i, 4));
            
            // Mặc định Xuất = false vì không hiện trên bảng, hoặc bạn có thể thêm 1 cột nữa cho Xuất
            q.setCoXuat(false); 
            listToSave.add(q);
        }

        quyenService.saveQuyenChucNangTheoNhom(selectedNhomQuyen.getId(), listToSave);

        JOptionPane.showMessageDialog(this, "Đã lưu phân quyền thành công cho " + selectedNhomQuyen.getTenNhom());
        btnSaveQuyen.setText("Lưu Phân Quyền");
        btnSaveQuyen.setEnabled(true);
    }
}