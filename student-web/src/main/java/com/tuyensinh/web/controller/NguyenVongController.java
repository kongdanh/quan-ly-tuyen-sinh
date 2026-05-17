package com.tuyensinh.web.controller;

import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.service.DiemService;
import com.tuyensinh.service.DotTuyenSinhService;
import com.tuyensinh.service.NganhService;
import com.tuyensinh.service.NguyenVongService;
import com.tuyensinh.service.NguyenVongService.SaveResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/thisinh/wishes")
public class NguyenVongController extends BaseController {

    @Autowired private NguyenVongService nvService;
    @Autowired private NganhService nganhService;
    @Autowired private DiemService diemService;
    @Autowired private DotTuyenSinhService dotService;

    @GetMapping
    public String wishes(HttpSession session, Model model) {
        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return "redirect:/login";

        model.addAttribute("wishes", nvService.findByCccd(user.getCccd()));
        model.addAttribute("listNganh", nganhService.findAllSync());
        model.addAttribute("isCongMo", dotService.isCongDangKyMo());
        return "private/wishes";
    }

    @PostMapping("/save")
    public String saveWish(@RequestParam("manganh") String manganh,
                           @RequestParam(value = "idnv", required = false) Integer idnv,
                           HttpSession session) {
        if (!dotService.isCongDangKyMo()) return "redirect:/thisinh/wishes?error=gate_closed";
        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return "redirect:/login";

        DiemThiXetTuyen diem = diemService.findByCccd(user.getCccd()).orElse(null);
        SaveResult result = nvService.saveWish(user.getCccd(), manganh, idnv, diem);

        return switch (result) {
            case OK            -> "redirect:/thisinh/wishes?success=add";
            case NOT_QUALIFIED -> "redirect:/thisinh/wishes?error=not_qualified";
            case MAX_REACHED   -> "redirect:/thisinh/wishes?error=max";
            case FORBIDDEN     -> "redirect:/thisinh/wishes?error=forbidden";
        };
    }

    @PostMapping("/delete")
    public String deleteWish(@RequestParam("idnv") Integer idnv, HttpSession session) {
        if (!dotService.isCongDangKyMo()) return "redirect:/thisinh/wishes?error=gate_closed";
        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return "redirect:/login";

        try {
            NguyenVongService.KetQuaDangKy kq = nvService.huyNguyenVong(idnv, user.getCccd());
            return "redirect:/thisinh/wishes?" + (kq.isThanhCong() ? "success=delete" : "error=forbidden");
        } catch (Exception e) {
            return "redirect:/thisinh/wishes?error=delete_failed";
        }
    }

    @PostMapping("/reorder")
    @ResponseBody
    public ResponseEntity<Void> reorderWishes(@RequestParam("ids") String idsParam, HttpSession session) {
        if (!dotService.isCongDangKyMo()) return ResponseEntity.status(403).build();
        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return ResponseEntity.status(401).build();

        if (idsParam == null || idsParam.isBlank()) return ResponseEntity.badRequest().build();
        
        nvService.reorderByIds(idsParam.split(","), user.getCccd());
        return ResponseEntity.ok().build();
    }
}