package com.tuyensinh.web.controller;

import com.tuyensinh.dao.NganhToHopDAO;
import com.tuyensinh.model.Nganh;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.service.NganhService;
import com.tuyensinh.service.TraCuuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tuyensinh.model.KetQuaXetTuyen;
import com.tuyensinh.dao.XetTuyenDAO;
import com.tuyensinh.web.service.EmailService;
import com.tuyensinh.util.PdfExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;

import java.util.*;

@Controller
@RequestMapping("/thisinh/tracuu")
public class TraCuuController extends BaseController {

    @Autowired private NganhService nganhService;
    
    // Tự khởi tạo không dùng Autowired (Bảo vệ Core-lib)
    private final TraCuuService traCuuService = new TraCuuService();
    private final NganhToHopDAO nganhToHopDAO = new NganhToHopDAO();
    private final com.tuyensinh.service.NguyenVongService nguyenVongService = new com.tuyensinh.service.NguyenVongService();

    // ==========================================
    // TRA CỨU TỔNG HỢP THEO CCCD
    // ==========================================
    @GetMapping("/tonghop")
    public String showTraCuuTongHopForm() {
        return "public/tracuu_tonghop";
    }

    @PostMapping("/tonghop")
    public String traCuuTongHop(@RequestParam("cccd") String cccd, 
                                @RequestParam(value = "ngaySinh", required = false) String ngaySinh, 
                                Model model) {
        model.addAttribute("cccd", cccd);
        model.addAttribute("ngaySinh", ngaySinh);
        
        List<com.tuyensinh.model.NguyenVong> listNv = nguyenVongService.findByCccd(cccd);
        
        if (listNv == null || listNv.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy dữ liệu nguyện vọng cho CCCD: " + cccd);
        } else {
            com.tuyensinh.model.ThiSinh ts = listNv.get(0).getThiSinh();
            
            // Bảo mật: Kiểm tra ngày sinh (mặc dù là public tra cứu nhưng cần DOB để bảo mật hơn)
            if (ngaySinh == null || ngaySinh.trim().isEmpty()) {
                model.addAttribute("error", "Vui lòng nhập Ngày sinh để tra cứu!");
                return "public/tracuu_tonghop";
            }
            
            if (ts.getNgaySinh() == null || !ts.getNgaySinh().trim().equals(ngaySinh.trim())) {
                model.addAttribute("error", "Ngày sinh không khớp với dữ liệu thí sinh. Vui lòng kiểm tra lại!");
                return "public/tracuu_tonghop";
            }

            model.addAttribute("thiSinh", ts);
            model.addAttribute("listNv", listNv);
            
            // Tính toán map điểm quy đổi (DGNL, VSAT)
            Map<Integer, Double> mapDiemQD = new java.util.HashMap<>();
            for (com.tuyensinh.model.NguyenVong nv : listNv) {
                if (nv.getDiemThxt() != null) {
                    if ("DGNL".equalsIgnoreCase(nv.getTtPhuongthuc()) || nv.getTtPhuongthuc().contains("ĐGNL")) {
                        mapDiemQD.put(nv.getId(), traCuuService.tinhDiemQuyDoiDGNL(nv.getDiemThxt().doubleValue()));
                    } else if ("VSAT".equalsIgnoreCase(nv.getTtPhuongthuc())) {
                        mapDiemQD.put(nv.getId(), traCuuService.tinhDiemQuyDoiVSAT(nv.getDiemThxt().doubleValue(), null));
                    }
                }
            }
            model.addAttribute("mapDiemQD", mapDiemQD);
        }
        
        return "public/tracuu_tonghop";
    }

    // ==========================================
    // PHƯƠNG THỨC ĐGNL
    // ==========================================
    @GetMapping("/dgnl")
    public String showTraCuuDGNLForm(Model model) {
        model.addAttribute("listNganh", nganhService.findAllSync());
        return "public/tracuu_dgnl";
    }

    @PostMapping("/dgnl")
    public String calculateDGNL(
            @RequestParam(value = "diemThi", defaultValue = "0") Double diemThi,
            @RequestParam("manganh") String manganh,
            @RequestParam(value = "diemCong", defaultValue = "0.0") Double diemCong,
            @RequestParam("khuVuc") String khuVuc,
            @RequestParam("doiTuong") String doiTuong,
            Model model) {

        List<Nganh> tatCaNganh = nganhService.findAllSync();
        model.addAttribute("listNganh", tatCaNganh);
        model.addAttribute("diemThi", diemThi);
        model.addAttribute("manganhSelected", manganh);
        model.addAttribute("diemCong", diemCong);
        model.addAttribute("khuVuc", khuVuc);
        model.addAttribute("doiTuong", doiTuong);

        Double diemQuyDoi = traCuuService.tinhDiemQuyDoiDGNL(diemThi);
        if (diemQuyDoi == null) {
            model.addAttribute("error", "Lỗi: Điểm nhập vào không nằm trong phân vị nào!");
            return "public/tracuu_dgnl";
        }

        Nganh nganh = nganhService.findByMaNganh(manganh).orElse(null); 
        double diemSan = (nganh != null && nganh.getNDiemsan() != null) ? nganh.getNDiemsan().doubleValue() : 0.0;
        
        double diemUuTien = traCuuService.tinhDiemUuTien(diemQuyDoi, khuVuc, doiTuong);
        double tongDiemXT = Math.round((diemQuyDoi + diemCong + diemUuTien) * 100.0) / 100.0;
        boolean datNguong = tongDiemXT >= diemSan;
        
        // 1. Tính toán độ chênh lệch (Gap Analysis)
        double doLech = Math.round((tongDiemXT - diemSan) * 100.0) / 100.0;

        // 2. Tính năng Gợi ý ngành (Chỉ tìm các ngành thỏa mãn điểm sàn và khác ngành đang chọn)
        List<Nganh> listGoiY = new java.util.ArrayList<>();
        if (tongDiemXT > 0) {
            for (Nganh n : tatCaNganh) {
                double ds = n.getNDiemsan() != null ? n.getNDiemsan().doubleValue() : 0.0;
                if (ds > 0 && tongDiemXT >= ds && !n.getManganh().equals(manganh)) {
                    listGoiY.add(n);
                }
            }
        }

        model.addAttribute("nganh", nganh);
        model.addAttribute("diemQuyDoi", diemQuyDoi);
        model.addAttribute("diemUuTien", diemUuTien);
        model.addAttribute("tongDiemXT", tongDiemXT);
        model.addAttribute("diemSan", diemSan);
        model.addAttribute("datNguong", datNguong);
        model.addAttribute("doLech", doLech);
        model.addAttribute("listGoiY", listGoiY);

        return "public/tracuu_dgnl";
    }

    // ==========================================
    // PHƯƠNG THỨC VSAT
    // ==========================================
    @GetMapping("/vsat")
    public String showTraCuuVSATForm(Model model) {
        model.addAttribute("listNganh", nganhService.findAllSync());
        return "public/tracuu_vsat";
    }

    @PostMapping("/vsat")
    public String calculateVSAT(
            @RequestParam(value = "diemTo", defaultValue = "0") Double diemTo,
            @RequestParam(value = "diemLi", defaultValue = "0") Double diemLi,
            @RequestParam(value = "diemHo", defaultValue = "0") Double diemHo,
            @RequestParam(value = "diemSi", defaultValue = "0") Double diemSi,
            @RequestParam(value = "diemVa", defaultValue = "0") Double diemVa,
            @RequestParam(value = "diemSu", defaultValue = "0") Double diemSu,
            @RequestParam(value = "diemDi", defaultValue = "0") Double diemDi,
            @RequestParam(value = "diemN1", defaultValue = "0") Double diemN1,
            @RequestParam("manganh") String manganh,
            @RequestParam("khuVuc") String khuVuc,
            @RequestParam("doiTuong") String doiTuong,
            @RequestParam(value = "diemCong", defaultValue = "0.0") Double diemCong,
            Model model) {

        model.addAttribute("listNganh", nganhService.findAllSync());
        model.addAttribute("diemTo", diemTo); model.addAttribute("diemLi", diemLi);
        model.addAttribute("diemHo", diemHo); model.addAttribute("diemSi", diemSi);
        model.addAttribute("diemVa", diemVa); model.addAttribute("diemSu", diemSu);
        model.addAttribute("diemDi", diemDi); model.addAttribute("diemN1", diemN1);
        model.addAttribute("manganhSelected", manganh);
        model.addAttribute("khuVuc", khuVuc);
        model.addAttribute("doiTuong", doiTuong);
        model.addAttribute("diemCong", diemCong);

        // Quy đổi điểm 150 -> 10
        Map<String, Double> diemQD = new HashMap<>();
        diemQD.put("TO", traCuuService.tinhDiemQuyDoiVSAT(diemTo, "TO"));
        diemQD.put("LI", traCuuService.tinhDiemQuyDoiVSAT(diemLi, "LI"));
        diemQD.put("HO", traCuuService.tinhDiemQuyDoiVSAT(diemHo, "HO"));
        diemQD.put("SI", traCuuService.tinhDiemQuyDoiVSAT(diemSi, "SI"));
        diemQD.put("VA", traCuuService.tinhDiemQuyDoiVSAT(diemVa, "VA"));
        diemQD.put("SU", traCuuService.tinhDiemQuyDoiVSAT(diemSu, "SU"));
        diemQD.put("DI", traCuuService.tinhDiemQuyDoiVSAT(diemDi, "DI"));
        diemQD.put("N1", traCuuService.tinhDiemQuyDoiVSAT(diemN1, "N1"));

        Nganh nganh = nganhService.findByMaNganh(manganh).orElse(null);
        if (nganh == null) {
            model.addAttribute("error", "Vui lòng chọn ngành hợp lệ.");
            return "public/tracuu_vsat";
        }

        // Truy xuất các Tổ hợp môn được phép xét tuyển vào ngành này
        List<NganhToHop> toHops = nganhToHopDAO.findByMaNganh(manganh);
        List<ResultToHopDTO> listResult = new ArrayList<>();
        ResultToHopDTO bestToHop = null;

        if (toHops != null && !toHops.isEmpty()) {
            for (NganhToHop th : toHops) {
                String m1 = th.getToHopMon().getMon1();
                String m2 = th.getToHopMon().getMon2();
                String m3 = th.getToHopMon().getMon3();

                Double d1 = diemQD.getOrDefault(m1, 0.0);
                Double d2 = diemQD.getOrDefault(m2, 0.0);
                Double d3 = diemQD.getOrDefault(m3, 0.0);

                // Luật: Điểm liệt (<1) sẽ không được xét tổ hợp đó
                if (d1 < 1.0 || d2 < 1.0 || d3 < 1.0) {
                    
                };

                double tong3Mon = Math.round((d1 + d2 + d3) * 100.0) / 100.0;
                double diemUuTien = traCuuService.tinhDiemUuTien(tong3Mon, khuVuc, doiTuong);
                
                // Giả sử lấy độ lệch từ DB (Nếu chưa có cột dolech, để 0)
                double dolech = 0.0; 
                double tongXT = Math.round((tong3Mon + diemUuTien + diemCong - dolech) * 100.0) / 100.0;

                ResultToHopDTO r = new ResultToHopDTO(th.getToHopMon().getMatohop(), m1, d1, m2, d2, m3, d3, tong3Mon, diemUuTien, dolech, tongXT);
                listResult.add(r);

                if (bestToHop == null || tongXT > bestToHop.getTongXT()) {
                    bestToHop = r;
                }
            }
        }

        model.addAttribute("nganh", nganh);
        model.addAttribute("listResult", listResult);
        model.addAttribute("bestToHop", bestToHop);
        
        double diemSan = nganh.getNDiemsan() != null ? nganh.getNDiemsan().doubleValue() : 0.0;
        model.addAttribute("diemSan", diemSan);

        // 1. Phân tích độ lệch và Xét Đạt Ngưỡng
        double doLech = 0.0;
        boolean datNguong = false;
        if (bestToHop != null) {
            datNguong = bestToHop.getTongXT() >= diemSan;
            doLech = Math.round((bestToHop.getTongXT() - diemSan) * 100.0) / 100.0;
        }
        model.addAttribute("datNguong", datNguong);
        model.addAttribute("doLech", doLech);

        // 2. Logic Gợi ý ngành (Dành cho V-SAT)
        List<Nganh> listGoiY = new ArrayList<>();
        if (bestToHop != null && bestToHop.getTongXT() > 0) {
            double diemXTCaoNhat = bestToHop.getTongXT();
            for (Nganh n : nganhService.findAllSync()) {
                double ds = n.getNDiemsan() != null ? n.getNDiemsan().doubleValue() : 0.0;
                // Nếu điểm Tổ hợp cao nhất lớn hơn sàn và khác ngành đang tra cứu
                if (ds > 0 && diemXTCaoNhat >= ds && !n.getManganh().equals(manganh)) {
                    listGoiY.add(n);
                }
            }
        }
        model.addAttribute("listGoiY", listGoiY);
        model.addAttribute("mapDiemQuyDoi", diemQD);

        return "public/tracuu_vsat";
    }

    // DTO Nội bộ để đẩy dữ liệu ra thẻ HTML sạch sẽ
    public static class ResultToHopDTO {
        private String maToHop, mon1, mon2, mon3;
        private Double d1, d2, d3, tong3Mon, diemUuTien, doLech, tongXT;

        public ResultToHopDTO(String maToHop, String mon1, Double d1, String mon2, Double d2, String mon3, Double d3, Double tong3Mon, Double diemUuTien, Double doLech, Double tongXT) {
            this.maToHop = maToHop; this.mon1 = mon1; this.d1 = d1; this.mon2 = mon2; this.d2 = d2; this.mon3 = mon3; this.d3 = d3;
            this.tong3Mon = tong3Mon; this.diemUuTien = diemUuTien; this.doLech = doLech; this.tongXT = tongXT;
        }
        public String getMaToHop() { return maToHop; } public String getMon1() { return mon1; } public Double getD1() { return d1; }
        public String getMon2() { return mon2; } public Double getD2() { return d2; } public String getMon3() { return mon3; }
        public Double getD3() { return d3; } public Double getTong3Mon() { return tong3Mon; } public Double getDiemUuTien() { return diemUuTien; }
        public Double getDoLech() { return doLech; } public Double getTongXT() { return tongXT; }
    }

    @Autowired private EmailService emailService;
    private final XetTuyenDAO xetTuyenDAO = new XetTuyenDAO();

    // 1. Xử lý Xác nhận nhập học
    @PostMapping("/ket-qua/xac-nhan")
    public String confirmEnrollment(HttpSession session) {
        // Giả sử lấy ID hồ sơ/thí sinh từ Session đã đăng nhập
        Integer idHoSo = (Integer) session.getAttribute("idHoSoDangNhap"); 
        if (idHoSo == null) return "redirect:/thisinh/login";

        // Lấy kết quả đậu của thí sinh này
        List<KetQuaXetTuyen> listKq = xetTuyenDAO.getKetQuaTheoHoSo(idHoSo);
        KetQuaXetTuyen kqDau = listKq.stream().filter(k -> "TRUNG_TUYEN".equals(k.getTrangThai())).findFirst().orElse(null);

        if (kqDau != null) {
            kqDau.setXacNhan(true);
            xetTuyenDAO.update(kqDau); // Lưu xuống DB

            // Gửi email báo thành công (Giả sử lấy được email từ thí sinh)
            String email = kqDau.getHoSo().getThiSinh().getEmail();
            String ten = kqDau.getHoSo().getThiSinh().getHo() + " " + kqDau.getHoSo().getThiSinh().getTen();
            if (email != null && !email.isEmpty()) {
                emailService.sendConfirmSuccessEmail(email, ten, kqDau.getNganh().getTennganh());
            }
        }
        return "redirect:/thisinh/ket-qua?success=true";
    }

    // 2. Xuất PDF Giấy báo (Trực tiếp tải về trình duyệt)
    @GetMapping("/ket-qua/in-giay-bao")
    public void downloadGiayBao(HttpSession session, HttpServletResponse response) {
        Integer idHoSo = (Integer) session.getAttribute("idHoSoDangNhap"); 
        if (idHoSo == null) return;

        try {
            List<KetQuaXetTuyen> listKq = xetTuyenDAO.getKetQuaTheoHoSo(idHoSo);
            File tempFile = File.createTempFile("GiayBao_", ".pdf");
            
            // Dùng Util đã viết ở bài trước để sinh PDF cho riêng 1 list này
            PdfExportUtil.exportGiayBaoTrungTuyen(tempFile, listKq);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"GiayBaoTrungTuyen_SGU.pdf\"");
            Files.copy(tempFile.toPath(), response.getOutputStream());
            response.getOutputStream().flush();
            tempFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}