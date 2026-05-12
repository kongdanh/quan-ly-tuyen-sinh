package com.tuyensinh.admin.ui.base;

import com.tuyensinh.admin.ui.components.*;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.admin.util.AdminSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

/**
 * Abstract base cho tất cả panel có bảng dữ liệu + search + pagination.
 * Subclass chỉ cần implement các method abstract để cung cấp:
 * - Mã chức năng (để tự động phân quyền)
 * - Tên cột, Dữ liệu mỗi dòng
 * - Query DAO
 * - Form dialog
 */
public abstract class BaseTablePanel<T> extends JPanel {

    // ----------------------------------------------------------------
    // UI Components
    // ----------------------------------------------------------------
    protected CustomTable       table;
    protected DefaultTableModel tableModel;
    protected PaginationPanel   paginationPanel;
    protected TableToolbar      toolbar;
    protected RoundPanel        card;

    // ----------------------------------------------------------------
    // State
    // ----------------------------------------------------------------
    protected int    currentPage        = 1;
    protected int    currentPageSize    = UIConstants.PAGE_SIZE;
    protected String currentSearchKeyword = "";
    protected Map<String, Object> activeFilters = new java.util.HashMap<>();

    // ================================================================
    // CONSTRUCTOR
    // ================================================================
    public BaseTablePanel(String title, String subtitle) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBackground(Color.decode(UIConstants.DASH_CONTENT_BG));
        setBorder(new EmptyBorder(
            UIConstants.SECTION_GAP,
            UIConstants.SECTION_GAP + 10,
            UIConstants.SECTION_GAP,
            UIConstants.SECTION_GAP + 10
        ));

        add(new HeaderPanel(title, subtitle), BorderLayout.NORTH);

        card = new RoundPanel(UIConstants.DASH_CARD_RADIUS, true);
        card.setLayout(new BorderLayout());

        toolbar = new TableToolbar(getToolbarTitle());
        setupToolbarActions();
        card.add(toolbar, BorderLayout.NORTH);

        setupTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(
            Color.decode(UIConstants.COLOR_BORDER)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(getHorizontalScrollBarPolicy());
        card.add(scrollPane, BorderLayout.CENTER);

        paginationPanel = new PaginationPanel((newPage, newSize) -> {
            currentPage     = newPage;
            currentPageSize = newSize;
            loadTableData();
        });
        card.add(paginationPanel, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    // ================================================================
    // ABSTRACT
    // ================================================================

    /** Khai báo mã chức năng để BaseTable tự động phân quyền (VD: Constants.QUYEN_THI_SINH) */
    protected abstract String getModuleCode();

    /** Tên cột của bảng, cột cuối thường là "Thao tác" */
    protected abstract String[] getColumnNames();

    /** Chuyển 1 entity T thành 1 dòng Object[] để đưa vào bảng */
    protected abstract Object[] toTableRow(T entity);

    /** Query lấy 1 trang dữ liệu (async) */
    protected abstract CompletableFuture<List<T>> fetchPage(String keyword, Map<String, Object> filters, int page, int pageSize);

    /** Query đếm tổng số record (async) */
    protected abstract CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters);

    /** Mở form thêm mới */
    protected abstract void showAddDialog();

    /** Mở form sửa theo dòng được chọn trong bảng */
    protected abstract void showEditDialog(int tableRow);

    /** Xóa record theo dòng được chọn */
    protected abstract void deleteRecord(int tableRow);

    // ================================================================
    // OPTIONAL OVERRIDE
    // ================================================================

    /** Tiêu đề nhỏ trên toolbar (mặc định lấy từ title) */
    protected String getToolbarTitle() { return "Danh sách 2026"; }

    /** Danh sách field để search trong DB */
    protected List<String> getSearchFields() { return List.of(); }

    /** Index cột chứa nút Thao tác (-1 nếu không có) */
    protected int getActionColumnIndex() { return getColumnNames().length - 1; }

    /** Chế độ auto resize của JTable */
    protected int getTableAutoResizeMode() { return JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS; }

    /** Chính sách hiển thị thanh cuộn ngang */
    protected int getHorizontalScrollBarPolicy() { return ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER; }

    /** Width của cột Thao tác */
    protected int getActionColumnWidth() { return 90; }

    /** Setup thêm (filter, import button...) — gọi sau khi toolbar được tạo */
    protected void setupExtras() {}

    // ================================================================
    // CORE LOGIC
    // ================================================================

    /** Setup bảng, gắn Action cell vào cột cuối kèm kiểm tra quyền */
    protected void setupTable() {
        tableModel = new DefaultTableModel(getColumnNames(), 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == getActionColumnIndex();
            }
        };

        table = new CustomTable();
        table.setModel(tableModel);
        table.setAutoResizeMode(getTableAutoResizeMode());

        configureColumns();

        int actionCol = getActionColumnIndex();
        if (actionCol >= 0) {
            // Kiểm tra quyền Sửa/Xóa từ AdminSession
            boolean canEdit = AdminSession.getInstance().canEdit(getModuleCode());
            boolean canDelete = AdminSession.getInstance().canDelete(getModuleCode());

            TableActionCell.TableActionEvent event = new TableActionCell.TableActionEvent() {
                @Override public void onEdit(int row) { 
                    if (canEdit) showEditDialog(row); 
                    else showError("Bạn không có quyền Chỉnh sửa dữ liệu này!");
                }
                @Override public void onDelete(int row) { 
                    if (canDelete) deleteRecord(row); 
                    else showError("Bạn không có quyền Xóa dữ liệu này!");
                }
            };
            table.getColumnModel().getColumn(actionCol)
                 .setCellRenderer(new TableActionCell.Renderer());
            table.getColumnModel().getColumn(actionCol)
                 .setCellEditor(new TableActionCell.Editor(event));
            table.getColumnModel().getColumn(actionCol)
                 .setPreferredWidth(getActionColumnWidth());
        }
    }

    /** Override để tùy chỉnh độ rộng cột */
    protected void configureColumns() {}

    /** Setup toolbar: search + nút thêm. Có kiểm tra quyền Thêm */
    protected void setupToolbarActions() {
        toolbar.addRealtimeSearchListener(() -> {
            currentSearchKeyword = toolbar.getSearchField().getText().trim();
            currentPage          = 1;
            loadTableData();
        });

        if (toolbar.getBtnAdd() != null) {
            // Kiểm tra quyền Thêm từ AdminSession
            boolean canAdd = AdminSession.getInstance().canAdd(getModuleCode());
            toolbar.getBtnAdd().setVisible(canAdd);

            if (canAdd) {
                toolbar.getBtnAdd().addActionListener(e -> showAddDialog());
            }
        }
    }

    /**
     * Load data từ DB vào bảng — dùng CompletableFuture để không block EDT.
     */
    protected void loadTableData() {
        System.out.println("loadTableData() called with keyword='" + currentSearchKeyword + "', page=" + currentPage + ", filters=" + activeFilters);
        paginationPanel.setEnabled(false);

        CompletableFuture<List<T>> dataFuture = fetchPage(
            currentSearchKeyword, activeFilters, currentPage, currentPageSize);
        CompletableFuture<Long> countFuture = fetchCount(
            currentSearchKeyword, activeFilters);

        CompletableFuture.allOf(dataFuture, countFuture).thenAccept(v -> {
            try {
                List<T> list  = dataFuture.get();
                long    total = countFuture.get();

                System.out.println("loadTableData got " + list.size() + " items, total=" + total);
                
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (T entity : list) {
                        Object[] row = toTableRow(entity);
                        tableModel.addRow(row);
                        System.out.println("  + Added row: " + java.util.Arrays.toString(row));
                    }
                    paginationPanel.updatePagination((int) total, currentPage);
                    paginationPanel.setEnabled(true);
                    toolbar.getSearchField().requestFocusInWindow();
                });
            } catch (Exception e) {
                System.err.println("loadTableData exception: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> paginationPanel.setEnabled(true));
            }
        });
    }

    // ================================================================
    // HELPER UTILITIES
    // ================================================================

    /** Lấy giá trị ô theo cột từ dòng đang chọn */
    protected Object getCellValue(int row, int col) {
        return tableModel.getValueAt(row, col);
    }

    /** Lấy ID (cột 0) từ dòng đang chọn */
    protected int getIdFromRow(int row) {
        return (int) getCellValue(row, 0);
    }

    /** Lấy tên hiển thị (cột 2 thường là tên) từ dòng đang chọn */
    protected String getNameFromRow(int row) {
        Object val = getCellValue(row, 2);
        return val != null ? val.toString() : "";
    }

    /** Hộp thoại xác nhận xóa chuẩn */
    protected boolean confirmDelete(String entityName) {
        return JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn xóa: " + entityName + "?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }

    /** Thông báo thành công */
    protected void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Thông báo lỗi */
    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
            "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /** Lấy Frame cha để làm parent cho Dialog */
    protected Frame getParentFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    protected void applyFilter(String fieldName, Object value) {
        if (value == null || value.toString().isEmpty() || value.toString().equals("Tất cả")) {
            activeFilters.remove(fieldName); // Bỏ lọc
        } else {
            activeFilters.put(fieldName, value); // Thêm lọc
        }
        currentPage = 1;
        loadTableData();
    }
}