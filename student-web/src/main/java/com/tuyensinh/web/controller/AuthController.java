package com.tuyensinh.web.controller;

import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.service.AuthService;
import com.tuyensinh.web.service.EmailService;
import com.tuyensinh.web.util.WebConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Random;

@Controller
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private EmailService emailService;

    // Cac trang public (landing page)

    @GetMapping("/")
    public String showLandingPage(HttpSession session) {
        if (session != null && session.getAttribute(WebConstants.SESSION_THISINH) != null) {
            return "redirect:/thisinh/dashboard";
        }
        return "public/index"; 
    }

    @GetMapping("/chi-tieu")
    public String chiTieu(HttpSession session) {
        if (session != null && session.getAttribute(WebConstants.SESSION_THISINH) != null) {
            return "redirect:/thisinh/dashboard";
        }
        return "public/chi-tieu";
    }

    @GetMapping("/quy-trinh")
    public String quyTrinh(HttpSession session) {
        if (session != null && session.getAttribute(WebConstants.SESSION_THISINH) != null) {
            return "redirect:/thisinh/dashboard";
        }
        return "public/quy-trinh";
    }

    @GetMapping("/huong-dan")
    public String huongDan(HttpSession session) {
        if (session != null && session.getAttribute(WebConstants.SESSION_THISINH) != null) {
            return "redirect:/thisinh/dashboard";
        }
        return "public/huong-dan";
    }

    // Dang nhap va dang xuat
    
    @GetMapping("/login")
    public String showLogin(HttpSession session) {
        if (session != null && session.getAttribute(WebConstants.SESSION_THISINH) != null) {
            return "redirect:/thisinh/dashboard";
        }
        return "public/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("cccd") String cccd, @RequestParam("password") String password, HttpServletRequest request, Model model) {
        ThiSinhSessionDTO thisinh = authService.loginThiSinh(cccd.trim(), password.trim());
        if (thisinh != null) {
            request.getSession().invalidate();
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute(WebConstants.SESSION_THISINH, thisinh);
            newSession.setMaxInactiveInterval(30 * 60);
            return "redirect:/thisinh/dashboard";
        } else {
            model.addAttribute("error", "Sai CCCD hoặc mật khẩu!");
            return "public/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/login";
    }

    // Dang ky tai khoan moi

    @GetMapping("/register")
    public String showRegister() {
        return "public/register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam("cccd") String cccd,
                                  @RequestParam("ho") String ho,
                                  @RequestParam("ten") String ten,
                                  @RequestParam("ngaySinh") String ngaySinh,
                                  @RequestParam("gioiTinh") String gioiTinh,
                                  @RequestParam("noiSinh") String noiSinh,
                                  @RequestParam("email") String email,
                                  @RequestParam("dienThoai") String dienThoai,
                                  @RequestParam("password") String password,
                                  @RequestParam("g-recaptcha-response") String recaptchaResponse,
                                  HttpSession session, Model model) {
        
        // Kiem tra Google reCAPTCHA
        String recaptchaSecret = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe";
        String recaptchaUrl = "https://www.google.com/recaptcha/api/siteverify?secret=" + recaptchaSecret + "&response=" + recaptchaResponse;
        RestTemplate restTemplate = new RestTemplate();
        Map responseMap = restTemplate.postForObject(recaptchaUrl, null, Map.class);
        boolean isCaptchaValid = (Boolean) responseMap.get("success");
        
        if (!isCaptchaValid) {
            model.addAttribute("error", "Vui lòng xác thực bạn không phải là robot!");
            return "public/register";
        }

        // Kiem tra CCCD da ton tai chua
        if (authService.checkExists(cccd.trim(), email.trim())) {
            model.addAttribute("error", "CCCD hoặc Email này đã được đăng ký trong hệ thống!");
            return "public/register";
        }

        // Tao OTP va gui email
        String otp = String.format("%06d", new Random().nextInt(999999));
        System.out.println("======> MÃ OTP ĐĂNG KÝ CỦA " + email + " LÀ: " + otp); 
        
        // Gửi mã OTP qua email cho thí sinh (Sử dụng cấu hình SMTP trong application.properties)
        emailService.sendOtpEmail(email.trim(), otp);

        // Luu thong tin dang ky vao session cho xac thuc OTP
        ThiSinh tempTs = new ThiSinh();
        tempTs.setCccd(cccd.trim());
        tempTs.setHo(ho.trim());
        tempTs.setTen(ten.trim());
        tempTs.setNgaySinh(ngaySinh.trim());
        tempTs.setGioiTinh(gioiTinh.trim());
        tempTs.setNoiSinh(noiSinh.trim());
        tempTs.setEmail(email.trim());
        tempTs.setDienThoai(dienThoai.trim());
        
        session.setAttribute("TEMP_USER", tempTs);
        session.setAttribute("TEMP_PASS", password.trim());
        session.setAttribute("TEMP_OTP", otp);
        
        return "redirect:/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtp(HttpSession session) {
        if (session.getAttribute("TEMP_OTP") == null) {
            return "redirect:/register";
        }
        return "public/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam("otp") String otpInput, HttpSession session, Model model) {
        String savedOtp = (String) session.getAttribute("TEMP_OTP");
        
        if (savedOtp != null && savedOtp.equals(otpInput.trim())) {
            // OTP dung, tao tai khoan chinh thuc
            ThiSinh ts = (ThiSinh) session.getAttribute("TEMP_USER");
            String pass = (String) session.getAttribute("TEMP_PASS");
            
            boolean success = authService.registerNewCandidate(ts, pass);
            
            if (success) {
                // Xoa session tam
                session.removeAttribute("TEMP_USER");
                session.removeAttribute("TEMP_PASS");
                session.removeAttribute("TEMP_OTP");
                model.addAttribute("msg", "Đăng ký thành công! Vui lòng đăng nhập.");
                return "public/login";
            } else {
                model.addAttribute("error", "Lỗi hệ thống khi tạo tài khoản. Vui lòng thử lại!");
                return "public/verify-otp";
            }
        } else {
            model.addAttribute("error", "Mã OTP không chính xác!");
            return "public/verify-otp";
        }
    }
}