package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.model.BangQuyDoi;
import com.tuyensinh.dao.BangQuyDoiDAO;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class BangQuyDoiDialog extends BaseFormDialog<BangQuyDoi> {

    private final BangQuyDoiDAO dao;

    private JComboBox<String> cbPhuongThuc;
    private JTextField txtToHop;
    private JTextField txtMon;
    private JTextField txtDiemA;
    private JTextField txtDiemB;
    private JTextField txtDiemC;
    private JTextField txtDiemD;

    public BangQuyDoiDialog(Frame parent, String title, BangQuyDoi entity, BangQuyDoiDAO dao) {
        super(parent, title, entity, entity == null, 500, 500);
        this.dao = dao;
        buildFormFields();
        populateForm(entity);
    }

    @Override
    protected void buildFormFields() {
        cbPhuongThuc = new JComboBox<>(new String[]{"VSAT", "DGNL", "IELTS", "THPT"});
        txtToHop = createTextField();
        txtMon = createTextField();
        txtDiemA = createTextField();
        txtDiemB = createTextField();
        txtDiemC = createTextField();
        txtDiemD = createTextField();

        addSectionHeader("Cấu hình Bảng Quy Đổi");
        addField("Phương Thức:", cbPhuongThuc);
        addField("Tổ Hợp:", txtToHop);
        addField("Mã Môn:", txtMon);
        addField("Điểm A:", txtDiemA);
        addField("Điểm B:", txtDiemB);
        addField("Điểm C:", txtDiemC);
        addField("Điểm D:", txtDiemD);
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(250, 35));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode(UIConstants.COLOR_BORDER)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return tf;
    }

    @Override
    protected void populateForm(BangQuyDoi entity) {
        if (entity != null) {
            if (entity.getDPhuongthuc() != null) cbPhuongThuc.setSelectedItem(entity.getDPhuongthuc());
            txtToHop.setText(entity.getDTohop());
            txtMon.setText(entity.getDMon());
            txtDiemA.setText(entity.getDDiema() != null ? entity.getDDiema().toString() : "");
            txtDiemB.setText(entity.getDDiemb() != null ? entity.getDDiemb().toString() : "");
            txtDiemC.setText(entity.getDDiemc() != null ? entity.getDDiemc().toString() : "");
            txtDiemD.setText(entity.getDDiemd() != null ? entity.getDDiemd().toString() : "");
        }
    }

    @Override
    protected void collectData(BangQuyDoi entity) {
        entity.setDPhuongthuc((String) cbPhuongThuc.getSelectedItem());
        entity.setDTohop(getText(txtToHop));
        entity.setDMon(getText(txtMon));
        
        try {
            entity.setDDiema(isBlank(txtDiemA) ? null : new BigDecimal(getText(txtDiemA)));
            entity.setDDiemb(isBlank(txtDiemB) ? null : new BigDecimal(getText(txtDiemB)));
            entity.setDDiemc(isBlank(txtDiemC) ? null : new BigDecimal(getText(txtDiemC)));
            entity.setDDiemd(isBlank(txtDiemD) ? null : new BigDecimal(getText(txtDiemD)));
        } catch (NumberFormatException e) {
            // Checked in validateData
        }

        // Tự động suy luận d_maquydoi nếu có đủ phương thức, tổ hợp
        String pt = entity.getDPhuongthuc() != null ? entity.getDPhuongthuc() : "";
        String th = entity.getDTohop() != null ? entity.getDTohop() : "";
        String mon = entity.getDMon() != null ? entity.getDMon() : "";
        entity.setDMaquydoi(pt + "_" + th + "_" + mon + "_" + System.currentTimeMillis());
    }

    @Override
    protected String validateData() {
        if (isBlank(txtMon)) return "Vui lòng nhập Mã Môn!";
        try {
            if (!isBlank(txtDiemA)) new BigDecimal(getText(txtDiemA));
            if (!isBlank(txtDiemB)) new BigDecimal(getText(txtDiemB));
            if (!isBlank(txtDiemC)) new BigDecimal(getText(txtDiemC));
            if (!isBlank(txtDiemD)) new BigDecimal(getText(txtDiemD));
        } catch (NumberFormatException e) {
            return "Điểm phải là số hợp lệ!";
        }
        return null;
    }

    @Override
    protected void persist(BangQuyDoi entityToSave) {
        BangQuyDoi e = isAddNew ? new BangQuyDoi() : this.entity;
        collectData(e);
        if (isAddNew) {
            dao.save(e);
        } else {
            dao.update(e);
        }
    }
}
