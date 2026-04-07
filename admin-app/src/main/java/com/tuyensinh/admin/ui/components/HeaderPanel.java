package com.tuyensinh.admin.ui.components;

import com.tuyensinh.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.tuyensinh.admin.ui.components.RoundedTextField.resolveFont;

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
        setBorder(new EmptyBorder(4, 0, Constants.SECTION_GAP + 4, 0));

        add(antiAliasedLabel(title,
                resolveFont(Font.BOLD, Constants.DASH_HEADER_TITLE_SIZE),
                Color.decode(Constants.DASH_TEXT_DARK)));

        if (subtitle != null && !subtitle.isEmpty()) {
            add(Box.createVerticalStrut(6));
            add(antiAliasedLabel(subtitle,
                    resolveFont(Font.PLAIN, Constants.DASH_HEADER_SUB_SIZE),
                    Color.decode(Constants.DASH_TEXT_MUTED)));
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
