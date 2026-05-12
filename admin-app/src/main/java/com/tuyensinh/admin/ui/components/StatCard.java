package com.tuyensinh.admin.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Card thong ke tren Dashboard, hien thi tieu de, gia tri va bieu do mini
 */
public class StatCard extends JPanel {
    private final JLabel lblValue;

    public StatCard(String title, String value, String iconName, String colorHex, int[] chartData) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        Color accentColor = Color.decode(colorHex);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 13f));
        lblTitle.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));
        
        lblValue = new JLabel(value);
        lblValue.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 28f));
        lblValue.setForeground(Color.decode(UIConstants.DASH_TEXT_DARK));

        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(lblValue);

        JPanel iconContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        iconContainer.setOpaque(false);
        iconContainer.setPreferredSize(new Dimension(48, 48));
        
        try {
            JLabel lblIcon = new JLabel(new FlatSVGIcon("assets/" + iconName, 24, 24));
            lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
            iconContainer.add(lblIcon, BorderLayout.CENTER);
        } catch (Exception ignored) {}

        topPanel.add(infoPanel, BorderLayout.CENTER);
        topPanel.add(iconContainer, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        if (chartData != null && chartData.length > 0) {
            MiniBarChart chart = new MiniBarChart(chartData, accentColor);
            chart.setPreferredSize(new Dimension(0, 40));
            
            JPanel bottomWrap = new JPanel(new BorderLayout());
            bottomWrap.setOpaque(false);
            bottomWrap.setBorder(new EmptyBorder(16, 0, 0, 0));
            bottomWrap.add(chart, BorderLayout.CENTER);
            
            add(bottomWrap, BorderLayout.SOUTH);
        }
    }
    public void setValue(String value) {
        lblValue.setText(value);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), UIConstants.DASH_CARD_RADIUS, UIConstants.DASH_CARD_RADIUS));
        g2.setColor(Color.decode(UIConstants.COLOR_BORDER));
        g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, UIConstants.DASH_CARD_RADIUS, UIConstants.DASH_CARD_RADIUS));

        g2.dispose();
        super.paintComponent(g);
    }
}