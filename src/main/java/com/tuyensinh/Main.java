package com.tuyensinh;

import com.tuyensinh.ui.LoginForm;
import com.tuyensinh.util.HibernateUtil;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // Khởi động Hibernate kết nối DB
            // try {
            //     HibernateUtil.getSessionFactory();
            // } catch (Exception e) {
            //     JOptionPane.showMessageDialog(null,
            //         "Không thể kết nối database!\n" + e.getMessage(),
            //         "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            //     System.exit(1);
            // }

            new LoginForm().setVisible(true);
        });
    }
}