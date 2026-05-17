package com.tuyensinh.web.controller;

import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.model.KetQuaXetTuyen;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.YeuCauCapNhat;
import com.tuyensinh.service.DiemService;
import com.tuyensinh.service.DotTuyenSinhService;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.XetTuyenService;
import com.tuyensinh.service.YeuCauCapNhatService;
import com.tuyensinh.web.util.WebConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/thisinh")
public class HoSoController extends BaseController {

    @Autowired private DotTuyenSinhService dotService;
    @Autowired private DiemService diemService;
    @Autowired private XetTuyenService xetTuyenService;
    @Autowired private ThiSinhService thiSinhService;
    private final YeuCauCapNhatService ycService = YeuCauCapNhatService.getInstance();

    @GetMapping("/scores")
    public String scores(HttpSession session, Model model) {
        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return "redirect:/login";

        DiemThiXetTuyen diem = diemService.findByCccd(user.getCccd()).orElse(null);
        List<Map<String, Object>> listToHop = (List<Map<String, Object>>) diemService.tinhListToHop(diem);

        model.addAttribute("diem", diem);
        model.addAttribute("listToHop", listToHop);
        if (listToHop != null && !listToHop.isEmpty()) {
            model.addAttribute("maxToHop", listToHop.stream()
                .max(Comparator.comparingDouble(m -> (Double) m.get("total"))).orElse(null));
        }
        return "private/scores";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return "redirect:/login";

        ThiSinh thongTin = thiSinhService.findByCccd(user.getCccd()).orElse(null);
        YeuCauCapNhat yeuCau = ycService.layYeuCauMoiNhat(user.getCccd());

        model.addAttribute("thongTin", thongTin);
        model.addAttribute("yeuCau", yeuCau);
        model.addAttribute("isCongMo", dotService.isCongDangKyMo()); // Truyền trạng thái cổng

        return "private/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("dienThoai") String dienThoai,
            @RequestParam("email") String email,
            @RequestParam("noiSinh") String noiSinh,
            @RequestParam("khuVuc") String khuVuc,
            @RequestParam("doiTuong") String doiTuong,
            @RequestParam(value = "fileMinhChung", required = false) MultipartFile fileMinhChung,
            HttpServletRequest request,
            HttpSession session) {
        
        if (!dotService.isCongDangKyMo()) return "redirect:/thisinh/profile?error=gate_closed";

        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return "redirect:/login";

        ThiSinh ts = thiSinhService.findByCccd(user.getCccd()).orElse(null);
        if (ts == null) return "redirect:/login";

        String fileName = saveUploadedFile(fileMinhChung, user.getCccd(), request);

        YeuCauCapNhat yc = new YeuCauCapNhat();
        yc.setKhuVuc(khuVuc);
        yc.setDoiTuong(doiTuong);
        yc.setDienThoai(dienThoai);
        yc.setEmail(email);
        yc.setNoiSinh(noiSinh);
        yc.setMinhChungUrl(fileName);

        boolean success = ycService.taoYeuCau(yc, ts.getId(), ts.getHo() + " " + ts.getTen());
        return "redirect:/thisinh/profile?" + (success ? "success=true" : "error=failed");
    }

    @GetMapping("/results")
    public String results(HttpSession session, Model model) {
        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return "redirect:/login";

        // Tìm kết quả trúng tuyển theo CCCD thí sinh
        KetQuaXetTuyen ketQua = xetTuyenService.timKetQuaTheoCccd(user.getCccd());
        
        // Giả sử có một biến hệ thống hoặc logic để kiểm tra đã đến ngày công bố chưa
        // Ở đây mình tạm set là true nếu đã có bản ghi trong bảng kết quả
        boolean daCoKetQua = (ketQua != null); 
        
        model.addAttribute("trungTuyen", ketQua);
        model.addAttribute("daCoKetQua", daCoKetQua);
        return "private/results";
    }

    @PostMapping("/notifications/read")
    @ResponseBody
    public ResponseEntity<Void> readNotifications(HttpSession session) {
        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        ycService.danhDauDaDoc(user.getCccd());
        return ResponseEntity.ok().build();
    }

    private String saveUploadedFile(MultipartFile file, String cccd, HttpServletRequest request) {
        if (file == null || file.isEmpty()) return null;
        try {
            String orig = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
            String fileName = "TS_" + cccd + "_" + System.currentTimeMillis() + "_" + orig;
            String dir = request.getServletContext().getRealPath("") + File.separator + WebConstants.UPLOAD_DIR;
            File uploadDir = new File(dir);
            if (!uploadDir.exists()) uploadDir.mkdirs();
            file.transferTo(new File(dir + File.separator + fileName));
            return fileName;
        } catch (IOException e) {
            return null;
        }
    }
}