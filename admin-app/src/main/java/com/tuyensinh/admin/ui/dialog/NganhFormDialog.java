package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.base.BaseFormDialog;
import com.tuyensinh.admin.ui.components.CustomComboBox;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.model.Nganh;
import com.tuyensinh.service.NganhService;

import java.awt.*;
import java.math.BigDecimal;

/**
 * NganhFormDialog - Form them moi / chinh sua nganh tuyen sinh.
 */
public class NganhFormDialog extends BaseFormDialog<Nganh> {

    private final NganhService nganhService = new NganhService();

    private RoundedTextField tfMaNganh;
    private RoundedTextField tfTenNganh;
    private RoundedTextField tfToHopGoc;
    private RoundedTextField tfChiTieu;
    private RoundedTextField tfDiemSan;
    private RoundedTextField tfDiemTrungTuyen;

    private CustomComboBox<String> cbTuyenThang;
    private CustomComboBox<String> cbDgnl;
    private CustomComboBox<String> cbThpt;
    private CustomComboBox<String> cbVsat;

    // "Khong" / "Co"
    private static final String[] YES_NO = { "Kh\u00f4ng", "C\u00f3" };

    private static final int W = 520;
    private static final int H = 540;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public NganhFormDialog(Frame parent, Nganh entity) {
        super(
                parent,
                // "Nganh"
                "Ng\u00e0nh",
                entity != null ? entity : new Nganh(),
                entity == null,
                W, H
        );
        buildFormFields();
        if (!isAddNew) {
            populateForm(this.entity);
            tfMaNganh.setEditable(false);
            tfMaNganh.setEnabled(false);
        }
    }

    // =========================================================================
    // ABSTRACT IMPLEMENTATIONS
    // =========================================================================

    @Override
    protected void buildFormFields() {
        // "Thong tin co ban"
        addSectionHeader("Th\u00f4ng tin c\u01a1 b\u1ea3n");

        // placeholders
        tfMaNganh  = new RoundedTextField("V\u00ed d\u1ee5: 7480201");
        tfTenNganh = new RoundedTextField("Nh\u1eadp t\u00ean ng\u00e0nh \u0111\u1ea7y \u0111\u1ee7");
        tfToHopGoc = new RoundedTextField("V\u00ed d\u1ee5: A00, D01, ...");
        tfChiTieu  = new RoundedTextField("V\u00ed d\u1ee5: 200");

        // labels
        addField("M\u00e3 ng\u00e0nh *",  tfMaNganh);
        addField("T\u00ean ng\u00e0nh *", tfTenNganh);
        addField("T\u1ed5 h\u1ee3p g\u1ed1c", tfToHopGoc);
        addField("Ch\u1ec9 ti\u00eau *",  tfChiTieu);

        // "Diem xet tuyen"
        addSectionHeader("\u0110i\u1ec3m x\u00e9t tuy\u1ec3n");

        tfDiemSan        = new RoundedTextField("V\u00ed d\u1ee5: 15.00");
        tfDiemTrungTuyen = new RoundedTextField("V\u00ed d\u1ee5: 18.50");

        addField("\u0110i\u1ec3m s\u00e0n",            tfDiemSan);
        addField("\u0110i\u1ec3m tr\u00fang tuy\u1ec3n", tfDiemTrungTuyen);

        // "Phuong thuc xet tuyen"
        addSectionHeader("Ph\u01b0\u01a1ng th\u1ee9c x\u00e9t tuy\u1ec3n");

        cbTuyenThang = new CustomComboBox<>(YES_NO);
        cbDgnl       = new CustomComboBox<>(YES_NO);
        cbThpt       = new CustomComboBox<>(YES_NO);
        cbVsat       = new CustomComboBox<>(YES_NO);

        // "Tuyen thang", "DGNL", "THPT QG", "V-SAT"
        addField("Tuy\u1ec3n th\u1eb3ng", cbTuyenThang);
        addField("\u0110GNL",             cbDgnl);
        addField("THPT QG",              cbThpt);
        addField("V-SAT",                cbVsat);
    }

    @Override
    protected void populateForm(Nganh n) {
        tfMaNganh.setText(safe(n.getManganh()));
        tfTenNganh.setText(safe(n.getTennganh()));
        tfToHopGoc.setText(safe(n.getNTohopgoc()));
        tfChiTieu.setText(n.getNChitieu() != null ? String.valueOf(n.getNChitieu()) : "");
        tfDiemSan.setText(n.getNDiemsan() != null ? n.getNDiemsan().toPlainString() : "");
        tfDiemTrungTuyen.setText(n.getNDiemtrungtuyen() != null ? n.getNDiemtrungtuyen().toPlainString() : "");

        cbTuyenThang.setSelectedItem(flag(n.getNTuyenthang()));
        cbDgnl.setSelectedItem(flag(n.getNDgnl()));
        cbThpt.setSelectedItem(flag(n.getNThpt()));
        cbVsat.setSelectedItem(flag(n.getNVsat()));
    }

    @Override
    protected void collectData(Nganh n) {
        n.setManganh(getText(tfMaNganh));
        n.setTennganh(getText(tfTenNganh));
        n.setNTohopgoc(blankNull(getText(tfToHopGoc)));
        n.setNChitieu(parseInt(getText(tfChiTieu)));
        n.setNDiemsan(parseBD(getText(tfDiemSan)));
        n.setNDiemtrungtuyen(parseBD(getText(tfDiemTrungTuyen)));
        n.setNTuyenthang(unflag(cbTuyenThang));
        n.setNDgnl(unflag(cbDgnl));
        n.setNThpt(unflag(cbThpt));
        n.setNVsat(unflag(cbVsat));
    }

    @Override
    protected String validateData() {
        // "Vui long nhap ma nganh."
        if (isBlank(tfMaNganh))  return "Vui l\u00f2ng nh\u1eadp m\u00e3 ng\u00e0nh.";
        // "Vui long nhap ten nganh."
        if (isBlank(tfTenNganh)) return "Vui l\u00f2ng nh\u1eadp t\u00ean ng\u00e0nh.";
        // "Vui long nhap chi tieu."
        if (isBlank(tfChiTieu))  return "Vui l\u00f2ng nh\u1eadp ch\u1ec9 ti\u00eau.";
        try {
            int ct = Integer.parseInt(getText(tfChiTieu));
            // "Chi tieu phai la so nguyen duong."
            if (ct <= 0) return "Ch\u1ec9 ti\u00eau ph\u1ea3i l\u00e0 s\u1ed1 nguy\u00ean d\u01b0\u01a1ng.";
        } catch (NumberFormatException e) {
            // "Chi tieu phai la so nguyen."
            return "Ch\u1ec9 ti\u00eau ph\u1ea3i l\u00e0 s\u1ed1 nguy\u00ean.";
        }
        String ds = getText(tfDiemSan);
        if (!ds.isEmpty()) {
            try { new BigDecimal(ds); }
            // "Diem san khong hop le."
            catch (NumberFormatException e) { return "\u0110i\u1ec3m s\u00e0n kh\u00f4ng h\u1ee3p l\u1ec7."; }
        }
        String dtt = getText(tfDiemTrungTuyen);
        if (!dtt.isEmpty()) {
            try { new BigDecimal(dtt); }
            // "Diem trung tuyen khong hop le."
            catch (NumberFormatException e) { return "\u0110i\u1ec3m tr\u00fang tuy\u1ec3n kh\u00f4ng h\u1ee3p l\u1ec7."; }
        }
        return null;
    }

    @Override
    protected void persist(Nganh n) {
        if (isAddNew) nganhService.save(n);
        else          nganhService.update(n);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    // "1" - "Co", else - "Khong"
    private String flag(String f)   { return "1".equals(f) ? "C\u00f3" : "Kh\u00f4ng"; }
    // "Co" - "1", else - "0"
    private String unflag(CustomComboBox<String> cb) {
        return "C\u00f3".equals(cb.getSelectedItem()) ? "1" : "0";
    }
    private String safe(String s)       { return s != null ? s : ""; }
    private String blankNull(String s)  { return (s == null || s.isEmpty()) ? null : s; }
    private int    parseInt(String s)   {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }
    private BigDecimal parseBD(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return new BigDecimal(s); } catch (NumberFormatException e) { return null; }
    }
}