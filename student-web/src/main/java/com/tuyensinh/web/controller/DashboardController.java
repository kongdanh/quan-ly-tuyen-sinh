package com.tuyensinh.web.controller;

import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.model.DiemThiXetTuyen;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.ThiSinhAccount;
import com.tuyensinh.service.DiemService;
import com.tuyensinh.service.NguyenVongService;
import com.tuyensinh.service.ThiSinhAccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/thisinh")
public class DashboardController extends BaseController {

    @Autowired private DiemService diemService;
    @Autowired private NguyenVongService nvService;
    @Autowired private ThiSinhAccountService accService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return "redirect:/login";

        String cccd = user.getCccd();
        DiemThiXetTuyen diem = diemService.findByCccd(cccd).orElse(null);
        
        model.addAttribute("diem", diem);
        model.addAttribute("maxToHop", diemService.tinhMaxToHop(diem));
        model.addAttribute("wishes", nvService.findByCccd(cccd));

        ThiSinh thongTin = thiSinhService.findByCccd(cccd).orElse(null);
        ThiSinhAccount acc = accService.findByCccd(cccd).orElse(null); 
        boolean isDefaultPassword = thongTin != null && acc != null && 
                                    org.mindrot.jbcrypt.BCrypt.checkpw(thongTin.getNgaySinh(), acc.getPasswordHash());
        model.addAttribute("isDefaultPassword", isDefaultPassword);

        return "private/dashboard";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPass,
                                 @RequestParam("newPassword") String newPass,
                                 @RequestParam("confirmPassword") String confirm,
                                 HttpSession session) {
        ThiSinhSessionDTO user = getUser(session);
        if (user == null) return "redirect:/login";

        if (newPass == null || !newPass.equals(confirm)) return "redirect:/thisinh/dashboard?error=pwd_mismatch";

        ThiSinhAccount acc = accService.findByCccd(user.getCccd()).orElse(null);
        if (acc != null && org.mindrot.jbcrypt.BCrypt.checkpw(oldPass, acc.getPasswordHash())) {
            acc.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw(newPass, org.mindrot.jbcrypt.BCrypt.gensalt(12)));
            accService.update(acc);
            return "redirect:/thisinh/dashboard?success=pwd_changed";
        }
        return "redirect:/thisinh/dashboard?error=wrong_old_pwd";
    }
}