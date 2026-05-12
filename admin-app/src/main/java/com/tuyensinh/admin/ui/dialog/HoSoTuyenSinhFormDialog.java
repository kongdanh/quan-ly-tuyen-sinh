package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.model.HoSoTuyenSinh;
import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.service.HoSoTuyenSinhService;
import com.tuyensinh.service.NguyenVongService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class HoSoTuyenSinhFormDialog extends BaseFormDialog<HoSoTuyenSinh> {

    private final HoSoTuyenSinhService service = new HoSoTuyenSinhService();
    private final NguyenVongService nvService = new NguyenVongService();
    
    private RoundedTextField txtCccd, txtHoTen, txtEmail, txtSdt, txtNgaySinh, txtGioiTinh, txtNoiSinh, txtKhuVuc, txtDoiTuong;
    private RoundedTextField txtMaHoSo, txtDiem;
    private JComboBox<String> cbTrangThai;
    private JTable tbNguyenVong;
    private DefaultTableModel nvModel;

    public HoSoTuyenSinhFormDialog(Frame parent, HoSoTuyenSinh entity) {
        super(parent, "Chi Tiết Hồ Sơ Xét Tuyển", entity, false, 950, 600);
        buildFormFields();
        populateForm(entity);
        loadNguyenVong(entity.getId());
    }

    @Override
    protected void buildFormFields() {
        // Cấu hình formBody là 2 cột
        formBody.setLayout(new GridLayout(1, 2, 25, 0));

        // LEFT PANEL: Thông tin thí sinh
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        txtCccd = new RoundedTextField(""); txtCccd.setEnabled(false);
        txtHoTen = new RoundedTextField(""); txtHoTen.setEnabled(false);
        txtEmail = new RoundedTextField(""); txtEmail.setEnabled(false);
        txtSdt = new RoundedTextField(""); txtSdt.setEnabled(false);
        txtNgaySinh = new RoundedTextField(""); txtNgaySinh.setEnabled(false);
        txtGioiTinh = new RoundedTextField(""); txtGioiTinh.setEnabled(false);
        txtNoiSinh = new RoundedTextField(""); txtNoiSinh.setEnabled(false);
        txtKhuVuc = new RoundedTextField(""); txtKhuVuc.setEnabled(false);
        txtDoiTuong = new RoundedTextField(""); txtDoiTuong.setEnabled(false);

        addSectionHeader(leftPanel, "THÔNG TIN THÍ SINH");
        addLeftField(leftPanel, "CCCD:", txtCccd);
        addLeftField(leftPanel, "Họ tên:", txtHoTen);
        addLeftField(leftPanel, "Ngày sinh:", txtNgaySinh);
        addLeftField(leftPanel, "Giới tính:", txtGioiTinh);
        addLeftField(leftPanel, "Khu vực:", txtKhuVuc);
        addLeftField(leftPanel, "Ưu tiên:", txtDoiTuong);
        addLeftField(leftPanel, "Email:", txtEmail);
        addLeftField(leftPanel, "Điện thoại:", txtSdt);
        // addLeftField(leftPanel, "Nơi sinh:", txtNoiSinh); // Ẩn bớt cho gọn

        // RIGHT PANEL: Thông tin hồ sơ & Nguyện vọng
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        txtMaHoSo = new RoundedTextField(""); txtMaHoSo.setEnabled(false);
        txtDiem = new RoundedTextField(""); txtDiem.setEnabled(false);
        cbTrangThai = new JComboBox<>(new String[]{"CHO_XET", "HOP_LE", "KHONG_HOP_LE", "TRUNG_TUYEN", "TRUOT"});

        addSectionHeader(rightPanel, "HỒ SƠ & NGUYỆN VỌNG");
        addLeftField(rightPanel, "Mã Hồ Sơ:", txtMaHoSo);
        addLeftField(rightPanel, "Tổng điểm:", txtDiem);
        addLeftField(rightPanel, "Trạng thái:", cbTrangThai);

        rightPanel.add(Box.createVerticalStrut(15));
        
        String[] cols = {"TT", "Ngành học", "Mã ngành", "Tổ hợp", "Điểm XT", "Kết quả"};
        nvModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tbNguyenVong = new JTable(nvModel);
        tbNguyenVong.setRowHeight(28);
        tbNguyenVong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(tbNguyenVong);
        scrollPane.setPreferredSize(new Dimension(450, 200));
        rightPanel.add(scrollPane);

        formBody.add(leftPanel);
        formBody.add(rightPanel);
    }

    private void addSectionHeader(JPanel panel, String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.decode("#1e40af"));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(8));
    }

    private void addLeftField(JPanel panel, String label, JComponent comp) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(1000, 30));
        
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(75, 26));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        row.add(lbl, BorderLayout.WEST);
        row.add(comp, BorderLayout.CENTER);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        
        panel.add(row);
    }

    @Override
    protected void populateForm(HoSoTuyenSinh hs) {
        txtMaHoSo.setText(hs.getMaHoSo());
        
        if (hs.getThiSinh() != null) {
            com.tuyensinh.model.ThiSinh ts = hs.getThiSinh();
            txtCccd.setText(ts.getCccd());
            txtHoTen.setText(ts.getHo() + " " + ts.getTen());
            txtEmail.setText(ts.getEmail());
            txtSdt.setText(ts.getDienThoai());
            txtNgaySinh.setText(ts.getNgaySinh() != null ? ts.getNgaySinh().toString() : "");
            txtGioiTinh.setText(ts.getGioiTinh());
            txtNoiSinh.setText(ts.getNoiSinh());
            txtKhuVuc.setText(ts.getKhuVuc() != null ? ts.getKhuVuc() : "KV3");
            txtDoiTuong.setText(ts.getDoiTuong() != null ? ts.getDoiTuong() : "Khong");
        }
        
        txtDiem.setText(hs.getTongDiemXetTuyen() != null ? String.valueOf(hs.getTongDiemXetTuyen()) : "0.0");
        cbTrangThai.setSelectedItem(hs.getTrangThai());
    }
    
    // Hàm gọi DB lấy nguyện vọng của hồ sơ này
    private void loadNguyenVong(Integer idHoSo) {
        try {
            // Lấy danh sách nguyện vọng theo id_ho_so
            List<NguyenVong> list = nvService.findPageWithFilters("", List.of(), Map.of("hoSoTuyenSinh.id", idHoSo), 1, 50).get();
            nvModel.setRowCount(0);
            
            for (NguyenVong nv : list) {
                String tenNganh = "";
                String maNganh = "";
                
                if (nv.getNganh() != null) {
                    tenNganh = nv.getNganh().getTennganh(); 
                    maNganh = nv.getNganh().getManganh();
                }

                nvModel.addRow(new Object[]{
                    nv.getNvTt(),
                    tenNganh,
                    maNganh,
                    nv.getTtThm(),
                    nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : "Chưa có",
                    nv.getNvKetqua()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String validateData() { return null; }

    @Override
    protected void collectData(HoSoTuyenSinh hs) {
        hs.setTrangThai(cbTrangThai.getSelectedItem().toString());
    }

    @Override
    protected void persist(HoSoTuyenSinh hs) {
        service.update(hs);
    }
}