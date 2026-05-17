package com.tuyensinh.admin.ui.panels;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.admin.util.AdminSession;
import com.tuyensinh.util.Constants;       
import com.tuyensinh.admin.util.UIConstants; 

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class SidebarPanel extends JPanel {

    private static class MenuDef {
        String title, icon, permissionCode;
        MenuDef(String t, String i, String p) { title = t; icon = i; permissionCode = p; }
    }

    private static final MenuDef[] ALL_MENUS = {
            new MenuDef("HE THONG & CAU HINH", "HEADER", null),
            new MenuDef("Dashboard",           "icon_dashboard.svg",   null), 
            new MenuDef("Đợt tuyển sinh",      "icon_cauhinh.svg",     Constants.QUYEN_CAU_HINH),
            new MenuDef("Bảng quy đổi",        "icon_bangquydoi.svg",  Constants.QUYEN_BANG_QUY_DOI), 
            new MenuDef("Điểm cộng",           "icon_diemcong.svg",    Constants.QUYEN_DIEM_CONG),

            new MenuDef("DANH MỤC XÉT TUYỂN",  "HEADER", null),
            new MenuDef("Quản lý điểm chuẩn",  "icon_diem.svg",        Constants.QUYEN_DIEM_CHUAN),
            new MenuDef("Ngành tuyển sinh",    "icon_nganh.svg",       Constants.QUYEN_NGANH),
            new MenuDef("Tổ hợp môn",          "icon_tohop.svg",       Constants.QUYEN_TOHOP),
            new MenuDef("Ngành - Tổ hợp",      "icon_nganh_tohop.svg", Constants.QUYEN_NGANH_TOHOP),

            new MenuDef("QUẢN LÝ HỒ SƠ",       "HEADER", null),
            new MenuDef("Quản lý thí sinh",    "icon_user.svg",        null),
            new MenuDef("Hồ sơ xét tuyển",     "icon_user.svg",        Constants.QUYEN_HO_SO),
            new MenuDef("Điểm thi",            "icon_diem.svg",        Constants.QUYEN_DIEM_THI),
            
            new MenuDef("XỬ LÝ KẾT QUẢ",       "HEADER", null),
            new MenuDef("Quản lý xét tuyển",   "icon_nguyenvongxt.svg",Constants.QUYEN_NGUYEN_VONG),
            new MenuDef("Thống kê",            "icon_thongke.svg",     Constants.QUYEN_THONG_KE),

            new MenuDef("PHÂN QUYỀN",          "HEADER", null),
            new MenuDef("Người dùng",          "icon_user.svg",        Constants.QUYEN_NGUOI_DUNG),
            new MenuDef("Nhóm quyền",          "icon_phanquyen.svg",   Constants.QUYEN_PHAN_QUYEN),
        };

    private final List<JPanel> menuItems = new ArrayList<>();
    private int selectedIndex = 0;
    private final MainFrame mainFrame;

    public SidebarPanel(MainFrame frame) {
        this.mainFrame = frame;
        setBackground(Color.decode(UIConstants.DASH_SIDEBAR_BG));
        
        Dimension fixedSize = new Dimension(UIConstants.SIDEBAR_WIDTH, UIConstants.WINDOW_HEIGHT);
        setPreferredSize(fixedSize);
        setMinimumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        setMaximumSize(fixedSize);
        setLayout(new BorderLayout());

        add(buildTop(), BorderLayout.NORTH);
        add(buildCenterScroll(), BorderLayout.CENTER);
        add(buildBottom(frame), BorderLayout.SOUTH);
    }

    private JPanel buildTop() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, UIConstants.DASH_SIDEBAR_PAD, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(14, 0, 10, 0));
        top.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 54));


        try { top.add(new JLabel(new FlatSVGIcon("assets/icon_login.svg", 22, 22))); } catch (Exception ignored) {}

        JLabel lbl = new JLabel(Constants.APP_TITLE_SHORT);
        lbl.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 16f));
        lbl.setForeground(Color.WHITE);
        top.add(lbl);
        return top;
    }

    private JScrollPane buildCenterScroll() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(10, 0, 0, 0));

        AdminSession session = AdminSession.getInstance();
        int currentIndex = 0;

        for (MenuDef def : ALL_MENUS) {
            if ("HEADER".equals(def.icon)) {
                JLabel headerLabel = new JLabel("  " + def.title);
                headerLabel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 10f)); 
                headerLabel.setForeground(new Color(255, 255, 255, 120)); 
                headerLabel.setBorder(new EmptyBorder(15, UIConstants.DASH_SIDEBAR_PAD, 5, 0));
                headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                center.add(headerLabel);
            } 
            else if (def.permissionCode == null || session.hasPermission(def.permissionCode)) {
                JPanel item = createMenuItem(def.title, def.icon, currentIndex);
                menuItems.add(item);
                center.add(item);
                center.add(Box.createVerticalStrut(2));
                currentIndex++;
            }
        }

        JScrollPane scrollPane = new JScrollPane(center);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    private JPanel createMenuItem(String text, String iconFile, int index) {
        Color activeBg  = new Color(37, 99, 235, 60);
        Color hoverBg   = new Color(255, 255, 255, 15);
        Color activeBar = Color.decode(UIConstants.DASH_ACTIVE_BAR);

        JPanel pnl = new JPanel(null) {
            boolean hovered = false;
            {
                setOpaque(false);
                Dimension itemSize = new Dimension(UIConstants.SIDEBAR_WIDTH, 38);
                setPreferredSize(itemSize);
                setMinimumSize(itemSize);
                setMaximumSize(itemSize);
                setAlignmentX(Component.LEFT_ALIGNMENT); 
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        selectedIndex = index;
                        mainFrame.switchPanel(text); 
                        for (JPanel p : menuItems) p.repaint(); 
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

                int w = getWidth(), h = getHeight();
                int inset = 12;

                if (index == selectedIndex) {
                    g2.setColor(activeBg);
                    g2.fill(new RoundRectangle2D.Double(inset, 0, w - inset * 2, h, 8, 8));
                    g2.setColor(activeBar);
                    g2.fill(new RoundRectangle2D.Double(0, 8, 4, h - 16, 4, 4));
                } else if (hovered) {
                    g2.setColor(hoverBg);
                    g2.fill(new RoundRectangle2D.Double(inset, 0, w - inset * 2, h, 8, 8));
                }

                try { new FlatSVGIcon("assets/" + iconFile, 16, 16).paintIcon(this, g2, 28, (h - 16) / 2); } catch (Exception ignored) {}


                int fontStyle = index == selectedIndex ? Font.BOLD : Font.BOLD;
                g2.setFont(UIManager.getFont("defaultFont").deriveFont(fontStyle, 14f));
                
                g2.setColor(index == selectedIndex ? Color.WHITE : new Color(255, 255, 255, 230));
                
                FontMetrics fm = g2.getFontMetrics();
                String drawText = text;
                int maxTextWidth = w - 56 - inset; 
                if (fm.stringWidth(drawText) > maxTextWidth) {
                    while (drawText.length() > 0 && fm.stringWidth(drawText + "...") > maxTextWidth) {
                        drawText = drawText.substring(0, drawText.length() - 1);
                    }
                    drawText += "...";
                }
                g2.drawString(drawText, 56, (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        return pnl;
    }

    private JPanel buildBottom(MainFrame frame) {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(0, UIConstants.DASH_SIDEBAR_PAD, 12, UIConstants.DASH_SIDEBAR_PAD));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH - 40, 1));
        sep.setForeground(new Color(255, 255, 255, 20));
        bottom.add(sep);
        bottom.add(Box.createVerticalStrut(10));

        JPanel account = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode(UIConstants.DASH_SIDEBAR_ACCOUNT_BG));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), UIConstants.DASH_CARD_RADIUS, UIConstants.DASH_CARD_RADIUS));
                g2.dispose();
            }
        };
        account.setOpaque(false);
        Dimension accSize = new Dimension(UIConstants.SIDEBAR_WIDTH - 40, 50);
        account.setPreferredSize(accSize);
        account.setMinimumSize(accSize);
        account.setMaximumSize(accSize);
        account.setAlignmentX(Component.LEFT_ALIGNMENT);
        account.setLayout(new BorderLayout(8, 0));
        account.setBorder(new EmptyBorder(8, 12, 8, 8));

        AdminSession auth = AdminSession.getInstance();
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel lblName = new JLabel(auth.getCurrentUsername());
        lblName.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 13f));
        lblName.setForeground(Color.WHITE);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(lblName);

        JLabel lblRole = new JLabel(auth.getCurrentRole());
        lblRole.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 11f));
        lblRole.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(lblRole);

        account.add(info, BorderLayout.CENTER);

        JPanel logoutBtn = new JPanel(null) {
            boolean hovered = false;
            {
                setOpaque(false);
                setPreferredSize(new Dimension(30, 30));
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
                try { new FlatSVGIcon("assets/icon_logout.svg", 14, 14).paintIcon(this, g2, (getWidth() - 14) / 2, (getHeight() - 14) / 2); } catch (Exception ignored) {}
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
}