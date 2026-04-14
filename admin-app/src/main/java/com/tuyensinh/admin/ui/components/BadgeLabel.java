package com.tuyensinh.admin.ui.components;

import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BadgeLabel extends JLabel {

    private Color bgColor;
    private Color textColor;

    public BadgeLabel(String text, String type) {
        super(text, SwingConstants.CENTER);
        setOpaque(false);
        setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 11f));
        setBorder(new EmptyBorder(4, 10, 4, 10));
        
        setType(type);
    }

    public void setType(String type) {
        switch (type.toUpperCase()) {
            case "SUCCESS":
                bgColor = Color.decode(UIConstants.BADGE_SUCCESS_BG);
                textColor = Color.decode(UIConstants.BADGE_SUCCESS_TEXT);
                break;
            case "WARNING":
                bgColor = Color.decode(UIConstants.BADGE_WARNING_BG);
                textColor = Color.decode(UIConstants.BADGE_WARNING_TEXT);
                break;
            case "DANGER":
                bgColor = Color.decode(UIConstants.BADGE_DANGER_BG);
                textColor = Color.decode(UIConstants.BADGE_DANGER_TEXT);
                break;
            case "INFO":
                bgColor = Color.decode(UIConstants.BADGE_INFO_BG);
                textColor = Color.decode(UIConstants.BADGE_INFO_TEXT);
                break;
            default:
                bgColor = Color.decode(UIConstants.BADGE_GRAY_BG);
                textColor = Color.decode(UIConstants.BADGE_GRAY_TEXT);
                break;
        }
        setForeground(textColor);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(bgColor);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
        
        g2.dispose();
        super.paintComponent(g);
    }
}