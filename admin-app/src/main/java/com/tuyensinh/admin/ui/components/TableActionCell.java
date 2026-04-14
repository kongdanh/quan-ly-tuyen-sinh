package com.tuyensinh.admin.ui.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.tuyensinh.admin.util.UIConstants;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;

public class TableActionCell {

    public interface TableActionEvent {
        void onEdit(int row);
        void onDelete(int row);
    }

    public static class ActionPanel extends JPanel {
        private final JButton btnEdit;
        private final JButton btnDelete;

        public ActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 8, 2));
            setOpaque(true);

            btnEdit = createIconButton("icon_edit.svg", "Chỉnh sửa");
            btnDelete = createIconButton("icon_delete.svg", "Xóa hồ sơ");

            add(btnEdit);
            add(btnDelete);
        }

        private JButton createIconButton(String iconFile, String tooltip) {
            JButton btn = new JButton();
            try {
                btn.setIcon(new FlatSVGIcon("assets/" + iconFile, 18, 18));
            } catch (Exception e) {
                btn.setText(tooltip);
            }
            
            btn.setToolTipText(tooltip);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setMargin(new Insets(2, 4, 2, 4));
            
            return btn;
        }

        public void initEvent(TableActionEvent event, int row) {
            for (ActionListener al : btnEdit.getActionListeners()) btnEdit.removeActionListener(al);
            for (ActionListener al : btnDelete.getActionListeners()) btnDelete.removeActionListener(al);

            btnEdit.addActionListener(e -> event.onEdit(row));
            btnDelete.addActionListener(e -> event.onDelete(row));
        }

        public void applyBackground(JTable table, int row, boolean isSelected) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : Color.decode(UIConstants.DASH_ZEBRA_ALT));
            }
        }
    }

    public static class Renderer extends AbstractCellEditor implements TableCellRenderer {
        private final ActionPanel actionPanel = new ActionPanel();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            actionPanel.applyBackground(table, row, isSelected);
            return actionPanel;
        }
        @Override public Object getCellEditorValue() { return null; }
    }

    public static class Editor extends AbstractCellEditor implements TableCellEditor {
        private final ActionPanel actionPanel = new ActionPanel();
        private final TableActionEvent event;

        public Editor(TableActionEvent event) {
            this.event = event;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            actionPanel.applyBackground(table, row, true);
            actionPanel.initEvent(event, row);
            return actionPanel;
        }
        @Override public Object getCellEditorValue() { return null; }
    }
}