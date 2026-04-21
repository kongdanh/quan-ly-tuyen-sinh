package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.model.NhomQuyen;
import com.tuyensinh.service.NhomQuyenService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class NhomQuyenFormDialog extends BaseFormDialog<NhomQuyen> {

    private final NhomQuyenService service = new NhomQuyenService();
    private RoundedTextField txtMaNhom;
    private RoundedTextField txtTenNhom;
    private RoundedTextField txtMoTa;
    private JComboBox<String> cbTrangThai;

    public NhomQuyenFormDialog(Frame parent, NhomQuyen nq, boolean isAddNew) {
        super(parent, "Nhóm Quyền", nq, isAddNew, 400, 320);
        buildFormFields();
        if (!isAddNew) populateForm(nq);
    }

    @Override
    protected void buildFormFields() {
        txtMaNhom = new RoundedTextField("Ví dụ: ADMIN, GIAO_VU...");
        txtTenNhom = new RoundedTextField("Tên hiển thị (Ví dụ: Trưởng phòng)");
        txtMoTa = new RoundedTextField("Mô tả nhiệm vụ...");
        cbTrangThai = new JComboBox<>(new String[]{"HOAT_DONG", "KHOA"});

        addField("Mã nhóm (*):", txtMaNhom);
        addField("Tên nhóm (*):", txtTenNhom);
        addField("Mô tả:", txtMoTa);
        addField("Trạng thái:", cbTrangThai);
    }

    @Override
    protected void populateForm(NhomQuyen nq) {
        txtMaNhom.setText(nq.getMaNhom());
        if (!isAddNew) txtMaNhom.setEnabled(false);
        txtTenNhom.setText(nq.getTenNhom());
        txtMoTa.setText(nq.getMoTa());
        cbTrangThai.setSelectedItem(nq.getTrangThai());
    }

    @Override
    protected String validateData() {
        if (isBlank(txtMaNhom) || isBlank(txtTenNhom)) {
            return "Vui lòng nhập đủ Mã nhóm và Tên nhóm!";
        }
        return null;
    }

    @Override
    protected void collectData(NhomQuyen nq) {
        nq.setMaNhom(getText(txtMaNhom).toUpperCase());
        nq.setTenNhom(getText(txtTenNhom));
        nq.setMoTa(getText(txtMoTa));
        nq.setTrangThai(cbTrangThai.getSelectedItem().toString());
        if (isAddNew) nq.setNgayTao(LocalDateTime.now());
    }

    @Override
    protected void persist(NhomQuyen nq) {
        if (isAddNew) service.save(nq);
        else service.update(nq);
    }
}