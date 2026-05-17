package com.tuyensinh.admin;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.tuyensinh.admin.ui.LoginForm;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.util.HibernateUtil;

import javax.swing.*;
import java.awt.Font;

public class Main {
    public static void main(String[] args) {
        try {
            //setup font
            FlatRobotoFont.install();
            
            FlatLaf.setPreferredFontFamily(FlatRobotoFont.FAMILY);
            FlatLaf.setPreferredLightFontFamily(FlatRobotoFont.FAMILY_LIGHT);
            FlatLaf.setPreferredSemiboldFontFamily(FlatRobotoFont.FAMILY_SEMIBOLD);
            FlatLaf.setPreferredMonospacedFontFamily(Font.MONOSPACED);
            
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

            FlatLightLaf.setup();
            
            Font uiFont = new Font(UIConstants.FONT_FAMILY, Font.PLAIN, UIConstants.FONT_SIZE_BASE);
            UIManager.put("defaultFont", uiFont);

        } catch (Exception ex) {
            System.err.println("Không thể khởi tạo giao diện FlatLaf.");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                HibernateUtil.getSessionFactory();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Không thể kết nối database!\nVui lòng kiểm tra lại cấu hình MySQL.\n" + e.getMessage(),
                    "Lỗi kết nối CSDL", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            new LoginForm().setVisible(true);
        });
    }
}