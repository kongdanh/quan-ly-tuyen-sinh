package com.tuyensinh.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.tuyensinh.service.*;

@Configuration
public class ServiceConfig {

    @Bean public AuthService authService() { return new AuthService(); }
    @Bean public ThiSinhAccountService thiSinhAccountService() { return new ThiSinhAccountService(); }
    @Bean public NganhService nganhService() { return new NganhService(); }
    @Bean public ThiSinhService thiSinhService() { return new ThiSinhService(); }
    @Bean public DiemService diemService() { return new DiemService(); }
    @Bean public NguyenVongService nguyenVongService() { return new NguyenVongService(); }
    @Bean public HoSoTuyenSinhService hoSoTuyenSinhService() { return new HoSoTuyenSinhService(); }
    @Bean public YeuCauCapNhatService yeuCauCapNhatService() { return new YeuCauCapNhatService(); }
    @Bean public XetTuyenService xetTuyenService() { return new XetTuyenService(); }
    @Bean public NganhToHopService nganhToHopService() { return new NganhToHopService(); }
    @Bean public DotTuyenSinhService dotTuyenSinhService() { return new DotTuyenSinhService(); }
    @Bean public NhatKyHoatDongService nhatKyHoatDongService() { return new NhatKyHoatDongService(); }
    @Bean public ToHopMonService toHopMonService() { return new ToHopMonService(); }
    @Bean public UserService userService() { return new UserService(); }
    @Bean public DiemChuanDotService diemChuanDotService() { return new DiemChuanDotService(); }
}