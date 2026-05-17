package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "xt_ho_so_tuyen_sinh")
public class HoSoTuyenSinh implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_thi_sinh", referencedColumnName = "idthisinh", nullable = false)
    private ThiSinh thiSinh;

    // Nối với bảng Đợt Tuyển Sinh
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_dot_tuyen_sinh", nullable = false)
    private DotTuyenSinh dotTuyenSinh;

    @Column(name = "ma_ho_so", nullable = false, unique = true)
    private String maHoSo;

    @Column(name = "tong_diem_xet_tuyen")
    private Double tongDiemXetTuyen;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "ngay_nop")
    private LocalDateTime ngayNop;

    @OneToMany(mappedBy = "hoSoTuyenSinh", fetch = FetchType.EAGER)
    private List<NguyenVong> nguyenVongs;
    
    private Double diemThi;
    private Double diemCong;

    public Double getTongDiem() {
        return (this.diemThi != null ? this.diemThi : 0.0) + 
               (this.diemCong != null ? this.diemCong : 0.0);
    }
    
    public List<NguyenVong> getNguyenVongs() { return this.nguyenVongs; }

    // Trong file HoSoTuyenSinh.java
    public Double getTongDiemXetTuyen() {
        if (this.nguyenVongs == null || this.nguyenVongs.isEmpty()) {
            return 0.0;
        }
        // Lấy điểm cao nhất trong tất cả các nguyện vọng của thí sinh
        return this.nguyenVongs.stream()
                .mapToDouble(nv -> nv.getDiemXettuyen() != null ? nv.getDiemXettuyen() : 0.0)
                .max()
                .orElse(0.0);
    }
}