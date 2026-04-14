package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.util.UIConstants;
import javax.swing.*;
import java.awt.*;
public class UserPanel extends JPanel {
    public UserPanel() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JLabel lblTitle = new JLabel("ĐÂY LÀ TRANG QUẢN LÝ NGƯỜI DÙNG");
        
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));

        add(lblTitle);
    }
}
