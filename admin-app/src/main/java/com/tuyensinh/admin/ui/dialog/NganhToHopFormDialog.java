package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.model.Nganh;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.model.ToHopMon;
import com.tuyensinh.service.NganhService;
import com.tuyensinh.service.NganhToHopService;
import com.tuyensinh.service.ToHopMonService;

import javax.swing.*;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class NganhToHopFormDialog extends BaseFormDialog<NganhToHop> {

    // Components cho việc chọn Ngành và Tổ hợp
    private JComboBox<Nganh> cbNganh;
    private JComboBox<ToHopMon> cbToHop;

    // Components cho cấu hình môn và hệ số
    private RoundedTextField txtMon1;
    private RoundedTextField txtHs1;
    private RoundedTextField txtMon2;
    private RoundedTextField txtHs2;
    private RoundedTextField txtMon3;
    private RoundedTextField txtHs3;
    private RoundedTextField txtDolech;

    private final NganhToHopService nganhToHopService = new NganhToHopService();
    private final NganhService nganhService = new NganhService();
    private final ToHopMonService toHopMonService = new ToHopMonService();

    private boolean isFilling = false;
    

    public NganhToHopFormDialog(Frame parent, NganhToHop entity, boolean isAddNew) {
        super(parent, "Liên kết Ngành - Tổ hợp", entity, isAddNew, 450, 580);

        buildFormFields();
        loadDataToComboBoxes();

        txtMon1.setEditable(false);
        txtMon2.setEditable(false);
        txtMon3.setEditable(false);

        cbToHop.addActionListener(e -> fillMonFromToHop());
        
        if (!isAddNew) {
            populateForm(entity);
        } else {
            // Giá trị mặc định cho hệ số khi thêm mới
            txtHs1.setText("1");
            txtHs2.setText("1");
            txtHs3.setText("1");
            txtDolech.setText("0.0");
        }
    }

    @Override
    protected void buildFormFields() {
        cbNganh = new JComboBox<>();
        cbToHop = new JComboBox<>();

        txtMon1 = new RoundedTextField("Tên môn 1");
        txtHs1 = new RoundedTextField("Hệ số môn 1");
        txtMon2 = new RoundedTextField("Tên môn 2");
        txtHs2 = new RoundedTextField("Hệ số môn 2");
        txtMon3 = new RoundedTextField("Tên môn 3");
        txtHs3 = new RoundedTextField("Hệ số môn 3");
        txtDolech = new RoundedTextField("Độ lệch điểm");

        addSectionHeader("Thông tin Liên kết");
        addField("Ngành *", cbNganh);
        addField("Tổ hợp *", cbToHop);

        addSectionHeader("Cấu hình Môn & Hệ số");
        addField("Môn 1", txtMon1);
        addField("Hệ số 1", txtHs1);
        
        addField("Môn 2", txtMon2);
        addField("Hệ số 2", txtHs2);

        addField("Môn 3", txtMon3);
        addField("Hệ số 3", txtHs3);

        addSectionHeader("Thông số khác");
        addField("Độ lệch", txtDolech);
    }

    private void fillMonFromToHop() {
        ToHopMon selected = (ToHopMon) cbToHop.getSelectedItem();
        if (selected == null) return;

        txtMon1.setText(selected.getMon1());
        txtMon2.setText(selected.getMon2());
        txtMon3.setText(selected.getMon3());
    }

    private void loadDataToComboBoxes() {
        // Load danh sách ngành
        List<Nganh> dsNganh = nganhService.getAll();
        dsNganh.forEach(cbNganh::addItem);

        // Load danh sách tổ hợp
        List<ToHopMon> dsToHop = toHopMonService.getAll();
        dsToHop.forEach(cbToHop::addItem);
    }

    @Override
    protected void populateForm(NganhToHop nt) {
        if (nt == null) return;

        isFilling = true;

        // Set Ngành
        cbNganh.setSelectedItem(nt.getNganh());

        // Set Tổ hợp
        cbToHop.setSelectedItem(nt.getToHopMon());

        // Fill môn theo tổ hợp
        fillMonFromToHop();

        // ====== HỆ SỐ ======
        txtHs1.setText(String.valueOf(nt.getHsmon1()));
        txtHs2.setText(String.valueOf(nt.getHsmon2()));
        txtHs3.setText(String.valueOf(nt.getHsmon3()));

        // ====== ĐỘ LỆCH ======
        txtDolech.setText(
            nt.getDolech() != null ? nt.getDolech().toString() : "0"
        );

        isFilling = false;
    }

        @Override
        protected void collectData(NganhToHop nt) {
            nt.setNganh((Nganh) cbNganh.getSelectedItem());
            nt.setToHopMon((ToHopMon) cbToHop.getSelectedItem());
            
            nt.setThMon1(getText(txtMon1));
            nt.setHsmon1(Byte.parseByte(getText(txtHs1)));
            
            nt.setThMon2(getText(txtMon2));
            nt.setHsmon2(Byte.parseByte(getText(txtHs2)));
            
            nt.setThMon3(getText(txtMon3));
            nt.setHsmon3(Byte.parseByte(getText(txtHs3)));
            
            nt.setDolech(BigDecimal.valueOf(
            Double.parseDouble(getText(txtDolech).isEmpty() ? "0" : getText(txtDolech))
        ));
    }

    @Override
    protected String validateData() {
        if (cbNganh.getSelectedItem() == null) return "Vui lòng chọn Ngành.";
        if (cbToHop.getSelectedItem() == null) return "Vui lòng chọn Tổ hợp môn.";
        
        try {
            Double.parseDouble(getText(txtHs1));
            Double.parseDouble(getText(txtHs2));
            Double.parseDouble(getText(txtHs3));
            Double.parseDouble(getText(txtDolech));
        } catch (NumberFormatException e) {
            return "Hệ số và Độ lệch phải là định dạng số (VD: 1.0).";
        }

        return null;
    }

    @Override
    protected void persist(NganhToHop nt) {
        if (isAddNew) {
            nganhToHopService.save(nt);
        } else {
            nganhToHopService.save(nt);
        }
    }
}