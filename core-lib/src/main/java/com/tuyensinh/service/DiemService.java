package com.tuyensinh.service;

import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.model.NganhToHop;

import java.util.*;

public class DiemService {

    /**
     * Tap hop cac to hop mon xet tuyen chuan: ma to hop, 3 mon, mo ta
     */
    private static final String[][] TAP_TO_HOP = {
        {"A00", "TO", "LI", "HO",   "Toan, Vat ly, Hoa hoc"},
        {"A01", "TO", "LI", "N1",   "Toan, Vat ly, Tieng Anh"},
        {"B00", "TO", "HO", "SI",   "Toan, Hoa hoc, Sinh hoc"},
        {"C00", "VA", "SU", "DI",   "Ngu van, Lich su, Dia ly"},
        {"C01", "TO", "VA", "LI",   "Toan, Ngu van, Vat ly"},
        {"D01", "TO", "VA", "N1",   "Toan, Ngu van, Tieng Anh"},
        {"C14", "VA", "TO", "KTPL", "Ngu van, Toan, GDCD"},
    };

    /**
     * Chuyen entity DiemThiXetTuyen thanh Map[MaMon -> Diem].
     * Ngoai ngu lay max(thi, chung chi quy doi).
     */
    public Map<String, Double> buildScoreMap(DiemThiXetTuyen d) {
        if (d == null) return Collections.emptyMap();
        Map<String, Double> m = new HashMap<>();
        putIfPositive(m, "TO",   d.getTo());
        putIfPositive(m, "LI",   d.getLi());
        putIfPositive(m, "HO",   d.getHo());
        putIfPositive(m, "SI",   d.getSi());
        putIfPositive(m, "VA",   d.getVa());
        putIfPositive(m, "SU",   d.getSu());
        putIfPositive(m, "DI",   d.getDi());
        putIfPositive(m, "KTPL", d.getKtpl());

        double n1 = Math.max(
            d.getN1Thi() != null ? d.getN1Thi().doubleValue() : 0,
            d.getN1Cc()  != null ? d.getN1Cc().doubleValue()  : 0
        );
        if (n1 > 0) m.put("N1", n1);
        return m;
    }

    /**
     * Tinh diem xet tuyen cho 1 to hop cu the cua 1 nganh.
     * Co tinh he so mon va do lech (diem cong khu vuc/doi tuong).
     * @return diem xet, hoac -1 neu thi sinh thieu mon trong to hop
     */
    public double tinhDiemXet(Map<String, Double> scoreMap, NganhToHop th) {
        String mon1 = th.getThMon1();
        String mon2 = th.getThMon2();
        String mon3 = th.getThMon3();

        if (!scoreMap.containsKey(mon1)
         || !scoreMap.containsKey(mon2)
         || !scoreMap.containsKey(mon3)) return -1.0;

        double hs1    = th.getHsmon1()  != null ? th.getHsmon1()              : 1.0;
        double hs2    = th.getHsmon2()  != null ? th.getHsmon2()              : 1.0;
        double hs3    = th.getHsmon3()  != null ? th.getHsmon3()              : 1.0;
        double doLech = th.getDolech()  != null ? th.getDolech().doubleValue() : 0.0;
        double tongHS = hs1 + hs2 + hs3;

        return ((scoreMap.get(mon1) * hs1
               + scoreMap.get(mon2) * hs2
               + scoreMap.get(mon3) * hs3) * 3.0 / tongHS) + doLech;
    }

    /**
     * Tinh tat ca to hop kha dung cua thi sinh (du diem 3 mon).
     * Dung cho trang Diem thi - hien thi bang to hop.
     * @return List cac Map co key: name, total, totalStr, desc, detail
     */
    public List<Map<String, Object>> tinhListToHop(DiemThiXetTuyen diem) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (diem == null) return list;

        Map<String, Double> m = buildScoreMap(diem);
        for (String[] th : TAP_TO_HOP) {
            String mon1 = th[1], mon2 = th[2], mon3 = th[3];
            if (!m.containsKey(mon1) || !m.containsKey(mon2) || !m.containsKey(mon3)) continue;

            double total = m.get(mon1) + m.get(mon2) + m.get(mon3);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name",     th[0]);
            map.put("total",    total);
            map.put("totalStr", String.format("%.2f", total));
            map.put("desc",     th[4]);
            map.put("detail",   String.format("%s: %.2f | %s: %.2f | %s: %.2f",
                                    mon1, m.get(mon1), mon2, m.get(mon2), mon3, m.get(mon3)));
            list.add(map);
        }
        return list;
    }

    /**
     * Tra ve to hop co tong diem cao nhat.
     * Dung cho Dashboard va card tom tat diem.
     */
    public Map<String, Object> tinhMaxToHop(DiemThiXetTuyen diem) {
        return tinhListToHop(diem).stream()
            .max(Comparator.comparingDouble(m -> (Double) m.get("total")))
            .orElse(null);
    }

    private void putIfPositive(Map<String, Double> m, String key, Number val) {
        if (val != null && val.doubleValue() > 0) m.put(key, val.doubleValue());
    }

    /**
     * Tim diem thi cua thi sinh theo CCCD
     */
    public java.util.Optional<com.tuyensinh.model.DiemThiXetTuyen> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return java.util.Optional.empty();
        return new com.tuyensinh.dao.DiemThiXetTuyenDAO().findByCccd(cccd);
    }

    /**
     * Tao bang diem rong cho thi sinh (khi ho so duoc duyet HOP_LE)
     */
    public void taoDiemRong(com.tuyensinh.model.ThiSinh ts) {
        if (ts == null) return;
        com.tuyensinh.dao.DiemThiXetTuyenDAO diemDAO = new com.tuyensinh.dao.DiemThiXetTuyenDAO();
        
        if (diemDAO.findByCccd(ts.getCccd()).isEmpty()) {
            DiemThiXetTuyen d = new DiemThiXetTuyen();
            d.setCccd(ts.getCccd());
            d.setThiSinh(ts);
            
            java.math.BigDecimal zero = java.math.BigDecimal.ZERO;
            d.setTo(zero); 
            d.setLi(zero); 
            d.setHo(zero); 
            d.setVa(zero); 
            d.setSu(zero); 
            d.setDi(zero); 
            d.setSi(zero); 
            d.setN1Thi(zero); 
            d.setKtpl(zero);
            
            try {
                diemDAO.save(d);
                System.out.println("[DiemService] Khoi tao bang diem rong cho CCCD=" + ts.getCccd());
            } catch(Exception e) {
                System.err.println("[DiemService] Loi khoi tao diem rong: " + e.getMessage());
            }
        }
    }

}