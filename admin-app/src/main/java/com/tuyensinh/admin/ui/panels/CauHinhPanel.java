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
        super("Cấu hình Tuyển sinh", "Quản lý các đợt và thời gian đóng/mở cổng");
        loadTableData();
    }

    @Override
    protected String getModuleCode() {
        return Constants.QUYEN_CAU_HINH;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Tên Đợt", "Bắt Đầu", "Kết Thúc", "Trạng Thái", "Thao tác"};
    }

    @Override
    protected Object[] toTableRow(DotTuyenSinh d) {
        return new Object[]{ d.getId(), d.getTenDot(), d.getNgayBatDau(), d.getNgayKetThuc(), d.getTrangThai(), "" };
    }

    @Override
    protected CompletableFuture<List<DotTuyenSinh>> fetchPage(String kw, Map<String, Object> flt, int pi, int ps) {
        return service.findPageWithFilters(kw, List.of("tenDot"), flt, pi, ps);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String kw, Map<String, Object> flt) {
        return service.countWithFiltersAsync(kw, List.of("tenDot"), flt);
    }

    @Override
    protected void showAddDialog() {
        DotTuyenSinhFormDialog gd = new DotTuyenSinhFormDialog(getParentFrame(), new DotTuyenSinh(), true);
        gd.setVisible(true);
        if (gd.isSaved()) loadTableData();
    }

    @Override
    protected void showEditDialog(int row) {
        DotTuyenSinh d = service.findById(getIdFromRow(row));
        if (d != null) {
            DotTuyenSinhFormDialog gd = new DotTuyenSinhFormDialog(getParentFrame(), d, false);
            gd.setVisible(true);
            if (gd.isSaved()) loadTableData();
        }
    }

    @Override
    protected void deleteRecord(int row) {
        if (confirmDelete(getNameFromRow(row))) {
            service.deleteByIdAsync(getIdFromRow(row)).thenAccept(res -> loadTableData());
        }
    }
}