package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "xt_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "ho_ten", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "bo_phan", length = 100)
    private String boPhan;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_nhom", nullable = false)
    private NhomQuyen nhomQuyen;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;
}