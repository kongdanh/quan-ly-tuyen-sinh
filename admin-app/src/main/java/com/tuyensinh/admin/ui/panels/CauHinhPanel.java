package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.DotTuyenSinhFormDialog;
import com.tuyensinh.model.DotTuyenSinh;
import com.tuyensinh.service.DotTuyenSinhService;
import com.tuyensinh.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CauHinhPanel extends BaseTablePanel<DotTuyenSinh> {
    private final DotTuyenSinhService service = new DotTuyenSinhService();

    public CauHinhPanel() {
        super("Cấu hình Tuyển sinh", "Thiết lập thời gian đóng/mở cổng đăng ký xét tuyển");
        loadTableData();
    }

    @Override
    protected String getModuleCode() {
        return Constants.QUYEN_CAU_HINH;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Tên Đợt", "Bắt Đầu", "Kết Thúc", "Ngày Công Bố", "Trạng Thái", "Thao tác"};
    }

    @Override
    protected Object[] toTableRow(DotTuyenSinh d) {
        return new Object[]{
            d.getId(),
            d.getTenDot(),
            d.getNgayBatDau(),
            d.getNgayKetThuc(),
            d.getNgayCongBo(),
            d.getTrangThai(),
            "" // Cột thao tác cho TableActionCell
        };
    }

    @Override
    protected CompletableFuture<List<DotTuyenSinh>> fetchPage(String keyword, Map<String, Object> filters, int page, int size) {
        return service.findPageWithFilters(keyword, List.of("tenDot"), filters, page, size);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
        return service.countWithFiltersAsync(keyword, List.of("tenDot"), filters);
    }

    @Override
    protected void showAddDialog() {
        DotTuyenSinhFormDialog dialog = new DotTuyenSinhFormDialog(getParentFrame(), new DotTuyenSinh(), true);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadTableData();
    }

    @Override
    protected void showEditDialog(int row) {
        Integer id = (Integer) getIdFromRow(row);
        DotTuyenSinh entity = service.findById(id);
        if (entity != null) {
            DotTuyenSinhFormDialog dialog = new DotTuyenSinhFormDialog(getParentFrame(), entity, false);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadTableData();
        }
    }

    @Override
    protected void deleteRecord(int tableRow) {
        Integer id = (Integer) getIdFromRow(tableRow);
        String name = (String) getCellValue(tableRow, 1); // Lấy tên đợt ở cột 1

        if (confirmDelete(name)) {
            service.deleteByIdAsync(id).thenAccept(success -> {
                if (success) {
                    showSuccess("Đã xóa đợt tuyển sinh thành công.");
                    loadTableData();
                } else {
                    showError("Không thể xóa đợt tuyển sinh này (có dữ liệu ràng buộc).");
                }
            });
        }
    }
}