package com.tuyensinh.admin.ui.components;

import com.tuyensinh.admin.util.UIConstants;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CustomTable extends JTable {

    public CustomTable() {
        // Cấu hình cơ bản
        setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(Color.decode(UIConstants.DASH_GRID_LINE));
        setSelectionBackground(Color.decode(UIConstants.DASH_SELECT_BG));
        setSelectionForeground(Color.decode(UIConstants.DASH_TEXT_DARK));
        
        // Font chữ cho dữ liệu
        setFont(UIManager.getFont("defaultFont").deriveFont(Font.PLAIN, (float) UIConstants.TABLE_FONT_SIZE));
        setForeground(Color.decode(UIConstants.COLOR_TEXT));

        // Cấu hình Tiêu đề bảng (Header)
        JTableHeader header = getTableHeader();
        
        header.setResizingAllowed(true);
        header.setReorderingAllowed(false);
        
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(Color.decode(UIConstants.TABLE_HEADER_BG));
                c.setForeground(Color.decode(UIConstants.DASH_TEXT_DARK));
                ((JLabel) c).setFont(UIManager.getFont("defaultFont").deriveFont(Font.BOLD, (float) UIConstants.TABLE_HEADER_FONT_SIZE));
                ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, UIConstants.TABLE_CELL_PADDING_X, 0, 0));
                return c;
            }
        });
        header.setPreferredSize(new Dimension(0, UIConstants.TABLE_HEADER_HEIGHT));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode(UIConstants.COLOR_BORDER)));
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == getColumnCount() - 1;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (!isRowSelected(row)) {
            c.setBackground(row % 2 == 0 ? Color.WHITE : Color.decode(UIConstants.DASH_ZEBRA_ALT));
        }
        if (c instanceof JLabel) {
            JLabel lbl = (JLabel) c;
            int align = lbl.getHorizontalAlignment();
            if (align == SwingConstants.RIGHT) {
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, UIConstants.TABLE_CELL_PADDING_X));
            } else if (align == SwingConstants.CENTER) {
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            } else {
                lbl.setBorder(BorderFactory.createEmptyBorder(0, UIConstants.TABLE_CELL_PADDING_X, 0, 0));
            }
        }
        return c;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getPreferredSize().width < getParent().getWidth();
    }
}