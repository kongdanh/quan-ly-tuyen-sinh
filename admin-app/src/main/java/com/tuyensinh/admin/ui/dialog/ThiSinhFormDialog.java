package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.dao.ThiSinhDAO;
import com.tuyensinh.model.ThiSinh;

import javax.swing.*;
import java.awt.*;

public class ThiSinhFormDialog extends BaseFormDialog<ThiSinh> {

    private RoundedTextField txtHo;
    private RoundedTextField txtTen;
    private RoundedTextField txtCccd;
    private RoundedTextField txtNgaySinh;
    private JComboBox<String> cbGioiTinh;
    private RoundedTextField txtDienThoai;
    private RoundedTextField txtEmail;
    private RoundedTextField txtNoiSinh;
    private RoundedTextField txtDoiTuong;
    private RoundedTextField txtKhuVuc;

    private final ThiSinhDAO thiSinhDAO = new ThiSinhDAO();

    public ThiSinhFormDialog(Frame parent, ThiSinh entity, boolean isAddNew) {
        super(parent, "Hồ sơ Thí sinh", entity, isAddNew, 420, 630);
        
        buildFormFields();
        if (!isAddNew) populateForm(entity);
        setupExtras();
    }

    @Override
    protected void buildFormFields() {
        txtHo        = new RoundedTextField("Nhập họ đệm");
        txtTen       = new RoundedTextField("Nhập tên");
        txtCccd      = new RoundedTextField("12 chữ số");
        txtNgaySinh  = new RoundedTextField("DD/MM/YYYY");
        cbGioiTinh   = new JComboBox<>(new String[]{"Nam", "Nữ"});
        txtNoiSinh   = new RoundedTextField("Tỉnh/Thành phố");
        txtKhuVuc    = new RoundedTextField("Khu vực");
        txtDoiTuong  = new RoundedTextField("Ưu tiên");
        txtDienThoai = new RoundedTextField("Số điện thoại");
        txtEmail     = new RoundedTextField("example@email.com");

        addSectionHeader("Thông tin cơ bản");
        addField("Họ đệm *",      txtHo);
        addField("Tên *",         txtTen);
        addField("CCCD *",        txtCccd);
        addField("Ngày sinh",     txtNgaySinh);
        addField("Giới tính",     cbGioiTinh);

        addSectionHeader("Liên hệ & Khu vực");
        addField("Nơi sinh",      txtNoiSinh);
        addField("Khu vực",       txtKhuVuc);
        addField("Đối tượng",     txtDoiTuong);
        addField("Điện thoại",    txtDienThoai);
        addField("Email",         txtEmail);
    }

    @Override
    protected void populateForm(ThiSinh ts) {
        if (ts == null) return;
        txtHo.setText(ts.getHo());
        txtTen.setText(ts.getTen());
        txtCccd.setText(ts.getCccd());
        txtNgaySinh.setText(ts.getNgaySinh());
        if (ts.getGioiTinh() != null) cbGioiTinh.setSelectedItem(ts.getGioiTinh());
        txtNoiSinh.setText(ts.getNoiSinh());
        txtKhuVuc.setText(ts.getKhuVuc());
        txtDoiTuong.setText(ts.getDoiTuong());
        txtDienThoai.setText(ts.getDienThoai());
        txtEmail.setText(ts.getEmail());
        
        if (!isAddNew) {
            txtCccd.setEnabled(false);
        }
    }

    @Override
    protected void collectData(ThiSinh ts) {
        ts.setHo(getText(txtHo));
        ts.setTen(getText(txtTen));
        if (isAddNew) ts.setCccd(getText(txtCccd)); 
        ts.setNgaySinh(getText(txtNgaySinh));
        ts.setGioiTinh((String) cbGioiTinh.getSelectedItem());
        ts.setNoiSinh(getText(txtNoiSinh));
        ts.setKhuVuc(getText(txtKhuVuc));
        ts.setDoiTuong(getText(txtDoiTuong));
        ts.setDienThoai(getText(txtDienThoai));
        ts.setEmail(getText(txtEmail));
    }

    @Override
    protected String validateData() {
        if (isBlank(txtHo))   return "Họ đệm không được để trống.";
        if (isBlank(txtTen))  return "Tên không được để trống.";
        if (isAddNew && isBlank(txtCccd)) return "Số CCCD không được để trống.";

        if (isAddNew) {
            String cccd = getText(txtCccd);
            if (!cccd.matches("^(\\d{12}|TS_?\\d+)$")) {
                return "CCCD phải là 12 chữ số hoặc dạng TS_0001.";
            }
        }

        String email = getText(txtEmail);
        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            return "Email không đúng định dạng.";
        }

        return null; 
    }

    @Override
    protected void persist(ThiSinh ts) {
        if (isAddNew) thiSinhDAO.save(ts);
        else          thiSinhDAO.update(ts);
    }
}