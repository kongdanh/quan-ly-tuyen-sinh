package com.tuyensinh.admin.ui.panels;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.tuyensinh.admin.ui.components.HeaderPanel;
import com.tuyensinh.admin.ui.components.StatCard;
import com.tuyensinh.admin.util.AdminSession;
import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBackground(Color.decode(UIConstants.DASH_CONTENT_BG));
        setBorder(new EmptyBorder(UIConstants.SECTION_GAP, UIConstants.SECTION_GAP + 10, UIConstants.SECTION_GAP, UIConstants.SECTION_GAP + 10));

        // 1. Header
        HeaderPanel header = new HeaderPanel("Dashboard", "Tổng quan hệ thống tuyển sinh");
        add(header, BorderLayout.NORTH);

        // 2. Nội dung chính
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel lblWelcome = new JLabel("Xin chào, " + AdminSession.getInstance().getCurrentUsername());
        lblWelcome.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, (float) UIConstants.FONT_SIZE_LG));
        lblWelcome.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));
        lblWelcome.setBorder(new EmptyBorder(0, 0, UIConstants.SECTION_GAP, 0));
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblWelcome);

        // ==========================================
        // 3. GRID 3 THẺ THỐNG KÊ (STAT CARDS)
        // ==========================================
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, UIConstants.SECTION_GAP, 0));
        cardsGrid.setOpaque(false);
        cardsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140)); 

        // Dữ liệu giả lập cho biểu đồ (Mock data)
        int[] dataHoso = {12, 19, 15, 25, 32, 45, 62};
        int[] dataDiem = {0, 0, 5, 12, 18, 20, 20}; 
        int[] dataTrungTuyen = {0, 0, 0, 0, 0, 5, 15};

        cardsGrid.add(new StatCard("Tổng số hồ sơ", "62", "icon_user.svg", UIConstants.DASH_PRIMARY, dataHoso));
        cardsGrid.add(new StatCard("Đã nhập điểm", "20", "icon_diem.svg", UIConstants.DASH_ACCENT_AMBER, dataDiem));
        cardsGrid.add(new StatCard("Đã duyệt trúng tuyển", "15", "icon_nganh_tohop.svg", UIConstants.DASH_ACCENT_SUCCESS, dataTrungTuyen));

        content.add(cardsGrid);
        content.add(Box.createVerticalStrut(UIConstants.SECTION_GAP * 2));

        // 4. Bảng Tiến độ (Giữ nguyên code cũ)
        JPanel progressPanel = createProgressPanel();
        content.add(progressPanel);

        add(content, BorderLayout.CENTER);
    }

    // Hàm tạo 1 thẻ Tóm tắt (Card)
    private JPanel createSummaryCard(String title, String desc, String icon, String colorHex) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), UIConstants.DASH_CARD_RADIUS, UIConstants.DASH_CARD_RADIUS));
                
                // Vẽ viền xám mờ
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

        // Tiêu đề
        g.gridy = 1;
        g.insets = new Insets(8, 0, 4, 0);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.FONT_SIZE_MD));
        lblTitle.setForeground(Color.decode(colorHex));
        card.add(lblTitle, g);

        // Mô tả
        g.gridy = 2;
        g.insets = new Insets(0, 0, 0, 0);
        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, (float) UIConstants.FONT_SIZE_SM));
        lblDesc.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));
        card.add(lblDesc, g);

        return card;
    }

    // Hàm tạo Bảng Tiến độ giả lập
    private JPanel createProgressPanel() {
        JPanel p = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), UIConstants.DASH_CARD_RADIUS, UIConstants.DASH_CARD_RADIUS));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 24, 24, 24));
        
        // Để nó giãn rộng ra hết cỡ nhưng giới hạn chiều cao
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel title = new JLabel("Tiến độ công việc");
        title.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.FONT_SIZE_MD));
        title.setForeground(Color.decode(UIConstants.DASH_TEXT_DARK));
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        p.add(title);

        p.add(createProgressBar("Nhập liệu thí sinh", 78, UIConstants.DASH_ACCENT_SUCCESS));
        p.add(Box.createVerticalStrut(12));
        p.add(createProgressBar("Nhập điểm thi", 45, "#F59E0B")); // Màu Amber/Vàng
        p.add(Box.createVerticalStrut(12));
        p.add(createProgressBar("Xét nguyện vọng", 20, UIConstants.DASH_ACCENT_ROSE));

        return p;
    }

    private JPanel createProgressBar(String label, int percent, String colorHex) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.FONT_SIZE_BASE));
        lbl.setForeground(Color.decode(UIConstants.DASH_TEXT_DARK));
        lbl.setPreferredSize(new Dimension(140, 20));
        row.add(lbl, BorderLayout.WEST);

        JPanel barContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ nền bar xám nhạt
                g2.setColor(Color.decode(UIConstants.DASH_GRID_LINE));
                g2.fillRoundRect(0, 4, getWidth(), 12, 12, 12);
                
                // Vẽ phần trăm hoàn thành
                g2.setColor(Color.decode(colorHex));
                int fillWidth = (int) (getWidth() * (percent / 100.0));
                g2.fillRoundRect(0, 4, fillWidth, 12, 12, 12);
                
                g2.dispose();
            }
        };
        barContainer.setOpaque(false);
        row.add(barContainer, BorderLayout.CENTER);

        JLabel lblPct = new JLabel(percent + "%");
        lblPct.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.FONT_SIZE_SM));
        lblPct.setForeground(Color.decode(UIConstants.DASH_TEXT_DARK));
        lblPct.setPreferredSize(new Dimension(30, 20));
        row.add(lblPct, BorderLayout.EAST);

        return row;
    }
}