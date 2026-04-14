package com.tuyensinh.admin.ui.components;

import com.tuyensinh.admin.util.UIConstants;
import java.awt.*;

public class SearchTextField extends RoundedTextField {

    public SearchTextField() {
        super("Tìm kiếm..."); // Placeholder mặc định
        
        setPreferredSize(new Dimension(UIConstants.SEARCH_WIDTH, UIConstants.INPUT_HEIGHT));
        
        setIconPainter((g2d, x, y, size, color) -> {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int r = size / 2 - 2;
            g2d.drawOval(x, y, r * 2, r * 2);
            
            int startX = x + r + (int)(r * 0.7);
            int startY = y + r + (int)(r * 0.7);
            g2d.drawLine(startX, startY, x + size - 2, y + size - 2);
        });
    }
}