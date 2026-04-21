package com.tuyensinh.dto;

import java.math.BigDecimal;

import lombok.Data;
@Data
public class NganhDTO {

    private Integer id;
    private String manganh;
    private String tennganh;
    private String nTohopgoc;
    private Integer nChitieu;
    private BigDecimal nDiemsan;
    private BigDecimal nDiemtrungtuyen;
    private String nTuyenthang;
    private String nDgnl;
    private String nThpt;
    private String nVsat;
    private Integer slXtt;
    private Integer slDgnl;
    private Integer slVsat;
    private String slThpt;

    public NganhDTO() {}

    public NganhDTO(Integer id, String manganh, String tennganh, String nTohopgoc,
                    Integer nChitieu, BigDecimal nDiemsan, BigDecimal nDiemtrungtuyen,
                    String nTuyenthang, String nDgnl, String nThpt, String nVsat,
                    Integer slXtt, Integer slDgnl, Integer slVsat, String slThpt) {
        this.id = id;
        this.manganh = manganh;
        this.tennganh = tennganh;
        this.nTohopgoc = nTohopgoc;
        this.nChitieu = nChitieu;
        this.nDiemsan = nDiemsan;
        this.nDiemtrungtuyen = nDiemtrungtuyen;
        this.nTuyenthang = nTuyenthang;
        this.nDgnl = nDgnl;
        this.nThpt = nThpt;
        this.nVsat = nVsat;
        this.slXtt = slXtt;
        this.slDgnl = slDgnl;
        this.slVsat = slVsat;
        this.slThpt = slThpt;
    }

    // getter/setter (bạn có thể dùng Lombok nếu muốn)
    // ...
}