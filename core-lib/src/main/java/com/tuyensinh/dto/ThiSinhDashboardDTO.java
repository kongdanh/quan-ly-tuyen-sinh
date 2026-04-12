package com.tuyensinh.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ThiSinhDashboardDTO {
    private String hoTen;
    private String cccd;
    private String soBaoDanh;
    private Map<String, Double> danhSachDiem; 
    private Double tongDiemCong;
}