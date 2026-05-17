package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.HoSoTuyenSinhFormDialog;
import com.tuyensinh.model.DotTuyenSinh;
import com.tuyensinh.model.HoSoTuyenSinh;
import com.tuyensinh.model.NguyenVong;
import com.tuyensinh.service.DotTuyenSinhService;
import com.tuyensinh.service.HoSoTuyenSinhService;
import com.tuyensinh.util.Constants;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HoSoTuyenSinhPanel extends BaseTablePanel<HoSoTuyenSinh> {

    private final HoSoTuyenSinhService service = new HoSoTuyenSinhService();
    private final DotTuyenSinhService dotService = new DotTuyenSinhService();

    private com.tuyensinh.admin.ui.components.RoundedButton btnYeuCauSua;
    private Timer timerYeuCau;

    public HoSoTuyenSinhPanel() {
        super("Hồ sơ Xét tuyển", "Quản lý và duyệt đơn đăng ký của thí sinh");
        setupExtras();
        loadTableData();
    }

    @Override
    protected void setupExtras() {
        // An nut "Them moi" vi ho so tu dong tao khi dang ky hoac import
        for (Component c : toolbar.getComponents()) {
            if (c instanceof JButton && ((JButton) c).getText().contains("Thêm mới")) {
                c.setVisible(false);
                break;
            }
        }

        // Bo loc theo dot tuyen sinh
        try {
            // Lấy danh sách đợt từ DB
            List<DotTuyenSinh> listDot = dotService.findPageWithFilters("", List.of(), Map.of(), 1, 100).get();
            
            java.util.List<String> dotOptions = new java.util.ArrayList<>();
            dotOptions.add("Tất cả các đợt");
            for (DotTuyenSinh dot : listDot) {
                dotOptions.add(dot.getTenDot());
            }

            toolbar.addDynamicFilterCategory(
                "Đợt Tuyển Sinh", 2,
                dotOptions,
                (col, val) -> {
                    if ("Tất cả các đợt".equals(val)) {
                        applyFilter("dotTuyenSinh", null);
                    } else {
                        // Tim dot co ten khop voi lua chon de lay ID
                        DotTuyenSinh selectedDot = listDot.stream()
                                .filter(d -> d.getTenDot().equals(val))
                                .findFirst()
                                .orElse(null);
                        
                        if (selectedDot != null) {
                            // Tao object chi chua ID de Hibernate loc dung
                            DotTuyenSinh dotFilter = new DotTuyenSinh();
                            dotFilter.setId(selectedDot.getId());
                            applyFilter("dotTuyenSinh", dotFilter);
                        }
                    }
                }
            );
            
            // Bo loc theo trang thai ho so
            toolbar.addDynamicFilterCategory(
                "Trạng Thái", 6,
                java.util.Arrays.asList("Tất cả", "CHO_XET", "HOP_LE", "KHONG_HOP_LE", "TRUNG_TUYEN", "TRUOT"),
                (col, val) -> applyFilter("trangThai", "Tất cả".equals(val) ? "" : val)
            );

            // Nut xem danh sach yeu cau cap nhat tu thi sinh
            btnYeuCauSua = new com.tuyensinh.admin.ui.components.RoundedButton("Yêu cầu sửa (0)");
            btnYeuCauSua.setBackground(Color.decode(com.tuyensinh.admin.util.UIConstants.COLOR_BG));
            btnYeuCauSua.setForeground(Color.decode(com.tuyensinh.admin.util.UIConstants.COLOR_TEXT_MUTED));
            btnYeuCauSua.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnYeuCauSua.setPreferredSize(new Dimension(130, com.tuyensinh.admin.util.UIConstants.INPUT_HEIGHT));
            
            btnYeuCauSua.addActionListener(e -> {
                new com.tuyensinh.admin.ui.dialog.DanhSachYeuCauDialog(getParentFrame()).setVisible(true);
                loadTableData();
                checkYeuCauCount();
            });

            JPanel rightPanel = toolbar.getRightPanel();
            if (rightPanel != null) {
                rightPanel.add(btnYeuCauSua, 2);
            } else {
                toolbar.add(btnYeuCauSua);
            }

            startYeuCauPolling();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkYeuCauCount() {
        com.tuyensinh.service.YeuCauCapNhatService.getInstance().demYeuCauChoDuyet().thenAccept(countLong -> {
            SwingUtilities.invokeLater(() -> {
                int count = countLong != null ? countLong.intValue() : 0; 
                if (count > 0) {
                    btnYeuCauSua.setText("Yêu cầu sửa (" + count + ")");
                    btnYeuCauSua.setBackground(Color.decode(com.tuyensinh.admin.util.UIConstants.COLOR_DANGER)); 
                    btnYeuCauSua.setForeground(Color.WHITE);
                } else {
                    btnYeuCauSua.setText("Yêu cầu sửa (0)");
                    btnYeuCauSua.setBackground(Color.decode(com.tuyensinh.admin.util.UIConstants.COLOR_BG)); 
                    btnYeuCauSua.setForeground(Color.decode(com.tuyensinh.admin.util.UIConstants.COLOR_TEXT_MUTED));
                }
            });
        }).exceptionally(ex -> {
            System.err.println("Lỗi gọi Service đếm yêu cầu: " + ex.getMessage());
            return null;
        });
    }

    private void startYeuCauPolling() {
        checkYeuCauCount();
        timerYeuCau = new Timer(30_000, e -> checkYeuCauCount());
        timerYeuCau.setRepeats(true);
        timerYeuCau.start();
    }

    @Override
    protected String getModuleCode() { return Constants.QUYEN_HO_SO; }

    @Override
    protected String[] getColumnNames() {
        return new String[]{"ID", "Mã Hồ Sơ", "Đợt", "CCCD", "Họ Tên Thí Sinh", "Tổng Điểm", "Trạng Thái", "Thao tác"};
    }

    @Override
    protected Object[] toTableRow(HoSoTuyenSinh hs) {
        String tenDot = hs.getDotTuyenSinh() != null ? hs.getDotTuyenSinh().getTenDot() : "";
        String cccd = hs.getThiSinh() != null ? hs.getThiSinh().getCccd() : "";
        String hoTen = hs.getThiSinh() != null ? (hs.getThiSinh().getHo() + " " + hs.getThiSinh().getTen()) : "";

        return new Object[]{
            hs.getId(), hs.getMaHoSo(), tenDot, cccd, hoTen.trim(),
            hs.getTongDiemXetTuyen() != null ? hs.getTongDiemXetTuyen() : 0.0,
            hs.getTrangThai(), ""
        };
    }

    @Override
    protected CompletableFuture<List<HoSoTuyenSinh>> fetchPage(String kw, Map<String, Object> flt, int pi, int ps) {
        return service.findPageWithFilters(kw, List.of("maHoSo"), flt, pi, ps);
    }

    @Override
    protected CompletableFuture<Long> fetchCount(String kw, Map<String, Object> flt) {
        return service.countWithFiltersAsync(kw, List.of("maHoSo"), flt);
    }  

    @Override
    protected void showAddDialog() {
        showError("Hồ sơ được tạo tự động khi thí sinh đăng ký Online hoặc Import từ Excel.");
    }

    @Override
    protected void showEditDialog(int row) {
        HoSoTuyenSinh hs = service.findById(getIdFromRow(row));
        if (hs != null) {
            // Debug: Log thông tin hồ sơ
            System.out.println("[HoSoTuyenSinhPanel] Xem chi tiết HoSo ID=" + hs.getId() + ", MaHoSo=" + hs.getMaHoSo());
            
            // Debug: Kiểm tra NguyenVong
            com.tuyensinh.service.NguyenVongService nvService = new com.tuyensinh.service.NguyenVongService();
            java.util.List<NguyenVong> nvList = nvService.findByHoSoId(hs.getId());
            System.out.println("[HoSoTuyenSinhPanel] Tìm thấy " + nvList.size() + " NguyenVong cho HoSo ID=" + hs.getId());
            for (NguyenVong nv : nvList) {
                System.out.println("  - NV #" + nv.getNvTt() + ": " + (nv.getNganh() != null ? nv.getNganh().getTennganh() : "N/A"));
            }
            
            HoSoTuyenSinhFormDialog dialog = new HoSoTuyenSinhFormDialog(getParentFrame(), hs);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadTableData();
        }
    }

    @Override
    protected void deleteRecord(int row) {
        if (confirmDelete("Hồ sơ " + getNameFromRow(row))) {
            service.deleteByIdAsync(getIdFromRow(row)).thenAccept(res -> loadTableData());
        }
    }
}