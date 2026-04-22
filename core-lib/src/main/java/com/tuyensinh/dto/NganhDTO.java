package com.tuyensinh.dto;

import java.math.BigDecimal;

/**
 * NganhDTO - Data Transfer Object cho Nganh.
 * Dùng để truyền dữ liệu giữa các layer (UI ↔ Service ↔ DAO).
 */
public class NganhDTO {

    private Integer id;
    private String manganh;
    private String tennganh;
    private String nTohopgoc;
    private Integer nChitieu;
    private BigDecimal nDiemsan;
    private BigDecimal nDiemtrungtuyen;
    private String nTuyenthang;   // "1" = có, "0" / null = không
    private String nDgnl;
    private String nThpt;
    private String nVsat;
    private Integer slXtt;
    private Integer slDgnl;
    private Integer slVsat;
    private String slThpt;

    // ================================================================
    // CONSTRUCTORS
    // ================================================================

    public NganhDTO() {}

    public NganhDTO(Integer id, String manganh, String tennganh, String nTohopgoc,
                    Integer nChitieu, BigDecimal nDiemsan, BigDecimal nDiemtrungtuyen,
                    String nTuyenthang, String nDgnl, String nThpt, String nVsat,
                    Integer slXtt, Integer slDgnl, Integer slVsat, String slThpt) {
        this.id             = id;
        this.manganh        = manganh;
        this.tennganh       = tennganh;
        this.nTohopgoc      = nTohopgoc;
        this.nChitieu       = nChitieu;
        this.nDiemsan       = nDiemsan;
        this.nDiemtrungtuyen = nDiemtrungtuyen;
        this.nTuyenthang    = nTuyenthang;
        this.nDgnl          = nDgnl;
        this.nThpt          = nThpt;
        this.nVsat          = nVsat;
        this.slXtt          = slXtt;
        this.slDgnl         = slDgnl;
        this.slVsat         = slVsat;
        this.slThpt         = slThpt;
    }

    // ================================================================
    // GETTERS & SETTERS
    // ================================================================

    public Integer getId()                        { return id; }
    public void setId(Integer id)                 { this.id = id; }

    public String getManganh()                    { return manganh; }
    public void setManganh(String manganh)        { this.manganh = manganh; }

    public String getTennganh()                   { return tennganh; }
    public void setTennganh(String tennganh)      { this.tennganh = tennganh; }

    public String getNTohopgoc()                  { return nTohopgoc; }
    public void setNTohopgoc(String nTohopgoc)   { this.nTohopgoc = nTohopgoc; }

    public Integer getNChitieu()                  { return nChitieu; }
    public void setNChitieu(Integer nChitieu)     { this.nChitieu = nChitieu; }

    public BigDecimal getNDiemsan()               { return nDiemsan; }
    public void setNDiemsan(BigDecimal nDiemsan) { this.nDiemsan = nDiemsan; }

    public BigDecimal getNDiemtrungtuyen()                        { return nDiemtrungtuyen; }
    public void setNDiemtrungtuyen(BigDecimal nDiemtrungtuyen)   { this.nDiemtrungtuyen = nDiemtrungtuyen; }

    public String getNTuyenthang()                { return nTuyenthang; }
    public void setNTuyenthang(String nTuyenthang){ this.nTuyenthang = nTuyenthang; }

    public String getNDgnl()                      { return nDgnl; }
    public void setNDgnl(String nDgnl)            { this.nDgnl = nDgnl; }

    public String getNThpt()                      { return nThpt; }
    public void setNThpt(String nThpt)            { this.nThpt = nThpt; }

    public String getNVsat()                      { return nVsat; }
    public void setNVsat(String nVsat)            { this.nVsat = nVsat; }

    public Integer getSlXtt()                     { return slXtt; }
    public void setSlXtt(Integer slXtt)           { this.slXtt = slXtt; }

    public Integer getSlDgnl()                    { return slDgnl; }
    public void setSlDgnl(Integer slDgnl)         { this.slDgnl = slDgnl; }

    public Integer getSlVsat()                    { return slVsat; }
    public void setSlVsat(Integer slVsat)         { this.slVsat = slVsat; }

    public String getSlThpt()                     { return slThpt; }
    public void setSlThpt(String slThpt)          { this.slThpt = slThpt; }

    @Override
    public String toString() {
        return manganh + " - " + tennganh;
    }
}