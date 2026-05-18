package com.tuyensinh.service;

import com.tuyensinh.dao.NganhToHopDAO;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.util.SystemLogger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import java.math.BigDecimal;
import com.tuyensinh.model.Nganh;
import com.tuyensinh.model.ToHopMon;

public class NganhToHopService {

    private final NganhToHopDAO dao;

    public NganhToHopService() {
        this.dao = new NganhToHopDAO();
    }

    /**
     * Tim kiem va phan trang to hop xet tuyen (ho tro loc theo ten nganh, ma to hop)
     */
    public CompletableFuture<List<NganhToHop>> findPageWithFilters(
            String keyword, 
            List<String> searchFields, 
            Map<String, Object> filters, 
            int page, 
            int pageSize) {
        
        return CompletableFuture.supplyAsync(() -> {
            String tenNganhFilter = (String) filters.getOrDefault("tennganh", "");
            String maToHopFilter = (String) filters.getOrDefault("matohop", "");
            String finalTenNganh = "Tất cả".equals(tenNganhFilter) ? "" : tenNganhFilter;
            String finalMaToHop = "Tất cả".equals(maToHopFilter) ? "" : maToHopFilter;
            return dao.findWithFilters(keyword, finalTenNganh, finalMaToHop, page, pageSize);
        });
    }

    /**
     * Dem tong so ban ghi to hop xet tuyen theo bo loc
     */
    public CompletableFuture<Long> countWithFiltersAsync(
            String keyword, 
            List<String> searchFields, 
            Map<String, Object> filters) {
        
        return CompletableFuture.supplyAsync(() -> {
            String tenNganhFilter = (String) filters.getOrDefault("tennganh", "");
            String maToHopFilter = (String) filters.getOrDefault("matohop", "");
            String finalTenNganh = "Tất cả".equals(tenNganhFilter) ? "" : tenNganhFilter;
            String finalMaToHop = "Tất cả".equals(maToHopFilter) ? "" : maToHopFilter;
            return dao.countWithFilters(keyword, finalTenNganh, finalMaToHop);
        });
    }

    /**
     * Xoa to hop xet tuyen theo ID
     */
    public CompletableFuture<Boolean> deleteByIdAsync(Integer id) { 
        return CompletableFuture.supplyAsync(() -> {
            if (id == null) return false;
            System.out.println("[NganhToHopService] Xoa to hop xet tuyen ID=" + id);
            dao.deleteById(id);
            SystemLogger.log(null, "System", "Xóa tổ hợp xét tuyển ID=" + id, true);
            return true;
        });
    }

    /**
     * Tim to hop xet tuyen theo ID
     */
    public CompletableFuture<NganhToHop> findByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> dao.findById(id));
    }

    /**
     * Lay toan bo danh sach to hop xet tuyen
     */
    public List<NganhToHop> getAll() {
        return dao.findAllFull();
    }

    /**
     * Them moi to hop xet tuyen cho nganh
     */
    public void save(NganhToHop entity) {
        if (entity == null || entity.getNganh() == null || entity.getToHopMon() == null) {
            throw new IllegalArgumentException("Du lieu nganh va to hop khong hop le");
        }
        System.out.println("[NganhToHopService] Them to hop xet tuyen cho nganh: " + entity.getNganh().getManganh());
        dao.save(entity);
        System.out.println("[NganhToHopService] Them to hop thanh cong");
        SystemLogger.log(null, "System", "Thêm tổ hợp xét tuyển cho ngành: " + entity.getNganh().getManganh(), true);
    }

    /**
     * Lay danh sach to hop mon theo ma nganh
     */
    public CompletableFuture<List<NganhToHop>> findByMaNganhAsync(String maNganh) {
        return CompletableFuture.supplyAsync(() -> dao.findByMaNganh(maNganh));
    }

    /**
     * Import hang loat tu file Excel
     */
    public CompletableFuture<Void> importExcel(java.io.File file) {
        return CompletableFuture.runAsync(() -> {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                 Workbook workbook = WorkbookFactory.create(fis)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                
                NganhService nganhService = new NganhService();
                ToHopMonService toHopMonService = new ToHopMonService();
                List<ToHopMon> allToHop = toHopMonService.getAll();
                
                int rowCount = 0;
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // Skip header
                    
                    String maNganh = getCellValueAsString(row.getCell(1));
                    String maToHopRaw = getCellValueAsString(row.getCell(3));
                    String tenToHop = getCellValueAsString(row.getCell(5));
                    
                    if (maNganh.isEmpty() || tenToHop.isEmpty()) continue;
                    
                    Nganh nganh = nganhService.findByMaNganh(maNganh).orElse(null);
                    if (nganh == null) {
                        System.out.println("[NganhToHopService] Khong tim thay nganh: " + maNganh);
                        continue;
                    }
                    
                    ToHopMon toHopMon = allToHop.stream().filter(t -> t.getMatohop().equals(tenToHop)).findFirst().orElse(null);
                    if (toHopMon == null) {
                        System.out.println("[NganhToHopService] Khong tim thay to hop: " + tenToHop);
                        continue;
                    }
                    
                    NganhToHop nt = new NganhToHop();
                    nt.setNganh(nganh);
                    nt.setToHopMon(toHopMon);
                    nt.setTbKeys(maNganh + "_" + tenToHop);
                    
                    // Mac dinh
                    nt.setN1(false); nt.setTo(false); nt.setLi(false); nt.setHo(false);
                    nt.setSi(false); nt.setVa(false); nt.setSu(false); nt.setDi(false);
                    nt.setTi(false); nt.setKhac(false); nt.setKtpl(false);
                    
                    // Parse MA_TO_HOP
                    // Vi du: B03(TO-3,VA-3,SI-1) hoac C01(TO-3,VA-3,LI-1)
                    int start = maToHopRaw.indexOf('(');
                    int end = maToHopRaw.indexOf(')');
                    if (start > 0 && end > start) {
                        String content = maToHopRaw.substring(start + 1, end);
                        String[] parts = content.split(",");
                        if (parts.length >= 1) {
                            String[] p = parts[0].split("-");
                            nt.setThMon1(p[0].trim());
                            nt.setHsmon1(Byte.parseByte(p[1].trim()));
                            setBooleanMon(nt, p[0].trim());
                        }
                        if (parts.length >= 2) {
                            String[] p = parts[1].split("-");
                            nt.setThMon2(p[0].trim());
                            nt.setHsmon2(Byte.parseByte(p[1].trim()));
                            setBooleanMon(nt, p[0].trim());
                        }
                        if (parts.length >= 3) {
                            String[] p = parts[2].split("-");
                            nt.setThMon3(p[0].trim());
                            nt.setHsmon3(Byte.parseByte(p[1].trim()));
                            setBooleanMon(nt, p[0].trim());
                        }
                    }
                    
                    // Do lech
                    String doLechStr = getCellValueAsString(row.getCell(7));
                    if (!doLechStr.isEmpty() && !doLechStr.equalsIgnoreCase("None")) {
                        try {
                            nt.setDolech(new BigDecimal(doLechStr));
                        } catch (Exception e) {
                            nt.setDolech(BigDecimal.ZERO);
                        }
                    } else {
                        nt.setDolech(BigDecimal.ZERO);
                    }
                    
                    try {
                        dao.save(nt);
                        rowCount++;
                    } catch (Exception e) {
                        System.out.println("[NganhToHopService] Loi luu (co the da ton tai tb_keys): " + nt.getTbKeys());
                    }
                }
                
                SystemLogger.log(null, "System", "Import Excel NganhToHop: " + rowCount + " dong", true);
            } catch (Exception e) {
                throw new RuntimeException("Loi doc file Excel: " + e.getMessage(), e);
            }
        });
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: 
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double val = cell.getNumericCellValue();
                    if (val == Math.floor(val)) {
                        return String.valueOf((long)val);
                    }
                    return String.valueOf(val);
                }
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    private void setBooleanMon(NganhToHop nt, String mon) {
        if (mon == null) return;
        switch (mon.toUpperCase()) {
            case "TO": nt.setTo(true); break;
            case "LI": nt.setLi(true); break;
            case "HO": nt.setHo(true); break;
            case "SI": nt.setSi(true); break;
            case "VA": nt.setVa(true); break;
            case "SU": nt.setSu(true); break;
            case "DI": nt.setDi(true); break;
            case "TI": nt.setTi(true); break;
            case "KTPL": nt.setKtpl(true); break;
            case "N1": nt.setN1(true); break;
            default: nt.setKhac(true); break;
        }
    }
}