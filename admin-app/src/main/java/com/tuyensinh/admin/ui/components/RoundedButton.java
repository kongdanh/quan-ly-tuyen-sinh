package com.tuyensinh.admin.ui.components;

import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton {
    private boolean isHover = false;
    private boolean isPressed = false;

    public RoundedButton(String text) {
        super(text);
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.BTN_FONT_SIZE));
        setForeground(Color.decode(UIConstants.BTN_PRIMARY_TEXT));
        setPreferredSize(new Dimension(130, 38));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHover = false;
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
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isPressed) {
            g2.setColor(Color.decode(UIConstants.COLOR_NAVY_DARK));
        } else if (isHover) {
            g2.setColor(Color.decode(UIConstants.BTN_PRIMARY_HOVER));
        } else {
            g2.setColor(Color.decode(UIConstants.BTN_PRIMARY_BG));
        }

        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), UIConstants.LOGIN_BTN_RADIUS, UIConstants.LOGIN_BTN_RADIUS));

        super.paintComponent(g);
        g2.dispose();
    }
}