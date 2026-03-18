# 🎓 Phần Mềm Quản Lý Tuyển Sinh

> Đồ án môn **Mô hình Phân lớp** — Trường Đại học Sài Gòn

---

## 📌 Mô tả đề tài

Ứng dụng desktop hỗ trợ quản lý toàn bộ quy trình xét tuyển đại học, bao gồm quản lý thí sinh, điểm thi, nguyện vọng và thực hiện xét tuyển tự động theo đúng quy chế của Bộ GD&ĐT — hỗ trợ 3 phương thức: **THPT**, **V-SAT** và **ĐGNL**.

---

## 👥 Thông tin nhóm thực hiện: Nhóm 9

| Họ và tên | MSSV |
|-----------|------|
| Nguyễn Trần Công Danh | 3123410046 |
| | |
| | |
| | |
| | |
| | |

---

## ⚙️ Công nghệ sử dụng

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

---

## 📁 Cấu trúc dự án

```
tuyen-sinh/
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
```

---

## 🔧 Chức năng

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
