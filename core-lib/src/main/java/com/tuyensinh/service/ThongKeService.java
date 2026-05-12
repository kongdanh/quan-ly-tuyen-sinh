package com.tuyensinh.service;

import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ThongKeService {

    /**
     * Thong ke tong quan: tong ho so, ho so hop le, trung tuyen, tong chi tieu
     */
    public CompletableFuture<Map<String, Object>> getTongQuanStats(Integer idDot) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> stats = new LinkedHashMap<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String dotFilter = (idDot != null && idDot > 0) ? " WHERE h.dotTuyenSinh.id = :idDot" : "";
                
                var q1 = session.createQuery("SELECT COUNT(h) FROM HoSoTuyenSinh h" + dotFilter, Long.class);
                if (idDot != null && idDot > 0) q1.setParameter("idDot", idDot);
                stats.put("tongHoSo", q1.uniqueResult());
                
                String dotFilterAnd = (idDot != null && idDot > 0) ? " AND h.dotTuyenSinh.id = :idDot" : "";
                var q2 = session.createQuery("SELECT COUNT(h) FROM HoSoTuyenSinh h WHERE h.trangThai = 'HOP_LE'" + dotFilterAnd, Long.class);
                if (idDot != null && idDot > 0) q2.setParameter("idDot", idDot);
                stats.put("tongHoSoHopLe", q2.uniqueResult());
                
                var q3 = session.createQuery("SELECT COUNT(k) FROM KetQuaXetTuyen k WHERE k.trangThai = 'TRUNG_TUYEN'" + dotFilterAnd.replace("h.", "k.hoSo."), Long.class);
                if (idDot != null && idDot > 0) q3.setParameter("idDot", idDot);
                stats.put("tongTrungTuyen", q3.uniqueResult());
                
                var q4 = session.createQuery("SELECT COALESCE(SUM(n.nChitieu), 0) FROM Nganh n", Long.class);
                stats.put("tongChiTieu", q4.uniqueResult());
                
            } catch (Exception e) {
                System.err.println("[ThongKeService] Loi thong ke tong quan: " + e.getMessage());
                e.printStackTrace();
            }
            return stats;
        });
    }

    /**
     * Phan bo trang thai xet tuyen cua thi sinh (Pie chart)
     */
    public CompletableFuture<Map<String, Long>> getTrangThaiXetTuyenThiSinh(Integer idDot) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> map = new LinkedHashMap<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "SELECT " +
                             "CASE " +
                             "  WHEN MAX(CASE WHEN nv.nvKetqua = 'DAU' THEN 1 ELSE 0 END) = 1 THEN 'Da trung tuyen' " +
                             "  WHEN MAX(CASE WHEN nv.nvKetqua = 'CHO' THEN 1 ELSE 0 END) = 1 THEN 'Cho xet tuyen' " +
                             "  WHEN MAX(CASE WHEN nv.nvKetqua = 'ROT' THEN 1 ELSE 0 END) = 1 THEN 'Khong trung tuyen' " +
                             "  ELSE 'Chua xet' " +
                             "END, COUNT(DISTINCT nv.thiSinh.id) " +
                             "FROM NguyenVong nv " +
                             "WHERE nv.nvKetqua IS NOT NULL ";
                if (idDot != null && idDot > 0) {
                    hql += " AND nv.hoSoTuyenSinh.dotTuyenSinh.id = :idDot ";
                }
                hql += " GROUP BY nv.thiSinh.id";
                
                var query = session.createQuery(hql, Object[].class);
                if (idDot != null && idDot > 0) query.setParameter("idDot", idDot);
                
                resetMap(map);
                for (Object[] row : query.getResultList()) {
                    String status = row[0].toString();
                    map.merge(status, 1L, Long::sum);
                }
            } catch (Exception e) {
                System.err.println("[ThongKeService] Loi thong ke trang thai xet tuyen: " + e.getMessage());
                e.printStackTrace();
            }
            return map;
        });
    }
    
    /**
     * Phan bo thi sinh theo phuong thuc xet tuyen (Pie chart)
     */
    public CompletableFuture<Map<String, Long>> getPhanBoTheoPhuongThuc(Integer idDot) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> map = new LinkedHashMap<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "SELECT h.dotTuyenSinh.maPhuongThuc, COUNT(DISTINCT h.thiSinh.id) FROM HoSoTuyenSinh h ";
                if (idDot != null && idDot > 0) {
                    hql += " WHERE h.dotTuyenSinh.id = :idDot ";
                }
                hql += " GROUP BY h.dotTuyenSinh.maPhuongThuc";
                var query = session.createQuery(hql, Object[].class);
                if (idDot != null && idDot > 0) query.setParameter("idDot", idDot);
                
                for (Object[] row : query.getResultList()) {
                    String method = row[0] != null ? row[0].toString() : "Khac";
                    String displayName = mapPhuongThuc(method);
                    map.put(displayName, (Long) row[1]);
                }
            }
            return map;
        });
    }

    /**
     * Thong ke so luong trung tuyen theo phuong thuc tung nganh (Table)
     */
    public CompletableFuture<List<Object[]>> getSoLuongTrungTuyenTheoPhuongThuc(Integer idDot) {
        return CompletableFuture.supplyAsync(() -> {
            List<Object[]> results = new ArrayList<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "SELECT k.nganh.tennganh, k.phuongThuc, COUNT(k.id) " +
                             "FROM KetQuaXetTuyen k " +
                             "WHERE k.trangThai = 'TRUNG_TUYEN' ";
                if (idDot != null && idDot > 0) {
                    hql += " AND k.hoSo.dotTuyenSinh.id = :idDot ";
                }
                hql += " GROUP BY k.nganh.tennganh, k.phuongThuc " +
                       " ORDER BY k.nganh.tennganh ASC, k.phuongThuc ASC";
                
                var query = session.createQuery(hql, Object[].class);
                if (idDot != null && idDot > 0) query.setParameter("idDot", idDot);
                
                results = query.getResultList();
            } catch (Exception e) {
                System.err.println("[ThongKeService] Loi thong ke trung tuyen theo phuong thuc: " + e.getMessage());
                e.printStackTrace();
            }
            return results;
        });
    }

    /**
     * Thong ke thi sinh theo gioi tinh (Pie chart)
     */
    public CompletableFuture<Map<String, Long>> getThongKeGioiTinh(Integer idDot) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> map = new LinkedHashMap<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "SELECT h.thiSinh.gioiTinh, COUNT(DISTINCT h.thiSinh.id) FROM HoSoTuyenSinh h ";
                if (idDot != null && idDot > 0) {
                    hql += " WHERE h.dotTuyenSinh.id = :idDot ";
                }
                hql += " GROUP BY h.thiSinh.gioiTinh";
                
                var query = session.createQuery(hql, Object[].class);
                if (idDot != null && idDot > 0) query.setParameter("idDot", idDot);
                
                long nam = 0, nu = 0, khac = 0;
                for (Object[] row : query.getResultList()) {
                    String gt = row[0] != null ? row[0].toString() : "";
                    long count = (Long) row[1];
                    if (gt.equals("1") || gt.equalsIgnoreCase("nam")) nam = count;
                    else if (gt.equals("0") || gt.equalsIgnoreCase("nu")) nu = count;
                    else khac = count;
                }
                if (nam > 0) map.put("Nam", nam);
                if (nu > 0) map.put("Nu", nu);
                if (khac > 0) map.put("Khong xac dinh", khac);
            }
            return map;
        });
    }

    /**
     * Thong ke thi sinh theo khu vuc (Pie chart)
     */
    public CompletableFuture<Map<String, Long>> getThongKeKhuVuc(Integer idDot) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> map = new LinkedHashMap<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "SELECT h.thiSinh.khuVuc, COUNT(DISTINCT h.thiSinh.id) FROM HoSoTuyenSinh h ";
                if (idDot != null && idDot > 0) {
                    hql += " WHERE h.dotTuyenSinh.id = :idDot ";
                }
                hql += " GROUP BY h.thiSinh.khuVuc";
                
                var query = session.createQuery(hql, Object[].class);
                if (idDot != null && idDot > 0) query.setParameter("idDot", idDot);
                
                for (Object[] row : query.getResultList()) {
                    String kv = row[0] != null ? row[0].toString() : "KV3";
                    String label = mapKhuVuc(kv);
                    map.merge(label, (Long) row[1], Long::sum);
                }
            }
            return map;
        });
    }

    /**
     * Thong ke thi sinh theo doi tuong uu tien (Pie chart)
     */
    public CompletableFuture<Map<String, Long>> getThongKeDoiTuongUuTien(Integer idDot) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> map = new LinkedHashMap<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "SELECT h.thiSinh.doiTuong, COUNT(DISTINCT h.thiSinh.id) FROM HoSoTuyenSinh h ";
                if (idDot != null && idDot > 0) {
                    hql += " WHERE h.dotTuyenSinh.id = :idDot ";
                }
                hql += " GROUP BY h.thiSinh.doiTuong";
                
                var query = session.createQuery(hql, Object[].class);
                if (idDot != null && idDot > 0) query.setParameter("idDot", idDot);
                
                for (Object[] row : query.getResultList()) {
                    String dt = row[0] != null ? row[0].toString() : "Khong";
                    map.put(dt, (Long) row[1]);
                }
            }
            return map;
        });
    }

    /**
     * Top N tinh/thanh co nhieu thi sinh nhat (Bar chart)
     */
    public CompletableFuture<Map<String, Long>> getTopTinhThanh(Integer idDot, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> map = new LinkedHashMap<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "SELECT h.thiSinh.tinhThanh, COUNT(DISTINCT h.thiSinh.id) FROM HoSoTuyenSinh h ";
                if (idDot != null && idDot > 0) {
                    hql += " WHERE h.dotTuyenSinh.id = :idDot ";
                }
                hql += " AND h.thiSinh.tinhThanh IS NOT NULL AND h.thiSinh.tinhThanh != '' ";
                hql += " GROUP BY h.thiSinh.tinhThanh ORDER BY COUNT(DISTINCT h.thiSinh.id) DESC";
                
                var query = session.createQuery(hql, Object[].class);
                if (idDot != null && idDot > 0) query.setParameter("idDot", idDot);
                query.setMaxResults(limit);
                
                for (Object[] row : query.getResultList()) {
                    map.put(row[0].toString(), (Long) row[1]);
                }
            }
            return map;
        });
    }

    /**
     * Top N nganh co nhieu nguyen vong nhat (Bar chart)
     */
    public CompletableFuture<Map<String, Long>> getTopNganhTheoNguyenVong(Integer idDot, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> map = new LinkedHashMap<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "SELECT n.tennganh, COUNT(nv.id) FROM NguyenVong nv " +
                             "JOIN nv.nganh n " +
                             "WHERE nv.nvKetqua != 'HUY' ";
                if (idDot != null && idDot > 0) {
                    hql += " AND nv.hoSoTuyenSinh.dotTuyenSinh.id = :idDot ";
                }
                hql += " GROUP BY n.tennganh ORDER BY COUNT(nv.id) DESC";
                
                var query = session.createQuery(hql, Object[].class);
                if (idDot != null && idDot > 0) query.setParameter("idDot", idDot);
                query.setMaxResults(limit);
                
                for (Object[] row : query.getResultList()) {
                    map.put(row[0].toString(), (Long) row[1]);
                }
            }
            return map;
        });
    }

    /**
     * Chi tiet ty le lap day chi tieu cua tung nganh (Table)
     */
    public CompletableFuture<List<Object[]>> getChiTietLapDayNganh(Integer idDot) {
        return CompletableFuture.supplyAsync(() -> {
            List<Object[]> results = new ArrayList<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "SELECT n.manganh, n.tennganh, n.nChitieu, " +
                             "COALESCE((SELECT COUNT(DISTINCT nv.thiSinh.id) FROM NguyenVong nv WHERE nv.nganh.id = n.id " +
                             (idDot != null && idDot > 0 ? " AND nv.hoSoTuyenSinh.dotTuyenSinh.id = :idDot" : "") + "), 0) as soDK, " +
                             "COALESCE((SELECT COUNT(k) FROM KetQuaXetTuyen k WHERE k.nganh.id = n.id AND k.trangThai = 'TRUNG_TUYEN' " +
                             (idDot != null && idDot > 0 ? " AND k.hoSo.dotTuyenSinh.id = :idDot" : "") + "), 0) as trungTuyen " +
                             "FROM Nganh n ORDER BY n.tennganh";
                
                var query = session.createQuery(hql, Object[].class);
                if (idDot != null && idDot > 0) query.setParameter("idDot", idDot);
                
                for (Object[] row : query.getResultList()) {
                    long chiTieu = row[2] != null ? ((Number) row[2]).longValue() : 0;
                    long soDK = row[3] != null ? ((Number) row[3]).longValue() : 0;
                    long trungTuyen = row[4] != null ? ((Number) row[4]).longValue() : 0;
                    double phanTram = chiTieu > 0 ? (trungTuyen * 100.0 / chiTieu) : 0;
                    String trangThai = getTrangThaiLapDay1(phanTram);
                    Object[] extended = new Object[]{
                        row[0], row[1], chiTieu, soDK, trungTuyen,
                        String.format("%.1f", phanTram) + "%",
                        trangThai
                    };
                    results.add(extended);
                }
            }
            return results;
        });
    }

    /**
     * Top N to hop mon duoc chon nhieu nhat (Bar chart)
     */
    public CompletableFuture<Map<String, Long>> getTopToHopMon(Integer idDot, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> map = new LinkedHashMap<>();
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                String hql = "SELECT nv.ttThm, COUNT(nv.id) FROM NguyenVong nv " +
                             "WHERE nv.ttThm IS NOT NULL AND nv.ttThm != '' AND nv.nvKetqua != 'HUY' ";
                if (idDot != null && idDot > 0) {
                    hql += " AND nv.hoSoTuyenSinh.dotTuyenSinh.id = :idDot ";
                }
                hql += " GROUP BY nv.ttThm ORDER BY COUNT(nv.id) DESC";
                
                var query = session.createQuery(hql, Object[].class);
                if (idDot != null && idDot > 0) query.setParameter("idDot", idDot);
                query.setMaxResults(limit);
                
                for (Object[] row : query.getResultList()) {
                    String th = row[0].toString();
                    map.put(th, (Long) row[1]);
                }
            }
            return map;
        });
    }

    private void resetMap(Map<String, Long> map) {
        map.put("Da trung tuyen", 0L);
        map.put("Cho xet tuyen", 0L);
        map.put("Khong trung tuyen", 0L);
        map.put("Chua xet", 0L);
    }
    
    private String mapPhuongThuc(String method) {
        switch (method) {
            case "THPT": return "THPT Quoc gia";
            case "HOCBA": return "Hoc ba THPT";
            case "DGNL": return "DGNL DHQG";
            case "VSAT": return "V-SAT";
            default: return method;
        }
    }

    private String mapKhuVuc(String kv) {
        switch (kv) {
            case "KV1": return "Khu vuc 1";
            case "KV2": return "Khu vuc 2";
            case "KV2NT": return "KV2 - Nong thon";
            default: return "Khu vuc 3";
        }
    }
    
    private String getTrangThaiLapDay1(double percent) {
        if (percent >= 100) return "Da du chi tieu";
        if (percent >= 80) return "Gan du chi tieu";
        if (percent >= 50) return "Dang tuyen";
        return "Con nhieu chi tieu";
    }
}