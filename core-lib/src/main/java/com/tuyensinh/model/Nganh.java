package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "xt_nganh")
public class Nganh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnganh", nullable = false)
    private Integer id;

    @Column(name = "manganh", nullable = false, length = 20)
    private String manganh;

    @Column(name = "tennganh", nullable = false, length = 200)
    private String tennganh;

    @Column(name = "n_tohopgoc", length = 10)
    private String nTohopgoc;

    @Column(name = "n_chitieu", nullable = false)
    private Integer nChitieu;

    @Column(name = "n_diemsan", precision = 10, scale = 2)
    private BigDecimal nDiemsan;

    @Column(name = "n_diemtrungtuyen", precision = 10, scale = 2)
    private BigDecimal nDiemtrungtuyen;

    @Column(name = "n_tuyenthang", length = 1)
    private String nTuyenthang;

    @Column(name = "n_dgnl", length = 1)
    private String nDgnl;

    @Column(name = "n_thpt", length = 1)
    private String nThpt;

    @Column(name = "n_vsat", length = 1)
    private String nVsat;

    @Column(name = "sl_xtt")
    private Integer slXtt;

    @Column(name = "sl_dgnl")
    private Integer slDgnl;

    @Column(name = "sl_vsat")
    private Integer slVsat;

    @Column(name = "sl_thpt", length = 45)
    private String slThpt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getManganh() {
        return manganh;
    }

    public void setManganh(String manganh) {
        this.manganh = manganh;
    }

    public String getTennganh() {
        return tennganh;
    }

    public void setTennganh(String tennganh) {
        this.tennganh = tennganh;
    }

    public String getnTohopgoc() {
        return nTohopgoc;
    }

    public void setnTohopgoc(String nTohopgoc) {
        this.nTohopgoc = nTohopgoc;
    }

    public Integer getnChitieu() {
        return nChitieu;
    }

    public void setnChitieu(Integer nChitieu) {
        this.nChitieu = nChitieu;
    }

    public BigDecimal getnDiemsan() {
        return nDiemsan;
    }

    public void setnDiemsan(BigDecimal nDiemsan) {
        this.nDiemsan = nDiemsan;
    }

    public BigDecimal getnDiemtrungtuyen() {
        return nDiemtrungtuyen;
    }

    public void setnDiemtrungtuyen(BigDecimal nDiemtrungtuyen) {
        this.nDiemtrungtuyen = nDiemtrungtuyen;
    }

    public String getnTuyenthang() {
        return nTuyenthang;
    }

    public void setnTuyenthang(String nTuyenthang) {
        this.nTuyenthang = nTuyenthang;
    }

    public String getnDgnl() {
        return nDgnl;
    }

    public void setnDgnl(String nDgnl) {
        this.nDgnl = nDgnl;
    }

    public String getnThpt() {
        return nThpt;
    }

    public void setnThpt(String nThpt) {
        this.nThpt = nThpt;
    }

    public String getnVsat() {
        return nVsat;
    }

    public void setnVsat(String nVsat) {
        this.nVsat = nVsat;
    }

    public Integer getSlXtt() {
        return slXtt;
    }

    public void setSlXtt(Integer slXtt) {
        this.slXtt = slXtt;
    }

    public Integer getSlDgnl() {
        return slDgnl;
    }

    public void setSlDgnl(Integer slDgnl) {
        this.slDgnl = slDgnl;
    }

    public Integer getSlVsat() {
        return slVsat;
    }

    public void setSlVsat(Integer slVsat) {
        this.slVsat = slVsat;
    }

    public String getSlThpt() {
        return slThpt;
    }

    public void setSlThpt(String slThpt) {
        this.slThpt = slThpt;
    }
}