// core-lib/src/main/java/com/tuyensinh/service/DiemService.java
package com.tuyensinh.service;

import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.model.NganhToHop;

import java.util.*;

public class DiemService {

    private static final String[][] TAP_TO_HOP = {
        {"A00", "TO", "LI", "HO",   "Toán, Vật lý, Hóa học"},
        {"A01", "TO", "LI", "N1",   "Toán, Vật lý, Tiếng Anh"},
        {"B00", "TO", "HO", "SI",   "Toán, Hóa học, Sinh học"},
        {"C00", "VA", "SU", "DI",   "Ngữ văn, Lịch sử, Địa lý"},
        {"C01", "TO", "VA", "LI",   "Toán, Ngữ văn, Vật lý"},
        {"D01", "TO", "VA", "N1",   "Toán, Ngữ văn, Tiếng Anh"},
        {"C14", "VA", "TO", "KTPL", "Ngữ văn, Toán, GDCD"},
    };

    // PUBLIC API

    /**
     * Chuyển entity DiemThiXetTuyen thành Map[MaMon -> Diem].
     * Ngoại ngữ lấy max(thi, chứng chỉ quy đổi).
     * Dùng chung cho tất cả các nơi cần tính điểm.
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
     * Tính điểm xét tuyển cho 1 tổ hợp cụ thể của 1 ngành.
     * Có tính hệ số môn và độ lệch (điểm cộng khu vực/đối tượng).
     * @return điểm xét, hoặc -1 nếu thí sinh thiếu môn trong tổ hợp
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
     * Tính tất cả tổ hợp khả dụng của thí sinh (đủ điểm 3 môn).
     * Dùng cho trang Điểm thi — hiển thị bảng tổ hợp.
     * @return List các Map có key: name, total, totalStr, desc, detail
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
     * Trả về tổ hợp có tổng điểm cao nhất.
     * Dùng cho Dashboard và card tóm tắt điểm.
     * @return Map tổ hợp tối ưu, hoặc null nếu không có tổ hợp nào
     */
    public Map<String, Object> tinhMaxToHop(DiemThiXetTuyen diem) {
        return tinhListToHop(diem).stream()
            .max(Comparator.comparingDouble(m -> (Double) m.get("total")))
            .orElse(null);
    }

    // PRIVATE HELPERS

    private void putIfPositive(Map<String, Double> m, String key, Number val) {
        if (val != null && val.doubleValue() > 0) m.put(key, val.doubleValue());
    }

    public java.util.Optional<com.tuyensinh.model.DiemThiXetTuyen> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return java.util.Optional.empty();
        return new com.tuyensinh.dao.DiemThiXetTuyenDAO().findByCccd(cccd);
    }

}