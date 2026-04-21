package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.dao.DiemThiXetTuyenDAO;
import com.tuyensinh.dao.ThiSinhDAO;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.model.ThiSinh;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class DiemThiFormDialog extends BaseFormDialog<DiemThiXetTuyen> {

    private final ThiSinhDAO thiSinhDAO = new ThiSinhDAO();
    private final DiemThiXetTuyenDAO diemDAO = new DiemThiXetTuyenDAO();

    private RoundedTextField txtCccd;
    private RoundedTextField txtSobaodanh;
    private JComboBox<String> cbPhuongThuc;

    private final Map<String, RoundedTextField> scoreFields = new HashMap<>();

    public DiemThiFormDialog(Frame parent, DiemThiXetTuyen entity, boolean isAddNew) {
        super(parent, "Điểm thi xét tuyển", entity, isAddNew, 540, 760);

        buildFormFields();
        if (!isAddNew) {
            populateForm(entity);
        }
        setupExtras();
    }

    @Override
    protected void buildFormFields() {
        txtCccd = new RoundedTextField("Nhập CCCD của thí sinh");
        txtSobaodanh = new RoundedTextField("Số báo danh (có thể để trống)");
        cbPhuongThuc = new JComboBox<>(new String[]{"THPT", "VSAT", "DGNL"});

        addSectionHeader("Thông tin chung");
        addField("CCCD *", txtCccd);
        addField("Số báo danh", txtSobaodanh);
        addField("Phương thức", cbPhuongThuc);

        addSectionHeader("Nhóm điểm học thuật");
        addScoreField("TO", "Toán");
        addScoreField("LI", "Vật lý");
        addScoreField("HO", "Hóa học");
        addScoreField("SI", "Sinh học");
        addScoreField("VA", "Ngữ văn");
        addScoreField("SU", "Lịch sử");
        addScoreField("DI", "Địa lý");
        addScoreField("KTPL", "GDCD");

        addSectionHeader("Nhóm ngoại ngữ và bổ sung");
        addScoreField("N1_THI", "Ngoại ngữ thi");
        addScoreField("N1_CC", "Ngoại ngữ chứng chỉ");
        addScoreField("CNCN", "Công nghệ CN");
        addScoreField("CNNN", "Công nghệ NN");
        addScoreField("TI", "Tin học");
        addScoreField("NL1", "ĐGNL");
        addScoreField("NK1", "Năng khiếu 1");
        addScoreField("NK2", "Năng khiếu 2");
    }

    @Override
    protected void populateForm(DiemThiXetTuyen d) {
        if (d == null) {
            return;
        }

        if (d.getThiSinh() != null) {
            txtCccd.setText(d.getThiSinh().getCccd());
        } else {
            txtCccd.setText(defaultText(d.getCccd()));
        }
        txtSobaodanh.setText(defaultText(d.getSobaodanh()));
        if (d.getDPhuongthuc() != null) {
            cbPhuongThuc.setSelectedItem(DiemThiXetTuyenDAO.normalizeMethod(d.getDPhuongthuc()));
        }

        setScoreText("TO", d.getTo());
        setScoreText("LI", d.getLi());
        setScoreText("HO", d.getHo());
        setScoreText("SI", d.getSi());
        setScoreText("VA", d.getVa());
        setScoreText("SU", d.getSu());
        setScoreText("DI", d.getDi());
        setScoreText("KTPL", d.getKtpl());
        setScoreText("N1_THI", d.getN1Thi());
        setScoreText("N1_CC", d.getN1Cc());
        setScoreText("CNCN", d.getCncn());
        setScoreText("CNNN", d.getCnnn());
        setScoreText("TI", d.getTi());
        setScoreText("NL1", d.getNl1());
        setScoreText("NK1", d.getNk1());
        setScoreText("NK2", d.getNk2());
    }

    @Override
    protected void collectData(DiemThiXetTuyen d) {
        String cccd = getText(txtCccd);

        ThiSinh thiSinh = thiSinhDAO.findByCccd(cccd)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thí sinh với CCCD: " + cccd));

        d.setThiSinh(thiSinh);
        d.setSobaodanh(getText(txtSobaodanh).isEmpty() ? thiSinh.getSobaodanh() : getText(txtSobaodanh));
        d.setDPhuongthuc((String) cbPhuongThuc.getSelectedItem());

        d.setTo(parseScore("TO"));
        d.setLi(parseScore("LI"));
        d.setHo(parseScore("HO"));
        d.setSi(parseScore("SI"));
        d.setVa(parseScore("VA"));
        d.setSu(parseScore("SU"));
        d.setDi(parseScore("DI"));
        d.setKtpl(parseScore("KTPL"));

        d.setN1Thi(parseScore("N1_THI"));
        d.setN1Cc(parseScore("N1_CC"));
        d.setCncn(parseScore("CNCN"));
        d.setCnnn(parseScore("CNNN"));
        d.setTi(parseScore("TI"));
        d.setNl1(parseScore("NL1"));
        d.setNk1(parseScore("NK1"));
        d.setNk2(parseScore("NK2"));
    }

    @Override
    protected String validateData() {
        String cccd = getText(txtCccd);
        if (cccd.isEmpty()) {
            return "CCCD không được để trống.";
        }

        if (isAddNew && diemDAO.findByCccd(cccd).isPresent()) {
            return "CCCD này đã có bản ghi điểm thi.";
        }

        if (thiSinhDAO.findByCccd(cccd).isEmpty()) {
            return "CCCD chưa tồn tại trong danh sách thí sinh.";
        }

        for (Map.Entry<String, RoundedTextField> entry : scoreFields.entrySet()) {
            String raw = entry.getValue().getText() == null ? "" : entry.getValue().getText().trim();
            if (raw.isEmpty()) {
                continue;
            }
            try {
                BigDecimal score = new BigDecimal(raw);
                BigDecimal max = "NL1".equals(entry.getKey()) ? new BigDecimal("1200") : new BigDecimal("10");
                if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(max) > 0) {
                    return "Điểm " + entry.getKey() + " phải nằm trong khoảng 0 - " + max.stripTrailingZeros().toPlainString() + ".";
                }
            } catch (NumberFormatException ex) {
                return "Điểm " + entry.getKey() + " không đúng định dạng số.";
            }
        }

        return null;
    }

    @Override
    protected void persist(DiemThiXetTuyen d) {
        if (isAddNew) {
            diemDAO.save(d);
        } else {
            diemDAO.update(d);
        }
    }

    @Override
    protected void setupExtras() {
        if (!isAddNew) {
            txtCccd.setEnabled(false);
        }
    }

    private void addScoreField(String key, String label) {
        RoundedTextField field = new RoundedTextField("0.00");
        scoreFields.put(key, field);
        addField(label, field);
    }

    private BigDecimal parseScore(String key) {
        RoundedTextField field = scoreFields.get(key);
        if (field == null || field.getText() == null || field.getText().trim().isEmpty()) {
            return null;
        }
        return new BigDecimal(field.getText().trim()).setScale(2, RoundingMode.HALF_UP);
    }

    private void setScoreText(String key, BigDecimal value) {
        RoundedTextField field = scoreFields.get(key);
        if (field != null && value != null) {
            field.setText(value.stripTrailingZeros().toPlainString());
        }
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }
}
