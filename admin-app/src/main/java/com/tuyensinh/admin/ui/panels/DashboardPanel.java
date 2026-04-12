package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.service.AuthService;
import com.tuyensinh.admin.ui.components.HeaderPanel;
import com.tuyensinh.admin.ui.components.RoundPanel;
import com.tuyensinh.admin.util.AdminSession;
import com.tuyensinh.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;

import static com.tuyensinh.admin.ui.components.RoundedTextField.resolveFont;

/**
 * Dashboard content panel. Role-based views (ADMIN vs USER).
 * All colors use Constants.DASH_* — zero hardcoded hex.
 */
public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setBackground(Color.decode(Constants.DASH_CONTENT_BG));
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(Constants.CONTENT_PADDING + 4, Constants.CONTENT_PADDING + 8,
                Constants.CONTENT_PADDING + 4, Constants.CONTENT_PADDING + 8));

        add(new HeaderPanel("Dashboard", "Tổng quan hệ thống tuyển sinh"), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        if (AdminSession.getInstance().getCurrentRole().equals("ADMIN")) {
            layoutAdmin(body);
        } else {
            layoutUser(body);
        }

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.decode(Constants.DASH_CONTENT_BG));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);
    }

    // ════════════════════ ADMIN LAYOUT (GridBagLayout) ════════════════════

    private void layoutAdmin(JPanel body) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;

        // Row 0: stat cards
        gc.gridy = 0; gc.weighty = 0;
        gc.insets = new Insets(0, 0, Constants.SECTION_GAP, 0);
        JPanel cards = new JPanel(new GridLayout(1, 4, Constants.CARDS_GAP, 0));
        cards.setOpaque(false);
        cards.add(statCard("Tổng thí sinh", "2,847", "+12%", c(Constants.DASH_PRIMARY)));
        cards.add(statCard("Số ngành",       "28",    "",     c(Constants.DASH_ACCENT_PURPLE)));
        cards.add(statCard("Đã xét tuyển",  "1,823", "+8%",  c(Constants.DASH_ACCENT_SUCCESS)));
        cards.add(statCard("Chờ xử lý",     "342",   "-5%",  c(Constants.DASH_ACCENT_AMBER)));
        body.add(cards, gc);

        // Row 1: charts
        gc.gridy = 1; gc.weighty = 1;
        JPanel charts = new JPanel(new GridLayout(1, 2, Constants.CARDS_GAP, 0));
        charts.setOpaque(false);
        charts.add(barChart());
        charts.add(lineChart());
        body.add(charts, gc);

        // Row 2: activity log
        gc.gridy = 2; gc.weighty = 0.6;
        gc.insets = new Insets(Constants.SECTION_GAP, 0, 0, 0);
        body.add(activityLog(), gc);
    }

    // ════════════════════ USER LAYOUT ════════════════════

    private void layoutUser(JPanel body) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1; gc.gridx = 0;

        gc.gridy = 0;
        gc.insets = new Insets(0, 0, Constants.SECTION_GAP, 0);
        JLabel welcome = new JLabel("Xin chào, " + AdminSession.getInstance().getCurrentUsername());
        welcome.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_LG));
        welcome.setForeground(c(Constants.DASH_TEXT_MUTED));
        body.add(welcome, gc);

        gc.gridy = 1;
        JPanel shortcuts = new JPanel(new GridLayout(1, 3, Constants.CARDS_GAP, 0));
        shortcuts.setOpaque(false);
        shortcuts.add(shortcutCard("🎓", "Quản lý thí sinh", "Xem, thêm, sửa thí sinh", c(Constants.DASH_PRIMARY)));
        shortcuts.add(shortcutCard("📊", "Nhập điểm",        "Nhập điểm thi xét tuyển", c(Constants.DASH_ACCENT_SUCCESS)));
        shortcuts.add(shortcutCard("📋", "Xét tuyển",        "Xét nguyện vọng thí sinh", c(Constants.DASH_ACCENT_PURPLE)));
        body.add(shortcuts, gc);

        gc.gridy = 2; gc.fill = GridBagConstraints.BOTH; gc.weighty = 1;
        gc.insets = new Insets(Constants.SECTION_GAP, 0, 0, 0);
        RoundPanel pCard = new RoundPanel(Constants.DASH_CARD_RADIUS);
        pCard.setLayout(new BoxLayout(pCard, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel("Tiến độ công việc");
        lbl.setFont(resolveFont(Font.BOLD, Constants.DASH_HEADER_SUB_SIZE));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        pCard.add(lbl);
        pCard.add(Box.createVerticalStrut(14));
        pCard.add(progressRow("Nhập liệu thí sinh", 78));
        pCard.add(Box.createVerticalStrut(10));
        pCard.add(progressRow("Nhập điểm thi", 45));
        pCard.add(Box.createVerticalStrut(10));
        pCard.add(progressRow("Xét nguyện vọng", 20));
        body.add(pCard, gc);
    }

    // ════════════════════ STAT CARD ════════════════════

    private RoundPanel statCard(String label, String value, String trend, Color accent) {
        RoundPanel card = new RoundPanel(Constants.DASH_CARD_RADIUS);
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;

        // Dot + label
        gc.gridy = 0; gc.insets = new Insets(0, 0, 6, 0);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.add(dotPanel(accent));
        JLabel l = new JLabel(label);
        l.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_SM));
        l.setForeground(c(Constants.DASH_TEXT_MUTED));
        row.add(l);
        card.add(row, gc);

        // Value
        gc.gridy = 1; gc.insets = new Insets(0, 0, 2, 0);
        JLabel v = new JLabel(value);
        v.setFont(resolveFont(Font.BOLD, 30));
        v.setForeground(c(Constants.DASH_TEXT_DARK));
        card.add(v, gc);

        // Trend
        if (!trend.isEmpty()) {
            gc.gridy = 2; gc.insets = new Insets(0, 0, 0, 0);
            boolean up = trend.startsWith("+");
            JLabel t = new JLabel((up ? "↑ " : "↓ ") + trend + " so với tuần trước");
            t.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_XS + 1));
            t.setForeground(up ? c(Constants.DASH_ACCENT_SUCCESS) : c(Constants.DASH_ACCENT_ROSE));
            card.add(t, gc);
        }
        return card;
    }

    // ════════════════════ BAR CHART ════════════════════

    private RoundPanel barChart() {
        RoundPanel card = new RoundPanel(Constants.DASH_CARD_RADIUS);
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Thí sinh theo ngành");
        title.setFont(resolveFont(Font.BOLD, Constants.DASH_HEADER_SUB_SIZE));
        title.setForeground(c(Constants.DASH_TEXT_DARK));
        title.setBorder(new EmptyBorder(0, 0, Constants.CARDS_GAP, 0));
        card.add(title, BorderLayout.NORTH);

        Color barColor = c(Constants.DASH_PRIMARY);
        Color gridColor = c(Constants.DASH_GRID_LINE);
        Color textDark = c(Constants.DASH_TEXT_DARK);
        Color textMuted = c(Constants.DASH_TEXT_MUTED);

        JPanel chart = new JPanel() {
            final String[] labels = {"CNTT", "Kế toán", "MKT", "Luật", "Y khoa", "SP Toán"};
            final int[] vals = {420, 310, 280, 250, 380, 190};
            final int maxVal = 500;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int pL = 44, pB = 28, pT = 4, pR = 10;
                int cw = w - pL - pR, ch = h - pB - pT;

                // Grid
                for (int i = 0; i <= 5; i++) {
                    int y = pT + (int)(ch * (1.0 - i / 5.0));
                    g2.setColor(gridColor);
                    g2.setStroke(new BasicStroke(0.5f));
                    g2.drawLine(pL, y, w - pR, y);
                    g2.setColor(textMuted);
                    g2.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_XS));
                    String s = String.valueOf(maxVal * i / 5);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(s, pL - fm.stringWidth(s) - 6, y + 4);
                }

                // Bars
                int n = vals.length, gap = 10;
                int bw = Math.min((cw - gap * (n + 1)) / n, 42);
                int totalW = n * bw + (n - 1) * gap;
                int startX = pL + (cw - totalW) / 2;

                for (int i = 0; i < n; i++) {
                    int barH = (int)((double) vals[i] / maxVal * ch);
                    int x = startX + i * (bw + gap);
                    int y = pT + ch - barH;

                    g2.setPaint(new GradientPaint(x, y, barColor, x, y + barH, withAlpha(barColor, 160)));
                    g2.fill(new RoundRectangle2D.Double(x, y, bw, barH, 6, 6));

                    // Value
                    g2.setColor(textDark);
                    g2.setFont(resolveFont(Font.BOLD, Constants.FONT_SIZE_XS));
                    FontMetrics fm = g2.getFontMetrics();
                    String sv = String.valueOf(vals[i]);
                    g2.drawString(sv, x + (bw - fm.stringWidth(sv)) / 2, y - 5);

                    // Label
                    g2.setColor(textMuted);
                    g2.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_XS));
                    fm = g2.getFontMetrics();
                    g2.drawString(labels[i], x + (bw - fm.stringWidth(labels[i])) / 2, h - 8);
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    // ════════════════════ LINE CHART (CubicCurve2D) ════════════════════

    private RoundPanel lineChart() {
        RoundPanel card = new RoundPanel(Constants.DASH_CARD_RADIUS);
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Lượt đăng ký theo ngày");
        title.setFont(resolveFont(Font.BOLD, Constants.DASH_HEADER_SUB_SIZE));
        title.setForeground(c(Constants.DASH_TEXT_DARK));
        title.setBorder(new EmptyBorder(0, 0, Constants.CARDS_GAP, 0));
        card.add(title, BorderLayout.NORTH);

        Color lineColor = c(Constants.DASH_PRIMARY);
        Color gridColor = c(Constants.DASH_GRID_LINE);
        Color textMuted = c(Constants.DASH_TEXT_MUTED);

        JPanel chart = new JPanel() {
            final int[] data = {45, 72, 58, 90, 82, 105, 68};
            final String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            final int maxVal = 120;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int pL = 44, pB = 28, pT = 4, pR = 10;
                int cw = w - pL - pR, ch = h - pB - pT;

                // Grid
                for (int i = 0; i <= 4; i++) {
                    int y = pT + (int)(ch * (1.0 - i / 4.0));
                    g2.setColor(gridColor);
                    g2.setStroke(new BasicStroke(0.5f));
                    g2.drawLine(pL, y, w - pR, y);
                    g2.setColor(textMuted);
                    g2.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_XS));
                    String s = String.valueOf(maxVal * i / 4);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(s, pL - fm.stringWidth(s) - 6, y + 4);
                }

                // Data points
                double[] px = new double[data.length];
                double[] py = new double[data.length];
                for (int i = 0; i < data.length; i++) {
                    px[i] = pL + (i + 0.5) / data.length * cw;
                    py[i] = pT + ch - (double) data[i] / maxVal * ch;
                }

                // Smooth path
                GeneralPath path = new GeneralPath();
                path.moveTo(px[0], py[0]);
                for (int i = 0; i < data.length - 1; i++) {
                    double cx1 = px[i] + (px[i + 1] - px[i]) / 3.0;
                    double cx2 = px[i + 1] - (px[i + 1] - px[i]) / 3.0;
                    path.curveTo(cx1, py[i], cx2, py[i + 1], px[i + 1], py[i + 1]);
                }

                // Gradient fill
                GeneralPath fill = new GeneralPath(path);
                fill.lineTo(px[data.length - 1], pT + ch);
                fill.lineTo(px[0], pT + ch);
                fill.closePath();
                g2.setPaint(new GradientPaint(0, pT, withAlpha(lineColor, 40), 0, pT + ch, withAlpha(lineColor, 5)));
                g2.fill(fill);

                // Line
                g2.setColor(lineColor);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(path);

                // Dots + labels
                for (int i = 0; i < data.length; i++) {
                    g2.setColor(lineColor);
                    g2.fill(new Ellipse2D.Double(px[i] - 5, py[i] - 5, 10, 10));
                    g2.setColor(Color.WHITE);
                    g2.fill(new Ellipse2D.Double(px[i] - 2.5, py[i] - 2.5, 5, 5));

                    g2.setColor(textMuted);
                    g2.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_XS + 1));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(days[i], (int) px[i] - fm.stringWidth(days[i]) / 2, h - 8);
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    // ════════════════════ ACTIVITY LOG ════════════════════

    private RoundPanel activityLog() {
        RoundPanel card = new RoundPanel(Constants.DASH_CARD_RADIUS);
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("Hoạt động gần đây");
        title.setFont(resolveFont(Font.BOLD, Constants.DASH_HEADER_SUB_SIZE));
        title.setForeground(c(Constants.DASH_TEXT_DARK));
        title.setBorder(new EmptyBorder(0, 0, Constants.CARDS_GAP, 0));
        card.add(title, BorderLayout.NORTH);

        String[] cols = {"Người dùng", "Thời gian", "Hành động"};
        Object[][] rows = {
                {"admin", "10:30, 24/03/2026", "Phê duyệt 15 hồ sơ xét tuyển ngành CNTT"},
                {"user",  "09:15, 24/03/2026", "Cập nhật điểm thi THPT cho 45 thí sinh"},
                {"admin", "08:42, 24/03/2026", "Nhập hồ sơ thí sinh Trần Thị B — ngành Kế toán"},
                {"admin", "17:20, 23/03/2026", "Hoàn tất xét tuyển đợt 1 — ngành Luật"},
                {"user",  "15:00, 23/03/2026", "Import danh sách 120 thí sinh từ Excel"},
                {"admin", "11:30, 23/03/2026", "Cập nhật bảng quy đổi điểm IELTS"},
        };

        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_BASE));
        table.setRowHeight(42);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(c(Constants.DASH_GRID_LINE));
        table.setSelectionBackground(c(Constants.DASH_SELECT_BG));
        table.setSelectionForeground(c(Constants.DASH_TEXT_DARK));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(0).setMaxWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setMaxWidth(160);

        // Cell renderer
        Color primary = c(Constants.DASH_PRIMARY);
        Color textDark = c(Constants.DASH_TEXT_DARK);
        Color textMuted = c(Constants.DASH_TEXT_MUTED);
        Color selectBg = c(Constants.DASH_SELECT_BG);
        Color zebra = c(Constants.DASH_ZEBRA_ALT);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel,
                                                           boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                lbl.setBorder(new EmptyBorder(0, Constants.SIDEBAR_ITEM_PADDING_X, 0, Constants.SIDEBAR_ITEM_PADDING_X));
                lbl.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_BASE));

                if (sel) {
                    lbl.setBackground(selectBg);
                    lbl.setForeground(textDark);
                    if (col == 0) {
                        lbl.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 3, 0, 0, primary),
                                new EmptyBorder(0, 11, 0, Constants.SIDEBAR_ITEM_PADDING_X)));
                    }
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : zebra);
                    lbl.setForeground(col == 0 ? primary : textDark);
                }
                if (col == 1 && !sel) lbl.setForeground(textMuted);
                return lbl;
            }
        });

        // Header renderer
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel,
                                                           boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                lbl.setFont(resolveFont(Font.BOLD, Constants.FONT_SIZE_SM));
                lbl.setForeground(textMuted);
                lbl.setBackground(zebra);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, c(Constants.DASH_GRID_LINE)),
                        new EmptyBorder(0, Constants.SIDEBAR_ITEM_PADDING_X, 0, Constants.SIDEBAR_ITEM_PADDING_X)));
                return lbl;
            }
        });
        header.setPreferredSize(new Dimension(0, 40));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(c(Constants.DASH_GRID_LINE)));
        sp.getViewport().setBackground(Color.WHITE);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ════════════════════ COMPONENTS ════════════════════

    private RoundPanel shortcutCard(String icon, String title, String desc, Color accent) {
        RoundPanel card = new RoundPanel(Constants.DASH_CARD_RADIUS);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblIcon.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(10));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(resolveFont(Font.BOLD, Constants.DASH_HEADER_SUB_SIZE));
        lblTitle.setForeground(accent);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_SM));
        lblDesc.setForeground(c(Constants.DASH_TEXT_MUTED));
        lblDesc.setAlignmentX(LEFT_ALIGNMENT);
        card.add(lblDesc);
        return card;
    }

    private JPanel progressRow(String label, int pct) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_BASE));
        lbl.setPreferredSize(new Dimension(160, 20));
        row.add(lbl, BorderLayout.WEST);

        Color trackColor = c(Constants.DASH_GRID_LINE);
        Color fillColor = pct >= 70 ? c(Constants.DASH_ACCENT_SUCCESS)
                : pct >= 40 ? c(Constants.DASH_ACCENT_AMBER) : c(Constants.DASH_ACCENT_ROSE);

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(trackColor);
                g2.fill(new RoundRectangle2D.Double(0, 3, w, h - 6, 8, 8));
                g2.setColor(fillColor);
                g2.fill(new RoundRectangle2D.Double(0, 3, (int)(w * pct / 100.0), h - 6, 8, 8));
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        row.add(bar, BorderLayout.CENTER);

        JLabel p = new JLabel(pct + "%");
        p.setFont(resolveFont(Font.BOLD, Constants.FONT_SIZE_SM));
        p.setPreferredSize(new Dimension(40, 20));
        p.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(p, BorderLayout.EAST);
        return row;
    }

    private JPanel dotPanel(Color accent) {
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, 2, 8, 8);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(14, 12));
        dot.setOpaque(false);
        return dot;
    }

    // ──────── Helpers ────────

    /** Shorthand for Color.decode */
    private static Color c(String hex) { return Color.decode(hex); }

    private static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}