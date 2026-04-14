package com.tuyensinh.admin.ui.components;

import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FormDialog extends JDialog {

    private JPanel contentBody;
    private RoundedButton btnSave;
    private JButton btnCancel;
    private Point dragOffset;

    public FormDialog(Frame owner, String title, int width, int height) {
        super(owner, title, true);
        setUndecorated(true);
        setSize(width, height);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(Color.decode(UIConstants.COLOR_BORDER), 1));

        // ==========================================
        // 1. HEADER
        // ==========================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.decode(UIConstants.DASH_PRIMARY));
        header.setPreferredSize(new Dimension(0, 44));
        header.setBorder(new EmptyBorder(0, 16, 0, 8));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 14f));
        header.add(lblTitle, BorderLayout.WEST);

        JButton btnClose = new JButton("X");
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 14f));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        header.add(btnClose, BorderLayout.EAST);

        header.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragOffset = e.getPoint(); }
        });
        header.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                setLocation(currCoords.x - dragOffset.x, currCoords.y - dragOffset.y);
            }
        });

        // ==========================================
        // 2. BODY 
        // ==========================================
        contentBody = new JPanel();
        contentBody.setLayout(new BoxLayout(contentBody, BoxLayout.Y_AXIS));
        contentBody.setOpaque(false);
        contentBody.setBorder(new EmptyBorder(20, 24, 20, 24));

        // ==========================================
        // 3. FOOTER 
        // ==========================================
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(Color.decode(UIConstants.COLOR_BG_INPUT));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode(UIConstants.COLOR_BORDER)));

        btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 12f));
        btnCancel.setForeground(Color.decode(UIConstants.COLOR_TEXT_MUTED));
        btnCancel.setContentAreaFilled(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        btnSave = new RoundedButton("Lưu dữ liệu");
        btnSave.setPreferredSize(new Dimension(120, 36));

        footer.add(btnCancel);
        footer.add(btnSave);

        root.add(header, BorderLayout.NORTH);
        root.add(contentBody, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    public JPanel getContentBody() { return contentBody; }
    public RoundedButton getBtnSave() { return btnSave; }
}