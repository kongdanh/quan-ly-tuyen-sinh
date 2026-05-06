-- ============================================================
-- PATCH: Bổ sung ràng buộc và bảng còn thiếu
-- Chạy file này SAU KHI đã import xettuyen2026_empty.sql thành công
-- 
-- MySQL 8.x compatible
-- Ngày tạo: 2026-04-22
-- ============================================================

USE `xettuyen2026`;

-- ============================================================
-- 1. SỬA BẢNG xt_bangquydoi
--    Vấn đề: thiếu AUTO_INCREMENT, PRIMARY KEY, UNIQUE KEY
-- ============================================================

-- Bước 1a: Đặt lại kiểu cột về INT AUTO_INCREMENT (phù hợp với @GeneratedValue IDENTITY)
ALTER TABLE `xt_bangquydoi`
  MODIFY COLUMN `idqd` INT NOT NULL AUTO_INCREMENT,
  ADD PRIMARY KEY (`idqd`),
  ADD UNIQUE KEY `d_maquydoi_UNIQUE` (`d_maquydoi`);

-- Bước 1b: Cập nhật AUTO_INCREMENT để tránh conflict với dữ liệu đã insert
-- (dữ liệu hiện tại có idqd tối đa = 94)
ALTER TABLE `xt_bangquydoi` AUTO_INCREMENT = 100;


-- ============================================================
-- 2. SỬA BẢNG xt_diemcongxetuyen
--    Vấn đề: thiếu AUTO_INCREMENT, PRIMARY KEY, UNIQUE KEY
-- ============================================================

ALTER TABLE `xt_diemcongxetuyen`
  MODIFY COLUMN `iddiemcong` INT NOT NULL AUTO_INCREMENT,
  ADD PRIMARY KEY (`iddiemcong`),
  ADD UNIQUE KEY `dc_keys_UNIQUE` (`dc_keys`),
  ADD KEY `fk_diemcong_thisinh` (`ts_cccd`);

-- Cập nhật AUTO_INCREMENT (dữ liệu hiện tại có thể lên đến vài nghìn)
ALTER TABLE `xt_diemcongxetuyen` AUTO_INCREMENT = 10000;


-- ============================================================
-- 3. SỬA BẢNG xt_nganh_tohop
--    Vấn đề: thiếu PRIMARY KEY, UNIQUE KEY
-- ============================================================

ALTER TABLE `xt_nganh_tohop`
  MODIFY COLUMN `id` INT NOT NULL AUTO_INCREMENT,
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `key_UNIQUE` (`tb_keys`);

-- Cập nhật AUTO_INCREMENT (dữ liệu hiện tại có id tối đa khoảng 1411+)
ALTER TABLE `xt_nganh_tohop` AUTO_INCREMENT = 2000;


-- ============================================================
-- 4. THÊM BẢNG xt_yeucau_capnhat (hoàn toàn thiếu trong file gốc)
--    Dùng cho chức năng thí sinh yêu cầu cập nhật hồ sơ
-- ============================================================

CREATE TABLE IF NOT EXISTS `xt_yeucau_capnhat` (
  `id`             INT          NOT NULL AUTO_INCREMENT,
  `cccd`           VARCHAR(20)  NOT NULL,
  `dien_thoai`     VARCHAR(20)  DEFAULT NULL,
  `email`          VARCHAR(100) DEFAULT NULL,
  `noi_sinh`       VARCHAR(100) DEFAULT NULL,
  `khu_vuc`        VARCHAR(45)  DEFAULT NULL,
  `doi_tuong`      VARCHAR(45)  DEFAULT NULL,
  `minh_chung_url` VARCHAR(255) DEFAULT NULL,
  `trang_thai`     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  `ngay_tao`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_yeucau_thisinh`
    FOREIGN KEY (`cccd`) REFERENCES `xt_thisinhxettuyen25`(`cccd`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
-- 5. KIỂM TRA KẾT QUẢ SAU KHI CHẠY PATCH
-- ============================================================

SELECT 'xt_bangquydoi'       AS bang, COUNT(*) AS so_luong FROM xt_bangquydoi
UNION ALL SELECT 'xt_diemcongxetuyen', COUNT(*) FROM xt_diemcongxetuyen
UNION ALL SELECT 'xt_nganh_tohop',     COUNT(*) FROM xt_nganh_tohop
UNION ALL SELECT 'xt_yeucau_capnhat',  COUNT(*) FROM xt_yeucau_capnhat;

-- Kiểm tra PRIMARY KEY đã được thêm chưa
SELECT TABLE_NAME, CONSTRAINT_NAME, CONSTRAINT_TYPE
FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'xettuyen2026'
  AND TABLE_NAME IN ('xt_bangquydoi', 'xt_diemcongxetuyen', 'xt_nganh_tohop', 'xt_yeucau_capnhat')
ORDER BY TABLE_NAME, CONSTRAINT_TYPE;
