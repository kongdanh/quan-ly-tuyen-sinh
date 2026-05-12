# Module Student-Web

Cổng thông tin trực tuyến dành cho Thí sinh, cung cấp trải nghiệm tra cứu thông tin cá nhân và kết quả tuyển sinh một cách minh bạch và tiện lợi.

## Tính năng chính
*   **Tra cứu kết quả:** Thí sinh sử dụng CCCD để đăng nhập và xem kết quả trúng tuyển theo thời gian thực.
*   **Chi tiết hồ sơ:** Hiển thị điểm thi tất cả các phương thức (THPT, ĐGNL, VSAT) đã được cán bộ phê duyệt.
*   **Trạng thái nguyện vọng:** Theo dõi danh sách nguyện vọng đã đăng ký và kết quả xét tuyển tương ứng (Trúng tuyển, Chờ xét, ...).
*   **Giao diện đáp ứng:** Thiết kế Responsive, tương thích tốt trên cả máy tính và điện thoại thông minh.

## Công nghệ sử dụng
*   **Spring Boot 3.2.x:** Framework mạnh mẽ cho việc xây dựng ứng dụng Web.
*   **Thymeleaf:** Template Engine để render giao diện phía Server.
*   **Bootstrap 5:** Xây dựng giao diện Web hiện đại, nhanh chóng.
*   **Spring Security:** (Nếu có) Bảo mật hệ thống và quản lý phiên đăng nhập.

## Hướng dẫn khởi chạy
Module này được cấu hình để chạy độc lập như một ứng dụng Spring Boot.
1.  Mở Terminal tại thư mục `student-web`.
2.  Chạy lệnh:
    ```bash
    mvn spring-boot:run
    ```
3.  Truy cập qua trình duyệt tại địa chỉ: `http://localhost:8080/`
