package com.tuyensinh.dto;

public class NganhDTO {
    private String maNganh;
    private String tenNganh;
    private String khoa;
    private int chiTieu;
    private int daDangKy;
    private int tiLe;
    private String trangThai;

    public NganhDTO(  String maNganh, String tenNganh, String khoa, int chiTieu,int daDangKy, int tiLe, String trangThai) {
        this.chiTieu = chiTieu;
        this.daDangKy = daDangKy;
        this.khoa = khoa;
        this.maNganh = maNganh;
        this.tenNganh = tenNganh;
        this.tiLe = tiLe;
        this.trangThai = trangThai;
    }
    
    public String getMaNganh() {
        return maNganh;
    }

    public void setMaNganh(String maNganh) {
        this.maNganh = maNganh;
    }

    public String getTenNganh() {
        return tenNganh;
    }

    public void setTenNganh(String tenNganh) {
        this.tenNganh = tenNganh;
    }

    public String getKhoa() {
        return khoa;
    }

    public void setKhoa(String khoa) {
        this.khoa = khoa;
    }

    public int getChiTieu() {
        return chiTieu;
    }

    public void setChiTieu(int chiTieu) {
        this.chiTieu = chiTieu;
    }

    public int getDaDangKy() {
        return daDangKy;
    }

    public void setDaDangKy(int daDangKy) {
        this.daDangKy = daDangKy;
    }

    public int getTiLe() {
        return tiLe;
    }

    public void setTiLe(int tiLe) {
        this.tiLe = tiLe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

  
}
