package com.tuyensinh.admin.ui.components;

import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
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
        
        for (String val : uniqueValues) {
            JMenuItem item = new JMenuItem(val);
            item.setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, 12f));
            item.addActionListener(e -> {
                btnFilter.setText("Lọc: " + val);
                lblTitle.setText(categoryName + ": " + val);
                onFilterAction.accept(columnIndex, val);
            });
            categoryMenu.add(item);
        }
        filterMenu.add(categoryMenu);
    }

    public SearchTextField getSearchField() { return txtSearch; }
    public RoundedButton getBtnAdd() { return btnAdd; }
    public RoundedButton getBtnImport() { return btnImport; }
    // public RoundedButton getBtnExport() { return btnExport; }
}