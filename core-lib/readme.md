# Module Core-Lib

Module này là trái tim của hệ thống, chứa toàn bộ logic xử lý dữ liệu và nghiệp vụ cốt lõi. Đây là thư viện dùng chung cho các ứng dụng đầu cuối (Admin và Web).

## Thành phần chính
*   **Mô hình dữ liệu (Entities):** Sử dụng Hibernate ORM để ánh xạ các bảng MySQL thành các đối tượng Java (Nganh, ThiSinh, HoSo, ...).
*   **Lớp truy xuất dữ liệu (DAO):** Triển khai Generic DAO để thực hiện các thao tác CRUD và các truy vấn phức tạp.
*   **Lớp nghiệp vụ (Services):**
    *   `XetTuyenService`: Thực thi thuật toán xét tuyển tự động.
    *   `ExcelService`: Xử lý logic import/export dữ liệu từ file Excel.
    *   `ThongKeService`: Cung cấp các số liệu tổng hợp cho báo cáo.
*   **Cấu hình (Util):** Quản lý kết nối cơ sở dữ liệu thông qua Hibernate SessionFactory.

## Công nghệ sử dụng
*   Hibernate 6.4.x
*   MySQL Connector 8.3
*   Apache POI (xử lý Excel)
*   jBCrypt (mã hóa mật khẩu)
*   Lombok (giảm thiểu code boilerplate)

## Hướng dẫn cấu hình
Toàn bộ thông tin kết nối database được cấu hình tại:
`src/main/resources/hibernate.cfg.xml`

Lưu ý: Luôn chạy lệnh `mvn clean install` tại thư mục này mỗi khi thay đổi logic nghiệp vụ để các module khác nhận được bản cập nhật mới nhất.
