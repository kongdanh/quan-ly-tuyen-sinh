package com.tuyensinh.mapper;

import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.model.ThiSinh;

public class ThiSinhMapper {
    private ThiSinhMapper() {}

    public static ThiSinhSessionDTO toSessionDTO(ThiSinh ts) {
        if (ts == null) return null;

        return ThiSinhSessionDTO.builder()
                .idThiSinh(ts.getId())
                .cccd(ts.getCccd())
                .hoTen((ts.getHo() != null ? ts.getHo().trim() : "") + " " + (ts.getTen() != null ? ts.getTen().trim() : ""))
                .email(ts.getEmail())
                .gioiTinh(ts.getGioiTinh())
                .ngaySinh(ts.getNgaySinh())
                .dienThoai(ts.getDienThoai())
                .noiSinh(ts.getNoiSinh())
                .khuVuc(ts.getKhuVuc())
                .doiTuong(ts.getDoiTuong())
                .build();
    }
}