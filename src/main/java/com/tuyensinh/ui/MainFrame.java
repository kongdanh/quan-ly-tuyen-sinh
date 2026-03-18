package com.tuyensinh.ui;

import com.tuyensinh.service.AuthService;
import com.tuyensinh.ui.panels.*;
import com.tuyensinh.util.Constants;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel contentPanel = new JPanel(cards);

    public MainFrame() {
        setTitle(Constants.APP_TITLE + " v" + Constants.APP_VERSION
                 + "  |  " + AuthService.getInstance().getCurrentUsername()
                 + " [" + AuthService.getInstance().getCurrentRole() + "]");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        // --- Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(30, 42, 56));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));

        JLabel logo = new JLabel("🎓 TUYỂN SINH SGU");
        logo.setForeground(new Color(200, 220, 255));
        logo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 8, 12, 0));
        sidebar.add(logo);

        // sau khi có ui panel thì mở lại dashboard
        // --- Đăng ký panels ---
        // contentPanel.add(new NganhPanel(),       "NGANH");
        // contentPanel.add(new TohopMonPanel(),    "TOHOP");
        // contentPanel.add(new NganhTohopPanel(),  "NGANH_TOHOP");
        // contentPanel.add(new ThiSinhPanel(),     "THI_SINH");
        // contentPanel.add(new DiemThiPanel(),     "DIEM_THI");
        // contentPanel.add(new DiemCongPanel(),    "DIEM_CONG");
        // contentPanel.add(new NguyenVongPanel(),  "NGUYEN_VONG");
        // contentPanel.add(new BangQuyDoiPanel(),  "BANG_QD");
        // contentPanel.add(new ThongKePanel(),     "THONG_KE");

        // --- Menu items ---
        String[][] menus = {
            {"NGANH",      "🏫  Ngành tuyển sinh"},
            {"TOHOP",      "📚  Tổ hợp môn"},
            {"NGANH_TOHOP","🔗  Ngành - Tổ hợp"},
            {"THI_SINH",   "🎓  Thí sinh"},
            {"DIEM_THI",   "📊  Điểm thi"},
            {"DIEM_CONG",  "⭐  Điểm cộng"},
            {"NGUYEN_VONG","📝  Nguyện vọng & XT"},
            {"BANG_QD",    "🔄  Bảng quy đổi"},
            {"THONG_KE",   "📈  Thống kê"},
        };
        for (String[] m : menus) {
            JButton btn = buildMenuBtn(m[1]);
            String key = m[0];
            btn.addActionListener(e -> cards.show(contentPanel, key));
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(3));
        }

        sidebar.add(Box.createVerticalGlue());
        JButton btnLogout = buildMenuBtn("🚪  Đăng xuất");
        btnLogout.setBackground(new Color(160, 40, 40));
        btnLogout.addActionListener(e -> {
            AuthService.getInstance().logout();
            dispose();
            new LoginForm().setVisible(true);
        });
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Hiển thị panel đầu tiên
        cards.show(contentPanel, "NGANH");
    }

    private JButton buildMenuBtn(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 94));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 6));
        return btn;
    }
}
