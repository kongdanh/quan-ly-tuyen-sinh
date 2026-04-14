package com.tuyensinh.admin.ui.components;

import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import java.awt.*;

public class CustomComboBox<E> extends JComboBox<E> {

    public CustomComboBox(E[] items) {
        super(items);
        init();
    }

    public CustomComboBox(DefaultComboBoxModel<E> model) {
        super(model);
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(200, UIConstants.INPUT_HEIGHT));
        setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, (float) UIConstants.FONT_SIZE_MD));
        setBackground(Color.WHITE);
        setForeground(Color.decode(UIConstants.COLOR_TEXT));
    }
}