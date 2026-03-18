package com.tuyensinh.ui;

import com.tuyensinh.service.AuthService;
import com.tuyensinh.util.Constants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;

    public LoginForm() {
        setTitle(Constants.APP_TITLE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        // Panel nền
        JPanel root = new JPanel(new BorderLayout());

        // --- Header ---
        JPanel header = new JPanel();
        header.setBackground(new Color(30, 90, 160));
        header.setPreferredSize(new Dimension(0, 80));
        header.setLayout(new GridBagLayout());
        JLabel lblTitle = new JLabel("🎓 " + Constants.APP_TITLE);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        header.add(lblTitle);

        // --- Form ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(24, 40, 16, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 4, 6, 4);

        // Tài khoản
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        form.add(new JLabel("Tài khoản:"), g);
        g.gridx = 1; g.weightx = 1;
        txtUsername = new JTextField();
        txtUsername.setPreferredSize(new Dimension(200, 30));
        form.add(txtUsername, g);

        // Mật khẩu
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        form.add(new JLabel("Mật khẩu:"), g);
        g.gridx = 1; g.weightx = 1;
        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(200, 30));
        form.add(txtPassword, g);

        // Nút đăng nhập
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; g.insets = new Insets(14, 4, 4, 4);
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setBackground(new Color(30, 90, 160));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        btnLogin.setPreferredSize(new Dimension(0, 36));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> doLogin());
        form.add(btnLogin, g);

        // Label trạng thái lỗi
        g.gridy = 3; g.insets = new Insets(2, 4, 2, 4);
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        form.add(lblStatus, g);

        // Enter để đăng nhập
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void doLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            lblStatus.setText("Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        // Chạy login trên thread riêng tránh đơ UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return AuthService.getInstance().login(user, pass);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        dispose();
                        new MainFrame().setVisible(true);
                    } else {
                        lblStatus.setText("Tài khoản hoặc mật khẩu không đúng.");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Đăng nhập");
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Lỗi: " + ex.getMessage());
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                }
            }
        };
        worker.execute();
    }
}
