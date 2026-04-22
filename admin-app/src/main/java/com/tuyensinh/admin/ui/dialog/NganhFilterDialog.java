package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.components.CustomComboBox;
import com.tuyensinh.admin.ui.components.RoundedButton;
import com.tuyensinh.admin.ui.components.RoundedTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * NganhFilterDialog - Bo loc nang cao cho NganhPanel.
 *
 * Tieu chi:
 *   - Khoa          (ComboBox)
 *   - Trang thai    (ComboBox)
 *   - Chi tieu tu / den (TextField so)
 *   - Ti le % tu / den  (TextField so)
 */
public class NganhFilterDialog extends JDialog {

    private CustomComboBox<String> cbKhoa;
    private CustomComboBox<String> cbTrangThai;
    private RoundedTextField       tfChiTieuMin;
    private RoundedTextField       tfChiTieuMax;
    private RoundedTextField       tfPhanTramMin;
    private RoundedTextField       tfPhanTramMax;

    private boolean applied = false;
    private Map<String, Object> resultFilters = new HashMap<>();

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public NganhFilterDialog(Frame parent, Map<String, Object> currentFilters) {
        // "Bo loc nang cao"
        super(parent, "B\u1ed9 l\u1ecdc n\u00e2ng cao", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // ESC dong
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        add(buildBody(currentFilters != null ? currentFilters : new HashMap<>()), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        // FIX: pack() truoc khi setLocationRelativeTo de dialog hien dung kich thuoc
        pack();
        setMinimumSize(new Dimension(420, 0));
        setLocationRelativeTo(parent);
    }

    // =========================================================================
    // BUILD UI
    // =========================================================================
    private JPanel buildBody(Map<String, Object> current) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 28, 8, 28));

        // - Section: Thong tin chung -
        // "Thong tin chung"
        addSectionHeader(body, "Th\u00f4ng tin chung");

        // "Tat ca", "Cong nghe", "Kinh te", "Ngoai ngu"
        cbKhoa = new CustomComboBox<>(new String[]{
                "T\u1ea5t c\u1ea3",
                "C\u00f4ng ngh\u1ec7",
                "Kinh t\u1ebf",
                "Ngo\u1ea1i ng\u1eef"
        });
        Object khoaVal = current.get("khoa");
        if (khoaVal != null) cbKhoa.setSelectedItem(khoaVal.toString());
        // "Khoa"
        addRow(body, "Khoa", cbKhoa);

        // "Tat ca","Dang tuyen","Da du chi tieu","Tam dung"
        cbTrangThai = new CustomComboBox<>(new String[]{
                "T\u1ea5t c\u1ea3",
                "\u0110ang tuy\u1ec3n",
                "\u0110\u00e3 \u0111\u1ee7 ch\u1ec9 ti\u00eau",
                "T\u1ea1m d\u1eebng"
        });
        Object ttVal = current.get("trangThai");
        if (ttVal != null) cbTrangThai.setSelectedItem(ttVal.toString());
        // "Trang thai"
        addRow(body, "Tr\u1ea1ng th\u00e1i", cbTrangThai);

        // - Section: Chi tieu -
        // "Chi tieu"
        addSectionHeader(body, "Ch\u1ec9 ti\u00eau");

        // "VD: 100", "VD: 300"
        tfChiTieuMin = new RoundedTextField("VD: 100");
        tfChiTieuMax = new RoundedTextField("VD: 300");
        Object ctMin = current.get("nChitieuMin");
        Object ctMax = current.get("nChitieuMax");
        if (ctMin != null) tfChiTieuMin.setText(ctMin.toString());
        if (ctMax != null) tfChiTieuMax.setText(ctMax.toString());
        // "Tu" / "Den"
        addRangeRow(body, "T\u1eeb", tfChiTieuMin, "\u0110\u1ebfn", tfChiTieuMax);

        // - Section: Ti le % -
        // "Ti le dang ky (%)"
        addSectionHeader(body, "T\u1ec9 l\u1ec7 \u0111\u0103ng k\u00fd (%)");

        tfPhanTramMin = new RoundedTextField("VD: 0");
        tfPhanTramMax = new RoundedTextField("VD: 100");
        Object ptMin = current.get("phanTramMin");
        Object ptMax = current.get("phanTramMax");
        if (ptMin != null) tfPhanTramMin.setText(ptMin.toString());
        if (ptMax != null) tfPhanTramMax.setText(ptMax.toString());
        addRangeRow(body, "T\u1eeb", tfPhanTramMin, "\u0110\u1ebfn", tfPhanTramMax);

        return body;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(new Color(0xF9FAFB));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE5E7EB)));

        // "Dat lai"
        RoundedButton btnReset = new RoundedButton("\u0110\u1eb7t l\u1ea1i");
        btnReset.setPreferredSize(new Dimension(100, 36));
        btnReset.addActionListener(e -> resetFilters());

        // "Ap dung"
        RoundedButton btnApply = new RoundedButton("\u00c1p d\u1ee5ng");
        btnApply.setPreferredSize(new Dimension(120, 36));
        btnApply.setBackground(new Color(0x1E3A8A));
        btnApply.setForeground(Color.WHITE);
        btnApply.addActionListener(e -> applyFilters());

        footer.add(btnReset);
        footer.add(btnApply);
        return footer;
    }

    // =========================================================================
    // ROW HELPERS
    // =========================================================================
    private void addSectionHeader(JPanel parent, String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
        lbl.setForeground(new Color(0x6B7280));
        lbl.setAlignmentX(LEFT_ALIGNMENT);

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(8, 0, 6, 0));
        row.add(lbl, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        parent.add(row);
    }

    private void addRow(JPanel parent, String labelText, JComponent comp) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(0, 0, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
        label.setForeground(new Color(0x374151));
        label.setPreferredSize(new Dimension(90, 30));

        comp.setPreferredSize(new Dimension(250, 30));
        row.add(label, BorderLayout.WEST);
        row.add(comp,  BorderLayout.CENTER);
        parent.add(row);
    }

    private void addRangeRow(JPanel parent,
                             String lbl1, RoundedTextField tf1,
                             String lbl2, RoundedTextField tf2) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(0, 0, 10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel from = new JLabel(lbl1);
        from.setFont(from.getFont().deriveFont(Font.PLAIN, 13f));
        from.setForeground(new Color(0x374151));

        tf1.setPreferredSize(new Dimension(115, 30));

        JLabel to = new JLabel(lbl2);
        to.setFont(to.getFont().deriveFont(Font.PLAIN, 13f));
        to.setForeground(new Color(0x374151));

        tf2.setPreferredSize(new Dimension(115, 30));

        row.add(from); row.add(tf1);
        row.add(to);   row.add(tf2);
        parent.add(row);
    }

    // =========================================================================
    // LOGIC
    // =========================================================================
    private void applyFilters() {
        Map<String, Object> filters = new HashMap<>();

        String khoa = (String) cbKhoa.getSelectedItem();
        // "Tat ca"
        if (khoa != null && !khoa.equals("T\u1ea5t c\u1ea3")) filters.put("khoa", khoa);

        String tt = (String) cbTrangThai.getSelectedItem();
        if (tt != null && !tt.equals("T\u1ea5t c\u1ea3")) filters.put("trangThai", tt);

        tryPutInt(filters, "nChitieuMin",  tfChiTieuMin.getText().trim());
        tryPutInt(filters, "nChitieuMax",  tfChiTieuMax.getText().trim());
        tryPutInt(filters, "phanTramMin",  tfPhanTramMin.getText().trim());
        tryPutInt(filters, "phanTramMax",  tfPhanTramMax.getText().trim());

        resultFilters = filters;
        applied = true;
        dispose();
    }

    private void resetFilters() {
        cbKhoa.setSelectedIndex(0);
        cbTrangThai.setSelectedIndex(0);
        tfChiTieuMin.setText("");
        tfChiTieuMax.setText("");
        tfPhanTramMin.setText("");
        tfPhanTramMax.setText("");
        resultFilters = new HashMap<>();
        applied = true;
        dispose();
    }

    private void tryPutInt(Map<String, Object> map, String key, String val) {
        if (val.isEmpty()) return;
        try { map.put(key, Integer.parseInt(val)); }
        catch (NumberFormatException ignored) {}
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================
    public boolean isApplied()              { return applied; }
    public Map<String, Object> getFilters() { return resultFilters; }
}