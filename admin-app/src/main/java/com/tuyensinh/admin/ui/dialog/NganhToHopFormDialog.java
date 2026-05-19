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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NganhToHopFormDialog extends BaseFormDialog<NganhToHop> {

    /**
     * Bảng độ lệch THPT: DOLECH_TABLE.get(toHopGoc).get(toHopXetTuyen) = độ lệch
     * Nguồn: Bảng độ lệch điểm THPT trong tài liệu "cac cong thuc tinh.docx"
     * Công thức: Điểm quy đổi = Điểm thí sinh - độ lệch
     * Tổ hợp không có trong bảng → độ lệch = 0
     */
    private static final Map<String, Map<String, Double>> DOLECH_TABLE = new HashMap<>();
    static {
        Map<String, Double> a00Row = new HashMap<>();
        a00Row.put("A01", -0.69);
        a00Row.put("B00", -1.21);
        a00Row.put("C00", 2.32);
        a00Row.put("C01", 0.94);
        a00Row.put("D01", -0.68);
        a00Row.put("D07", -1.62);
        DOLECH_TABLE.put("A00", a00Row);

        Map<String, Double> a01Row = new HashMap<>();
        a01Row.put("A00", 0.69);
        a01Row.put("B00", -0.52);
        a01Row.put("C00", 3.01);
        a01Row.put("C01", 1.63);
        a01Row.put("D01", 0.01);
        a01Row.put("D07", -0.93);
        DOLECH_TABLE.put("A01", a01Row);

        Map<String, Double> b00Row = new HashMap<>();
        b00Row.put("A00", 1.21);
        b00Row.put("A01", 0.52);
        b00Row.put("C00", 3.53);
        b00Row.put("C01", 2.15);
        b00Row.put("D01", 0.53);
        b00Row.put("D07", -0.41);
        DOLECH_TABLE.put("B00", b00Row);

        Map<String, Double> c00Row = new HashMap<>();
        c00Row.put("A00", -2.32);
        c00Row.put("A01", -3.01);
        c00Row.put("B00", -3.53);
        c00Row.put("C01", -1.38);
        c00Row.put("D01", -3.00);
        c00Row.put("D07", -3.94);
        DOLECH_TABLE.put("C00", c00Row);

        Map<String, Double> c01Row = new HashMap<>();
        c01Row.put("A00", -0.94);
        c01Row.put("A01", -1.63);
        c01Row.put("B00", -2.15);
        c01Row.put("C00", 1.38);
        c01Row.put("D01", -1.62);
        c01Row.put("D07", -2.56);
        DOLECH_TABLE.put("C01", c01Row);

        Map<String, Double> d01Row = new HashMap<>();
        d01Row.put("A00", 0.68);
        d01Row.put("A01", -0.01);
        d01Row.put("B00", -0.53);
        d01Row.put("C00", 3.00);
        d01Row.put("C01", 1.62);
        d01Row.put("D07", -0.94);
        DOLECH_TABLE.put("D01", d01Row);
    }

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
    private JLabel lblDoLechCongThuc;

    private final NganhToHopService nganhToHopService = new NganhToHopService();
    private final NganhService nganhService = new NganhService();
    private final ToHopMonService toHopMonService = new ToHopMonService();

    private boolean isFilling = false;

    public NganhToHopFormDialog(Frame parent, NganhToHop entity, boolean isAddNew) {
        super(parent, "Liên kết Ngành - Tổ hợp", entity, isAddNew, 450, 580);

        buildFormFields();

        txtMon1.setEditable(false);
        txtMon2.setEditable(false);
        txtMon3.setEditable(false);

        cbToHop.addActionListener(e -> fillMonFromToHop());
        cbNganh.addActionListener(e -> updateToHopList());

        loadDataToComboBoxes();

        if (!isAddNew) {
            populateForm(entity);
        } else {
            // Giá trị mặc định cho hệ số khi thêm mới
            txtHs1.setText("1");
            txtHs2.setText("1");
            txtHs3.setText("1");
            // txtDolech đã được tự động điền theo công thức độ lệch THPT
            // bởi updateDoLechLabel() (gọi qua loadDataToComboBoxes → updateToHopList → fillMonFromToHop)
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

        lblDoLechCongThuc = new JLabel("(Chưa xác định)");
        lblDoLechCongThuc.setFont(lblDoLechCongThuc.getFont().deriveFont(Font.ITALIC, 12f));
        lblDoLechCongThuc.setForeground(new Color(0, 120, 200));
        addField("Theo công thức", lblDoLechCongThuc);
    }

    private void fillMonFromToHop() {
        ToHopMon selected = (ToHopMon) cbToHop.getSelectedItem();
        if (selected == null)
            return;

        txtMon1.setText(selected.getMon1());
        txtMon2.setText(selected.getMon2());
        txtMon3.setText(selected.getMon3());

        updateDoLechLabel();
    }

    /**
     * Tính độ lệch theo bảng THPT và cập nhật label.
     * Công thức: doLech = DOLECH_TABLE[toHopGoc][toHopXetTuyen]
     * Nếu tổ hợp không có trong bảng → độ lệch = 0
     */
    /**
     * Tính độ lệch từ bảng DOLECH_TABLE dựa trên tổ hợp gốc của ngành và tổ hợp xét
     * tuyển.
     * Trả về null nếu không thể xác định (ngành null, tổ hợp null, hoặc ngành chưa
     * có tổ hợp gốc).
     */
    private Double calculateDoLech(Nganh nganh, ToHopMon toHop) {
        if (nganh == null || toHop == null)
            return null;
        String toHopGoc = nganh.getNTohopgoc();
        if (toHopGoc == null || toHopGoc.isBlank())
            return null;
        String toHopXetTuyen = toHop.getMatohop();
        // Tổ hợp xét tuyển = tổ hợp gốc → độ lệch = 0
        if (toHopGoc.equalsIgnoreCase(toHopXetTuyen))
            return 0.0;
        // Tra bảng
        Map<String, Double> rowMap = DOLECH_TABLE.get(toHopGoc.toUpperCase());
        if (rowMap != null) {
            Double doLech = rowMap.get(toHopXetTuyen.toUpperCase());
            if (doLech != null)
                return doLech;
        }
        // Không có trong bảng → độ lệch = 0
        return 0.0;
    }

    /**
     * Cập nhật label hiển thị và tự động điền txtDolech theo công thức.
     * Công thức: doLech = DOLECH_TABLE[toHopGoc][toHopXetTuyen]
     */
    private void updateDoLechLabel() {
        if (lblDoLechCongThuc == null)
            return;

        Nganh selectedNganh = (Nganh) cbNganh.getSelectedItem();
        ToHopMon selectedToHop = (ToHopMon) cbToHop.getSelectedItem();

        if (selectedNganh == null || selectedToHop == null) {
            lblDoLechCongThuc.setText("(Chưa xác định)");
            lblDoLechCongThuc.setForeground(new Color(150, 150, 150));
            return;
        }

        String toHopGoc = selectedNganh.getNTohopgoc();
        if (toHopGoc == null || toHopGoc.isBlank()) {
            lblDoLechCongThuc.setText("(Ngành chưa có tổ hợp gốc)");
            lblDoLechCongThuc.setForeground(new Color(200, 100, 0));
            return;
        }

        Double doLech = calculateDoLech(selectedNganh, selectedToHop);
        if (doLech == null)
            return;

        // Tự động điền vào txtDolech
        txtDolech.setText(String.format("%.2f", doLech));

        // Cập nhật label mô tả
        boolean isGoc = toHopGoc.equalsIgnoreCase(selectedToHop.getMatohop());
        boolean isInTable = DOLECH_TABLE.containsKey(toHopGoc.toUpperCase())
                && DOLECH_TABLE.get(toHopGoc.toUpperCase()).containsKey(selectedToHop.getMatohop().toUpperCase());

        String suffix = isGoc ? "  (là tổ hợp gốc)" : (!isInTable ? "  (không có trong bảng quy định)" : "");
        String sign = doLech >= 0 ? "+" : "";
        lblDoLechCongThuc.setText(String.format("%s%.2f%s", sign, doLech, suffix));
        lblDoLechCongThuc.setForeground(
                isGoc || !isInTable ? new Color(100, 100, 100)
                        : doLech < 0 ? new Color(180, 0, 0)
                                : new Color(0, 130, 60));
    }

    private void loadDataToComboBoxes() {
        isFilling = true;
        // Load danh sách ngành
        List<Nganh> dsNganh = nganhService.findAllSync();
        dsNganh.forEach(cbNganh::addItem);
        isFilling = false;

        updateToHopList();
    }

    private void updateToHopList() {
        if (isFilling)
            return;

        Nganh selectedNganh = (Nganh) cbNganh.getSelectedItem();
        ToHopMon currentlySelectedToHop = (ToHopMon) cbToHop.getSelectedItem();

        isFilling = true;
        cbToHop.removeAllItems();

        if (selectedNganh != null) {
            List<ToHopMon> dsToHop = toHopMonService.getAvailableForNganh(selectedNganh.getManganh());

            // Allow the currently assigned ToHopMon when editing
            if (entity != null && !isAddNew && entity.getNganh() != null &&
                    entity.getNganh().getManganh().equals(selectedNganh.getManganh()) && entity.getToHopMon() != null) {
                boolean containsCurrent = dsToHop.stream()
                        .anyMatch(t -> t.getMatohop().equals(entity.getToHopMon().getMatohop()));
                if (!containsCurrent) {
                    cbToHop.addItem(entity.getToHopMon());
                }
            }

            dsToHop.forEach(cbToHop::addItem);

            if (currentlySelectedToHop != null) {
                for (int i = 0; i < cbToHop.getItemCount(); i++) {
                    if (cbToHop.getItemAt(i).getMatohop().equals(currentlySelectedToHop.getMatohop())) {
                        cbToHop.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
        isFilling = false;
        fillMonFromToHop();
    }

    @Override
    protected void populateForm(NganhToHop nt) {
        if (nt == null)
            return;

        isFilling = true;

        // Set Ngành
        if (nt.getNganh() != null) {
            for (int i = 0; i < cbNganh.getItemCount(); i++) {
                if (cbNganh.getItemAt(i).getManganh().equals(nt.getNganh().getManganh())) {
                    cbNganh.setSelectedIndex(i);
                    break;
                }
            }
        }

        isFilling = false;
        updateToHopList(); // Manually update ToHopList after Nganh is selected
        isFilling = true;

        // Set Tổ hợp
        if (nt.getToHopMon() != null) {
            for (int i = 0; i < cbToHop.getItemCount(); i++) {
                if (cbToHop.getItemAt(i).getMatohop().equals(nt.getToHopMon().getMatohop())) {
                    cbToHop.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Fill môn theo tổ hợp
        fillMonFromToHop();

        // ====== HỆ SỐ ======
        txtHs1.setText(String.valueOf(nt.getHsmon1()));
        txtHs2.setText(String.valueOf(nt.getHsmon2()));
        txtHs3.setText(String.valueOf(nt.getHsmon3()));

        // ====== ĐỘ LỆCH ======
        txtDolech.setText(
                nt.getDolech() != null ? nt.getDolech().toString() : "0");

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
                Double.parseDouble(getText(txtDolech).isEmpty() ? "0" : getText(txtDolech))));
    }

    @Override
    protected String validateData() {
        if (cbNganh.getSelectedItem() == null)
            return "Vui lòng chọn Ngành.";
        if (cbToHop.getSelectedItem() == null)
            return "Vui lòng chọn Tổ hợp môn.";

        // Hệ số phải là số tự nhiên dương (1, 2, 3...)
        String[] hsFields = { getText(txtHs1), getText(txtHs2), getText(txtHs3) };
        String[] hsTenMon = { "Môn 1", "Môn 2", "Môn 3" };
        for (int i = 0; i < hsFields.length; i++) {
            if (hsFields[i].isEmpty())
                return "Hệ số " + hsTenMon[i] + " không được để trống.";
            try {
                int hs = Integer.parseInt(hsFields[i]);
                if (hs <= 0)
                    return "Hệ số " + hsTenMon[i] + " phải là số nguyên dương (≥ 1).";
            } catch (NumberFormatException e) {
                return "Hệ số " + hsTenMon[i] + " không hợp lệ — chỉ nhập số nguyên dương.";
            }
        }

        // Độ lệch phải là số thực, âm dùng dấu '-' liền với số (không có khoảng trắng)
        String dolech = getText(txtDolech);
        if (!dolech.isEmpty() && !dolech.matches("-?\\d+(\\.\\d+)?")) {
            return "Độ lệch không hợp lệ — dùng định dạng số, VD: 0, 1.5, -0.69.";
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