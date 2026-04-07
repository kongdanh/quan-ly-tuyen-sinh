package com.tuyensinh.admin.ui.components;

import com.tuyensinh.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedTextField extends JTextField {

    private String placeholder;
    private final int arcSize;
    private final Color borderNormal;
    private final Color borderFocus;
    private final Color borderError;
    private boolean hasError = false;
    private boolean focused = false;
    private IconPainter iconPainter;

    @FunctionalInterface
    public interface IconPainter {
        void paint(Graphics2D g2d, int x, int y, int size, Color color);
    }

    public RoundedTextField(String placeholder) {
        this(placeholder, Constants.LOGIN_INPUT_RADIUS);
    }

    public RoundedTextField(String placeholder, int arcSize) {
        this.placeholder = placeholder;
        this.arcSize = arcSize;
        this.borderNormal = Color.decode(Constants.INPUT_BORDER);
        this.borderFocus = Color.decode(Constants.INPUT_BORDER_FOCUS);
        this.borderError = Color.decode(Constants.COLOR_DANGER);

        setOpaque(false);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(0, Constants.INPUT_PADDING_X + 2, 0, Constants.INPUT_PADDING_X));
        setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_MD));
        setForeground(Color.decode(Constants.COLOR_TEXT));
        setCaretColor(Color.decode(Constants.COLOR_NAVY));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                hasError = false;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                repaint();
            }
        });
    }

    public void setIconPainter(IconPainter painter) {
        this.iconPainter = painter;
        if (painter != null) {
            setBorder(BorderFactory.createEmptyBorder(0, 40, 0, Constants.INPUT_PADDING_X));
        }
        repaint();
    }

    public void setPlaceholder(String p) {
        this.placeholder = p;
        repaint();
    }

    public void setError(boolean error) {
        this.hasError = error;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, arcSize, arcSize));
        g2.dispose();

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (iconPainter != null) {
            Color iconColor = focused ? borderFocus : Color.decode(Constants.COLOR_TEXT_MUTED);
            iconPainter.paint(g2d, 14, (getHeight() - 16) / 2, 16, iconColor);
        }

        if (getText().isEmpty() && placeholder != null) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(Color.decode(Constants.COLOR_TEXT_MUTED));
            g2d.setFont(getFont());
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(placeholder, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2d.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (hasError) {
            g2.setColor(borderError);
            g2.setStroke(new BasicStroke(1.5f));
        } else if (focused) {
            g2.setColor(borderFocus);
            g2.setStroke(new BasicStroke(1.5f));
        } else {
            g2.setColor(borderNormal);
            g2.setStroke(new BasicStroke(1f));
        }
        g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, arcSize, arcSize));
        g2.dispose();
    }

    public static Font resolveFont(int style, int size) {
        Font f = new Font(Constants.FONT_FAMILY, style, size);
        if (f.getFamily().equalsIgnoreCase(Constants.FONT_FAMILY))
            return f;
        return new Font("Segoe UI", style, size);
    }

    public static void paintUserIcon(Graphics2D g, int x, int y, int size, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.5f));
        int headR = size / 4;
        g.drawOval(x + size / 2 - headR, y, headR * 2, headR * 2);
        g.drawArc(x + 1, y + headR * 2 - 2, size - 2, size - headR * 2, 0, 180);
    }

    public static void paintLockIcon(Graphics2D g, int x, int y, int size, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.5f));
        int bodyW = size * 3 / 4, bodyH = size / 2;
        int bodyX = x + (size - bodyW) / 2, bodyY = y + size - bodyH;
        g.drawRoundRect(bodyX, bodyY, bodyW, bodyH, 3, 3);
        int shW = bodyW * 2 / 3, shX = bodyX + (bodyW - shW) / 2;
        g.drawArc(shX, y, shW, bodyY - y, 0, 180);
        g.fillOval(x + size / 2 - 2, bodyY + bodyH / 3, 4, 4);
    }
}
