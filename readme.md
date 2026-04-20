# 🎓 Hệ Thống Quản Lý Tuyển Sinh Đại Học 2026

<<<<<<< HEAD
> Hệ thống phần mềm toàn diện dành cho quy trình tuyển sinh đại học, được xây dựng theo kiến trúc Maven Multi-module. Cung cấp giải pháp quản lý tập trung cho Cán bộ (Desktop App) và cổng tra cứu trực tuyến cho Thí sinh (Web Portal).
=======
> Đồ án môn **Mô hình Phân lớp** — Trường Đại học Sài Gòn
>>>>>>> f8f8949dc8616fb56e6f3483d5cdf1943b2afa92

---

## 📌 Mô tả đề tài

<<<<<<< HEAD
- [Tổng Quan](#tổng-quan)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Cơ Sở Dữ Liệu](#cơ-sở-dữ-liệu)
- [Chức Năng Chính](#chức-năng-chính)
- [Cài Đặt & Khởi Chạy](#cài-đặt--khởi-chạy)
- [Tài Khoản Mặc Định](#tài-khoản-mặc-định)
=======
Ứng dụng desktop hỗ trợ quản lý toàn bộ quy trình xét tuyển đại học, bao gồm quản lý thí sinh, điểm thi, nguyện vọng và thực hiện xét tuyển tự động theo đúng quy chế của Bộ GD&ĐT — hỗ trợ 3 phương thức: **THPT**, **V-SAT** và **ĐGNL**.
>>>>>>> f8f8949dc8616fb56e6f3483d5cdf1943b2afa92

---

## 👥 Thông tin nhóm thực hiện: Nhóm 9

<<<<<<< HEAD
Hệ thống hỗ trợ tự động hóa toàn bộ quy trình xét tuyển:

`Import Hồ Sơ/Điểm` → `Đăng Ký Nguyện Vọng` → `Thuật Toán Xét Tuyển` → `Công Bố Kết Quả`

**Quy trình xét tuyển tự động:**
1. Chuẩn hóa các loại điểm (THPT, VSAT, ĐGNL) về cùng một thang đo dựa trên Bảng quy đổi.
2. Cộng điểm ưu tiên khu vực và đối tượng chính sách.
3. So sánh tổng điểm tổ hợp với điểm chuẩn của từng ngành.
4. Lọc trúng tuyển theo thứ tự ưu tiên của nguyện vọng (NV1 > NV2 > ...).
5. Đồng bộ kết quả lên Web cho thí sinh tra cứu.
=======
| Họ và tên | MSSV |
|-----------|------|
| Nguyễn Trần Công Danh | 3123410046 |
| | |
| | |
| | |
| | |
| | |
>>>>>>> f8f8949dc8616fb56e6f3483d5cdf1943b2afa92

---

## ⚙️ Công nghệ sử dụng

<<<<<<< HEAD
Kiến trúc 3 lớp (3-Tier) chia sẻ chung một Core nghiệp vụ cho nhiều nền tảng.

| Thành Phần | Công Nghệ Sử Dụng |
| :--- | :--- |
| **Lõi xử lý nghiệp vụ (Core)** | Java 17, Hibernate 6.4.x |
| **Ứng dụng quản lý (Admin)** | Java Swing, FlatLaf |
| **Cổng thông tin (Web)** | Servlet API 4.0, JSP, HTML/CSS |
| **Hệ quản trị CSDL** | MySQL 8.0+ |
| **Web Server** | Jetty 10.x |
| **Tiện ích xử lý** | Apache POI, jBCrypt, JFreeChart |
=======
| Thành phần | Công nghệ | Phiên bản |
|-----------|-----------|-----------|
| Ngôn ngữ | Java | 17 |
| Giao diện | Java Swing | JDK 17 |
| ORM | Hibernate | 6.4.4 |
| Database | MySQL | 8.0 |
| Build tool | Maven | 3.8+ |
| Import Excel | Apache POI | 5.2.5 |
| Mã hóa mật khẩu | jBCrypt | 0.4 |
| Biểu đồ | JFreeChart | 1.5.4 |
| Connection pool | HikariCP | 5.1.0 |

---

## 🗄️ Cơ sở dữ liệu

Database: `xettuyen2026`

| Bảng | Mô tả |
|------|-------|
| `xt_nganh` | Danh sách ngành tuyển sinh và chỉ tiêu |
| `xt_tohop_monthi` | Tổ hợp môn xét tuyển |
| `xt_nganh_tohop` | Liên kết ngành — tổ hợp, hệ số môn, độ lệch |
| `xt_thisinhxettuyen25` | Thông tin thí sinh |
| `xt_diemthixettuyen` | Điểm thi theo từng môn (THPT / V-SAT / ĐGNL) |
| `xt_diemcongxetuyen` | Điểm cộng (chứng chỉ TA, giải HSG, khu vực, đối tượng) |
| `xt_nguyenvongxettuyen` | Nguyện vọng và kết quả xét tuyển |
| `xt_bangquydoi` | Bảng quy đổi điểm V-SAT / ĐGNL sang thang THPT |
>>>>>>> f8f8949dc8616fb56e6f3483d5cdf1943b2afa92

---

## 📁 Cấu trúc dự án

```text
tuyen-sinh/
<<<<<<< HEAD
├── core-lib/                  ← MODULE 1: Database & Logic (DAL & BLL)
│   ├── src/main/java/...
│   │   ├── model/             ← Ánh xạ Entity Hibernate
│   │   ├── dao/               ← Truy vấn CSDL
│   │   ├── service/           ← Thuật toán xét tuyển, Import logic
│   │   └── util/              ← Cấu hình Hibernate, Utils
│   └── src/main/resources/    ← File cấu hình hibernate.cfg.xml
│
├── admin-app/                 ← MODULE 2: Giao diện Cán bộ (Presentation)
│   └── src/main/java/...
│       ├── Main.java          ← Entry point Desktop
│       └── ui/                ← Forms, Panels, Components (Swing)
│
└── student-web/               ← MODULE 3: Giao diện Thí sinh (Presentation)
    └── src/main/
        ├── java/...           ← Controllers (Servlet), Filters
        └── webapp/            
            ├── WEB-INF/       ← Cấu hình web.xml
            └── views/         ← Giao diện .jsp
=======
├── pom.xml
├── database.sql
├── doc/                        # Tài liệu tham khảo
└── src/main/java/com/tuyensinh/
    ├── Main.java
    ├── model/                  # Entity — ánh xạ bảng DB
    ├── dao/                    # Truy vấn dữ liệu (Hibernate)
    ├── service/                # Xử lý nghiệp vụ
    ├── ui/
    │   ├── LoginForm.java
    │   ├── MainFrame.java
    │   └── panels/             # Giao diện từng chức năng
    └── util/                   # Tiện ích dùng chung
>>>>>>> f8f8949dc8616fb56e6f3483d5cdf1943b2afa92
```

---

## 🔧 Chức năng

<<<<<<< HEAD
### Sơ Đồ Quan Hệ (ERD) Lõi

```text
xt_users (Cán bộ)
xt_thisinh_account (Tài khoản thí sinh)

xt_thisinh (1) ──── (N) xt_diemthixettuyen (THPT/VSAT/DGNL)
    │
    ├── (1) ─────── (N) xt_diemcongxetuyen
    └── (1) ─────── (N) xt_nguyenvongxettuyen
                                │
xt_nganh (1) ── (N) xt_nganh_tohop (N) ── (1) xt_tohopmon
                        │
                (nguyen_vong FK → nganh_to_hop)

xt_bangquydoi (Tra cứu chuẩn hóa điểm)
```

---

## 🔧 Chức Năng Chính

### 👨‍💻 Phân hệ Cán Bộ Quản Lý (Admin App)
* **Quản lý Danh mục:** Khởi tạo ngành học, tổ hợp môn, định mức điểm chuẩn.
* **Import Dữ liệu Tốc độ cao:** Nhập hàng loạt hồ sơ, điểm thi, bảng quy đổi từ file Excel (`.xlsx`). Hỗ trợ validate dữ liệu trước khi lưu.
* **Động cơ Xét tuyển:** Chạy thuật toán tự động phân bổ trúng tuyển theo chỉ tiêu và độ ưu tiên nguyện vọng.
* **Báo cáo Thống kê:** Hiển thị biểu đồ phổ điểm, tình trạng tuyển sinh trực quan bằng JFreeChart.
* **Quản trị Hệ thống:** Quản lý tài khoản, phân quyền (Admin/User).

### 🎓 Phân hệ Thí Sinh (Student Web)
* **Xác thực:** Đăng nhập an toàn bằng mã định danh (CCCD).
* **Tra cứu Cá nhân:** Xem thông tin hồ sơ, điểm tổng hợp các môn.
* **Tra cứu Kết quả:** Xem trạng thái trúng tuyển của các nguyện vọng đăng ký theo thời gian thực.

---

## 🚀 Cài Đặt & Khởi Chạy

### Bước 1: Khởi tạo Database
Chạy file script `database.sql` (hoặc file `.sql` tương ứng trong thư mục `doc/`) bằng MySQL Workbench hoặc Terminal.

### Bước 2: Cấu hình kết nối
Mở file `core-lib/src/main/resources/hibernate.cfg.xml` và điền mật khẩu Database của bạn:
```xml
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">MAT_KHAU_CUA_BAN</property>
```

### Bước 3: Biên dịch hệ thống
Mở Terminal tại thư mục gốc của dự án (`tuyen-sinh/`) và chạy lệnh:
```bash
mvn clean install -DskipTests
```

### Bước 4: Khởi chạy

**🖥 Dành cho Admin (Desktop):**
Mở IDE, đi tới module `admin-app` và Run file `Main.java`.

**🌐 Dành cho Thí sinh (Web):**
Mở Terminal, di chuyển vào thư mục Web và bật Server:
```bash
cd student-web
mvn jetty:run
```
Truy cập trình duyệt tại: `http://localhost:8080/`

---

## 🔐 Tài Khoản Mặc Định

| Hệ thống | Tài khoản | Mật khẩu | Quyền |
| :--- | :--- | :--- | :--- |
| **Admin App** | `admin` | `Admin@123` | Quản trị viên |
| **Web Thí Sinh** | *(Nhập CCCD có trong DB)* | `123456` | Thí sinh |
```
=======
| # | Chức năng | Mô tả |
|---|-----------|-------|
| 1 | Quản lý ngành | CRUD ngành tuyển sinh, chỉ tiêu, ngưỡng đầu vào |
| 2 | Quản lý tổ hợp môn | CRUD tổ hợp, hệ số môn chính |
| 3 | Quản lý ngành — tổ hợp | Liên kết ngành với tổ hợp, độ lệch điểm |
| 4 | Quản lý thí sinh | Import Excel, tìm kiếm, phân trang 20 dòng/trang |
| 5 | Quản lý điểm thi | Import điểm THPT / V-SAT / ĐGNL |
| 6 | Quản lý điểm cộng | Chứng chỉ TA, giải HSG, khu vực, đối tượng |
| 7 | Nguyện vọng & Xét tuyển | Đăng ký NV, chạy xét tuyển tự động theo thứ tự ưu tiên |
| 8 | Bảng quy đổi | Quản lý bảng quy đổi V-SAT / ĐGNL theo bách phân vị |
| 9 | Thống kê | Biểu đồ phân phối điểm theo môn, phương thức |

---

## 🚀 Cài đặt & Chạy

```bash
# 1. Import database
mysql -u root -p < database.sql

# 2. Cấu hình kết nối
# Sửa username/password trong: src/main/resources/hibernate.cfg.xml

# 3. Build
mvn clean package

# 4. Chạy
java -jar target/tuyensinh-1.0-SNAPSHOT.jar
```

---

## 📐 Kiến trúc phân lớp

```
UI Layer        (Swing — panels/)
      ↓
Service Layer   (Xử lý nghiệp vụ, tính điểm xét tuyển)
      ↓
DAO Layer       (Hibernate — truy vấn DB)
      ↓
Database        (MySQL — xettuyen2026)
```
>>>>>>> f8f8949dc8616fb56e6f3483d5cdf1943b2afa92
