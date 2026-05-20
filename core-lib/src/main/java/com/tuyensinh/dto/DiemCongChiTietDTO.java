package com.tuyensinh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for "Điểm Cộng Chi Tiết" (Bonus Points Detail) View.
 * Aggregates bonus information from multiple sources (IELTS, HSG, Regional Priority)
 * for each candidate's preference/combination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiemCongChiTietDTO {

    private String cccd;                        // Citizen ID
    private String hoTen;                       // Full Name
    private Integer nguyenVongThu;              // Preference Order (1, 2, 3...)
    private String maNganh;                     // Program Code
    private String maToHop;                     // Subject Combination Code
    private double diemCongTiengAnh;            // English Certificate Bonus (IELTS)
    private double diemCongHsg;                 // Academic Excellence (HSG) Bonus
    private double tongDiemCong;                // Total Bonus (capped at 3.0)
}
