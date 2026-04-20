package com.tuyensinh.admin.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.math.BigDecimal;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.dao.NganhDAO;
import com.tuyensinh.model.Nganh;

public class NganhToHopDialog extends BaseFormDialog<Nganh> {

    private RoundedTextField txtMaNganh;
    private JComboBox<String> cbKhoa;
    private RoundedTextField txtTenNganh;
    private RoundedTextField txtChiTieu;
    private RoundedTextField txtDiemSan;
    private JComboBox<String> cbTrangThai;

    private final NganhDAO nganhDAO = new NganhDAO();

    public NganhToHopDialog(Frame parent, Nganh entity, boolean isAddNew) { 
        super(parent, "Ngành tuyển sinh", entity, isAddNew, 500, 400);
        
        // Luôn khởi tạo fields trước khi populate
        buildFormFields();
        if (!isAddNew) populateForm(entity);
        setupExtras();
    }

    @Override
    protected void buildFormFields() {
        // --- BƯỚC 1: KHỞI TẠO COMPONENT (QUAN TRỌNG - KHÔNG ĐƯỢC THIẾU) ---
        txtMaNganh   = new RoundedTextField("VD: CNTT01");
        cbKhoa       = new JComboBox<>(new String[]{"Công nghệ thông tin", "Kinh tế", "Ngoại ngữ", "Luật"});
        txtTenNganh  = new RoundedTextField("Nhập tên ngành đầy đủ");
        txtChiTieu   = new RoundedTextField("0");
        txtDiemSan   = new RoundedTextField("0");
        cbTrangThai  = new JComboBox<>(new String[]{"Đang tuyển", "Tạm dừng", "Đã đủ chỉ tiêu"});

        // Đặt màu nền trắng cho ComboBox giống ảnh
        cbKhoa.setBackground(Color.WHITE);
        cbTrangThai.setBackground(Color.WHITE);

        // --- BƯỚC 2: XẾP LAYOUT ---
        
        // Hàng 1: Mã ngành & Khoa
        JPanel row1 = new JPanel(new GridLayout(1, 2, 20, 0));
        row1.setBackground(Color.WHITE);
        row1.add(createFieldGroup("Mã ngành *", txtMaNganh));
        row1.add(createFieldGroup("Khoa", cbKhoa));
        formBody.add(row1);
        formBody.add(Box.createVerticalStrut(15));

        // Hàng 2: Tên ngành (Full Width bằng cách dùng BorderLayout bọc lại)
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setBackground(Color.WHITE);
        row2.add(createFieldGroup("Tên ngành *", txtTenNganh), BorderLayout.CENTER);
        formBody.add(row2);
        formBody.add(Box.createVerticalStrut(15));

        // Hàng 3: Chỉ tiêu & Điểm sàn
        JPanel row3 = new JPanel(new GridLayout(1, 2, 20, 0));
        row3.setBackground(Color.WHITE);
        row3.add(createFieldGroup("Chỉ tiêu", txtChiTieu));
        row3.add(createFieldGroup("Điểm sàn", txtDiemSan));
        formBody.add(row3);
        formBody.add(Box.createVerticalStrut(15));

        // Hàng 4: Trạng thái (Full Width)
        JPanel row4 = new JPanel(new BorderLayout());
        row4.setBackground(Color.WHITE);
        row4.add(createFieldGroup("Trạng thái", cbTrangThai), BorderLayout.CENTER);
        formBody.add(row4);
    }

    private JPanel createFieldGroup(String labelText, JComponent component) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBackground(Color.WHITE);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 12));
        label.setForeground(Color.decode("#374151"));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Đảm bảo component chiếm không gian
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        component.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));

        group.add(label);
        group.add(Box.createVerticalStrut(5));
        group.add(component);
        return group;
    }

    @Override
    protected void populateForm(Nganh n) {
        if (n == null) return;  
        txtMaNganh.setText(n.getManganh());
        txtTenNganh.setText(n.getTennganh());
        // Giả định n.getKhoa() trả về String đúng tên khoa trong ComboBox
        cbKhoa.setSelectedItem(n.getManganh() != null ? "KHOA" : "Chọn khoa"); 
        txtChiTieu.setText(String.valueOf(n.getnChitieu()));
        txtDiemSan.setText(n.getnDiemsan() != null ? n.getnDiemsan().toString() : "0");
        cbTrangThai.setSelectedItem("Đang tuyển");

        if (!isAddNew) {
            txtMaNganh.setEnabled(false);
        }
    }

    @Override
    protected void collectData(Nganh n) {
        n.setManganh(getText(txtMaNganh));
        n.setTennganh(getText(txtTenNganh));
        // n.setMaKhoa(...) - Cần logic map từ tên khoa sang mã khoa ở đây
        n.setnChitieu(Integer.parseInt(getText(txtChiTieu).isEmpty() ? "0" : getText(txtChiTieu)));
        
        String diemSanStr = getText(txtDiemSan);
        n.setnDiemsan(new BigDecimal(diemSanStr.isEmpty() ? "0" : diemSanStr));
    }

    @Override
    protected String validateData() {
        if (isBlank(txtMaNganh)) return "Mã ngành không được để trống.";
        if (isBlank(txtTenNganh)) return "Tên ngành không được để trống.";
        
        try {
            Integer.parseInt(getText(txtChiTieu));
            new BigDecimal(getText(txtDiemSan));
        } catch (NumberFormatException e) {
            return "Chỉ tiêu và điểm sàn phải là số hợp lệ.";
        }
        return null;
    }

    @Override
    protected void persist(Nganh n) {
        if (isAddNew) nganhDAO.save(n);
        else nganhDAO.update(n);
    }
}