package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.BangQuyDoiDialog;
import com.tuyensinh.util.Constants;
import com.tuyensinh.dao.BangQuyDoiDAO;
import com.tuyensinh.model.BangQuyDoi;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BangQuyDoiPanel extends BaseTablePanel<BangQuyDoi> {

    private final BangQuyDoiDAO dao = new BangQuyDoiDAO();

    public BangQuyDoiPanel() {
        super("Bảng Quy Đổi", "Quản lý các mức quy đổi điểm (chứng chỉ, DGNL, VSAT)");
        loadTableData();
    }

    @Override
    protected String getModuleCode() {
        return Constants.QUYEN_BANG_QUY_DOI;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{
            "ID", "Phương Thức", "Tổ Hợp", "Mã Môn", "Điểm A", "Điểm B", "Điểm C", "Điểm D", "Thao Tác"
        };
    }

    @Override
    protected Object[] toTableRow(BangQuyDoi entity) {
        return new Object[]{
            entity.getIdqd(),
            entity.getDPhuongthuc(),
            entity.getDTohop(),
            entity.getDMon(),
            entity.getDDiema(),
            entity.getDDiemb(),
            entity.getDDiemc(),
            entity.getDDiemd(),
            "..."
        };
    }

    @Override
    protected List<String> getSearchFields() {
        return List.of("dMon", "dPhuongthuc", "dMaquydoi");
    }

    @Override
    protected CompletableFuture<List<BangQuyDoi>> fetchPage(String keyword, Map<String, Object> filters, int page, int pageSize) {
        return dao.findPageWithFilters(keyword, getSearchFields(), filters, page, pageSize);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
        return dao.countWithFiltersAsync(keyword, getSearchFields(), filters);
    }

    @Override
    protected void setupExtras() {
        toolbar.addClearFilterOption(() -> applyFilter("dMon", null));
        toolbar.addDynamicFilterCategory("Mã Môn", -1, List.of("TO_VS", "LI_VS", "VA_VS", "N1_VS", "DGNL", "IELTS"), (col, val) -> {
            applyFilter("dMon", val);
        });
    }

    @Override
    protected void showAddDialog() {
        BangQuyDoiDialog dialog = new BangQuyDoiDialog(getParentFrame(), "Bảng Quy Đổi", null, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadTableData();
        }
    }

    @Override
    protected void showEditDialog(int tableRow) {
        Long id = Long.parseLong(getCellValue(tableRow, 0).toString());
        BangQuyDoi entity = dao.findById(id);
        if (entity != null) {
            BangQuyDoiDialog dialog = new BangQuyDoiDialog(getParentFrame(), "Bảng Quy Đổi", entity, dao);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadTableData();
            }
        }
    }

    @Override
    protected void deleteRecord(int tableRow) {
        Long id = Long.parseLong(getCellValue(tableRow, 0).toString());
        String maMon = getNameFromRow(tableRow); 
        if (confirmDelete("Quy đổi môn " + maMon)) {
            dao.deleteByIdAsync(id).thenAccept(success -> {
                if (success) {
                    showSuccess("Đã xóa bảng quy đổi thành công!");
                    loadTableData();
                } else {
                    showError("Lỗi: Không thể xóa bảng quy đổi!");
                }
            });
        }
    }
}
