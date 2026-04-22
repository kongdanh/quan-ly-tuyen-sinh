package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
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

    public DotTuyenSinhFormDialog(Frame parent, DotTuyenSinh entity, boolean isAddNew) {
        super(parent, "Đợt Tuyển Sinh", entity, isAddNew, 450, 420);
        buildFormFields();
        if (!isAddNew) populateForm(entity);
    }

    // Hàm tiện ích tạo Spinner chọn ngày giờ
    private JSpinner createDateTimeSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd HH:mm");
        spinner.setEditor(editor);
        return spinner;
    }

    // Chuyển từ LocalDateTime sang java.util.Date cho Spinner
    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    // Chuyển từ java.util.Date của Spinner sang LocalDateTime cho Database
    private LocalDateTime toLDT(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Override
    protected void buildFormFields() {
        txtTen = new RoundedTextField("Tên đợt (VD: Xét học bạ đợt 1)");
        
        // Thay thế TextField bằng Spinner
        spinBatDau = createDateTimeSpinner();
        spinKetThuc = createDateTimeSpinner();
        spinCongBo = createDateTimeSpinner();
        
        cbTrangThai = new JComboBox<>(new String[]{"MO_CONG", "DONG_CONG", "KET_THUC"});

        addSectionHeader("Thông tin thời gian");
        addField("Tên đợt:", txtTen);
        addField("Bắt đầu:", spinBatDau);
        addField("Kết thúc:", spinKetThuc);
        addField("Công bố:", spinCongBo);
        addField("Trạng thái:", cbTrangThai);
    }

    @Override
    protected void populateForm(DotTuyenSinh d) {
        txtTen.setText(d.getTenDot());
        spinBatDau.setValue(toDate(d.getNgayBatDau()));
        spinKetThuc.setValue(toDate(d.getNgayKetThuc()));
        if (d.getNgayCongBo() != null) {
            spinCongBo.setValue(toDate(d.getNgayCongBo()));
        }
        cbTrangThai.setSelectedItem(d.getTrangThai());
    }

    @Override
    protected String validateData() {
        if (isBlank(txtTen)) return "Vui lòng nhập Tên đợt tuyển sinh!";
        
        LocalDateTime start = toLDT((Date) spinBatDau.getValue());
        LocalDateTime end = toLDT((Date) spinKetThuc.getValue());
        
        if (end.isBefore(start)) {
            return "Lỗi: Ngày kết thúc không thể nhỏ hơn ngày bắt đầu!";
        }
        return null;
    }

    @Override
    protected void collectData(DotTuyenSinh d) {
        d.setTenDot(getText(txtTen));
        d.setNgayBatDau(toLDT((Date) spinBatDau.getValue()));
        d.setNgayKetThuc(toLDT((Date) spinKetThuc.getValue()));
        d.setNgayCongBo(toLDT((Date) spinCongBo.getValue()));
        d.setTrangThai(cbTrangThai.getSelectedItem().toString());
        if (isAddNew) d.setNgayTao(LocalDateTime.now());
    }

    @Override
    protected void persist(DotTuyenSinh d) {
        if (isAddNew) service.save(d); else service.update(d);
    }
}