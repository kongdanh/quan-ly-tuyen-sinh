package com.tuyensinh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThiSinhSessionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idThiSinh;
    private String cccd;
    private String hoTen;
    private String email;
    private String gioiTinh;
    private String ngaySinh;
    private String dienThoai;
    private String noiSinh;
    private String khuVuc;
    private String doiTuong;
}