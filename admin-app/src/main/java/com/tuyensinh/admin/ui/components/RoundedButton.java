package com.tuyensinh.admin.ui.components;

import com.tuyensinh.util.Constants;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton {

    private final int arcSize;
    private final Color normalBg;
    private final Color hoverBg;
    private final Color pressBg;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public RoundedButton(String text) {
        this(text, Constants.LOGIN_BTN_RADIUS);
    }

    public RoundedButton(String text, int arcSize) {
        super(text);
        this.arcSize = arcSize;
        this.normalBg = Color.decode(Constants.BTN_PRIMARY_BG);
        this.hoverBg = Color.decode(Constants.BTN_PRIMARY_HOVER);
        this.pressBg = Color.decode(Constants.COLOR_NAVY_DARK);

        setContentAreaFilled(false);
        setOpaque(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(Color.WHITE);
        setFont(RoundedTextField.resolveFont(Font.BOLD, Constants.BTN_FONT_SIZE + 2));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = isPressed ? pressBg : (isHovered ? hoverBg : normalBg);
        if (!isEnabled())
            bg = new Color(180, 190, 200);

        g2d.setColor(bg);
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arcSize, arcSize));
        g2d.dispose();

        super.paintComponent(g);
    }
}
