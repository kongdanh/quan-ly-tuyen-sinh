package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.model.DiemCong;
import com.tuyensinh.dao.DiemCongDAO;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DiemCongDialog extends BaseFormDialog<DiemCong> {

    private final DiemCongDAO dao;

    private JTextField txtCccd;
    private JTextField txtMaNganh;
    private JTextField txtMaToHop;
    private JComboBox<String> cbPhuongThuc;
    private JTextField txtDiemCC;
    private JTextField txtDiemUtxt;
    private JTextField txtDiemTong;
    private JTextField txtGhiChu;

    public DiemCongDialog(Frame parent, String title, DiemCong entity, DiemCongDAO dao) {
        super(parent, title, entity, entity == null, 500, 600);
        this.dao = dao;
        buildFormFields();
        populateForm(entity);
        setupExtras();
    }

    @Override
    protected void buildFormFields() {
        txtCccd = createTextField();
        txtMaNganh = createTextField();
        txtMaToHop = createTextField();
        cbPhuongThuc = new JComboBox<>(new String[]{"VSAT", "DGNL", "IELTS", "THPT"});
        cbPhuongThuc.setPreferredSize(new Dimension(250, 35));
        
        txtDiemCC = createTextField();
        txtDiemUtxt = createTextField();
        
        txtDiemTong = createTextField();
        txtDiemTong.setEditable(false); // Chỉ đọc, tự tính toán
        txtDiemTong.setBackground(Color.decode("#f3f4f6"));

        txtGhiChu = createTextField();

        addSectionHeader("Thông Tin Hồ Sơ");
        addField("CCCD:", txtCccd);
        addField("Mã Ngành:", txtMaNganh);
        addField("Tổ Hợp:", txtMaToHop);
        addField("Phương Thức:", cbPhuongThuc);
        
        addSectionHeader("Điểm Quy Đổi & Điểm Cộng");
        addField("Điểm Chứng Chỉ:", txtDiemCC);
        addField("Điểm Ưu Tiên:", txtDiemUtxt);
        addField("Tổng Điểm:", txtDiemTong);
        addField("Ghi Chú:", txtGhiChu);
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
    protected void setupExtras() {
        // Tự động tính DiemTong = DiemCC + DiemUtxt
        DocumentListener recalcListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalc(); }
            @Override public void removeUpdate(DocumentEvent e) { recalc(); }
            @Override public void changedUpdate(DocumentEvent e) { recalc(); }
            private void recalc() {
                try {
                    BigDecimal dcc = isBlank(txtDiemCC) ? BigDecimal.ZERO : new BigDecimal(getText(txtDiemCC));
                    BigDecimal dut = isBlank(txtDiemUtxt) ? BigDecimal.ZERO : new BigDecimal(getText(txtDiemUtxt));
                    BigDecimal tong = dcc.add(dut).setScale(2, RoundingMode.HALF_UP);
                    txtDiemTong.setText(tong.toString());
                } catch (NumberFormatException ignored) {
                    txtDiemTong.setText("Lỗi định dạng số");
                }
            }
        };
        txtDiemCC.getDocument().addDocumentListener(recalcListener);
        txtDiemUtxt.getDocument().addDocumentListener(recalcListener);
    }

    @Override
    protected void populateForm(DiemCong entity) {
        if (entity != null) {
            if (entity.getThiSinh() != null) txtCccd.setText(entity.getThiSinh().getCccd());
            txtMaNganh.setText(entity.getManganh());
            txtMaToHop.setText(entity.getMatohop());
            if (entity.getPhuongthuc() != null) cbPhuongThuc.setSelectedItem(entity.getPhuongthuc());
            
            txtDiemCC.setText(entity.getDiemCC() != null ? entity.getDiemCC().toString() : "");
            txtDiemUtxt.setText(entity.getDiemUtxt() != null ? entity.getDiemUtxt().toString() : "");
            txtDiemTong.setText(entity.getDiemTong() != null ? entity.getDiemTong().toString() : "");
            txtGhiChu.setText(entity.getGhichu());
            
            // Nếu edit thì không nên cho sửa CCCD
            txtCccd.setEditable(false);
        }
    }

    @Override
    protected void collectData(DiemCong entity) {
        // Note: Việc gán ThiSinh (foreign key) cần phải lấy từ DB qua ThiSinhDAO.
        // Tạm thời chỉ thiết lập nếu đã có, còn nếu add new thì cần logic load ThiSinh (bỏ qua ở demo này hoặc có service kèm theo).
        
        entity.setManganh(getText(txtMaNganh));
        entity.setMatohop(getText(txtMaToHop));
        entity.setPhuongthuc((String) cbPhuongThuc.getSelectedItem());
        
        try {
            entity.setDiemCC(isBlank(txtDiemCC) ? BigDecimal.ZERO : new BigDecimal(getText(txtDiemCC)));
            entity.setDiemUtxt(isBlank(txtDiemUtxt) ? BigDecimal.ZERO : new BigDecimal(getText(txtDiemUtxt)));
            entity.setDiemTong(isBlank(txtDiemTong) || txtDiemTong.getText().equals("Lỗi định dạng số") 
                ? BigDecimal.ZERO : new BigDecimal(getText(txtDiemTong)));
        } catch (NumberFormatException ignored) {}
        
        entity.setGhichu(getText(txtGhiChu));

        // Tự động suy luận dc_keys (ts_cccd_manganh_matohop)
        String cccd = getText(txtCccd);
        entity.setDcKeys(cccd + "_" + entity.getManganh() + "_" + entity.getMatohop());
    }

    @Override
    protected String validateData() {
        if (isBlank(txtCccd)) return "CCCD không được trống!";
        if (isBlank(txtMaNganh) || isBlank(txtMaToHop)) return "Mã ngành/Tổ hợp không được trống!";
        try {
            if (!isBlank(txtDiemCC)) new BigDecimal(getText(txtDiemCC));
            if (!isBlank(txtDiemUtxt)) new BigDecimal(getText(txtDiemUtxt));
        } catch (NumberFormatException e) {
            return "Điểm phải là số hợp lệ!";
        }
        return null;
    }

    @Override
    protected void persist(DiemCong entityToSave) {
        // Trong thực tế, lúc add new cần gọi thiSinhDAO.findByCccd để map quan hệ ThiSinh.
        // Do DiemCong phụ thuộc vào tồn tại của ThiSinh.
        DiemCong e = isAddNew ? new DiemCong() : this.entity;
        collectData(e);
        if (isAddNew) {
            // Không thực hiện việc create ThiSinh giả ở đây, giả định logic update cho demo.
            dao.save(e);
        } else {
            dao.update(e);
        }
    }
}
