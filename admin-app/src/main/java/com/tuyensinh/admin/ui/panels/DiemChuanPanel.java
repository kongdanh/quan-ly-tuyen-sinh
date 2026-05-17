package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.components.CustomComboBox;
import com.tuyensinh.model.DiemChuanDot;
import com.tuyensinh.model.DotTuyenSinh;
import com.tuyensinh.service.DiemChuanDotService;
import com.tuyensinh.service.DotTuyenSinhService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DiemChuanPanel extends BaseTablePanel<DiemChuanDot> {
    private final DiemChuanDotService dcService = new DiemChuanDotService();
    private final DotTuyenSinhService dotService = new DotTuyenSinhService();
    
    private CustomComboBox<DotTuyenSinh> cbDotFilter;
    private List<DiemChuanDot> listDataPage = new ArrayList<>();
    private DiemChuanDotService service = new DiemChuanDotService();
    
    public DiemChuanPanel() {
        super("Quản lý Điểm chuẩn", "Thiết lập điểm trúng tuyển cho từng Đợt và Tổ hợp");
        setupBatchFilter();
    }

    private java.awt.event.ActionListener dotFilterListener;
    
    private void setupBatchFilter() {
        cbDotFilter = new CustomComboBox<>(new DotTuyenSinh[0]);
        cbDotFilter.setPreferredSize(new java.awt.Dimension(250, 32));
        
        toolbar.add(new JLabel(" Đợt: "), 0);
        toolbar.add(cbDotFilter, 1);
        toolbar.add(Box.createHorizontalStrut(10), 2);

        dotFilterListener = e -> {
            if (cbDotFilter.getSelectedItem() != null) {
                System.out.println("Combo box selection changed -> Loading data...");
                loadTableData();
            }
        };

        // Nạp dữ liệu Đợt từ database
        dotService.findPageWithFilters("", List.of(), Map.of(), 1, 100).thenAccept(dots -> {
            SwingUtilities.invokeLater(() -> {
                System.out.println("✓ Loaded " + dots.size() + " Đợt from database");
                
                for (DotTuyenSinh d : dots) {
                    cbDotFilter.addItem(d);
                }
                cbDotFilter.addActionListener(dotFilterListener);
                
                if (cbDotFilter.getItemCount() > 0) {
                    System.out.println("Combo box has " + cbDotFilter.getItemCount() + " items -> Loading table data...");
                    loadTableData();
                } else {
                    System.err.println("No Đợt data found in database!");
                }
            });
        }).exceptionally(ex -> {
            System.err.println("Error loading Đợt data: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });
    }

    @Override
    public String getModuleCode() { return "QUAN_LY_DIEM"; }

    @Override
    public String[] getColumnNames() {
        return new String[]{"ID", "Mã Ngành", "Tên Ngành", "Tổ Hợp", "Điểm Chuẩn (Sửa & Enter)"};
    }

    @Override
    protected void setupTable() {
        super.setupTable();
        // Cho phép edit cột số 4
        tableModel = new DefaultTableModel(getColumnNames(), 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        table.setModel(tableModel);

        // LOGIC TỰ ĐỘNG LƯU: Khi dữ liệu bảng thay đổi
        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 4) { // Nếu sửa cột Điểm chuẩn
                    handleAutoSave(row);
                }
            }
        });
    }

    private void handleAutoSave(int row) {
        try {
            Object valueObj = tableModel.getValueAt(row, 4);
            BigDecimal value = new BigDecimal(valueObj.toString());
            
            // Lấy object tương ứng từ danh sách dữ liệu
            DiemChuanDot dc = listDataPage.get(row);
            dc.setDiemChuan(value);
            
            // Lưu xuống DB ngay lập tức
            dcService.updateBatch(List.of(dc));
            
            // Hiển thị trạng thái nhỏ ở chân trang nếu cần, hoặc log
            System.out.println("Đã tự động lưu ID " + dc.getId() + ": " + value);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng số! Vui lòng nhập đúng (VD: 24.5)");
            loadTableData(); // Reset lại bảng
        }
    }

    @Override
    public Object[] toTableRow(DiemChuanDot dc) {
        try {
            return new Object[]{
                dc.getId(),
                dc.getNganhToHop().getNganh().getManganh(),
                dc.getNganhToHop().getNganh().getTennganh(),
                dc.getNganhToHop().getToHopMon().getMatohop(),
                dc.getDiemChuan()
            };
        } catch (Exception e) {
            System.err.println("Lỗi trong toTableRow() khi lấy dữ liệu DiemChuanDot ID=" + dc.getId());
            e.printStackTrace();
            return new Object[]{dc.getId(), "[Lỗi]", "[Lỗi]", "[Lỗi]", dc.getDiemChuan()};
        }
    }

    @Override
    public CompletableFuture<List<DiemChuanDot>> fetchPage(String kw, Map<String, Object> flt, int page, int size) {
        DotTuyenSinh selected = (DotTuyenSinh) cbDotFilter.getSelectedItem();
        if (selected == null) {
            System.err.println("fetchPage: selected Đợt is null!");
            return CompletableFuture.completedFuture(new java.util.ArrayList<>());
        }

        // TẠO MAP MỚI thay vì clear map cũ để tránh side effects
        Map<String, Object> newFilters = new java.util.HashMap<>();
        newFilters.put("dotTuyenSinh.id", selected.getId());
        
        System.out.println("fetchPage: Đợt ID=" + selected.getId() + ", keyword='" + kw + "', page=" + page + ", size=" + size);
        
        return service.findPage(kw, newFilters, page, size).thenApply(list -> {
            System.out.println("fetchPage returned " + list.size() + " DiemChuanDot records");
            
            // Nếu không có dữ liệu -> tự động khởi tạo DiemChuanDot cho đợt này
            if (list.isEmpty() && (kw == null || kw.trim().isEmpty())) {
                System.out.println("Đợt ID=" + selected.getId() + " chưa có DiemChuanDot -> Tự động khởi tạo...");
                boolean created = dcService.initializeForDotIfEmpty(selected);
                if (created) {
                    // Re-query sau khi khởi tạo
                    try {
                        list = service.findPage(kw, newFilters, page, size).get();
                        System.out.println("Re-query returned " + list.size() + " DiemChuanDot records sau khi auto-init");
                    } catch (Exception e) {
                        System.err.println("Re-query exception: " + e.getMessage());
                    }
                }
            }
            
            this.listDataPage = list;
            return list;
        }).exceptionally(ex -> {
            System.err.println("fetchPage exception: " + ex.getMessage());
            ex.printStackTrace();
            return new java.util.ArrayList<>();
        });
    }

    @Override
    public CompletableFuture<Long> fetchCount(String kw, Map<String, Object> flt) {
        DotTuyenSinh selected = (DotTuyenSinh) cbDotFilter.getSelectedItem();
        if (selected == null) {
            System.err.println("fetchCount: selected Đợt is null!");
            return CompletableFuture.completedFuture(0L);
        }

        Map<String, Object> newFilters = new java.util.HashMap<>();
        newFilters.put("dotTuyenSinh.id", selected.getId());
        
        System.out.println("fetchCount: Đợt ID=" + selected.getId() + ", keyword='" + kw + "'");
        
        return service.countAsync(kw, newFilters).thenApply(count -> {
            System.out.println("fetchCount returned " + count + " total records");
            return count;
        }).exceptionally(ex -> {
            System.err.println("fetchCount exception: " + ex.getMessage());
            ex.printStackTrace();
            return 0L;
        });
    }

    /**
     * Override loadTableData để xử lý trường hợp auto-init:
     * Gọi fetchPage trước, nếu có auto-init thì re-count.
     */
    @Override
    protected void loadTableData() {
        System.out.println("loadTableData() called with keyword='" + currentSearchKeyword + "', page=" + currentPage + ", filters=" + activeFilters);
        paginationPanel.setEnabled(false);

        fetchPage(currentSearchKeyword, activeFilters, currentPage, currentPageSize)
            .thenCompose(list -> {
                // Sau khi fetchPage hoàn thành (bao gồm cả auto-init), lấy count
                return fetchCount(currentSearchKeyword, activeFilters).thenApply(total -> {
                    // Nếu count = 0 nhưng list có dữ liệu (vừa auto-init), dùng list size
                    long finalTotal = total;
                    if (total == 0 && !list.isEmpty()) {
                        finalTotal = list.size();
                    }
                    return new Object[]{list, finalTotal};
                });
            })
            .thenAccept(result -> {
                @SuppressWarnings("unchecked")
                List<DiemChuanDot> list = (List<DiemChuanDot>) ((Object[]) result)[0];
                long total = (long) ((Object[]) result)[1];

                System.out.println("loadTableData got " + list.size() + " items, total=" + total);

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (DiemChuanDot entity : list) {
                        Object[] row = toTableRow(entity);
                        tableModel.addRow(row);
                        System.out.println("  + Added row: " + java.util.Arrays.toString(row));
                    }
                    paginationPanel.updatePagination((int) total, currentPage);
                    paginationPanel.setEnabled(true);
                    toolbar.getSearchField().requestFocusInWindow();
                });
            })
            .exceptionally(ex -> {
                System.err.println("loadTableData exception: " + ex.getMessage());
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> paginationPanel.setEnabled(true));
                return null;
            });
    }

    // Vô hiệu hóa các nút thêm/xóa mặc định vì điểm chuẩn sinh tự động theo đợt
    @Override protected void showAddDialog() {}
    @Override protected void showEditDialog(int row) {}
    @Override protected void deleteRecord(int row) {}
}