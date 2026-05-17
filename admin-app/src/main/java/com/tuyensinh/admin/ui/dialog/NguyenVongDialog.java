package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.service.NguyenVongService;

import javax.swing.*;
import java.awt.*;

public class NguyenVongDialog extends BaseFormDialog<NguyenVong> {

    private final NguyenVongService nvService;

    // Các Component nhập liệu
    private JTextField txtCccd;
    private JTextField txtMaNganh;
    private JTextField txtMaToHop;
    private JSpinner spinThuTu;
    private JComboBox<String> cbPhuongThuc;
    
    public NguyenVongDialog(Frame parent, String title, NguyenVong nv, NguyenVongService nvService) {
        super(parent, title, nv, nv == null, 450, 400);
        this.nvService = nvService;
        buildFormFields();
        populateForm(nv);
    }

    @Override
    protected void buildFormFields() {
        txtCccd = createTextField();
        txtMaNganh = createTextField();
        txtMaToHop = createTextField();
        spinThuTu = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        cbPhuongThuc = new JComboBox<>(new String[]{"THPT", "DGNL", "VSAT"});
        cbPhuongThuc.setPreferredSize(new Dimension(200, 35));

        addSectionHeader("Thông tin đăng ký");
        addField("CCCD:", txtCccd);
        addField("Mã Ngành:", txtMaNganh);
        addField("Mã Tổ Hợp:", txtMaToHop);
        addField("Thứ tự:", spinThuTu);
        addField("Phương thức:", cbPhuongThuc);
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(200, 35));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode(UIConstants.COLOR_BORDER)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return tf;
    }

    @Override
    protected void populateForm(NguyenVong entity) {
        if (entity != null) {
            if (entity.getThiSinh() != null) txtCccd.setText(entity.getThiSinh().getCccd());
            if (entity.getNganh() != null) txtMaNganh.setText(entity.getNganh().getManganh());
            txtMaToHop.setText(entity.getTtThm());
            if (entity.getNvTt() != null) spinThuTu.setValue(entity.getNvTt());
            if (entity.getTtPhuongthuc() != null) cbPhuongThuc.setSelectedItem(entity.getTtPhuongthuc());
            
            // Cấm sửa CCCD/Ngành nếu đang ở chế độ Edit
            txtCccd.setEnabled(false);
            txtMaNganh.setEnabled(false);
        }
    }

    @Override
    protected void collectData(NguyenVong entity) {
        // Form xử lý qua NVService, không cần map ngược vào entity ở đây
    }

    @Override
    protected String validateData() {
        if (isBlank(txtCccd) || isBlank(txtMaNganh) || isBlank(txtMaToHop)) {
            return "Vui lòng nhập đầy đủ thông tin (CCCD, Mã ngành, Tổ hợp)!";
        }
        return null; // OK
    }

    @Override
    protected void persist(NguyenVong entity) {
        String cccd = getText(txtCccd);
        String manganh = getText(txtMaNganh);
        String matohop = getText(txtMaToHop);
        int thutu = (Integer) spinThuTu.getValue();
        String pt = (String) cbPhuongThuc.getSelectedItem();

        if (isAddNew) {
            NguyenVongService.KetQuaDangKy kq = nvService.dangKyNguyenVong(cccd, manganh, matohop, thutu, pt, matohop);
            if (!kq.isThanhCong()) {
                throw new RuntimeException(kq.getThongDiep());
            }
        } else {
            NguyenVongService.SaveResult res = nvService.saveWish(cccd, manganh, entity.getId(), null);
            if (res != NguyenVongService.SaveResult.OK) {
                throw new RuntimeException("Cập nhật thất bại: " + res.toString());
            }
        }
    }
}
