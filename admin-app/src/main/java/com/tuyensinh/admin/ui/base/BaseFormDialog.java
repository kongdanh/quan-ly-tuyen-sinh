package com.tuyensinh.admin.ui.base;

import com.tuyensinh.admin.ui.components.RoundedButton;
import com.tuyensinh.admin.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public abstract class BaseFormDialog<T> extends JDialog {

    protected final JPanel formBody;
    private final JButton btnSave;
    private final JButton btnCancel;
    private final JLabel lblStatus;

    protected final T entity;
    protected final boolean isAddNew;
    private boolean saved = false;

    public BaseFormDialog(Frame parent, String title, T entity, boolean isAddNew, int width, int height) {
        super(parent, (isAddNew ? "Thêm mới " : "Chỉnh sửa ") + title, true); 
        this.entity = entity;
        this.isAddNew = isAddNew;

        setSize(width, height);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getRootPane().registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setLayout(new BorderLayout());

        formBody = new JPanel();
        formBody.setLayout(new BoxLayout(formBody, BoxLayout.Y_AXIS));
        formBody.setBackground(Color.WHITE);
        formBody.setBorder(new EmptyBorder(16, 24, 0, 24));

        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.decode("#ef4444"));
        lblStatus.setFont(lblStatus.getFont().deriveFont(12f));
        lblStatus.setBorder(new EmptyBorder(2, 24, 6, 24));

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(Color.WHITE);
        centerWrapper.add(formBody, BorderLayout.CENTER);
        centerWrapper.add(lblStatus, BorderLayout.SOUTH); 
        
        add(centerWrapper, BorderLayout.CENTER);

        btnSave = new RoundedButton(isAddNew ? "Thêm mới" : "Lưu thay đổi");
        btnCancel = new RoundedButton("Hủy");
        add(buildFooter(), BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());
    }

    // ================================================================
    // ABSTRACT METHODS
    // ================================================================
    protected abstract void buildFormFields();
    protected abstract void populateForm(T entity);
    protected abstract void collectData(T entity);
    protected abstract String validateData(); 
    protected abstract void persist(T entity);

    protected void setupExtras() {}

    // ================================================================
    // GIAO DIỆN COMPONENT
    // ================================================================
    
    protected void addField(String labelText, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
        label.setForeground(Color.decode("#374151"));
        label.setPreferredSize(new Dimension(95, 30));

        component.setPreferredSize(new Dimension(250, 30));

        row.add(label, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        row.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        formBody.add(row);
    }

    protected void addSectionHeader(String sectionTitle) {
        JLabel lbl = new JLabel(sectionTitle);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
        lbl.setForeground(Color.decode("#6b7280")); 
        lbl.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.add(lbl, BorderLayout.CENTER);
        row.setBorder(new EmptyBorder(6, 0, 12, 0));

        formBody.add(row);
    }

    protected void showValidationError(String message) { lblStatus.setText("⚠ " + message); }
    protected void clearError() { lblStatus.setText(" "); }
    protected boolean isBlank(JTextField field) { return field.getText() == null || field.getText().trim().isEmpty(); }
    protected String getText(JTextField field) { return field.getText() != null ? field.getText().trim() : ""; }
    public boolean isSaved() { return saved; }

    // ================================================================
    // LOGIC LƯU
    // ================================================================
    private void handleSave() {
        clearError();

        String error = validateData();
        if (error != null) {
            showValidationError(error);
            return;
        }

        collectData(entity);
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                persist(entity);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    saved = true;
                    dispose();
                } catch (Exception e) {
                    btnSave.setEnabled(true);
                    btnSave.setText(isAddNew ? "Thêm mới" : "Lưu thay đổi");
                    showValidationError("Lỗi khi lưu: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setBackground(Color.decode("#f9fafb"));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#e5e7eb")));

        btnCancel.setPreferredSize(new Dimension(100, 36));
        btnSave.setPreferredSize(new Dimension(140, 36));
        btnSave.setBackground(Color.decode(UIConstants.COLOR_BLUE));
        btnSave.setForeground(Color.WHITE);

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }
}