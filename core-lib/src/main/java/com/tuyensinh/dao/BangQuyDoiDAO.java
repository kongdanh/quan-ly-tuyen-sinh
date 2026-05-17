package com.tuyensinh.dao;

import com.tuyensinh.model.BangQuyDoi;
import com.tuyensinh.util.HibernateUtil;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.*;

/**
 * DAO cho bảng xt_bangquydoi — hỗ trợ nội suy điểm quy đổi và tra cứu cache.
 */
public class BangQuyDoiDAO extends GenericDAO<BangQuyDoi> {

    public BangQuyDoiDAO() {
        super(BangQuyDoi.class);
    }

    // ── Truy vấn cơ bản ─────────────────────────────────────────────────────────

    public List<BangQuyDoi> getBangQuyDoiByType(String type) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BangQuyDoi WHERE dPhuongthuc = :type ORDER BY dDiema ASC";
            return session.createQuery(hql, BangQuyDoi.class)
                          .setParameter("type", type)
                          .list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<BangQuyDoi> getBangQuyDoiByTypeAndMon(String type, String mon) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BangQuyDoi WHERE dPhuongthuc = :type AND dMon = :mon ORDER BY dDiema ASC";
            return session.createQuery(hql, BangQuyDoi.class)
                          .setParameter("type", type)
                          .setParameter("mon", mon)
                          .list();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ── Logic nội suy và Cache (Từ Branch) ──────────────────────────────────────

    public BigDecimal[] findInterpolationRange(String maMon, BigDecimal diemTho) {
        if (maMon == null || diemTho == null) return null;
        List<BangQuyDoi> rows = findAllByMaMon(maMon);
        return interpolateFromRows(rows, diemTho);
    }

    public List<BangQuyDoi> findAllByMaMon(String maMon) {
        if (maMon == null) return Collections.emptyList();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM BangQuyDoi b WHERE b.dMon = :maMon ORDER BY b.dDiema ASC", BangQuyDoi.class)
                    .setParameter("maMon", maMon).list();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static BigDecimal[] interpolateFromRows(List<BangQuyDoi> rows, BigDecimal diemTho) {
        if (rows == null || rows.isEmpty() || diemTho == null) return null;
        for (BangQuyDoi row : rows) {
            BigDecimal a = row.getDDiema(), b = row.getDDiemb();
            BigDecimal c = row.getDDiemc(), d = row.getDDiemd();
            if (a != null && b != null && diemTho.compareTo(a) >= 0 && diemTho.compareTo(b) <= 0) {
                return new BigDecimal[]{a, b, c, d};
            }
        }
        // Clamp logic
        BangQuyDoi first = rows.get(0);
        BangQuyDoi last = rows.get(rows.size() - 1);
        if (diemTho.compareTo(first.getDDiema()) < 0) return new BigDecimal[]{first.getDDiema(), first.getDDiemb(), first.getDDiemc(), first.getDDiemd()};
        return new BigDecimal[]{last.getDDiema(), last.getDDiemb(), last.getDDiemc(), last.getDDiemd()};
    }

    public List<BangQuyDoi> findAllByPhuongThucAndMon(String phuongThuc, String mon) {
        if (phuongThuc == null || mon == null) return Collections.emptyList();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM BangQuyDoi b WHERE b.dPhuongthuc = :pt AND b.dMon = :mon ORDER BY b.dDiema ASC", BangQuyDoi.class)
                    .setParameter("pt", phuongThuc).setParameter("mon", mon).list();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public BigDecimal lookupDiemb(String phuongThuc, String mon, BigDecimal diemTho) {
        List<BangQuyDoi> rows = findAllByPhuongThucAndMon(phuongThuc, mon);
        BigDecimal result = null;
        for (BangQuyDoi row : rows) {
            if (row.getDDiema() != null && diemTho.compareTo(row.getDDiema()) >= 0) result = row.getDDiemb();
            else break;
        }
        return result;
    }

    public static String normalizeMaMon(String maMon) {
        if (maMon == null || maMon.isBlank()) return maMon;
        return maMon.trim().toUpperCase().replaceAll("_[A-Z0-9]{2,5}$", "");
    }

    public Map<String, List<BangQuyDoi>> loadAllAsCache() {
        Map<String, List<BangQuyDoi>> cache = new LinkedHashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<BangQuyDoi> all = session.createQuery("FROM BangQuyDoi b ORDER BY b.dMon ASC, b.dDiema ASC", BangQuyDoi.class).list();
            for (BangQuyDoi row : all) {
                String rawKey = row.getDMon() == null ? "" : row.getDMon().trim().toUpperCase();
                String normKey = normalizeMaMon(rawKey);
                cache.computeIfAbsent(rawKey, k -> new ArrayList<>()).add(row);
                if (!normKey.equals(rawKey)) cache.computeIfAbsent(normKey, k -> new ArrayList<>()).add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cache;
    }

    public static List<BangQuyDoi> lookupFromCache(Map<String, List<BangQuyDoi>> cache, String maMon) {
        if (cache == null || maMon == null) return Collections.emptyList();
        String upper = maMon.trim().toUpperCase();
        List<BangQuyDoi> direct = cache.get(upper);
        if (direct != null && !direct.isEmpty()) return direct;
        return cache.getOrDefault(normalizeMaMon(upper), Collections.emptyList());
    }
}
