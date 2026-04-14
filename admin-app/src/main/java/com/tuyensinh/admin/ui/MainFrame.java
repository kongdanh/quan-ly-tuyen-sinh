package com.tuyensinh.admin.ui;

import com.tuyensinh.admin.ui.panels.*;
import com.tuyensinh.admin.util.AdminSession;
import com.tuyensinh.util.Constants;
import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    private Point dragOffset;
    private CardLayout cardLayout;
    private JPanel contentPanel;

    public MainFrame() {
        setUndecorated(true);
        setTitle(Constants.APP_TITLE);
        initComponents();
    }

    private void initComponents() {
        setSize(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(UIConstants.WINDOW_MIN_WIDTH, UIConstants.WINDOW_MIN_HEIGHT));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.decode(UIConstants.DASH_CONTENT_BG));
        root.setBorder(BorderFactory.createLineBorder(Color.decode(UIConstants.COLOR_BORDER), 1));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(new DashboardPanel(), "Dashboard");
        contentPanel.add(new NganhPanel(), "Ngành tuyển sinh");
        contentPanel.add(new ToHopMonPanel(), "Tổ hợp môn");
        contentPanel.add(new NganhToHopPanel(), "Ngành - Tổ hợp");
        contentPanel.add(new ThiSinhPanel(), "Quản lý thí sinh");
        contentPanel.add(new DiemCongPanel(), "Điểm cộng");
        contentPanel.add(new NguyenVongPanel(), "Nguyện vọng");
        contentPanel.add(new BangQuyDoiPanel(), "Bảng quy đổi");
        contentPanel.add(new ThongKePanel(), "Thống kê");
        contentPanel.add(new UserPanel(), "Người dùng");
        contentPanel.add(new NhomQuyenPanel(), "Nhóm quyền");
        contentPanel.add(new QuyenChucNangPanel(), "Quyền chức năng");

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(buildTopBar(), BorderLayout.NORTH);
        right.add(contentPanel, BorderLayout.CENTER);

        root.add(new SidebarPanel(this), BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);

        setContentPane(root);
    }

    public void switchPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.decode(UIConstants.DASH_PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, UIConstants.HEADER_HEIGHT));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);
        controls.add(ctrlBtn("—", Color.decode(UIConstants.DASH_GRID_LINE), e -> setState(Frame.ICONIFIED)));
        controls.add(ctrlBtn("X", Color.decode(UIConstants.DASH_ACCENT_ROSE), e -> { dispose(); System.exit(0); }));
        bar.add(controls, BorderLayout.EAST);

        attachDrag(bar);
        return bar;
    }

    private JButton ctrlBtn(String text, Color hoverBg, ActionListener action) {
        JButton btn = new JButton(text) {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                if (hover) {
                    g2.setColor(hoverBg);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.setColor(hover ? Color.WHITE : new Color(200, 210, 230));
                g2.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, (float) UIConstants.HEADER_FONT_SIZE));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(UIConstants.DASH_CTRL_BTN_W, UIConstants.HEADER_HEIGHT));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    private void attachDrag(JComponent comp) {
        comp.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragOffset = SwingUtilities.convertPoint(comp, e.getPoint(), MainFrame.this); }
        });
        comp.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point loc = e.getLocationOnScreen();
                setLocation(loc.x - dragOffset.x, loc.y - dragOffset.y);
            }
        });
    }

    public void handleLogout() {
        int r = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            AdminSession.getInstance().logout();
            dispose();
            new LoginForm().setVisible(true);
        }
    }
}