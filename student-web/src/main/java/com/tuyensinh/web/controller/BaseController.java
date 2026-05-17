package com.tuyensinh.web.controller;

import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.YeuCauCapNhat;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.YeuCauCapNhatService;
import com.tuyensinh.web.util.WebConstants;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

public abstract class BaseController {

    @Autowired protected ThiSinhService thiSinhService;
    @Autowired protected YeuCauCapNhatService ycService;

    // Nap du lieu chung (navbar, thong bao) truoc moi request
    @ModelAttribute
    public void addCommonAttributes(HttpSession session, Model model) {
        ThiSinhSessionDTO user = getUser(session);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("thongTin", thiSinhService.findByCccd(user.getCccd()).orElse(null));
            
            List<YeuCauCapNhat> notifs = ycService.layDanhSachThongBao(user.getCccd());
            long unreadCount = notifs.stream().filter(n -> Boolean.FALSE.equals(n.getIsRead())).count();
            model.addAttribute("notifications", notifs);
            model.addAttribute("unreadCount", unreadCount);
        }
    }

    protected ThiSinhSessionDTO getUser(HttpSession session) {
        return (ThiSinhSessionDTO) session.getAttribute(WebConstants.SESSION_THISINH);
    }
}