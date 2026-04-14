package com.tuyensinh.admin.ui.components;

import com.tuyensinh.admin.util.UIConstants;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class PaginationPanel extends JPanel {

    public interface PaginationListener {
        void onPageChanged(int newPage, int newPageSize);
    }

    private int currentPage = 1;
    private int totalPages = 1;
    private int pageSize = UIConstants.PAGE_SIZE;
    private int totalRecords = 0;
    
    private final PaginationListener listener;
    private final JPanel pageButtonsPanel;
    private final JComboBox<Integer> comboPageSize;
    private final JLabel lblInfo;
    private final JTextField txtGoTo;

    public PaginationPanel(PaginationListener listener) {
        this.listener = listener;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 0, 0, 0));

        // ==========================================
        // leftpanel
        // ==========================================
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        lblInfo = new JLabel("Đang hiển thị 0 bản ghi");
        lblInfo.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 12f));
        lblInfo.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));
        
        Integer[] sizes = {10, 15, 20, 50, 100};
        comboPageSize = new JComboBox<>(sizes);
        comboPageSize.setSelectedItem(pageSize);
        comboPageSize.setFocusable(false);
        comboPageSize.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 12f));
        comboPageSize.addActionListener(e -> {
            pageSize = (Integer) comboPageSize.getSelectedItem();
            currentPage = 1; 
            triggerEvent();
        });

        leftPanel.add(new JLabel("Hiển thị:"));
        leftPanel.add(comboPageSize);
        leftPanel.add(lblInfo);

        // ==========================================
        // rightpanel
        // ==========================================
        JPanel rightWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightWrapper.setOpaque(false);

        pageButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pageButtonsPanel.setOpaque(false);

        // Khung Go To
        JPanel goToPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        goToPanel.setOpaque(false);
        JLabel lblGoTo = new JLabel("Go to:");
        lblGoTo.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 12f));
        lblGoTo.setForeground(Color.decode(UIConstants.DASH_TEXT_MUTED));
        
        txtGoTo = new JTextField(3);
        txtGoTo.setHorizontalAlignment(SwingConstants.CENTER);
        txtGoTo.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 12f));
        txtGoTo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode(UIConstants.COLOR_BORDER)),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        
        // event go to page
        txtGoTo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        int targetPage = Integer.parseInt(txtGoTo.getText().trim());
                        if (targetPage >= 1 && targetPage <= totalPages && targetPage != currentPage) {
                            currentPage = targetPage;
                            triggerEvent();
                        } else {
                            txtGoTo.setText(String.valueOf(currentPage));
                        }
                    } catch (NumberFormatException ex) {
                        txtGoTo.setText(String.valueOf(currentPage));
                    }
                }
            }
        });

        goToPanel.add(lblGoTo);
        goToPanel.add(txtGoTo);

        rightWrapper.add(pageButtonsPanel);
        rightWrapper.add(goToPanel);

        add(leftPanel, BorderLayout.WEST);
        add(rightWrapper, BorderLayout.EAST);
    }

    public void updatePagination(int totalRecords, int currentPage) {
        this.totalRecords = totalRecords;
        this.totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        if (this.totalPages == 0) this.totalPages = 1;
        
        this.currentPage = currentPage;
        if (this.currentPage > this.totalPages) this.currentPage = this.totalPages;
        
        lblInfo.setText(String.format("- Tổng: %d bản ghi", totalRecords));
        txtGoTo.setText(String.valueOf(this.currentPage));
        
        renderPageButtons();
    }

    private void renderPageButtons() {
        pageButtonsPanel.removeAll();

        // Nút First (<<) và Prev (<)
        pageButtonsPanel.add(createPageBtn("\u00AB", 1, currentPage > 1, false)); 
        pageButtonsPanel.add(createPageBtn("\u2039", currentPage - 1, currentPage > 1, false));

        // Logic cửa sổ 5 trang (Sliding Window)
        int maxPagesToShow = 5;
        int startPage = Math.max(1, currentPage - maxPagesToShow / 2);
        int endPage = Math.min(totalPages, startPage + maxPagesToShow - 1);

        // Điều chỉnh lại nếu cửa sổ bị lệch
        if (endPage - startPage + 1 < maxPagesToShow) {
            startPage = Math.max(1, endPage - maxPagesToShow + 1);
        }

        for (int i = startPage; i <= endPage; i++) {
            pageButtonsPanel.add(createPageBtn(String.valueOf(i), i, true, i == currentPage));
        }

        // Nút Next (>) và Last (>>)
        pageButtonsPanel.add(createPageBtn("\u203A", currentPage + 1, currentPage < totalPages, false));
        pageButtonsPanel.add(createPageBtn("\u00BB", totalPages, currentPage < totalPages, false));

        pageButtonsPanel.revalidate();
        pageButtonsPanel.repaint();
    }

    private JPanel createPageBtn(String text, int targetPage, boolean isEnabled, boolean isActive) {
        JPanel btn = new JPanel(new BorderLayout()) {
            boolean isHover = false;
            {
                setOpaque(false);
                if (isEnabled && !isActive) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { isHover = true; repaint(); }
                        @Override public void mouseExited(MouseEvent e) { isHover = false; repaint(); }
                        @Override public void mouseClicked(MouseEvent e) { 
                            currentPage = targetPage;
                            triggerEvent(); 
                        }
                    });
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isActive) {
                    g2.setColor(Color.decode(UIConstants.DASH_PRIMARY));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 6, 6));
                } 
                else if (isEnabled && isHover) {
                    g2.setColor(Color.decode(UIConstants.DASH_GRID_LINE));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 6, 6));
                }
                g2.dispose();
            }
        };
        
        btn.setPreferredSize(new Dimension(30, 30));
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        
        lbl.setFont(UIManager.getFont("defaultFont").deriveFont(isActive ? Font.BOLD : Font.PLAIN, 13f));
        
        if (isActive) lbl.setForeground(Color.WHITE);
        else if (!isEnabled) lbl.setForeground(new Color(200, 200, 200));
        else lbl.setForeground(Color.decode(UIConstants.DASH_TEXT_DARK));
        
        btn.add(lbl, BorderLayout.CENTER);
        return btn;
    }

    private void triggerEvent() {
        if (listener != null) {
            listener.onPageChanged(currentPage, pageSize);
        }
    }
}