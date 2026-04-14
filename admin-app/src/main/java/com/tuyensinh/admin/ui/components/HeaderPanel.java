package com.tuyensinh.admin.ui.components;

import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Typography-first header. Title (32pt Bold) + subtitle (14pt, muted).
 */
public class HeaderPanel extends JPanel {

    public HeaderPanel(String title) {
        this(title, null);
    }

    public HeaderPanel(String title, String subtitle) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(4, 0, UIConstants.SECTION_GAP + 4, 0));

        add(antiAliasedLabel(title,
                UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.DASH_HEADER_TITLE_SIZE),
                Color.decode(UIConstants.DASH_TEXT_DARK)));

        if (subtitle != null && !subtitle.isEmpty()) {
            add(Box.createVerticalStrut(6));
            add(antiAliasedLabel(subtitle,
                    UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, (float) UIConstants.DASH_HEADER_SUB_SIZE),
                    Color.decode(UIConstants.DASH_TEXT_MUTED)));
        }
    }

    private JLabel antiAliasedLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                ((Graphics2D) g).setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paintComponent(g);
            }
        };
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }
}