package com.tuyensinh.admin.ui.components;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Reusable rounded panel with soft shadow and configurable corner radius.
 * Used as base for all dashboard cards and chart containers.
 */
public class RoundPanel extends JPanel {

    private final int radius;
    private final boolean showShadow;
    private Color cardBackground = Color.WHITE;

    public RoundPanel() {
        this(12, true);
    }

    public RoundPanel(int radius) {
        this(radius, true);
    }

    public RoundPanel(int radius, boolean showShadow) {
        this.radius = radius;
        this.showShadow = showShadow;
        setOpaque(false);
        setBorder(new EmptyBorder(16, 16, 16, 16));
    }

    public void setCardBackground(Color bg) {
        this.cardBackground = bg;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (showShadow) {
            // Multi-layer shadow for depth
            for (int i = 3; i >= 1; i--) {
                g2.setColor(new Color(0, 0, 0, 4 * i));
                g2.fill(new RoundRectangle2D.Double(i, i + 1, w - i * 2, h - i * 2, radius, radius));
            }
        }

        // Card body
        g2.setColor(cardBackground);
        g2.fill(new RoundRectangle2D.Double(0, 0,
                showShadow ? w - 3 : w, showShadow ? h - 3 : h, radius, radius));

        // Subtle border
        g2.setColor(new Color(0, 0, 0, 8));
        g2.setStroke(new BasicStroke(0.5f));
        g2.draw(new RoundRectangle2D.Double(0, 0,
                showShadow ? w - 3 : w, showShadow ? h - 3 : h, radius, radius));

        g2.dispose();
        super.paintComponent(g);
    }
}
