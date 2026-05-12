package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.DiemCongDialog;
import com.tuyensinh.util.Constants;
import com.tuyensinh.dao.DiemCongDAO;
import com.tuyensinh.model.DiemCong;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DiemCongPanel extends BaseTablePanel<DiemCong> {

    private final DiemCongDAO dao = new DiemCongDAO();

    public DiemCongPanel() {
        super("Quản lý Điểm Cộng", "Danh sách các loại điểm ưu tiên của thí sinh");
        loadTableData();
    }

    @Override
    protected String getModuleCode() {
        return Constants.QUYEN_DIEM_CONG;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[]{
            "ID", "CCCD", "Mã Ngành", "Tổ Hợp", "Phương Thức", "Điểm Chứng Chỉ", "Điểm Ưu Tiên", "Tổng Điểm", "Thao Tác"
        };
    }

    @Override
    protected Object[] toTableRow(DiemCong entity) {
        return new Object[]{
            entity.getId(),
            entity.getThiSinh() != null ? entity.getThiSinh().getCccd() : "",
            entity.getManganh(),
            entity.getMatohop(),
            entity.getPhuongthuc(),
            entity.getDiemCC(),
            entity.getDiemUtxt(),
            entity.getDiemTong(),
            "..."
        };
    }

    @Override
    protected List<String> getSearchFields() {
        // Hỗ trợ tìm kiếm theo CCCD thí sinh hoặc mã ngành
        return List.of("thiSinh.cccd", "manganh", "dcKeys");
    }

    @Override
    protected CompletableFuture<List<DiemCong>> fetchPage(String keyword, Map<String, Object> filters, int page, int pageSize) {
        return dao.findPageWithFilters(keyword, getSearchFields(), filters, page, pageSize);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
        return dao.countWithFiltersAsync(keyword, getSearchFields(), filters);
    }

    @Override
    protected void showAddDialog() {
        DiemCongDialog dialog = new DiemCongDialog(getParentFrame(), "Điểm Cộng", null, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadTableData();
        }
    }

    @Override
    protected void showEditDialog(int tableRow) {
        int id = getIdFromRow(tableRow);
        DiemCong entity = dao.findById(id);
        if (entity != null) {
            DiemCongDialog dialog = new DiemCongDialog(getParentFrame(), "Điểm Cộng", entity, dao);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadTableData();
            }
        }
    }

    @Override
    protected void deleteRecord(int tableRow) {
        int id = getIdFromRow(tableRow);
        String cccd = getNameFromRow(tableRow); 
        if (confirmDelete("Điểm cộng của CCCD " + cccd)) {
            dao.deleteByIdAsync(id).thenAccept(success -> {
                if (success) {
                    showSuccess("Đã xóa điểm cộng thành công!");
                    loadTableData();
                } else {
                    showError("Lỗi: Không thể xóa điểm cộng!");
                }
            });
        }
    }
}
