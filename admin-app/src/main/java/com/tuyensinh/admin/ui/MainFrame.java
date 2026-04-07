package com.tuyensinh.admin.ui;

import com.tuyensinh.service.AuthService;
import com.tuyensinh.admin.ui.panels.DashboardPanel;
import com.tuyensinh.admin.ui.panels.SidebarPanel;
import com.tuyensinh.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static com.tuyensinh.admin.ui.components.RoundedTextField.resolveFont;

public class MainFrame extends JFrame {

    private Point dragOffset;

    public MainFrame() {
        setUndecorated(true);
        setTitle(Constants.APP_TITLE);
        initComponents();
        initEvents();
    }

    // ──────────── Init ────────────

    private void initComponents() {
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(Constants.WINDOW_MIN_WIDTH, Constants.WINDOW_MIN_HEIGHT));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.decode(Constants.DASH_CONTENT_BG));
        root.setBorder(BorderFactory.createLineBorder(Color.decode(Constants.COLOR_BORDER), 1));

        root.add(new SidebarPanel(this), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(buildTopBar(), BorderLayout.NORTH);
        right.add(new DashboardPanel(), BorderLayout.CENTER);
        root.add(right, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void initEvents() { /* future menu switching */ }

    // ──────────── Top Bar ────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.decode(Constants.DASH_PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Bottom shadow line
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, Constants.HEADER_HEIGHT));

        // Controls (right-aligned)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);
        controls.add(ctrlBtn("—", Color.decode(Constants.DASH_GRID_LINE),
                e -> setState(Frame.ICONIFIED)));
        controls.add(ctrlBtn("X", Color.decode(Constants.DASH_ACCENT_ROSE),
                e -> { dispose(); System.exit(0); }));
        bar.add(controls, BorderLayout.EAST);

        // Drag
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
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(resolveFont(Font.PLAIN, Constants.HEADER_FONT_SIZE));
        btn.setPreferredSize(new Dimension(Constants.DASH_CTRL_BTN_W, Constants.HEADER_HEIGHT));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    // ──────────── Drag ────────────

    private void attachDrag(JComponent comp) {
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = SwingUtilities.convertPoint(comp, e.getPoint(), MainFrame.this);
            }
        });
        comp.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point loc = e.getLocationOnScreen();
                setLocation(loc.x - dragOffset.x, loc.y - dragOffset.y);
            }
        });
    }

    // ──────────── Logout ────────────

    public void handleLogout() {
        int r = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            AuthService.getInstance().logout();
            dispose();
            new LoginForm().setVisible(true);
        }
    }
}