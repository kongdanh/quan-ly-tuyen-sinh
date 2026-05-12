package com.tuyensinh.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public boolean sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("hethongtuyensinh.sgu@gmail.com"); 
            message.setTo(toEmail);
            message.setSubject("Mã xác thực OTP - Cổng thông tin Tuyển sinh SGU");
            message.setText("Xin chào,\n\n"
                    + "Mã xác thực OTP để hoàn tất đăng ký tài khoản của bạn là: " + otp + "\n\n"
                    + "Mã này có hiệu lực trong vòng 15 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n"
                    + "Trân trọng,\nBan Tuyển sinh.");
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("[EmailService] Lỗi gửi email: " + e.getMessage());
            return false;
        }
    }

    // 1. Gửi mail Đậu
    public void sendPassEmail(String toEmail, String tenThiSinh, String tenNganh) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("hethongtuyensinh.sgu@gmail.com");
            msg.setTo(toEmail);
            msg.setSubject("THÔNG BÁO TRÚNG TUYỂN ĐẠI HỌC - SGU");
            msg.setText("Chào bạn " + tenThiSinh + ",\n\n"
                    + "Chúc mừng bạn đã TRÚNG TUYỂN vào ngành: " + tenNganh + ".\n"
                    + "Vui lòng đăng nhập vào hệ thống để tải Giấy báo và Xác nhận nhập học trực tuyến.\n\n"
                    + "Trân trọng,\nBan Tuyển sinh SGU.");
            mailSender.send(msg);
        } catch (Exception e) { System.err.println("Lỗi gửi mail: " + e.getMessage()); }
    }

    // 2. Gửi mail Rớt
    public void sendFailEmail(String toEmail, String tenThiSinh) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("hethongtuyensinh.sgu@gmail.com");
            msg.setTo(toEmail);
            msg.setSubject("THÔNG BÁO KẾT QUẢ XÉT TUYỂN - SGU");
            msg.setText("Chào bạn " + tenThiSinh + ",\n\n"
                    + "Hệ thống đã có kết quả xét tuyển đợt này. Rất tiếc điểm của bạn chưa đủ điều kiện trúng tuyển vào các nguyện vọng đã đăng ký.\n"
                    + "Chúc bạn may mắn ở các đợt xét tuyển sau.\n\n"
                    + "Trân trọng,\nBan Tuyển sinh SGU.");
            mailSender.send(msg);
        } catch (Exception e) { System.err.println("Lỗi gửi mail: " + e.getMessage()); }
    }

    // 3. Gửi mail Xác nhận nhập học thành công
    public void sendConfirmSuccessEmail(String toEmail, String tenThiSinh, String tenNganh) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("hethongtuyensinh.sgu@gmail.com");
            msg.setTo(toEmail);
            msg.setSubject("XÁC NHẬN NHẬP HỌC THÀNH CÔNG - SGU");
            msg.setText("Chào bạn " + tenThiSinh + ",\n\n"
                    + "Hệ thống đã ghi nhận bạn XÁC NHẬN NHẬP HỌC thành công vào ngành: " + tenNganh + ".\n"
                    + "Vui lòng chuẩn bị hồ sơ giấy tờ để nộp trực tiếp tại trường theo đúng thời gian quy định.\n\n"
                    + "Trân trọng,\nBan Tuyển sinh SGU.");
            mailSender.send(msg);
        } catch (Exception e) { System.err.println("Lỗi gửi mail: " + e.getMessage()); }
    }

}