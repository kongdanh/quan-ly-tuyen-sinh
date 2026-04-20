package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "xt_nhatky_hoatdong")
public class NhatKyHoatDong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "hanh_dong", length = 255)
    private String hanhDong;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // SUCCESS hoặc FAILED
    
    @PrePersist
    protected void onCreate() {
        thoiGian = LocalDateTime.now();
    }
}