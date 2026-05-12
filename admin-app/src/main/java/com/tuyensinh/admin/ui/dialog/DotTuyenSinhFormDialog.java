package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.ui.components.CustomComboBox;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.model.DotTuyenSinh;
import com.tuyensinh.service.DotTuyenSinhService;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.*;

public class DotTuyenSinhFormDialog extends BaseFormDialog<DotTuyenSinh> {
    private final DotTuyenSinhService service = new DotTuyenSinhService();

    private RoundedTextField txtTen;
    private JSpinner spinBatDau, spinKetThuc, spinCongBo;
    private JComboBox<String> cbTrangThai;
    private CustomComboBox<String> cbPhuongThuc;
    
    public DotTuyenSinhFormDialog(Frame parent, DotTuyenSinh entity, boolean isAddNew) {
        // Tăng chiều cao lên 480 để chứa thêm ComboBox phương thức
        super(parent, "Đợt Tuyển Sinh", entity, isAddNew, 450, 480);
        buildFormFields();
        if (!isAddNew) populateForm(entity);
    }

    private JSpinner createDateTimeSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd/MM/yyyy HH:mm");
        spinner.setEditor(editor);
        return spinner;
    }

    protected void buildFormFields() {
        txtTen = new RoundedTextField(String.valueOf(20));
        formBody.add(createRow("Tên đợt:", txtTen));

        // Sử dụng CustomComboBox của bạn
        cbPhuongThuc = new CustomComboBox<>(new String[]{"THPT", "DGNL", "VSAT", "HOCBA"});
        formBody.add(createRow("Phương thức xét:", cbPhuongThuc));

        spinBatDau = createDateTimeSpinner();
        formBody.add(createRow("Bắt đầu:", spinBatDau));

        spinKetThuc = createDateTimeSpinner();
        formBody.add(createRow("Kết thúc:", spinKetThuc));

        spinCongBo = createDateTimeSpinner();
        formBody.add(createRow("Công bố:", spinCongBo));

        cbTrangThai = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        formBody.add(createRow("Trạng thái:", cbTrangThai));
    }

    protected void populateForm(DotTuyenSinh d) {
        txtTen.setText(d.getTenDot());
        if (d.getMaPhuongThuc() != null) cbPhuongThuc.setSelectedItem(d.getMaPhuongThuc());
        if (d.getNgayBatDau() != null) spinBatDau.setValue(toDate(d.getNgayBatDau()));
        if (d.getNgayKetThuc() != null) spinKetThuc.setValue(toDate(d.getNgayKetThuc()));
        if (d.getNgayCongBo() != null) spinCongBo.setValue(toDate(d.getNgayCongBo()));
        if (d.getTrangThai() != null) cbTrangThai.setSelectedItem(d.getTrangThai());
    }

    @Override
    public String validateData() { // Đổi thành public để hết lỗi visibility
        if (isBlank(txtTen)) return "Vui lòng nhập Tên đợt tuyển sinh!";
        return null;
    }

    @Override
    public void collectData(DotTuyenSinh d) { // Đổi thành public
        d.setTenDot(getText(txtTen));
        d.setMaPhuongThuc((String) cbPhuongThuc.getSelectedItem());
        d.setNgayBatDau(toLDT((Date) spinBatDau.getValue()));
        d.setNgayKetThuc(toLDT((Date) spinKetThuc.getValue()));
        d.setNgayCongBo(toLDT((Date) spinCongBo.getValue()));
        d.setTrangThai((String) cbTrangThai.getSelectedItem());
    }

    @Override
    public void persist(DotTuyenSinh entity) { // Đổi thành public
        if (isAddNew) {
            entity.setNgayTao(LocalDateTime.now());
            service.save(entity);
        } else {
            service.update(entity);
        }
    }

    private Date toDate(LocalDateTime ldt) {
        if (ldt == null) return new Date();
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLDT(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    protected JPanel createRow(String labelText, JComponent comp) {
        JPanel row = new JPanel(new java.awt.BorderLayout(10, 10));
        row.setOpaque(false);
        row.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel lbl = new JLabel(labelText);
        lbl.setPreferredSize(new java.awt.Dimension(110, 30));
        lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        
        row.add(lbl, java.awt.BorderLayout.WEST);
        row.add(comp, java.awt.BorderLayout.CENTER);
        return row;
    }

}