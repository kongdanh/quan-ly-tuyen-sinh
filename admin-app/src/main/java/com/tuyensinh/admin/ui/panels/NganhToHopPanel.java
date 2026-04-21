package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.NganhToHopFormDialog;
import com.tuyensinh.model.Nganh;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.model.ToHopMon;
import com.tuyensinh.service.NganhService;
import com.tuyensinh.service.NganhToHopService;
import com.tuyensinh.service.NganhService;
import com.tuyensinh.service.ToHopMonService;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NganhToHopPanel extends BaseTablePanel<NganhToHop> {

    private final NganhToHopService nganhToHopService = new NganhToHopService();

    public NganhToHopPanel() {
        super("Ngành - Tổ hợp môn", "Liên kết và cấu hình trọng số môn thi cho từng ngành");
        
        setupExtras();
        loadTableData();

    }

    // ----------------------------------------------------------------
    // CONFIG COLUMNS
    // ----------------------------------------------------------------

    @Override
    protected String[] getColumnNames() {
        return new String[]{
            "ID", 
            "Mã Ngành", 
            "Tên Ngành", 
            "Mã Tổ Hợp", 
            "Cấu hình Môn (Hệ số)", 
            "Độ lệch", 
            "Thao tác"
        };
    }

    @Override
    protected void configureColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // Mã Ngành
        table.getColumnModel().getColumn(2).setPreferredWidth(200);  // Tên Ngành
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Mã Tổ Hợp
        table.getColumnModel().getColumn(4).setPreferredWidth(250);  // Cấu hình môn
        table.getColumnModel().getColumn(5).setPreferredWidth(80);   // Độ lệch
    }

    @Override
    protected List<String> getSearchFields() {
        // Tìm kiếm theo mã ngành hoặc mã tổ hợp
        return Arrays.asList("manganh", "matohop");
    }

    // ----------------------------------------------------------------
    // DATA MAPPING
    // ----------------------------------------------------------------

    @Override
    protected Object[] toTableRow(NganhToHop nt) {
        // Tạo chuỗi hiển thị cấu hình môn: ví dụ "Toán(2), Lý(1), Hóa(1)"
        StringBuilder config = new StringBuilder();
        if (nt.getThMon1() != null) config.append(nt.getThMon1()).append("(").append(nt.getHsmon1()).append(")");
        if (nt.getThMon2() != null) config.append(", ").append(nt.getThMon2()).append("(").append(nt.getHsmon2()).append(")");
        if (nt.getThMon3() != null) config.append(", ").append(nt.getThMon3()).append("(").append(nt.getHsmon3()).append(")");

        return new Object[]{
            nt.getId(),
            nt.getNganh().getManganh(),
            nt.getNganh().getTennganh(),
            nt.getToHopMon().getMatohop(),
            config.toString(),
            nt.getDolech(),
            "" // Action column
        };
    }

    // ----------------------------------------------------------------
    // QUERY LOGIC
    // ----------------------------------------------------------------


    @Override
    protected CompletableFuture<List<NganhToHop>> fetchPage(
            String keyword, 
            Map<String, Object> filters, 
            int page, 
            int pageSize) {

        return nganhToHopService.findPageWithFilters(
                keyword,
                getSearchFields(),
                filters,
                page-1,
                pageSize
        );
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
        return nganhToHopService.countWithFiltersAsync(
                keyword,
                getSearchFields(),
                filters
        );
    }

    // ----------------------------------------------------------------
    // CRUD ACTIONS
    // ----------------------------------------------------------------

    @Override
    protected void showAddDialog() {
        // Giả định bạn sẽ tạo NganhToHopFormDialog tương tự ThiSinhFormDialog
        NganhToHopFormDialog dialog = new NganhToHopFormDialog(getParentFrame(), new NganhToHop(), true);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadTableData();
            showSuccess("Thêm liên kết Ngành - Tổ hợp thành công!");
        }
    }

    @Override
    protected void showEditDialog(int tableRow) {
        int id = getIdFromRow(tableRow);
        // Tìm entity trong list hiện tại hoặc gọi service
        NganhToHop nt = nganhToHopService.getAll().stream()
                .filter(item -> item.getId() == id)
                .findFirst().orElse(null);

        if (nt != null) {
            NganhToHopFormDialog dialog = new NganhToHopFormDialog(getParentFrame(), nt, false);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadTableData();
                showSuccess("Cập nhật cấu hình thành công!");
            }
        }
    }

    @Override
    protected void deleteRecord(int tableRow) {
        String code = getCellValue(tableRow, 1).toString() + " - " + getCellValue(tableRow, 3).toString();
        if (!confirmDelete("liên kết " + code)) return;

        int id = getIdFromRow(tableRow);
        CompletableFuture.runAsync(() -> {
            nganhToHopService.deleteByIdAsync(id);
        }).thenRun(() -> SwingUtilities.invokeLater(() -> {
            loadTableData();
            showSuccess("Đã xóa liên kết ngành!");
        }));
    }

   @Override
    protected void setupExtras() {
        // 1. Thêm bộ lọc Search theo Tên Ngành (Sử dụng TextField hoặc Filter tùy biến)
       
        
        List<Nganh> dsNganh = new NganhService().findAllSync();
        List<ToHopMon> dsToHop = new ToHopMonService().getAll();

        List<String> tenNganhList = new java.util.ArrayList<>();
        tenNganhList.add("Tất cả");
        

        for (Nganh n : dsNganh) {
            tenNganhList.add(n.getTennganh());
        }

        List<String> toHopList = new java.util.ArrayList<>();
        toHopList.add("Tất cả");

        for (ToHopMon t : dsToHop) {
            toHopList.add(t.getMatohop());
        }
        

        toolbar.addDynamicFilterCategory(
        "Tên Ngành",
        2,
        tenNganhList,
        (col, val) -> {
            if ("Tất cả".equals(val)) {
                applyFilter("tennganh", null);
            } else {
                applyFilter("tennganh", val);
            }
        }
        );

        toolbar.addDynamicFilterCategory(
            "Tổ hợp môn",
            3,
            toHopList,
            (col, val) -> {
                if ("Tất cả".equals(val)) {
                    applyFilter("matohop", null);
                } else {
                    applyFilter("matohop", val);
                }
            }
        );
    }

        
}