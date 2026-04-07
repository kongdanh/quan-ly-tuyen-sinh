package com.tuyensinh.admin.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.tuyensinh.service.AuthService;
import com.tuyensinh.admin.ui.components.RoundedButton;
import com.tuyensinh.admin.ui.components.RoundedTextField;
import com.tuyensinh.admin.ui.components.RoundedPasswordField;
import com.tuyensinh.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static com.tuyensinh.admin.ui.components.RoundedTextField.resolveFont;

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
        setSize(Constants.LOGIN_WIDTH, Constants.LOGIN_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Root panel with subtle border
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);
        rootPanel.setBorder(BorderFactory.createLineBorder(Color.decode(Constants.COLOR_BORDER), 1));

        rootPanel.add(createLeftPanel(), BorderLayout.WEST);
        rootPanel.add(createRightPanel(), BorderLayout.CENTER);
        setContentPane(rootPanel);

        // ENTER triggers login
        getRootPane().setDefaultButton(btnLogin);
    }

    // ======================== LEFT PANEL ========================
    private JPanel createLeftPanel() {
        JPanel left = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0,
                        Color.decode(Constants.LOGIN_GRADIENT_TOP), 0, getHeight(),
                        Color.decode(Constants.LOGIN_GRADIENT_BOTTOM));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        left.setLayout(new GridBagLayout());
        left.setPreferredSize(new Dimension(Constants.LOGIN_LEFT_WIDTH, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        // SVG Logo
        gbc.gridy = 0;
        try {
            FlatSVGIcon icon = new FlatSVGIcon("assets/icon_login.svg",
                    Constants.LOGIN_ICON_SIZE, Constants.LOGIN_ICON_SIZE);
            left.add(new JLabel(icon), gbc);
        } catch (Exception e) {
            JLabel fb = new JLabel("🎓");
            fb.setFont(new Font("Segoe UI", Font.PLAIN, 80));
            fb.setForeground(Color.WHITE);
            left.add(fb, gbc);
        }

        // University name
        gbc.gridy = 1;
        JLabel lblUni = new JLabel(Constants.APP_UNIVERSITY);
        lblUni.setFont(resolveFont(Font.BOLD, 32));
        lblUni.setForeground(Color.WHITE);
        left.add(lblUni, gbc);

        // Subtitle
        gbc.gridy = 2;
        JLabel lblSub = new JLabel("Hệ thống Quản lý Tuyển sinh");
        lblSub.setFont(resolveFont(Font.PLAIN, 18));
        lblSub.setForeground(new Color(200, 220, 255));
        left.add(lblSub, gbc);

        // Draggable
        attachDrag(left);
        return left;
    }

    // ======================== RIGHT PANEL ========================
    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);

        right.add(createHeader(), BorderLayout.NORTH);

        // Center the form vertically + horizontally
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(createFormPanel());
        right.add(wrapper, BorderLayout.CENTER);

        return right;
    }

    // ======================== HEADER (minimize / close) ========================
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, Constants.LOGIN_HEADER_HEIGHT));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);

        // Minimize → gray hover
        JButton btnMin = makeControlBtn("—", new Color(230, 230, 230));
        btnMin.addActionListener(e -> setState(Frame.ICONIFIED));
        controls.add(btnMin);

        // Close → red hover
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
        btn.setFont(resolveFont(Font.PLAIN, 13));
        btn.setPreferredSize(new Dimension(46, Constants.LOGIN_HEADER_HEIGHT));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.decode(Constants.COLOR_TEXT_MUTED));
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
                btn.setForeground(Color.decode(Constants.COLOR_TEXT_MUTED));
            }
        });
        return btn;
    }

    // ======================== FORM PANEL ========================
    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        // Title
        g.gridy = 0;
        g.insets = new Insets(0, 0, Constants.SECTION_GAP * 2, 0);
        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setFont(resolveFont(Font.BOLD, Constants.LOGIN_TITLE_FONT_SIZE));
        lblTitle.setForeground(Color.decode(Constants.COLOR_TEXT));
        form.add(lblTitle, g);

        // Username label
        g.gridy = 1;
        g.insets = new Insets(0, 0, 6, 0);
        form.add(makeLabel("Tên đăng nhập"), g);

        // Username field
        g.gridy = 2;
        g.insets = new Insets(0, 0, Constants.FORM_GAP, 0);
        txtUsername = new RoundedTextField("Nhập tên đăng nhập");
        txtUsername.setPreferredSize(new Dimension(Constants.LOGIN_FORM_WIDTH, Constants.LOGIN_INPUT_HEIGHT));
        form.add(txtUsername, g);

        // Password label
        g.gridy = 3;
        g.insets = new Insets(0, 0, 6, 0);
        form.add(makeLabel("Mật khẩu"), g);

        // Password field
        g.gridy = 4;
        g.insets = new Insets(0, 0, Constants.SECTION_GAP + 10, 0);
        txtPassword = new RoundedPasswordField("Nhập mật khẩu");
        txtPassword.setPreferredSize(new Dimension(Constants.LOGIN_FORM_WIDTH, Constants.LOGIN_INPUT_HEIGHT));
        form.add(txtPassword, g);

        // Login button
        g.gridy = 5;
        g.insets = new Insets(0, 0, 10, 0);
        btnLogin = new RoundedButton("Đăng nhập");
        btnLogin.setPreferredSize(new Dimension(Constants.LOGIN_FORM_WIDTH, Constants.LOGIN_BTN_HEIGHT));
        btnLogin.addActionListener(e -> doLogin());
        form.add(btnLogin, g);

        // Status
        g.gridy = 6;
        g.insets = new Insets(0, 0, 0, 0);
        g.anchor = GridBagConstraints.CENTER;
        lblStatus = new JLabel(" ");
        lblStatus.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_SM));
        lblStatus.setForeground(Color.decode(Constants.COLOR_DANGER));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        form.add(lblStatus, g);

        return form;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(resolveFont(Font.BOLD, Constants.FONT_SIZE_MD));
        l.setForeground(Color.decode(Constants.COLOR_TEXT));
        return l;
    }

    // ======================== DRAG SUPPORT ========================
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

    // ======================== LOGIN LOGIC ========================
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

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Thread.sleep(800);
                return AuthService.getInstance().login(username, password);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
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
                    lblStatus.setText("Lỗi kết nối hệ thống!");
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                }
            }
        };
        worker.execute();
    }
}