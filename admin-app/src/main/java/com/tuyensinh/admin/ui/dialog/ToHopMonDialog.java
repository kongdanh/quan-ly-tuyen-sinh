package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.model.ToHopMon;
import com.tuyensinh.dao.ToHopMonDAO;

import javax.swing.*;
import java.awt.*;

public class ToHopMonDialog extends BaseFormDialog<ToHopMon> {

    private final ToHopMonDAO dao;

    private JTextField txtMaToHop;
    private JTextField txtTenToHop;
    private JTextField txtMon1;
    private JTextField txtMon2;
    private JTextField txtMon3;

    public ToHopMonDialog(Frame parent, String title, ToHopMon entity, ToHopMonDAO dao) {
        super(parent, title, entity, entity == null, 400, 450);
        this.dao = dao;
        buildFormFields();
        populateForm(entity);
    }

    @Override
    protected void buildFormFields() {
        txtMaToHop = createTextField();
        txtTenToHop = createTextField();
        txtMon1 = createTextField();
        txtMon2 = createTextField();
        txtMon3 = createTextField();

        addSectionHeader("Thông Tin Tổ Hợp");
        addField("Mã Tổ Hợp:", txtMaToHop);
        addField("Tên Tổ Hợp:", txtTenToHop);
        addField("Môn 1:", txtMon1);
        addField("Môn 2:", txtMon2);
        addField("Môn 3:", txtMon3);
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
    protected void populateForm(ToHopMon entity) {
        if (entity != null) {
            txtMaToHop.setText(entity.getMatohop());
            txtTenToHop.setText(entity.getTentohop());
            txtMon1.setText(entity.getMon1());
            txtMon2.setText(entity.getMon2());
            txtMon3.setText(entity.getMon3());
            
            if (!isAddNew) {
                txtMaToHop.setEditable(false);
            }
        }
    }

    @Override
    protected void collectData(ToHopMon entity) {
        entity.setMatohop(getText(txtMaToHop));
        entity.setTentohop(getText(txtTenToHop));
        entity.setMon1(getText(txtMon1));
        entity.setMon2(getText(txtMon2));
        entity.setMon3(getText(txtMon3));
    }

    @Override
    protected String validateData() {
        if (isBlank(txtMaToHop)) return "Mã tổ hợp không được trống!";
        if (isBlank(txtMon1)) return "Môn 1 không được trống!";
        if (isBlank(txtMon2)) return "Môn 2 không được trống!";
        if (isBlank(txtMon3)) return "Môn 3 không được trống!";
        return null;
    }

    @Override
    protected void persist(ToHopMon entityToSave) {
        ToHopMon e = isAddNew ? new ToHopMon() : this.entity;
        collectData(e);
        
        if (isAddNew) {
            dao.save(e);
        } else {
            dao.update(e);
        }
    }
}
