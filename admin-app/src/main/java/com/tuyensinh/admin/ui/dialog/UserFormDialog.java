package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.ui.components.RoundedPasswordField;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.model.NhomQuyen;
import com.tuyensinh.model.User;
import com.tuyensinh.service.NhomQuyenService;
import com.tuyensinh.service.UserService;
import com.tuyensinh.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class UserFormDialog extends BaseFormDialog<User> {

    private final UserService userService = new UserService();
    private final NhomQuyenService nqService = new NhomQuyenService();

    private RoundedTextField txtUsername;
    private RoundedPasswordField txtPassword;
    private RoundedTextField txtHoTen;
    private RoundedTextField txtBoPhan;
    private JComboBox<NhomQuyen> cbNhomQuyen;
    private JComboBox<String> cbTrangThai;

    public UserFormDialog(Frame parent, User user, boolean isAddNew) {
        super(parent, "Người Dùng", user, isAddNew, 450, 480);
        buildFormFields();
        if (!isAddNew) populateForm(user);
    }

    @Override
    protected void buildFormFields() {
        txtUsername = new RoundedTextField("Tên đăng nhập (Bắt buộc)");
        txtPassword = new RoundedPasswordField(isAddNew ? "Mật khẩu (Bắt buộc)" : "Để trống nếu không đổi pass");
        txtHoTen = new RoundedTextField("Họ và tên");
        txtBoPhan = new RoundedTextField("Phòng ban / Khoa");

        // Setup ComboBox Nhóm Quyền
        cbNhomQuyen = new JComboBox<>();
        for (NhomQuyen nq : nqService.findAllSync()) {
            if ("HOAT_DONG".equals(nq.getTrangThai())) cbNhomQuyen.addItem(nq);
        }
        cbNhomQuyen.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhomQuyen) setText(((NhomQuyen) value).getTenNhom());
                return this;
            }
        });

        cbTrangThai = new JComboBox<>(new String[]{"HOAT_DONG", "KHOA"});

        addField("Tên đăng nhập:", txtUsername);
        addField("Mật khẩu:", txtPassword);
        addField("Họ tên:", txtHoTen);
        addField("Bộ phận:", txtBoPhan);
        addField("Nhóm Quyền:", cbNhomQuyen);
        addField("Trạng thái:", cbTrangThai);
    }

    @Override
    protected void populateForm(User u) {
        txtUsername.setText(u.getUsername());
        txtUsername.setEnabled(false); // Không cho sửa username
        txtHoTen.setText(u.getHoTen());
        txtBoPhan.setText(u.getBoPhan());
        cbTrangThai.setSelectedItem(u.getTrangThai());
        
        for (int i = 0; i < cbNhomQuyen.getItemCount(); i++) {
            if (cbNhomQuyen.getItemAt(i).getId().equals(u.getNhomQuyen().getId())) {
                cbNhomQuyen.setSelectedIndex(i);
                break;
            }
        }
    }

    @Override
    protected String validateData() {
        if (isBlank(txtUsername) || isBlank(txtHoTen) || cbNhomQuyen.getSelectedItem() == null) {
            return "Vui lòng nhập đủ các trường bắt buộc và chọn Nhóm quyền!";
        }
        String p = new String(txtPassword.getPassword()).trim();
        if (isAddNew && p.isEmpty()) {
            return "Thêm mới bắt buộc phải nhập Mật khẩu!";
        }
        return null;
    }

    @Override
    protected void collectData(User u) {
        u.setUsername(getText(txtUsername));
        u.setHoTen(getText(txtHoTen));
        u.setBoPhan(getText(txtBoPhan));
        u.setNhomQuyen((NhomQuyen) cbNhomQuyen.getSelectedItem());
        u.setTrangThai(cbTrangThai.getSelectedItem().toString());

        String p = new String(txtPassword.getPassword()).trim();
        if (!p.isEmpty()) {
            u.setPasswordHash(PasswordUtil.hash(p));
        }
        if (isAddNew) u.setNgayTao(LocalDateTime.now());
    }

    @Override
    protected void persist(User u) {
        if (isAddNew) userService.save(u);
        else userService.update(u);
    }
}