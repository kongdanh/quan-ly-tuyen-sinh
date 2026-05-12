package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.model.DiemCong;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.service.DiemCongService;
import com.tuyensinh.service.ThiSinhService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Dialog thêm/sửa điểm cộng.
 */
public class DiemCongDialog extends BaseFormDialog<DiemCong> {

    private final DiemCongService diemCongService = new DiemCongService();
    private final ThiSinhService thiSinhService = new ThiSinhService();

    private JTextField txtCccd;
    private JTextField txtMaNganh;
    private JTextField txtMaToHop;
    private JComboBox<String> cbPhuongThuc;
    private JTextField txtDiemCC;
    private JTextField txtDiemUtxt;
    private JTextField txtDiemTong;
    private JTextArea txtGhiChu;

    public DiemCongDialog(Frame parent, String title, DiemCong entity) {
        super(parent, title, entity, entity == null, 500, 600);
    }

    @Override
    protected void buildFormFields() {
        txtCccd = new JTextField();
        txtMaNganh = new JTextField();
        txtMaToHop = new JTextField();
        cbPhuongThuc = new JComboBox<>(new String[]{"THPT", "DGNL", "VSAT", "IELTS"});
        
        txtDiemCC = new JTextField();
        txtDiemUtxt = new JTextField();
        txtDiemTong = new JTextField();
        txtDiemTong.setEditable(false);
        txtDiemTong.setBackground(new Color(245, 245, 245));

        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setLineWrap(true);

        addField("CCCD Thí sinh:", txtCccd);
        addField("Mã Ngành:", txtMaNganh);
        addField("Mã Tổ Hợp:", txtMaToHop);
        addField("Phương Thức:", cbPhuongThuc);
        addField("Điểm Chứng Chỉ:", txtDiemCC);
        addField("Điểm Ưu Tiên:", txtDiemUtxt);
        addField("Tổng Điểm Cộng:", txtDiemTong);
        
        // Ghi chú dùng JTextArea
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
    protected void setupExtras() {
        DocumentListener recalcListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalc(); }
            @Override public void removeUpdate(DocumentEvent e) { recalc(); }
            @Override public void changedUpdate(DocumentEvent e) { recalc(); }
            private void recalc() {
                try {
                    BigDecimal dcc = getBigDecimal(txtDiemCC);
                    BigDecimal dut = getBigDecimal(txtDiemUtxt);
                    txtDiemTong.setText(dcc.add(dut).setScale(2, RoundingMode.HALF_UP).toString());
                } catch (Exception ignored) {}
            }
        };
        txtDiemCC.getDocument().addDocumentListener(recalcListener);
        txtDiemUtxt.getDocument().addDocumentListener(recalcListener);
    }

    private BigDecimal getBigDecimal(JTextField tf) {
        String val = tf.getText().trim();
        if (val.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(val);
    }

    @Override
    protected void populateForm(DiemCong entity) {
        if (entity == null) return;
        txtCccd.setText(entity.getTsCccd());
        txtMaNganh.setText(entity.getManganh());
        txtMaToHop.setText(entity.getMatohop());
        cbPhuongThuc.setSelectedItem(entity.getPhuongthuc());
        txtDiemCC.setText(entity.getDiemCC() != null ? entity.getDiemCC().toString() : "0");
        txtDiemUtxt.setText(entity.getDiemUtxt() != null ? entity.getDiemUtxt().toString() : "0");
        txtDiemTong.setText(entity.getDiemTong() != null ? entity.getDiemTong().toString() : "0");
        txtGhiChu.setText(entity.getGhichu());
    }

    @Override
    protected void collectData(DiemCong entity) {
        entity.setTsCccd(txtCccd.getText().trim());
        entity.setManganh(txtMaNganh.getText().trim());
        entity.setMatohop(txtMaToHop.getText().trim());
        entity.setPhuongthuc((String) cbPhuongThuc.getSelectedItem());
        entity.setDiemCC(getBigDecimal(txtDiemCC));
        entity.setDiemUtxt(getBigDecimal(txtDiemUtxt));
        entity.setDiemTong(entity.getDiemCC().add(entity.getDiemUtxt()));
        entity.setGhichu(txtGhiChu.getText().trim());
        entity.setDcKeys(entity.getTsCccd() + "_" + entity.getManganh() + "_" + entity.getMatohop());
    }

    @Override
    protected String validateData() {
        if (txtCccd.getText().trim().isEmpty()) return "Vui lòng nhập CCCD";
        if (txtMaNganh.getText().trim().isEmpty()) return "Vui lòng nhập Mã ngành";
        try {
            getBigDecimal(txtDiemCC);
            getBigDecimal(txtDiemUtxt);
        } catch (Exception e) {
            return "Điểm phải là số hợp lệ";
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
