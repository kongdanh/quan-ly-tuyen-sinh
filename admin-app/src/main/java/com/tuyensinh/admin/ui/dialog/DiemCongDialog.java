package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.model.DiemCong;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.service.DiemCongService;
import com.tuyensinh.service.ThiSinhService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Optional;

public class DiemCongDialog extends BaseFormDialog<DiemCong> {

    private final DiemCongService diemCongService = new DiemCongService();
    private final ThiSinhService thiSinhService = new ThiSinhService();

    private JTextField txtCccd;
    private JTextField txtDiemCC;
    private JTextField txtDiemUt;
    private JTextArea txtGhiChu;
    
    public DiemCongDialog(Frame parent, DiemCong entity) {
        super(parent, "Điểm cộng", entity, entity == null, 450, 400);
    }

    @Override
    protected void buildFormFields() {
        txtCccd = new JTextField();
        txtDiemCC = new JTextField();
        txtDiemUt = new JTextField();
        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setLineWrap(true);

        addField("CCCD:", txtCccd);
        addField("Điểm CC:", txtDiemCC);
        addField("Điểm ƯT:", txtDiemUt);
        
        JPanel areaRow = new JPanel(new BorderLayout(10, 0));
        areaRow.setBackground(Color.WHITE);
        JLabel lbl = new JLabel("Ghi chú:");
        lbl.setPreferredSize(new Dimension(95, 30));
        areaRow.add(lbl, BorderLayout.WEST);
        areaRow.add(new JScrollPane(txtGhiChu), BorderLayout.CENTER);
        formBody.add(areaRow);

        if (!isAddNew) {
            txtCccd.setEditable(false);
            populateForm(entity);
        }
    }

    @Override
    protected void populateForm(DiemCong entity) {
        if (entity == null) return;
        txtCccd.setText(entity.getTsCccd());
        txtDiemCC.setText(entity.getDiemCC() != null ? entity.getDiemCC().toString() : "0");
        txtDiemUt.setText(entity.getDiemUtxt() != null ? entity.getDiemUtxt().toString() : "0");
        txtGhiChu.setText(entity.getGhichu());
    }

    @Override
    protected void collectData(DiemCong entity) {
        entity.setTsCccd(txtCccd.getText().trim());
        entity.setDiemCC(new BigDecimal(txtDiemCC.getText().trim()));
        entity.setDiemUtxt(new BigDecimal(txtDiemUt.getText().trim()));
        entity.setDiemTong(entity.getDiemCC().add(entity.getDiemUtxt()));
        entity.setGhichu(txtGhiChu.getText().trim());
        entity.setPhuongthuc("THPT");
    }

    @Override
    protected String validateData() {
        if (txtCccd.getText().trim().isEmpty()) return "Vui lòng nhập CCCD";
        try {
            new BigDecimal(txtDiemCC.getText().trim());
            new BigDecimal(txtDiemUt.getText().trim());
        } catch (Exception e) {
            return "Điểm phải là định dạng số";
        }
        return null;
    }

    @Override
    protected void persist(DiemCong entity) {
        if (isAddNew) {
            Optional<ThiSinh> tsOpt = thiSinhService.findByCccd(entity.getTsCccd());
            if (tsOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy thí sinh với CCCD này!");
            }
            entity.setThiSinh(tsOpt.get());
        }
        diemCongService.saveOrUpdateAsync(entity).join();
    }
}
