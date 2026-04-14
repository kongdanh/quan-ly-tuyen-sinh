package com.tuyensinh.admin.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.tuyensinh.service.AuthService;
import com.tuyensinh.admin.ui.components.RoundedButton;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.admin.ui.components.RoundedPasswordField;
import com.tuyensinh.util.Constants;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.admin.util.AdminSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginForm extends JFrame {

    private RoundedTextField txtUsername;
    private RoundedPasswordField txtPassword;
    private RoundedButton btnLogin;
    private JLabel lblStatus;
    private Point dragOffset;

    public LoginForm() {
        setUndecorated(true);
        setTitle("");
        initComponents();
    }

    private void initComponents() {
        setSize(UIConstants.LOGIN_WIDTH, UIConstants.LOGIN_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setBorder(BorderFactory.createLineBorder(Color.decode(UIConstants.COLOR_BORDER), 1));

        rootPanel.add(createLeftPanel(), BorderLayout.WEST);
        rootPanel.add(createRightPanel(), BorderLayout.CENTER);
        setContentPane(rootPanel);

        getRootPane().setDefaultButton(btnLogin);
    }

    private JPanel createLeftPanel() {
        JPanel left = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0,
                        Color.decode(UIConstants.LOGIN_GRADIENT_TOP), 0, getHeight(),
                        Color.decode(UIConstants.LOGIN_GRADIENT_BOTTOM));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        left.setLayout(new GridBagLayout());
        left.setPreferredSize(new Dimension(UIConstants.LOGIN_LEFT_WIDTH, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        gbc.gridy = 0;
        try {
            FlatSVGIcon icon = new FlatSVGIcon("assets/icon_login.svg",
                    UIConstants.LOGIN_ICON_SIZE, UIConstants.LOGIN_ICON_SIZE);
            left.add(new JLabel(icon), gbc);
        } catch (Exception e) {
            JLabel fb = new JLabel("🎓");
            fb.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 80f));
            fb.setForeground(Color.WHITE);
            left.add(fb, gbc);
        }

        gbc.gridy = 1;
        JLabel lblUni = new JLabel(Constants.APP_UNIVERSITY); // APP_UNIVERSITY nằm ở Constants
        lblUni.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 32f));
        lblUni.setForeground(Color.WHITE);
        left.add(lblUni, gbc);

        gbc.gridy = 2;
        JLabel lblSub = new JLabel("Hệ thống Quản lý Tuyển sinh");
        lblSub.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 18f));
        lblSub.setForeground(new Color(200, 220, 255));
        left.add(lblSub, gbc);

        attachDrag(left);
        return left;
    }

    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);

        right.add(createHeader(), BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(createFormPanel());
        right.add(wrapper, BorderLayout.CENTER);

        return right;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, UIConstants.LOGIN_HEADER_HEIGHT));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);

        JButton btnMin = makeControlBtn("—", new Color(230, 230, 230));
        btnMin.addActionListener(e -> setState(Frame.ICONIFIED));
        controls.add(btnMin);

        JButton btnClose = makeControlBtn("X", new Color(220, 53, 69));
        btnClose.addActionListener(e -> {
            dispose();
            System.exit(0);
        });
        controls.add(btnClose);

        header.add(controls, BorderLayout.EAST);
        attachDrag(header);
        return header;
    }

    private JButton makeControlBtn(String text, Color hoverBg) {
        JButton btn = new JButton(text);
        btn.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 13f));
        btn.setPreferredSize(new Dimension(46, UIConstants.LOGIN_HEADER_HEIGHT));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.decode(UIConstants.COLOR_TEXT_MUTED));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusable(false);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
                btn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.decode(UIConstants.COLOR_TEXT_MUTED));
            }
        });
        return btn;
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridy = 0;
        g.insets = new Insets(0, 0, UIConstants.SECTION_GAP * 2, 0);
        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.LOGIN_TITLE_FONT_SIZE));
        lblTitle.setForeground(Color.decode(UIConstants.COLOR_TEXT));
        form.add(lblTitle, g);

        g.gridy = 1;
        g.insets = new Insets(0, 0, 6, 0);
        form.add(makeLabel("Tên đăng nhập"), g);

        g.gridy = 2;
        g.insets = new Insets(0, 0, UIConstants.FORM_GAP, 0);
        txtUsername = new RoundedTextField("Nhập tên đăng nhập");
        txtUsername.setPreferredSize(new Dimension(UIConstants.LOGIN_FORM_WIDTH, UIConstants.LOGIN_INPUT_HEIGHT));
        form.add(txtUsername, g);

        g.gridy = 3;
        g.insets = new Insets(0, 0, 6, 0);
        form.add(makeLabel("Mật khẩu"), g);

        g.gridy = 4;
        g.insets = new Insets(0, 0, UIConstants.SECTION_GAP + 10, 0);
        txtPassword = new RoundedPasswordField("Nhập mật khẩu");
        txtPassword.setPreferredSize(new Dimension(UIConstants.LOGIN_FORM_WIDTH, UIConstants.LOGIN_INPUT_HEIGHT));
        form.add(txtPassword, g);

        g.gridy = 5;
        g.insets = new Insets(0, 0, 10, 0);
        btnLogin = new RoundedButton("Đăng nhập");
        btnLogin.setPreferredSize(new Dimension(UIConstants.LOGIN_FORM_WIDTH, UIConstants.LOGIN_BTN_HEIGHT));
        btnLogin.addActionListener(e -> doLogin());
        form.add(btnLogin, g);

        g.gridy = 6;
        g.insets = new Insets(0, 0, 0, 0);
        g.anchor = GridBagConstraints.CENTER;
        lblStatus = new JLabel(" ");
        lblStatus.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, (float) UIConstants.FONT_SIZE_SM));
        lblStatus.setForeground(Color.decode(UIConstants.COLOR_DANGER));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        form.add(lblStatus, g);

        return form;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.FONT_SIZE_MD));
        l.setForeground(Color.decode(UIConstants.COLOR_TEXT));
        return l;
    }

    private void attachDrag(JComponent comp) {
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = SwingUtilities.convertPoint(comp, e.getPoint(), LoginForm.this);
            }
        });
        comp.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point s = e.getLocationOnScreen();
                setLocation(s.x - dragOffset.x, s.y - dragOffset.y);
            }
        });
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        txtUsername.setError(false);
        txtPassword.setError(false);

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Vui lòng nhập đầy đủ thông tin!");
            if (username.isEmpty())
                txtUsername.setError(true);
            if (password.isEmpty())
                txtPassword.setError(true);
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang xác thực...");
        lblStatus.setText(" ");

        SwingWorker<com.tuyensinh.model.User, Void> worker = new SwingWorker<>() {
            @Override
            protected com.tuyensinh.model.User doInBackground() throws Exception {
                Thread.sleep(800);
                return AuthService.getInstance().loginAdmin(username, password);
            }

            @Override
            protected void done() {
                try {
                    com.tuyensinh.model.User loggedInUser = get();
                    
                    if (loggedInUser != null) {
                        com.tuyensinh.dao.QuyenChucNangDAO quyenDAO = new com.tuyensinh.dao.QuyenChucNangDAO();
                        java.util.List<String> listQuyen = quyenDAO.getMaChucNangCoXem(loggedInUser.getNhomQuyen().getId());
                        
                        java.util.Set<String> setQuyen = new java.util.HashSet<>(listQuyen);

                        AdminSession.getInstance().login(
                                loggedInUser.getUsername(), 
                                loggedInUser.getNhomQuyen().getMaNhom(), 
                                setQuyen
                        );

                        dispose();
                        new MainFrame().setVisible(true);
                    } else {
                        lblStatus.setText("Sai tài khoản hoặc mật khẩu!");
                        txtPassword.setText("");
                        txtPassword.requestFocus();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Đăng nhập");
                    }
                } catch (Exception e) {
                    e.printStackTrace(); 
                    lblStatus.setText("Lỗi kết nối hệ thống!");
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                }
            }
        };
        worker.execute();
    }
}