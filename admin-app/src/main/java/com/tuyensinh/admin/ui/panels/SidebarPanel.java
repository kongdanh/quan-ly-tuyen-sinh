package com.tuyensinh.admin.ui.panels;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.tuyensinh.service.AuthService;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import static com.tuyensinh.admin.ui.components.RoundedTextField.resolveFont;

/**
 * Sidebar: Top (logo) → Center (menu) → Bottom (account + logout).
 * All colors reference Constants.DASH_*.
 */
public class SidebarPanel extends JPanel {

    private static final String[][] MENU = {
            {"Dashboard",          "icon_dashboard.svg"},
            {"Ngành tuyển sinh",   "icon_nganh.svg"},
            {"Tổ hợp môn",        "icon_tohop.svg"},
            {"Quản lý thí sinh",  "icon_user.svg"},
            {"Điểm thi",          "icon_diem.svg"},
            {"Điểm cộng",         "icon_diemcong.svg"},
            {"Nguyện vọng",       "icon_nguyenvongxt.svg"},
            {"Thống kê",          "icon_thongke.svg"},
    };

    private int selectedIndex = 0;
    private final Color sidebarBg;

    public SidebarPanel(MainFrame frame) {
        sidebarBg = Color.decode(Constants.DASH_SIDEBAR_BG);
        setBackground(sidebarBg);
        setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 0));
        setLayout(new BorderLayout());

        add(buildTop(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottom(frame), BorderLayout.SOUTH);
    }

    // ──────── TOP: logo ────────

    private JPanel buildTop() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, Constants.DASH_SIDEBAR_PAD, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(22, 0, 18, 0));
        top.setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 64));

        try {
            top.add(new JLabel(new FlatSVGIcon("assets/icon_login.svg", 22, 22)));
        } catch (Exception ignored) {}

        JLabel lbl = new JLabel(Constants.APP_TITLE_SHORT);
        lbl.setFont(resolveFont(Font.BOLD, 15));
        lbl.setForeground(Color.WHITE);
        top.add(lbl);
        return top;
    }

    // ──────── CENTER: menu items ────────

    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Section label
        JLabel menuLabel = new JLabel("  MENU");
        menuLabel.setFont(resolveFont(Font.BOLD, Constants.SIDEBAR_LABEL_FONT_SIZE));
        menuLabel.setForeground(new Color(255, 255, 255, 80));
        menuLabel.setBorder(new EmptyBorder(0, Constants.DASH_SIDEBAR_PAD, 8, 0));
        menuLabel.setAlignmentX(LEFT_ALIGNMENT);
        center.add(menuLabel);

        for (int i = 0; i < MENU.length; i++) {
            center.add(menuItem(i));
            center.add(Box.createVerticalStrut(2));
        }
        return center;
    }

    private JPanel menuItem(int index) {
        boolean active = index == selectedIndex;
        String text = MENU[index][0];
        String iconFile = MENU[index][1];

        Color activeBg  = withAlpha(Color.decode(Constants.DASH_PRIMARY), 80);
        Color hoverBg   = new Color(255, 255, 255, 14);
        Color activeBar = Color.decode(Constants.DASH_ACTIVE_BAR);
        Color textNormal = new Color(255, 255, 255, 150);
        Color textHover  = new Color(255, 255, 255, 210);

        return new JPanel(null) {
            boolean hovered = false;

            {
                setOpaque(false);
                setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, Constants.DASH_MENU_ITEM_HEIGHT));
                setMaximumSize(new Dimension(Constants.SIDEBAR_WIDTH, Constants.DASH_MENU_ITEM_HEIGHT));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int inset = 8;

                if (active) {
                    g2.setColor(activeBg);
                    g2.fill(new RoundRectangle2D.Double(inset, 2, w - inset * 2, h - 4, 8, 8));
                    g2.setColor(activeBar);
                    g2.fill(new RoundRectangle2D.Double(0, 8, 3, h - 16, 3, 3));
                } else if (hovered) {
                    g2.setColor(hoverBg);
                    g2.fill(new RoundRectangle2D.Double(inset, 2, w - inset * 2, h - 4, 8, 8));
                }

                // Icon
                int iconX = 26, iconY = (h - 16) / 2;
                try {
                    new FlatSVGIcon("assets/" + iconFile, 16, 16).paintIcon(this, g2, iconX, iconY);
                } catch (Exception ignored) {}

                // Text
                g2.setFont(resolveFont(active ? Font.BOLD : Font.PLAIN, Constants.SIDEBAR_ITEM_FONT_SIZE));
                g2.setColor(active ? Color.WHITE : (hovered ? textHover : textNormal));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, 52, (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
    }

    // ──────── BOTTOM: account + logout ────────

    private JPanel buildBottom(MainFrame frame) {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, Constants.DASH_SIDEBAR_PAD, Constants.DASH_SIDEBAR_PAD, Constants.DASH_SIDEBAR_PAD));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Constants.SIDEBAR_WIDTH - 40, 1));
        sep.setForeground(new Color(255, 255, 255, 20));
        bottom.add(sep);
        bottom.add(Box.createVerticalStrut(14));

        // Account panel (single row: user info | logout)
        JPanel account = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode(Constants.DASH_SIDEBAR_ACCOUNT_BG));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(),
                        Constants.DASH_CARD_RADIUS, Constants.DASH_CARD_RADIUS));
                g2.dispose();
            }
        };
        account.setOpaque(false);
        account.setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH - 40, 56));
        account.setMaximumSize(new Dimension(Constants.SIDEBAR_WIDTH - 40, 56));
        account.setAlignmentX(LEFT_ALIGNMENT);
        account.setLayout(new BorderLayout(8, 0));
        account.setBorder(new EmptyBorder(10, 14, 10, 10));

        // Left: name + role
        AuthService auth = AuthService.getInstance();
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblName = new JLabel(auth.getCurrentUsername());
        lblName.setFont(resolveFont(Font.BOLD, Constants.FONT_SIZE_BASE));
        lblName.setForeground(Color.WHITE);
        lblName.setAlignmentX(LEFT_ALIGNMENT);
        info.add(lblName);

        JLabel lblRole = new JLabel(auth.getCurrentRole());
        lblRole.setFont(resolveFont(Font.PLAIN, Constants.FONT_SIZE_XS));
        lblRole.setForeground(Color.decode(Constants.DASH_TEXT_MUTED));
        lblRole.setAlignmentX(LEFT_ALIGNMENT);
        info.add(lblRole);

        account.add(info, BorderLayout.CENTER);

        // Right: logout icon button
        JPanel logoutBtn = new JPanel(null) {
            boolean hovered = false;
            {
                setOpaque(false);
                setPreferredSize(new Dimension(32, 32));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) { frame.handleLogout(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(new Color(255, 255, 255, 15));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                }
                try {
                    new FlatSVGIcon("assets/icon_logout.svg", 16, 16)
                            .paintIcon(this, g2, (getWidth() - 16) / 2, (getHeight() - 16) / 2);
                } catch (Exception ignored) {}
                g2.dispose();
            }
        };

        JPanel rightWrap = new JPanel(new GridBagLayout());
        rightWrap.setOpaque(false);
        rightWrap.add(logoutBtn);
        account.add(rightWrap, BorderLayout.EAST);

        bottom.add(account);
        return bottom;
    }

    // ──────── Helpers ────────

    private static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}
