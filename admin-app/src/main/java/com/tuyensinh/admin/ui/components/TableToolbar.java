package com.tuyensinh.admin.ui.components;

import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.BiConsumer;

public class TableToolbar extends JPanel {

    private final JLabel lblTitle;
    private final SearchTextField txtSearch;
    private final RoundedButton btnFilter;
    private final RoundedButton btnAdd;
    private final JPopupMenu filterMenu;
    private final RoundedButton btnImport;
    private final RoundedButton btnExport;

    public TableToolbar(String title) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, UIConstants.TABLE_TOOLBAR_GAP, 0));

        lblTitle = new JLabel(title);
        lblTitle.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 14f));
        lblTitle.setForeground(Color.decode(UIConstants.DASH_TEXT_DARK));
        add(lblTitle, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        txtSearch = new SearchTextField();
        
        btnFilter = new RoundedButton("Bộ lọc"); 
        btnFilter.setPreferredSize(new Dimension(100, UIConstants.INPUT_HEIGHT)); 

        btnExport = new RoundedButton("Xuất Excel");
        btnExport.setBackground(Color.decode(UIConstants.COLOR_SUCCESS));
        btnExport.setPreferredSize(new Dimension(100, UIConstants.INPUT_HEIGHT));

        btnImport = new RoundedButton("Nhập Excel");
        btnImport.setBackground(Color.decode(UIConstants.COLOR_WARNING));
        btnImport.setPreferredSize(new Dimension(100, UIConstants.INPUT_HEIGHT));
        
        btnAdd = new RoundedButton("+ Thêm mới");
        btnAdd.setPreferredSize(new Dimension(110, UIConstants.INPUT_HEIGHT));

        rightPanel.add(txtSearch);
        rightPanel.add(btnFilter);
        // rightPanel.add(btnExport);
        rightPanel.add(btnImport);
        rightPanel.add(btnAdd);
        
        add(rightPanel, BorderLayout.EAST);
        
        filterMenu = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(Color.decode(UIConstants.COLOR_BORDER));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };
        filterMenu.setOpaque(false);
        filterMenu.setBorder(new EmptyBorder(4, 4, 4, 4));

        btnFilter.addActionListener(e -> filterMenu.show(btnFilter, 0, btnFilter.getHeight() + 4));
    }

    public void addRealtimeSearchListener(Runnable onSearchAction) {
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { trigger(); }
            @Override public void removeUpdate(DocumentEvent e) { trigger(); }
            @Override public void changedUpdate(DocumentEvent e) { trigger(); }
            private void trigger() {
                SwingUtilities.invokeLater(onSearchAction);
            }
        });
    }

    public void addClearFilterOption(Runnable onClearAction) {
        JMenuItem itemClear = new JMenuItem("Xóa bộ lọc (Tất cả)");
        itemClear.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 12f));
        itemClear.setForeground(Color.decode(UIConstants.COLOR_DANGER));
        itemClear.addActionListener(e -> {
            btnFilter.setText("Bộ lọc");
            lblTitle.setText("Danh sách 2026");
            onClearAction.run();
        });
        filterMenu.add(itemClear);
        filterMenu.addSeparator();
    }

    public void addDynamicFilterCategory(String categoryName, int columnIndex, List<String> uniqueValues, BiConsumer<Integer, String> onFilterAction) {
        JMenu categoryMenu = new JMenu(categoryName);
        categoryMenu.setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, 12f));
        
        MenuPanel panel = new MenuPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        for (String val : uniqueValues) {
            FilterMenuItem item = new FilterMenuItem(val, () -> {
                btnFilter.setText("Lọc: " + val);
                lblTitle.setText(categoryName + ": " + val);
                onFilterAction.accept(columnIndex, val);
                filterMenu.setVisible(false);
            });
            item.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 12f));
            panel.add(item);
        }
        
        MenuScrollPane scrollPane = new MenuScrollPane(panel);
        scrollPane.setBorder(null);
        int maxItems = Math.min(uniqueValues.size(), 10);
        scrollPane.setPreferredSize(new Dimension(200, maxItems * 25 + 10));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        categoryMenu.add(scrollPane);
        filterMenu.add(categoryMenu);
    }

    public SearchTextField getSearchField() { return txtSearch; }
    public RoundedButton getBtnAdd() { return btnAdd; }
    public RoundedButton getBtnImport() { return btnImport; }
    public JPanel getRightPanel() {
        for (Component c : getComponents()) {
            if (c instanceof JPanel) {
                LayoutManager lm = ((JPanel) c).getLayout();
                if (lm instanceof FlowLayout && ((FlowLayout) lm).getAlignment() == FlowLayout.RIGHT) {
                    return (JPanel) c;
                }
            }
        }
        return null;
    }
    
    // --- Custom Menu Elements for Scrollable JMenu ---
    
    private static class MenuScrollBar extends JScrollBar implements MenuElement {
        public MenuScrollBar(int orientation) { super(orientation); }
        @Override public void processMouseEvent(java.awt.event.MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
            manager.setSelectedPath(path);
        }
        @Override public void processKeyEvent(java.awt.event.KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {}
        @Override public void menuSelectionChanged(boolean isIncluded) {}
        @Override public MenuElement[] getSubElements() { return new MenuElement[0]; }
        @Override public Component getComponent() { return this; }
    }

    private static class MenuPanel extends JPanel implements MenuElement {
        @Override public void processMouseEvent(java.awt.event.MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
            manager.setSelectedPath(path);
        }
        @Override public void processKeyEvent(java.awt.event.KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {}
        @Override public void menuSelectionChanged(boolean isIncluded) {}
        @Override public MenuElement[] getSubElements() {
            java.util.List<MenuElement> list = new java.util.ArrayList<>();
            for (Component c : getComponents()) {
                if (c instanceof MenuElement) {
                    list.add((MenuElement) c);
                }
            }
            return list.toArray(new MenuElement[0]);
        }
        @Override public Component getComponent() { return this; }
    }

    private static class MenuViewport extends JViewport implements MenuElement {
        @Override public void processMouseEvent(java.awt.event.MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
            manager.setSelectedPath(path);
        }
        @Override public void processKeyEvent(java.awt.event.KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {}
        @Override public void menuSelectionChanged(boolean isIncluded) {}
        @Override public MenuElement[] getSubElements() {
            Component view = getView();
            if (view instanceof MenuElement) {
                return new MenuElement[] { (MenuElement) view };
            }
            return new MenuElement[0];
        }
        @Override public Component getComponent() { return this; }
    }

    private static class MenuScrollPane extends JScrollPane implements MenuElement {
        public MenuScrollPane(Component view) {
            super();
            MenuViewport viewport = new MenuViewport();
            viewport.setView(view);
            setViewport(viewport);
            setVerticalScrollBar(new MenuScrollBar(JScrollBar.VERTICAL));
            setHorizontalScrollBar(new MenuScrollBar(JScrollBar.HORIZONTAL));
        }
        @Override public void processMouseEvent(java.awt.event.MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
            manager.setSelectedPath(path);
        }
        @Override public void processKeyEvent(java.awt.event.KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {}
        @Override public void menuSelectionChanged(boolean isIncluded) {}
        @Override public MenuElement[] getSubElements() {
            java.util.List<MenuElement> list = new java.util.ArrayList<>();
            if (getVerticalScrollBar() instanceof MenuElement) {
                list.add((MenuElement) getVerticalScrollBar());
            }
            if (getHorizontalScrollBar() instanceof MenuElement) {
                list.add((MenuElement) getHorizontalScrollBar());
            }
            if (getViewport() instanceof MenuElement) {
                list.add((MenuElement) getViewport());
            }
            return list.toArray(new MenuElement[0]);
        }
        @Override public Component getComponent() { return this; }
    }
    
    private static class FilterMenuItem extends JLabel implements MenuElement {
        private final Runnable onClick;

        public FilterMenuItem(String text, Runnable onClick) {
            super(text);
            this.onClick = onClick;
            setOpaque(true);
            setBackground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(4, 10, 4, 10));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    setBackground(Color.decode(UIConstants.DASH_SELECT_BG));
                }
                @Override public void mouseExited(MouseEvent e) {
                    setBackground(Color.WHITE);
                }
                @Override public void mousePressed(MouseEvent e) {
                    // Xử lý ngay khi nhấn xuống, không chờ nhả chuột
                    MenuSelectionManager.defaultManager().clearSelectedPath();
                    onClick.run();
                }
            });
        }

        @Override public void processMouseEvent(MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
            // Chỉ dùng để duy trì path khi hover, KHÔNG xử lý click ở đây
            // để tránh bị gọi 2 lần
            if (event.getID() == MouseEvent.MOUSE_ENTERED) {
                manager.setSelectedPath(path);
            }
            // Relay event xuống component để MouseAdapter bắt được
            dispatchEvent(event);
        }
        @Override public void processKeyEvent(KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {}
        @Override public void menuSelectionChanged(boolean isIncluded) {
            setBackground(isIncluded ? Color.decode(UIConstants.DASH_SELECT_BG) : Color.WHITE);
        }
        @Override public MenuElement[] getSubElements() { return new MenuElement[0]; }
        @Override public Component getComponent() { return this; }
    }
    
    // public RoundedButton getBtnExport() { return btnExport; }
}