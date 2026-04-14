package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.util.UIConstants;
import javax.swing.*;
import java.awt.*;
public class ToHopMonPanel extends JPanel {

    public ToHopMonPanel() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JLabel lblTitle = new JLabel("ĐÂY LÀ TRANG TỔ HỢP MÔN");
        
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));

        add(lblTitle);
    }
    
}
