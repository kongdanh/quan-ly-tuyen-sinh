# Module Admin-App

Ứng dụng quản trị dành cho Cán bộ Tuyển sinh với giao diện Desktop hiện đại, tập trung vào hiệu năng xử lý dữ liệu và tính trực quan trong thống kê.

## Tính năng chính
*   **Quản trị danh mục:** Quản lý thông tin Ngành học, Tổ hợp môn thi và cấu hình Quy định tuyển sinh.
*   **Hệ thống Import dữ liệu:** Hỗ trợ nhập hàng loạt hồ sơ, điểm thi (THPT, ĐGNL, VSAT) từ file Excel. Có cơ chế kiểm tra lỗi dữ liệu trước khi nạp vào hệ thống.
*   **Động cơ xét tuyển:** Chạy thuật toán xét tuyển tự động cho toàn bộ thí sinh, xử lý lọc ảo và phân bổ trúng tuyển theo chỉ tiêu.
*   **Dashboard & Thống kê:** Hiển thị biểu đồ phổ điểm, tình hình nộp hồ sơ và tỷ lệ trúng tuyển qua các biểu đồ JFreeChart.

## Công nghệ giao diện
*   **Java Swing:** Nền tảng xây dựng giao diện.
*   **FlatLaf:** Thư viện Look and Feel hiện đại, hỗ trợ chế độ Dark Mode chuyên nghiệp.
*   **FlatSVGIcon:** Đảm bảo các icon luôn sắc nét trên mọi độ phân giải màn hình.
*   **MigLayout:** Quản lý bố cục giao diện linh hoạt và đáp ứng.

## Cách chạy ứng dụng
1.  Đảm bảo module `core-lib` đã được build thành công.
2.  Khởi chạy class `com.tuyensinh.admin.Main` từ công cụ lập trình (IDE) hoặc sử dụng file Jar đã đóng gói.
