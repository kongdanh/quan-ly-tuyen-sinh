# Hệ Thống Quản Lý Tuyển Sinh Đại Học 2026

Hệ thống Quản lý Tuyển sinh 2026 là một giải pháp phần mềm tích hợp, được thiết kế để tự động hóa và tối ưu hóa quy trình xét tuyển đại học. Dự án được phát triển theo kiến trúc Maven Multi-module, đảm bảo khả năng mở rộng, bảo trì và chia sẻ logic nghiệp vụ giữa các nền tảng Desktop và Web.

---

## Mục Lục
- [Tổng Quan Hệ Thống](#tổng-quan-hệ-thống)
- [Kiến Trúc Dự Án](#kiến-trúc-dự-án)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Quy Trình Xét Tuyển](#quy-trình-xét-tuyển)
- [Hướng Dẫn Cài Đặt](#hướng-dẫn-cài-đặt)
- [Tài Khoản Truy Cập](#tài-khoản-truy-cập)

---

## Tổng Quan Hệ Thống

Hệ thống bao gồm hai ứng dụng chính chia sẻ chung một lõi nghiệp vụ:
1.  **Phân hệ Quản lý (Admin App):** Ứng dụng Desktop dành cho cán bộ quản lý tuyển sinh. Hỗ trợ quản lý hồ sơ, điểm thi, và thực hiện thuật toán xét tuyển.
2.  **Phân hệ Thí sinh (Student Web):** Cổng thông tin trực tuyến dành cho thí sinh tra cứu điểm, nguyện vọng và kết quả trúng tuyển.

---

## Kiến Trúc Dự Án

Dự án được chia thành 3 module chính:

*   **[core-lib](./core-lib/):** Chứa mô hình dữ liệu (Entities), lớp truy xuất dữ liệu (DAO) và logic nghiệp vụ chính (Services). Đây là thành phần lõi được sử dụng bởi cả hai phân hệ trên.
*   **[admin-app](./admin-app/):** Giao diện đồ họa (Java Swing) dành cho cán bộ. Tập trung vào các tính năng quản trị, import dữ liệu và thống kê.
*   **[student-web](./student-web/):** Ứng dụng Web (Spring Boot) dành cho thí sinh. Cung cấp giao diện tra cứu hiện đại và bảo mật.

---

## Công Nghệ Sử Dụng

### Cơ sở hạ tầng
*   **Ngôn ngữ:** Java 17
*   **Hệ quản trị CSDL:** MySQL 8.0
*   **Quản lý dự án:** Maven

### Lõi nghiệp vụ (Core)
*   **ORM:** Hibernate 6.4.4.Final
*   **Bảo mật:** jBCrypt
*   **Xử lý Excel:** Apache POI

### Giao diện
*   **Admin App:** Java Swing, FlatLaf, JFreeChart
*   *   **Student Web:** Spring Boot 3.2.x, Thymeleaf, Bootstrap 5

---

## Quy Trình Xét Tuyển

Quy trình xét tuyển được triển khai tự động theo các bước:
1.  **Tiếp nhận:** Cán bộ import hồ sơ và điểm thi (THPT, ĐGNL, VSAT) từ Excel.
2.  **Chuẩn hóa:** Hệ thống tự động quy đổi điểm về thang điểm chung theo quy định.
3.  **Ưu tiên:** Cộng điểm ưu tiên dựa trên khu vực và đối tượng thí sinh.
4.  **Xét duyệt:** Chạy thuật toán lọc ảo, xét trúng tuyển theo thứ tự nguyện vọng và chỉ tiêu từng ngành.
5.  **Công bố:** Kết quả được đồng bộ lên Web để thí sinh tra cứu.

---

## Hướng Dẫn Cài Đặt

### 1. Cấu hình Cơ sở dữ liệu
*   Import file `GeneralData.sql` vào MySQL để khởi tạo các danh mục ngành, tổ hợp và quy định.
*   (Tùy chọn) Import `D1.sql` để có dữ liệu thí sinh mẫu.
*   Chỉnh sửa thông tin kết nối trong `core-lib/src/main/resources/hibernate.cfg.xml`.

### 2. Biên dịch dự án
Tại thư mục gốc, chạy lệnh:
```bash
mvn clean install -DskipTests
```

### 3. Khởi chạy các phân hệ
*   **Admin App:** Mở và chạy class `com.tuyensinh.admin.Main` trong module `admin-app`.
*   **Student Web:** Di chuyển vào thư mục `student-web` và chạy lệnh:
    ```bash
    mvn spring-boot:run
    ```

---

## Tài Khoản Truy Cập

| Đối tượng | Tài khoản | Mật khẩu |
| :--- | :--- | :--- |
| Cán bộ (Admin) | `admin` | `Admin@123` |
| Thí sinh | `CCCD của thí sinh` | `123456` |

---
*Dự án Quản lý Tuyển sinh 2026*