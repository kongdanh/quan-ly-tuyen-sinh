package com.tuyensinh.admin.ui.components;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

public class MiniBarChart extends JPanel {
    private final int[] data;
    private final Color color;
    private final int maxDataValue;

    public MiniBarChart(int[] data, Color color) {
        this.data = data;
        this.color = color;
        setOpaque(false);

        int max = 0;
        for (int val : data) {
            if (val > max) max = val;
        }
        this.maxDataValue = max == 0 ? 1 : max;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.length == 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int gap = 4;
        int barWidth = (width - (data.length - 1) * gap) / data.length;

        for (int i = 0; i < data.length; i++) {
            int barHeight = (int) (((double) data[i] / maxDataValue) * height);
            int x = i * (barWidth + gap);
            int y = height - barHeight;

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
            g2.fill(new RoundRectangle2D.Double(x, y + 2, barWidth, barHeight, 4, 4));

            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Double(x, y, barWidth, barHeight, 4, 4));
        }
        g2.dispose();
    }
}