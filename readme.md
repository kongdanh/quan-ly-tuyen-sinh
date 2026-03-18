# 🎓 Phần Mềm Quản Lý Tuyển Sinh

> Ứng dụng desktop Java Swing dành cho Admin quản lý toàn bộ quy trình tuyển sinh đại học: từ quản lý thí sinh, điểm thi, nguyện vọng đến xét tuyển tự động.

---

## 📋 Mục Lục

- [Tổng Quan](#tổng-quan)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Cơ Sở Dữ Liệu](#cơ-sở-dữ-liệu)
- [Chức Năng](#chức-năng)
- [Cài Đặt & Chạy](#cài-đặt--chạy)
- [Hướng Dẫn Sử Dụng](#hướng-dẫn-sử-dụng)
- [Tài Khoản Mặc Định](#tài-khoản-mặc-định)

---

## 🗺 Tổng Quan

Hệ thống quản lý tuyển sinh hỗ trợ đầy đủ quy trình:

```
Import thí sinh → Nhập/Import điểm → Đăng ký nguyện vọng → Xét tuyển tự động → Kết quả
```

**Luồng xét tuyển:**
1. Admin import danh sách thí sinh và điểm thi (THPT / VSAT / ĐGNL)
2. Hệ thống tra bảng quy đổi để chuẩn hóa điểm về cùng thang
3. Cộng điểm ưu tiên (khu vực, đối tượng)
4. So sánh tổng điểm theo từng tổ hợp môn với điểm chuẩn từng ngành
5. Xét theo thứ tự ưu tiên nguyện vọng (NV1 > NV2 > ...)
6. Xuất kết quả: TRÚNG TUYỂN / KHÔNG ĐẠT / CHỜ

---

## ⚙️ Công Nghệ Sử Dụng

| Lớp | Công Nghệ | Phiên Bản | Ghi Chú |
|-----|-----------|-----------|---------|
| Giao diện | Java Swing | JDK 17+ | JFrame, JTable, JDialog, JPanel |
| ORM | Hibernate | 6.4.x | Mapping Entity ↔ MySQL Table |
| Database | MySQL | 8.0+ | InnoDB, UTF8MB4 |
| Build | Maven | 3.8+ | Quản lý dependency |
| Import file | Apache POI | 5.2.x | Đọc Excel .xlsx / .xls |
| Bảo mật | jBCrypt | 0.4 | Hash mật khẩu |
| Biểu đồ | JFreeChart | 1.5.x | Thống kê điểm |
| Kết nối DB | HikariCP | 5.x | Connection pooling |

### Dependency chính (pom.xml)

```xml
<!-- Hibernate + MySQL -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.4.4.Final</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>

<!-- Connection Pool -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>

<!-- Apache POI (Excel) -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>

<!-- BCrypt -->
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>

<!-- JFreeChart -->
<dependency>
    <groupId>org.jfree</groupId>
    <artifactId>jfreechart</artifactId>
    <version>1.5.4</version>
</dependency>
```

---

## 💻 Yêu Cầu Hệ Thống

- **Java:** JDK 17 trở lên
- **Maven:** 3.8+
- **MySQL:** 8.0+
- **RAM:** Tối thiểu 512MB
- **OS:** Windows 10/11, Linux, macOS

---

## 📁 Cấu Trúc Dự Án

```
tuyen-sinh/
│
├── pom.xml                                  ← Maven build config
├── README.md
├── database.sql                             ← Script tạo DB + dữ liệu mẫu
│
└── src/
    └── main/
        ├── java/
        │   └── com/tuyen_sinh/
        │       │
        │       ├── Main.java                ← Entry point
        │       │
        │       ├── model/                   ← Entity Hibernate (@Entity)
        │       │   ├── User.java
        │       │   ├── ThiSinh.java
        │       │   ├── Nganh.java
        │       │   ├── ToHopMon.java
        │       │   ├── NganhToHop.java
        │       │   ├── DiemThiSinh.java
        │       │   ├── DiemCong.java
        │       │   ├── NguyenVong.java
        │       │   └── BangQuyDoi.java
        │       │
        │       ├── dao/                     ← Data Access Object
        │       │   ├── GenericDAO.java      ← Base DAO (CRUD chung)
        │       │   ├── UserDAO.java
        │       │   ├── ThiSinhDAO.java      ← + tìm kiếm, phân trang
        │       │   ├── NganhDAO.java
        │       │   ├── ToHopMonDAO.java
        │       │   ├── NganhToHopDAO.java
        │       │   ├── DiemThiSinhDAO.java  ← + thống kê
        │       │   ├── DiemCongDAO.java
        │       │   ├── NguyenVongDAO.java
        │       │   └── BangQuyDoiDAO.java
        │       │
        │       ├── service/                 ← Business Logic
        │       │   ├── AuthService.java     ← Đăng nhập, phân quyền
        │       │   ├── ThiSinhService.java
        │       │   ├── DiemService.java     ← Tính điểm, quy đổi
        │       │   ├── XetTuyenService.java ← Thuật toán xét tuyển
        │       │   └── ImportService.java   ← Đọc và validate Excel
        │       │
        │       ├── ui/                      ← Giao diện Swing
        │       │   ├── MainFrame.java       ← Cửa sổ chính + menu
        │       │   ├── LoginForm.java       ← Màn hình đăng nhập
        │       │   └── panels/
        │       │       ├── UserPanel.java
        │       │       ├── ThiSinhPanel.java
        │       │       ├── NganhPanel.java
        │       │       ├── ToHopMonPanel.java
        │       │       ├── NganhToHopPanel.java
        │       │       ├── DiemThiSinhPanel.java
        │       │       ├── DiemCongPanel.java
        │       │       ├── NguyenVongPanel.java
        │       │       ├── BangQuyDoiPanel.java
        │       │       └── ThongKePanel.java ← Biểu đồ JFreeChart
        │       │
        │       └── util/
        │           ├── HibernateUtil.java   ← SessionFactory singleton
        │           ├── ExcelImporter.java   ← Apache POI helper
        │           ├── PasswordUtil.java    ← BCrypt wrapper
        │           ├── PaginationHelper.java← Phân trang 20 row/page
        │           └── Constants.java       ← Hằng số toàn cục
        │
        └── resources/
            ├── hibernate.cfg.xml            ← Cấu hình Hibernate + MySQL
            └── assets/
                └── icon.png
```

---

## 🗄 Cơ Sở Dữ Liệu

### Sơ Đồ Quan Hệ (ERD)

```
users
  └── (quản lý hệ thống)

thi_sinh (1) ─────────────────────── (N) diem_thi_sinh
    │                                        (loai_diem: THPT/VSAT/DGNL)
    ├── (1) ────────────────────────── (N) diem_cong
    └── (1) ────────────────────────── (N) nguyen_vong
                                                │
nganh (1) ──── (N) nganh_to_hop (N) ──── (1) to_hop_mon
                        │
                (nguyen_vong FK → nganh_to_hop)

bang_quy_doi  (độc lập - bảng tra cứu quy đổi điểm)
```

### Mô Tả Bảng

| Bảng | Mô Tả |
|------|--------|
| `users` | Tài khoản đăng nhập, phân quyền ADMIN/USER |
| `thi_sinh` | Thông tin thí sinh, CCCD là định danh duy nhất |
| `nganh` | Danh sách ngành tuyển sinh + chỉ tiêu |
| `to_hop_mon` | Tổ hợp môn xét tuyển (VD: A00 = Toán+Lý+Hóa) |
| `nganh_to_hop` | Liên kết ngành ↔ tổ hợp kèm điểm chuẩn |
| `diem_thi_sinh` | Điểm từng môn, chia theo loại đề thi |
| `diem_cong` | Điểm ưu tiên (khu vực, đối tượng chính sách) |
| `nguyen_vong` | Đăng ký nguyện vọng + kết quả xét tuyển |
| `bang_quy_doi` | Bảng quy đổi điểm giữa các thang điểm |

---

## 🔧 Chức Năng

### 1. Quản Lý Người Dùng
- Xem danh sách, thêm/sửa/xóa user
- Đổi mật khẩu (hash BCrypt)
- Phân quyền: ADMIN (toàn quyền) / USER (chỉ xem)
- Enable / Disable tài khoản

### 2. Quản Lý Thí Sinh
- Import từ file Excel (.xlsx)
- Xem danh sách **phân trang 20 dòng/trang**
- Tìm kiếm theo CCCD hoặc Họ Tên (real-time)
- Thêm / Sửa / Xóa thông tin

### 3. Quản Lý Ngành Tuyển Sinh
- Import danh sách từ Excel
- CRUD đầy đủ: mã ngành, tên ngành, chỉ tiêu

### 4. Quản Lý Tổ Hợp Môn
- Định nghĩa các tổ hợp môn (A00, B00, C00, D01, ...)
- CRUD đầy đủ

### 5. Quản Lý Ngành - Tổ Hợp
- Liên kết ngành với các tổ hợp xét tuyển
- Đặt điểm chuẩn cho từng cặp ngành-tổ hợp

### 6. Quản Lý Điểm Thi Sinh
- Import điểm theo 3 loại: **THPT / VSAT / ĐGNL**
- CRUD điểm từng môn
- **Thống kê:** biểu đồ phân phối điểm theo loại/môn (JFreeChart)

### 7. Quản Lý Điểm Cộng
- Import và quản lý điểm ưu tiên khu vực, đối tượng
- CRUD đầy đủ

### 8. Quản Lý Nguyện Vọng & Xét Tuyển
- Xem danh sách đăng ký nguyện vọng
- **Chạy xét tuyển tự động:**
  1. Tính tổng điểm = Điểm tổ hợp + Điểm cộng (sau quy đổi)
  2. Xét NV1 trước, nếu không đủ → xét NV2, ...
  3. Cập nhật kết quả: TRÚNG TUYỂN / KHÔNG ĐẠT / CHỜ

### 9. Quản Lý Bảng Quy Đổi
- Import bảng quy đổi điểm
- CRUD + tìm kiếm
- Dùng để chuẩn hóa điểm VSAT/ĐGNL về thang THPT

---

## 🚀 Cài Đặt & Chạy

### Bước 1: Tạo Database

```bash
mysql -u root -p < database.sql
```

Hoặc mở MySQL Workbench / DBeaver và chạy file `database.sql`.

### Bước 2: Cấu Hình Kết Nối

Chỉnh sửa `src/main/resources/hibernate.cfg.xml`:

```xml
<property name="hibernate.connection.url">
    jdbc:mysql://localhost:3306/tuyen_sinh?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">YOUR_PASSWORD</property>
```

### Bước 3: Build & Chạy

```bash
# Build
mvn clean package

# Chạy
java -jar target/tuyen-sinh-1.0.jar
```

Hoặc mở project bằng **IntelliJ IDEA / Eclipse** và chạy `Main.java`.

---

## 📖 Hướng Dẫn Sử Dụng

### Đăng Nhập
Mở ứng dụng → nhập tài khoản → chọn module từ menu bên trái.

### Import Dữ Liệu từ Excel
1. Chọn module cần import (ThíSinh / Điểm / Ngành...)
2. Nhấn nút **"Import Excel"**
3. Chọn file `.xlsx`
4. Xem preview → xác nhận import
5. Hệ thống báo số dòng thành công / lỗi

### Chạy Xét Tuyển
1. Vào module **Nguyện Vọng & Xét Tuyển**
2. Đảm bảo đã có đầy đủ: điểm thi, nguyện vọng, điểm chuẩn, bảng quy đổi
3. Nhấn **"Chạy Xét Tuyển"**
4. Xem kết quả theo từng thí sinh

---

## 🔐 Tài Khoản Mặc Định

| Tài Khoản | Mật Khẩu | Quyền |
|-----------|----------|-------|
| `admin` | `Admin@123` | ADMIN |
| `user1` | `User@123` | USER |

> ⚠️ Vui lòng đổi mật khẩu sau lần đăng nhập đầu tiên.

---

## 📝 Ghi Chú Kỹ Thuật

- **Phân trang:** Dùng Hibernate `setFirstResult((page-1)*20).setMaxResults(20)` — không load toàn bộ dữ liệu vào RAM.
- **Tìm kiếm:** Dùng HQL `LIKE :keyword` với index trên cột `cccd` và `ho_ten`.
- **Import Excel:** Validate từng dòng trước khi commit — lỗi 1 dòng không làm hỏng cả batch.
- **Xét tuyển:** Chạy trong transaction riêng, rollback nếu có lỗi giữa chừng.
- **Mật khẩu:** BCrypt với cost factor 12 — không lưu plaintext trong DB.