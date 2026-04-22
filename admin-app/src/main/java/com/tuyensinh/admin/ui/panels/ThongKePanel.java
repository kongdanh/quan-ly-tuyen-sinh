package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.components.HeaderPanel;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.dao.DiemThiXetTuyenDAO;
import com.tuyensinh.model.DiemThiXetTuyen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThongKePanel extends JPanel {

    private final DiemThiXetTuyenDAO diemDAO = new DiemThiXetTuyenDAO();

    private final DefaultTableModel typeModel = new DefaultTableModel(
            new String[]{"Loại điểm", "Số bản ghi", "TB TO", "TB VA", "TB N1", "TB NL1"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel subjectModel = new DefaultTableModel(
            new String[]{"Môn", "Số bản ghi", "Thấp nhất", "Cao nhất", "Trung bình"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public ThongKePanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBackground(Color.decode(UIConstants.DASH_CONTENT_BG));
        setBorder(BorderFactory.createEmptyBorder(
                UIConstants.SECTION_GAP,
                UIConstants.SECTION_GAP + 10,
                UIConstants.SECTION_GAP,
                UIConstants.SECTION_GAP + 10
        ));

        add(new HeaderPanel("Thống kê", "Thống kê điểm theo loại điểm và theo môn"), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toolbar.setOpaque(false);
        JButton btnReload = new JButton("Làm mới");
        btnReload.addActionListener(e -> loadStatistics());
        toolbar.add(btnReload);
        content.add(toolbar, BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 12));
        tables.setOpaque(false);
        tables.add(buildTableCard("1) Theo loại điểm", typeModel));
        tables.add(buildTableCard("2) Theo môn", subjectModel));
        content.add(tables, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        loadStatistics();
    }

    private JPanel buildTableCard(String title, DefaultTableModel model) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode(UIConstants.COLOR_BORDER)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lbl = new JLabel(title);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 14f));
        lbl.setForeground(Color.decode(UIConstants.DASH_TEXT_DARK));
        card.add(lbl, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void loadStatistics() {
        diemDAO.findAllForAdmin().thenAccept(list -> SwingUtilities.invokeLater(() -> {
            fillByTypeTable(list);
            fillBySubjectTable(list);
        }));
    }

    private void fillByTypeTable(List<DiemThiXetTuyen> list) {
        typeModel.setRowCount(0);

        Map<String, List<DiemThiXetTuyen>> grouped = new LinkedHashMap<>();
        grouped.put(DiemThiXetTuyenDAO.PT_THPT, new ArrayList<>());
        grouped.put(DiemThiXetTuyenDAO.PT_VSAT, new ArrayList<>());
        grouped.put(DiemThiXetTuyenDAO.PT_DGNL, new ArrayList<>());

        for (DiemThiXetTuyen d : list) {
            String method = DiemThiXetTuyenDAO.normalizeMethod(d.getDPhuongthuc());
            if (grouped.containsKey(method)) {
                grouped.get(method).add(d);
            }
        }

        for (Map.Entry<String, List<DiemThiXetTuyen>> entry : grouped.entrySet()) {
            List<DiemThiXetTuyen> rows = entry.getValue();
            typeModel.addRow(new Object[]{
                    entry.getKey(),
                    rows.size(),
                    avg(rows, "TO"),
                    avg(rows, "VA"),
                    avg(rows, "N1"),
                    avg(rows, "NL1")
            });
        }
    }

    private void fillBySubjectTable(List<DiemThiXetTuyen> list) {
        subjectModel.setRowCount(0);

        String[] subjects = {
                "TO", "LI", "HO", "SI", "SU", "DI", "VA", "N1", "KTPL", "TI", "NL1", "NK1", "NK2"
        };

        for (String subject : subjects) {
            SubjectStats stats = statsFor(list, subject);
            subjectModel.addRow(new Object[]{
                    subject,
                    stats.count,
                    format(stats.min),
                    format(stats.max),
                    format(stats.avg())
            });
        }
    }

    private String avg(List<DiemThiXetTuyen> list, String subject) {
        return format(statsFor(list, subject).avg());
    }

    private SubjectStats statsFor(List<DiemThiXetTuyen> list, String subject) {
        SubjectStats stats = new SubjectStats();
        for (DiemThiXetTuyen d : list) {
            BigDecimal value = pick(d, subject);
            if (value == null) {
                continue;
            }
            stats.add(value.doubleValue());
        }
        return stats;
    }

    private BigDecimal pick(DiemThiXetTuyen d, String subject) {
        return switch (subject) {
            case "TO" -> d.getTo();
            case "LI" -> d.getLi();
            case "HO" -> d.getHo();
            case "SI" -> d.getSi();
            case "SU" -> d.getSu();
            case "DI" -> d.getDi();
            case "VA" -> d.getVa();
            case "KTPL" -> d.getKtpl();
            case "TI" -> d.getTi();
            case "NL1" -> d.getNl1();
            case "NK1" -> d.getNk1();
            case "NK2" -> d.getNk2();
            case "N1" -> {
                BigDecimal thi = d.getN1Thi();
                BigDecimal cc = d.getN1Cc();
                if (thi == null) {
                    yield cc;
                }
                if (cc == null) {
                    yield thi;
                }
                yield thi.max(cc);
            }
            default -> null;
        };
    }

    private String format(double value) {
        if (Double.isNaN(value)) {
            return "-";
        }
        return String.format("%.2f", value);
    }

    private static class SubjectStats {
        int count = 0;
        double sum = 0;
        double min = Double.NaN;
        double max = Double.NaN;

        void add(double value) {
            count++;
            sum += value;
            if (Double.isNaN(min) || value < min) {
                min = value;
            }
            if (Double.isNaN(max) || value > max) {
                max = value;
            }
        }

        double avg() {
            return count == 0 ? Double.NaN : (sum / count);
        }
    }
}
