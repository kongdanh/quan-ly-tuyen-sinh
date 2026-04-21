package com.tuyensinh.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "xt_tohop_monthi")
public class ToHopMon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtohop", nullable = false)
    private Integer id;

    @Column(name = "matohop", nullable = false, length = 45)
    private String matohop;

    @Column(name = "mon1", nullable = false, length = 10)
    private String mon1;

    @Column(name = "mon2", nullable = false, length = 10)
    private String mon2;

    @Column(name = "mon3", nullable = false, length = 10)
    private String mon3;

    @Column(name = "tentohop", length = 100)
    private String tentohop;

    @Override
    public String toString() {
    return matohop + " - (" + mon1 + " , " +mon2 + " , " + mon3 + ")" ;
    }
}