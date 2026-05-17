-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 12, 2026 at 12:18 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `xettuyen2026`
--

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_diem_cong_thisinh`
-- (See below for the actual view)
--
CREATE TABLE `v_diem_cong_thisinh` (
`cccd` varchar(20)
,`ho_ten` varchar(201)
,`tong_diem_cong` decimal(26,2)
,`diem_cong_ap_dung` decimal(26,2)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_thongke_nguyen_vong`
-- (See below for the actual view)
--
CREATE TABLE `v_thongke_nguyen_vong` (
`manganh` varchar(20)
,`tennganh` varchar(200)
,`n_chitieu` int(11)
,`tong_nguyen_vong` bigint(21)
,`so_nv1` decimal(22,0)
,`so_trung_tuyen` decimal(22,0)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_user_quyen`
-- (See below for the actual view)
--
CREATE TABLE `v_user_quyen` (
`id` int(11)
,`username` varchar(50)
,`ho_ten` varchar(150)
,`bo_phan` varchar(100)
,`trang_thai` varchar(20)
,`ma_nhom` varchar(50)
,`ten_nhom` varchar(100)
,`ma_chuc_nang` varchar(50)
,`co_xem` tinyint(1)
,`co_them` tinyint(1)
,`co_sua` tinyint(1)
,`co_xoa` tinyint(1)
,`co_xuat` tinyint(1)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_xettuyen_summary`
-- (See below for the actual view)
--
CREATE TABLE `v_xettuyen_summary` (
`cccd` varchar(20)
,`ho_ten` varchar(201)
,`gioi_tinh` varchar(10)
,`khu_vuc` varchar(45)
,`doi_tuong` varchar(45)
,`nv_manganh` varchar(45)
,`tennganh` varchar(200)
,`thu_tu_NV` int(11)
,`diem_xettuyen` double
,`diem_san` decimal(10,2)
,`nv_ketqua` varchar(45)
,`tt_phuongthuc` varchar(45)
);

-- --------------------------------------------------------

--
-- Table structure for table `xt_bangquydoi`
--

CREATE TABLE `xt_bangquydoi` (
  `idqd` bigint(20) NOT NULL,
  `d_phuongthuc` varchar(45) DEFAULT NULL,
  `d_tohop` varchar(45) DEFAULT NULL,
  `d_mon` varchar(45) DEFAULT NULL,
  `d_diema` decimal(6,2) DEFAULT NULL,
  `d_diemb` decimal(6,2) DEFAULT NULL,
  `d_diemc` decimal(6,2) DEFAULT NULL,
  `d_diemd` decimal(6,2) DEFAULT NULL,
  `d_maquydoi` varchar(45) DEFAULT NULL,
  `d_phanvi` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_diemcongxetuyen`
--

CREATE TABLE `xt_diemcongxetuyen` (
  `iddiemcong` int(11) NOT NULL,
  `ts_cccd` varchar(20) NOT NULL,
  `manganh` varchar(45) DEFAULT NULL,
  `matohop` varchar(45) DEFAULT NULL,
  `phuongthuc` varchar(10) DEFAULT NULL,
  `diemCC` decimal(4,2) DEFAULT NULL,
  `diemUtxt` decimal(4,2) DEFAULT NULL,
  `diemTong` decimal(4,2) DEFAULT NULL,
  `ghichu` varchar(200) DEFAULT NULL,
  `dc_keys` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_diemthixettuyen`
--

CREATE TABLE `xt_diemthixettuyen` (
  `iddiemthi` int(11) NOT NULL,
  `cccd` varchar(20) NOT NULL,
  `sobaodanh` varchar(45) DEFAULT NULL,
  `d_phuongthuc` varchar(10) DEFAULT NULL,
  `TO` decimal(8,2) DEFAULT 0.00,
  `LI` decimal(8,2) DEFAULT 0.00,
  `HO` decimal(8,2) DEFAULT 0.00,
  `SI` decimal(8,2) DEFAULT 0.00,
  `SU` decimal(8,2) DEFAULT 0.00,
  `DI` decimal(8,2) DEFAULT 0.00,
  `VA` decimal(8,2) DEFAULT 0.00,
  `N1_THI` decimal(8,2) DEFAULT NULL,
  `N1_CC` decimal(8,2) DEFAULT 0.00,
  `CNCN` decimal(8,2) DEFAULT 0.00,
  `CNNN` decimal(8,2) DEFAULT 0.00,
  `TI` decimal(8,2) DEFAULT 0.00,
  `KTPL` decimal(8,2) DEFAULT 0.00,
  `NL1` decimal(8,2) DEFAULT NULL,
  `NK1` decimal(8,2) DEFAULT NULL,
  `NK2` decimal(8,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_diem_chuan_dot`
--

CREATE TABLE `xt_diem_chuan_dot` (
  `id` int(11) NOT NULL,
  `id_dot` int(11) NOT NULL,
  `id_nganh_tohop` int(11) NOT NULL,
  `diem_chuan` decimal(38,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_dot_tuyen_sinh`
--

CREATE TABLE `xt_dot_tuyen_sinh` (
  `id` int(11) NOT NULL,
  `ten_dot` varchar(255) NOT NULL,
  `ngay_bat_dau` datetime NOT NULL,
  `ngay_ket_thuc` datetime NOT NULL,
  `ngay_cong_bo` datetime DEFAULT NULL,
  `trang_thai` varchar(50) DEFAULT 'MO_CONG',
  `ngay_tao` datetime DEFAULT current_timestamp(),
  `ma_phuong_thuc` varchar(20) DEFAULT 'THPT' COMMENT 'THPT, DGNL, VSAT, HOCBA'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_ho_so_tuyen_sinh`
--

CREATE TABLE `xt_ho_so_tuyen_sinh` (
  `id` int(11) NOT NULL,
  `id_thi_sinh` int(11) NOT NULL,
  `id_dot_tuyen_sinh` int(11) NOT NULL,
  `ma_ho_so` varchar(50) NOT NULL,
  `tong_diem_xet_tuyen` double DEFAULT 0,
  `trang_thai` varchar(50) DEFAULT 'CHO_XET',
  `ngay_nop` datetime DEFAULT current_timestamp(),
  `diemCong` double DEFAULT NULL,
  `diemThi` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_ket_qua_xet_tuyen`
--

CREATE TABLE `xt_ket_qua_xet_tuyen` (
  `id` int(11) NOT NULL,
  `id_ho_so` int(11) NOT NULL,
  `id_nganh` int(11) NOT NULL,
  `diem_xet_tuyen` double NOT NULL,
  `nguyen_vong_thu` int(11) NOT NULL,
  `ma_to_hop` varchar(50) DEFAULT NULL,
  `phuong_thuc` varchar(50) DEFAULT NULL,
  `trang_thai` varchar(50) DEFAULT 'TRUNG_TUYEN',
  `ngay_tao` timestamp NULL DEFAULT current_timestamp(),
  `xac_nhan` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_nganh`
--

CREATE TABLE `xt_nganh` (
  `idnganh` int(11) NOT NULL,
  `manganh` varchar(20) NOT NULL,
  `tennganh` varchar(200) NOT NULL,
  `n_tohopgoc` varchar(10) DEFAULT NULL,
  `n_chitieu` int(11) NOT NULL DEFAULT 0,
  `n_diemsan` decimal(10,2) DEFAULT NULL,
  `n_diemtrungtuyen` decimal(10,2) DEFAULT NULL,
  `n_tuyenthang` varchar(1) DEFAULT NULL,
  `n_dgnl` varchar(1) DEFAULT NULL,
  `n_thpt` varchar(1) DEFAULT NULL,
  `n_vsat` varchar(1) DEFAULT NULL,
  `sl_xtt` int(11) DEFAULT NULL,
  `sl_dgnl` int(11) DEFAULT NULL,
  `sl_vsat` int(11) DEFAULT NULL,
  `sl_thpt` varchar(45) DEFAULT NULL,
  `sl_dadangky` int(11) NOT NULL DEFAULT 0 COMMENT 'Tổng số thí sinh đã đăng ký (= sl_xtt + sl_dgnl + sl_vsat + sl_thpt)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_nganh_tohop`
--

CREATE TABLE `xt_nganh_tohop` (
  `id` int(11) NOT NULL,
  `manganh` varchar(45) NOT NULL,
  `matohop` varchar(45) NOT NULL,
  `th_mon1` varchar(10) DEFAULT NULL,
  `hsmon1` tinyint(4) DEFAULT NULL,
  `th_mon2` varchar(10) DEFAULT NULL,
  `hsmon2` tinyint(4) DEFAULT NULL,
  `th_mon3` varchar(10) DEFAULT NULL,
  `hsmon3` tinyint(4) DEFAULT NULL,
  `tb_keys` varchar(45) DEFAULT NULL,
  `N1` tinyint(1) DEFAULT NULL,
  `TO` tinyint(1) DEFAULT NULL,
  `LI` tinyint(1) DEFAULT NULL,
  `HO` tinyint(1) DEFAULT NULL,
  `SI` tinyint(1) DEFAULT NULL,
  `VA` tinyint(1) DEFAULT NULL,
  `SU` tinyint(1) DEFAULT NULL,
  `DI` tinyint(1) DEFAULT NULL,
  `TI` tinyint(1) DEFAULT NULL,
  `KHAC` tinyint(1) DEFAULT NULL,
  `KTPL` tinyint(1) DEFAULT NULL,
  `dolech` decimal(6,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_nguyenvongxettuyen`
--

CREATE TABLE `xt_nguyenvongxettuyen` (
  `idnv` int(11) NOT NULL,
  `id_ho_so` int(11) DEFAULT NULL,
  `nn_cccd` varchar(45) NOT NULL,
  `nv_manganh` varchar(45) NOT NULL,
  `nv_tt` int(11) NOT NULL,
  `diem_thxt` decimal(10,5) DEFAULT NULL,
  `diem_utqd` decimal(10,5) DEFAULT NULL,
  `diem_cong` decimal(6,2) DEFAULT NULL,
  `diem_xettuyen` double DEFAULT NULL,
  `nv_ketqua` varchar(45) DEFAULT 'CHO',
  `nv_keys` varchar(45) DEFAULT NULL,
  `tt_phuongthuc` varchar(45) DEFAULT NULL,
  `tt_thm` varchar(45) DEFAULT NULL,
  `id_dot` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_nhatky_hoatdong`
--

CREATE TABLE `xt_nhatky_hoatdong` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `hanh_dong` varchar(255) NOT NULL,
  `thoi_gian` datetime NOT NULL,
  `trang_thai` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_nhom_quyen`
--

CREATE TABLE `xt_nhom_quyen` (
  `id` int(11) NOT NULL,
  `ma_nhom` varchar(50) NOT NULL,
  `ten_nhom` varchar(100) NOT NULL,
  `mo_ta` varchar(200) DEFAULT NULL,
  `trang_thai` varchar(20) NOT NULL DEFAULT 'HOAT_DONG',
  `ngay_tao` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_quyen_chuc_nang`
--

CREATE TABLE `xt_quyen_chuc_nang` (
  `id` int(11) NOT NULL,
  `id_nhom` int(11) NOT NULL,
  `ma_chuc_nang` varchar(50) NOT NULL,
  `co_xem` tinyint(1) NOT NULL DEFAULT 0,
  `co_them` tinyint(1) NOT NULL DEFAULT 0,
  `co_sua` tinyint(1) NOT NULL DEFAULT 0,
  `co_xoa` tinyint(1) NOT NULL DEFAULT 0,
  `co_xuat` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_thisinhxettuyen25`
--

CREATE TABLE `xt_thisinhxettuyen25` (
  `idthisinh` int(11) NOT NULL,
  `cccd` varchar(20) NOT NULL,
  `sobaodanh` varchar(45) DEFAULT NULL,
  `ho` varchar(100) DEFAULT NULL,
  `ten` varchar(100) DEFAULT NULL,
  `ngay_sinh` varchar(45) DEFAULT NULL,
  `dien_thoai` varchar(20) DEFAULT NULL,
  `gioi_tinh` varchar(10) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `noi_sinh` varchar(45) DEFAULT NULL,
  `updated_at` date DEFAULT NULL,
  `doi_tuong` varchar(45) DEFAULT NULL,
  `khu_vuc` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_thisinh_account`
--

CREATE TABLE `xt_thisinh_account` (
  `id` int(11) NOT NULL,
  `cccd` varchar(20) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `trang_thai` varchar(20) NOT NULL DEFAULT 'HOAT_DONG',
  `lan_dang_nhap_cuoi` datetime DEFAULT NULL,
  `ngay_tao` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_tohop_monthi`
--

CREATE TABLE `xt_tohop_monthi` (
  `idtohop` int(11) NOT NULL,
  `matohop` varchar(45) NOT NULL,
  `mon1` varchar(10) NOT NULL,
  `mon2` varchar(10) NOT NULL,
  `mon3` varchar(10) NOT NULL,
  `tentohop` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_users`
--

CREATE TABLE `xt_users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `ho_ten` varchar(150) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `bo_phan` varchar(100) DEFAULT NULL,
  `id_nhom` int(11) NOT NULL,
  `trang_thai` varchar(20) NOT NULL DEFAULT 'HOAT_DONG',
  `ngay_tao` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `xt_yeucau_capnhat`
--

CREATE TABLE `xt_yeucau_capnhat` (
  `id` int(11) NOT NULL,
  `cccd` varchar(20) NOT NULL,
  `dien_thoai` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `noi_sinh` varchar(45) DEFAULT NULL,
  `khu_vuc` varchar(45) DEFAULT NULL,
  `doi_tuong` varchar(45) DEFAULT NULL,
  `minh_chung_url` varchar(255) DEFAULT NULL,
  `trang_thai` varchar(20) DEFAULT 'PENDING',
  `ngay_tao` datetime DEFAULT current_timestamp(),
  `note` varchar(255) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure for view `v_diem_cong_thisinh`
--
DROP TABLE IF EXISTS `v_diem_cong_thisinh`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_diem_cong_thisinh`  AS SELECT `ts`.`cccd` AS `cccd`, concat(`ts`.`ho`,' ',`ts`.`ten`) AS `ho_ten`, coalesce(sum(`dc`.`diemTong`),0) AS `tong_diem_cong`, least(coalesce(sum(`dc`.`diemTong`),0),3.0) AS `diem_cong_ap_dung` FROM (`xt_thisinhxettuyen25` `ts` left join `xt_diemcongxetuyen` `dc` on(`dc`.`ts_cccd` = `ts`.`cccd`)) GROUP BY `ts`.`cccd`, `ts`.`ho`, `ts`.`ten` ;

-- --------------------------------------------------------

--
-- Structure for view `v_thongke_nguyen_vong`
--
DROP TABLE IF EXISTS `v_thongke_nguyen_vong`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_thongke_nguyen_vong`  AS SELECT `n`.`manganh` AS `manganh`, `n`.`tennganh` AS `tennganh`, `n`.`n_chitieu` AS `n_chitieu`, count(`nv`.`idnv`) AS `tong_nguyen_vong`, sum(case when `nv`.`nv_tt` = 1 then 1 else 0 end) AS `so_nv1`, sum(case when `nv`.`nv_ketqua` = 'TRUNG_TUYEN' then 1 else 0 end) AS `so_trung_tuyen` FROM (`xt_nganh` `n` left join `xt_nguyenvongxettuyen` `nv` on(`nv`.`nv_manganh` = `n`.`manganh`)) GROUP BY `n`.`manganh`, `n`.`tennganh`, `n`.`n_chitieu` ;

-- --------------------------------------------------------

--
-- Structure for view `v_user_quyen`
--
DROP TABLE IF EXISTS `v_user_quyen`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_user_quyen`  AS SELECT `u`.`id` AS `id`, `u`.`username` AS `username`, `u`.`ho_ten` AS `ho_ten`, `u`.`bo_phan` AS `bo_phan`, `u`.`trang_thai` AS `trang_thai`, `n`.`ma_nhom` AS `ma_nhom`, `n`.`ten_nhom` AS `ten_nhom`, `q`.`ma_chuc_nang` AS `ma_chuc_nang`, `q`.`co_xem` AS `co_xem`, `q`.`co_them` AS `co_them`, `q`.`co_sua` AS `co_sua`, `q`.`co_xoa` AS `co_xoa`, `q`.`co_xuat` AS `co_xuat` FROM ((`xt_users` `u` join `xt_nhom_quyen` `n` on(`n`.`id` = `u`.`id_nhom`)) join `xt_quyen_chuc_nang` `q` on(`q`.`id_nhom` = `n`.`id`)) WHERE `u`.`trang_thai` = 'HOAT_DONG' ;

-- --------------------------------------------------------

--
-- Structure for view `v_xettuyen_summary`
--
DROP TABLE IF EXISTS `v_xettuyen_summary`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_xettuyen_summary`  AS SELECT `ts`.`cccd` AS `cccd`, concat(`ts`.`ho`,' ',`ts`.`ten`) AS `ho_ten`, `ts`.`gioi_tinh` AS `gioi_tinh`, `ts`.`khu_vuc` AS `khu_vuc`, `ts`.`doi_tuong` AS `doi_tuong`, `nv`.`nv_manganh` AS `nv_manganh`, `n`.`tennganh` AS `tennganh`, `nv`.`nv_tt` AS `thu_tu_NV`, `nv`.`diem_xettuyen` AS `diem_xettuyen`, `n`.`n_diemsan` AS `diem_san`, `nv`.`nv_ketqua` AS `nv_ketqua`, `nv`.`tt_phuongthuc` AS `tt_phuongthuc` FROM ((`xt_nguyenvongxettuyen` `nv` join `xt_thisinhxettuyen25` `ts` on(`ts`.`cccd` = `nv`.`nn_cccd`)) join `xt_nganh` `n` on(`n`.`manganh` = `nv`.`nv_manganh`)) ORDER BY `nv`.`diem_xettuyen` DESC ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `xt_bangquydoi`
--
ALTER TABLE `xt_bangquydoi`
  ADD PRIMARY KEY (`idqd`),
  ADD UNIQUE KEY `d_maquydoi_UNIQUE` (`d_maquydoi`);

--
-- Indexes for table `xt_diemcongxetuyen`
--
ALTER TABLE `xt_diemcongxetuyen`
  ADD PRIMARY KEY (`iddiemcong`),
  ADD UNIQUE KEY `dc_keys_UNIQUE` (`dc_keys`),
  ADD KEY `fk_dc_thisinh` (`ts_cccd`);

--
-- Indexes for table `xt_diemthixettuyen`
--
ALTER TABLE `xt_diemthixettuyen`
  ADD PRIMARY KEY (`iddiemthi`),
  ADD UNIQUE KEY `cccd_UNIQUE` (`cccd`);

--
-- Indexes for table `xt_diem_chuan_dot`
--
ALTER TABLE `xt_diem_chuan_dot`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_dot_tohop` (`id_dot`,`id_nganh_tohop`),
  ADD KEY `fk_dc_tohop` (`id_nganh_tohop`);

--
-- Indexes for table `xt_dot_tuyen_sinh`
--
ALTER TABLE `xt_dot_tuyen_sinh`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `xt_ho_so_tuyen_sinh`
--
ALTER TABLE `xt_ho_so_tuyen_sinh`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ma_ho_so` (`ma_ho_so`),
  ADD KEY `fk_hoso_thisinh` (`id_thi_sinh`),
  ADD KEY `fk_hoso_dot` (`id_dot_tuyen_sinh`);

--
-- Indexes for table `xt_ket_qua_xet_tuyen`
--
ALTER TABLE `xt_ket_qua_xet_tuyen`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_ho_so` (`id_ho_so`),
  ADD KEY `id_nganh` (`id_nganh`);

--
-- Indexes for table `xt_nganh`
--
ALTER TABLE `xt_nganh`
  ADD PRIMARY KEY (`idnganh`),
  ADD UNIQUE KEY `manganh_UNIQUE` (`manganh`);

--
-- Indexes for table `xt_nganh_tohop`
--
ALTER TABLE `xt_nganh_tohop`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `key_UNIQUE` (`tb_keys`),
  ADD KEY `fk_nt_nganh` (`manganh`),
  ADD KEY `fk_nt_tohop` (`matohop`);

--
-- Indexes for table `xt_nguyenvongxettuyen`
--
ALTER TABLE `xt_nguyenvongxettuyen`
  ADD PRIMARY KEY (`idnv`),
  ADD UNIQUE KEY `nv_keys_UNIQUE` (`nv_keys`),
  ADD KEY `fk_nv_thisinh` (`nn_cccd`),
  ADD KEY `fk_nv_nganh` (`nv_manganh`),
  ADD KEY `fk_nv_hoso` (`id_ho_so`),
  ADD KEY `FKdrhbvxnsukp96esfuf7lafllm` (`id_dot`);

--
-- Indexes for table `xt_nhatky_hoatdong`
--
ALTER TABLE `xt_nhatky_hoatdong`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `xt_nhom_quyen`
--
ALTER TABLE `xt_nhom_quyen`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ma_nhom_UNIQUE` (`ma_nhom`);

--
-- Indexes for table `xt_quyen_chuc_nang`
--
ALTER TABLE `xt_quyen_chuc_nang`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_nhom_chucnang` (`id_nhom`,`ma_chuc_nang`);

--
-- Indexes for table `xt_thisinhxettuyen25`
--
ALTER TABLE `xt_thisinhxettuyen25`
  ADD PRIMARY KEY (`idthisinh`),
  ADD UNIQUE KEY `cccd_UNIQUE` (`cccd`);

--
-- Indexes for table `xt_thisinh_account`
--
ALTER TABLE `xt_thisinh_account`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cccd_UNIQUE` (`cccd`);

--
-- Indexes for table `xt_tohop_monthi`
--
ALTER TABLE `xt_tohop_monthi`
  ADD PRIMARY KEY (`idtohop`),
  ADD UNIQUE KEY `matohop_UNIQUE` (`matohop`);

--
-- Indexes for table `xt_users`
--
ALTER TABLE `xt_users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username_UNIQUE` (`username`),
  ADD UNIQUE KEY `email_UNIQUE` (`email`),
  ADD KEY `fk_users_nhom` (`id_nhom`);

--
-- Indexes for table `xt_yeucau_capnhat`
--
ALTER TABLE `xt_yeucau_capnhat`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_yc_thisinh` (`cccd`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `xt_bangquydoi`
--
ALTER TABLE `xt_bangquydoi`
  MODIFY `idqd` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_diemcongxetuyen`
--
ALTER TABLE `xt_diemcongxetuyen`
  MODIFY `iddiemcong` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_diemthixettuyen`
--
ALTER TABLE `xt_diemthixettuyen`
  MODIFY `iddiemthi` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_diem_chuan_dot`
--
ALTER TABLE `xt_diem_chuan_dot`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_dot_tuyen_sinh`
--
ALTER TABLE `xt_dot_tuyen_sinh`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_ho_so_tuyen_sinh`
--
ALTER TABLE `xt_ho_so_tuyen_sinh`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_ket_qua_xet_tuyen`
--
ALTER TABLE `xt_ket_qua_xet_tuyen`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_nganh`
--
ALTER TABLE `xt_nganh`
  MODIFY `idnganh` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_nganh_tohop`
--
ALTER TABLE `xt_nganh_tohop`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_nguyenvongxettuyen`
--
ALTER TABLE `xt_nguyenvongxettuyen`
  MODIFY `idnv` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_nhatky_hoatdong`
--
ALTER TABLE `xt_nhatky_hoatdong`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_nhom_quyen`
--
ALTER TABLE `xt_nhom_quyen`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_quyen_chuc_nang`
--
ALTER TABLE `xt_quyen_chuc_nang`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_thisinhxettuyen25`
--
ALTER TABLE `xt_thisinhxettuyen25`
  MODIFY `idthisinh` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_thisinh_account`
--
ALTER TABLE `xt_thisinh_account`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_tohop_monthi`
--
ALTER TABLE `xt_tohop_monthi`
  MODIFY `idtohop` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_users`
--
ALTER TABLE `xt_users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `xt_yeucau_capnhat`
--
ALTER TABLE `xt_yeucau_capnhat`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `xt_diemcongxetuyen`
--
ALTER TABLE `xt_diemcongxetuyen`
  ADD CONSTRAINT `fk_dc_thisinh` FOREIGN KEY (`ts_cccd`) REFERENCES `xt_thisinhxettuyen25` (`cccd`);

--
-- Constraints for table `xt_diemthixettuyen`
--
ALTER TABLE `xt_diemthixettuyen`
  ADD CONSTRAINT `fk_diem_thisinh` FOREIGN KEY (`cccd`) REFERENCES `xt_thisinhxettuyen25` (`cccd`);

--
-- Constraints for table `xt_diem_chuan_dot`
--
ALTER TABLE `xt_diem_chuan_dot`
  ADD CONSTRAINT `fk_dc_dot` FOREIGN KEY (`id_dot`) REFERENCES `xt_dot_tuyen_sinh` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_dc_tohop` FOREIGN KEY (`id_nganh_tohop`) REFERENCES `xt_nganh_tohop` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `xt_ho_so_tuyen_sinh`
--
ALTER TABLE `xt_ho_so_tuyen_sinh`
  ADD CONSTRAINT `fk_hoso_dot` FOREIGN KEY (`id_dot_tuyen_sinh`) REFERENCES `xt_dot_tuyen_sinh` (`id`),
  ADD CONSTRAINT `fk_hoso_thisinh` FOREIGN KEY (`id_thi_sinh`) REFERENCES `xt_thisinhxettuyen25` (`idthisinh`) ON DELETE CASCADE;

--
-- Constraints for table `xt_ket_qua_xet_tuyen`
--
ALTER TABLE `xt_ket_qua_xet_tuyen`
  ADD CONSTRAINT `xt_ket_qua_xet_tuyen_ibfk_1` FOREIGN KEY (`id_ho_so`) REFERENCES `xt_ho_so_tuyen_sinh` (`id`),
  ADD CONSTRAINT `xt_ket_qua_xet_tuyen_ibfk_2` FOREIGN KEY (`id_nganh`) REFERENCES `xt_nganh` (`idnganh`);

--
-- Constraints for table `xt_nganh_tohop`
--
ALTER TABLE `xt_nganh_tohop`
  ADD CONSTRAINT `fk_nt_nganh` FOREIGN KEY (`manganh`) REFERENCES `xt_nganh` (`manganh`),
  ADD CONSTRAINT `fk_nt_tohop` FOREIGN KEY (`matohop`) REFERENCES `xt_tohop_monthi` (`matohop`);

--
-- Constraints for table `xt_nguyenvongxettuyen`
--
ALTER TABLE `xt_nguyenvongxettuyen`
  ADD CONSTRAINT `FKdrhbvxnsukp96esfuf7lafllm` FOREIGN KEY (`id_dot`) REFERENCES `xt_dot_tuyen_sinh` (`id`),
  ADD CONSTRAINT `fk_nv_hoso` FOREIGN KEY (`id_ho_so`) REFERENCES `xt_ho_so_tuyen_sinh` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_nv_nganh` FOREIGN KEY (`nv_manganh`) REFERENCES `xt_nganh` (`manganh`),
  ADD CONSTRAINT `fk_nv_thisinh` FOREIGN KEY (`nn_cccd`) REFERENCES `xt_thisinhxettuyen25` (`cccd`);

--
-- Constraints for table `xt_quyen_chuc_nang`
--
ALTER TABLE `xt_quyen_chuc_nang`
  ADD CONSTRAINT `fk_quyen_nhom` FOREIGN KEY (`id_nhom`) REFERENCES `xt_nhom_quyen` (`id`);

--
-- Constraints for table `xt_thisinh_account`
--
ALTER TABLE `xt_thisinh_account`
  ADD CONSTRAINT `fk_thisinh_acc` FOREIGN KEY (`cccd`) REFERENCES `xt_thisinhxettuyen25` (`cccd`);

--
-- Constraints for table `xt_users`
--
ALTER TABLE `xt_users`
  ADD CONSTRAINT `fk_users_nhom` FOREIGN KEY (`id_nhom`) REFERENCES `xt_nhom_quyen` (`id`);

--
-- Constraints for table `xt_yeucau_capnhat`
--
ALTER TABLE `xt_yeucau_capnhat`
  ADD CONSTRAINT `fk_yc_thisinh` FOREIGN KEY (`cccd`) REFERENCES `xt_thisinhxettuyen25` (`cccd`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
