package com.tuyensinh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for importing Học sinh giỏi (Academic Excellence Certificate) bonus data.
 * Represents each row from the Excel sheet "ds thi sinh".
 *
 * Columns:
 * - cccd: Citizen Identification / Passport number
 * - maMonDatGiai: Subject code that received the prize (e.g., "N1", "SU", "TO", "VA")
 * - diemCoMon: Bonus points when the admission combination CONTAINS the prize subject
 * - diemKhongMon: Bonus points when the admission combination DOES NOT contain the prize subject
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HsgImportDTO {

    private String cccd;                // Citizen ID / CCCD
    private String maMonDatGiai;        // Prize subject code
    private BigDecimal diemCoMon;       // Bonus if subject is in combination
    private BigDecimal diemKhongMon;    // Bonus if subject is NOT in combination
}
