package com.tuyensinh.admin.ui.panels;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.tuyensinh.admin.ui.components.CustomTable;
import com.tuyensinh.admin.ui.components.HeaderPanel;
import com.tuyensinh.admin.ui.components.StatCard;
import com.tuyensinh.admin.util.AdminSession;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.model.NhatKyHoatDong;
import com.tuyensinh.service.NhatKyHoatDongService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;

public class DashboardPanel extends JPanel {

    private CustomTable logTable;
    private DefaultTableModel logTableModel;
    private StatCard cardHoso, cardTrungTuyen, cardChiTieu;
    private JLabel lblKvStats, lblDtStats;
    
    private final NhatKyHoatDongService logService = new NhatKyHoatDongService();
    private final com.tuyensinh.service.ThongKeService statsService = new com.tuyensinh.service.ThongKeService();

    public DashboardPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBackground(Color.decode(UIConstants.DASH_CONTENT_BG));
        setBorder(new EmptyBorder(UIConstants.SECTION_GAP, UIConstants.SECTION_GAP + 10, UIConstants.SECTION_GAP, UIConstants.SECTION_GAP + 10));

        HeaderPanel header = new HeaderPanel("Dashboard", "Tổng quan hệ thống tuyển sinh");
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel lblWelcome = new JLabel("Xin chào, " + AdminSession.getInstance().getCurrentUsername());
        lblWelcome.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, (float) UIConstants.FONT_SIZE_LG));
        lblWelcome.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));
        lblWelcome.setBorder(new EmptyBorder(0, 0, UIConstants.SECTION_GAP, 0));
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblWelcome);

        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, UIConstants.SECTION_GAP, 0));
        cardsGrid.setOpaque(false);
        cardsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140)); 

        int[] dataHoso = {10, 15, 20, 30, 45, 55, 62};
        int[] dataTrungTuyen = {0, 0, 2, 5, 10, 12, 15};
        int[] dataChiTieu = {200, 200, 200, 200, 200, 200, 200};

        cardHoso = new StatCard("Tổng số hồ sơ", "0", "icon_user.svg", UIConstants.DASH_PRIMARY, dataHoso);
        cardTrungTuyen = new StatCard("Đã trúng tuyển", "0", "icon_nganh_tohop.svg", UIConstants.DASH_ACCENT_SUCCESS, dataTrungTuyen);
        cardChiTieu = new StatCard("Tổng chỉ tiêu", "0", "icon_nganh.svg", UIConstants.DASH_ACCENT_AMBER, dataChiTieu);

        cardsGrid.add(cardHoso);
        cardsGrid.add(cardTrungTuyen);
        cardsGrid.add(cardChiTieu);

        content.add(cardsGrid);
        content.add(Box.createVerticalStrut(UIConstants.SECTION_GAP));

        // Panel thống kê chi tiết theo Khu vực & Đối tượng
        JPanel detailsPanel = new JPanel(new GridLayout(1, 2, UIConstants.SECTION_GAP, 0));
        detailsPanel.setOpaque(false);
        detailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        lblKvStats = new JLabel("Đang tải...");
        lblKvStats.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDtStats = new JLabel("Đang tải...");
        lblDtStats.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        detailsPanel.add(createDetailBox("Thống kê theo Khu vực", lblKvStats));
        detailsPanel.add(createDetailBox("Thống kê theo Đối tượng", lblDtStats));
        
        content.add(detailsPanel);
        content.add(Box.createVerticalStrut(UIConstants.SECTION_GAP * 2));

        JPanel activityPanel = createActivityLogPanel();
        content.add(activityPanel);

        add(content, BorderLayout.CENTER);

        loadActivityLogs();
        updateStats();

        /**
         * Tu dong tai lai nhat ky khi panel duoc hien thi lai
         */
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadActivityLogs();
                updateStats();
            }
        });
    }

    private JPanel createDetailBox(String title, JLabel contentLbl) {
        JPanel p = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Color.decode(UIConstants.COLOR_BORDER));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.decode(UIConstants.DASH_PRIMARY));
        p.add(lblTitle, BorderLayout.NORTH);
        p.add(contentLbl, BorderLayout.CENTER);
        return p;
    }

    private void updateStats() {
        statsService.getTongQuanStats(null).thenAccept(stats -> SwingUtilities.invokeLater(() -> {
            cardHoso.setValue(stats.get("tongHoSo").toString());
            cardTrungTuyen.setValue(stats.get("tongTrungTuyen").toString());
            cardChiTieu.setValue(stats.get("tongChiTieu").toString());
        }));

        statsService.getThongKeKhuVuc(null).thenAccept(kv -> SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder("<html>");
            kv.forEach((k, v) -> sb.append("<b>").append(k).append("</b>: ").append(v).append(" thí sinh<br>"));
            sb.append("</html>");
            lblKvStats.setText(sb.toString());
        }));

        statsService.getThongKeDoiTuongUuTien(null).thenAccept(dt -> SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder("<html>");
            dt.forEach((k, v) -> sb.append("<b>").append(k).append("</b>: ").append(v).append(" thí sinh<br>"));
            sb.append("</html>");
            lblDtStats.setText(sb.toString());
        }));
    }

    /**
     * Tao bang nhat ky hoat dong he thong
     */
    private JPanel createActivityLogPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), UIConstants.DASH_CARD_RADIUS, UIConstants.DASH_CARD_RADIUS));
                
                g2.setColor(Color.decode(UIConstants.COLOR_BORDER));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, UIConstants.DASH_CARD_RADIUS, UIConstants.DASH_CARD_RADIUS));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(20, 24, 24, 24));

        JLabel title = new JLabel("Nhật ký hoạt động hệ thống");
        title.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.FONT_SIZE_MD));
        title.setForeground(Color.decode(UIConstants.DASH_TEXT_DARK));
        p.add(title, BorderLayout.NORTH);

        String[] columns = {"Thời gian", "Username", "Hành động", "Trạng thái"};
        logTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        logTable = new CustomTable();
        logTable.setModel(logTableModel);
        
        logTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        logTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        logTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        logTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(logTable);
        scroll.setBorder(BorderFactory.createLineBorder(Color.decode(UIConstants.COLOR_BORDER)));
        scroll.getViewport().setBackground(Color.WHITE);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    /**
     * Tai du lieu nhat ky tu database len bang (async)
     */
    private void loadActivityLogs() {
        logService.getLatestLogs(20).thenAccept(logs -> {
            SwingUtilities.invokeLater(() -> {
                logTableModel.setRowCount(0);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                
                for (NhatKyHoatDong log : logs) {
                    logTableModel.addRow(new Object[]{
                        log.getThoiGian() != null ? log.getThoiGian().format(formatter) : "",
                        log.getUsername() != null ? log.getUsername() : "Hệ thống",
                        log.getHanhDong(),
                        log.getTrangThai()
                    });
                }
            });
        });
    }

    private JPanel createSummaryCard(String title, String desc, String icon, String colorHex) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), UIConstants.DASH_CARD_RADIUS, UIConstants.DASH_CARD_RADIUS));
                
                g2.setColor(Color.decode(UIConstants.COLOR_BORDER));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, UIConstants.DASH_CARD_RADIUS, UIConstants.DASH_CARD_RADIUS));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.weightx = 1; g.anchor = GridBagConstraints.WEST;

        // Icon
        try {
            JLabel lblIcon = new JLabel(new FlatSVGIcon("assets/" + icon, 24, 24));
            card.add(lblIcon, g);
        } catch (Exception ignored) {}

        g.gridy = 1;
        g.insets = new Insets(8, 0, 4, 0);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.FONT_SIZE_MD));
        lblTitle.setForeground(Color.decode(colorHex));
        card.add(lblTitle, g);

        g.gridy = 2;
        g.insets = new Insets(0, 0, 0, 0);
        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, (float) UIConstants.FONT_SIZE_SM));
        lblDesc.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));
        card.add(lblDesc, g);

        return card;
    }
}