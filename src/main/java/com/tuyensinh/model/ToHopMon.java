package com.tuyensinh.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "xt_tohop_monthi")
public class ToHopMon {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idtohop")
  private Integer idtohop;

  @Column(name = "matohop", unique = true, nullable = false)
  private String matohop;

  @OneToMany(mappedBy = "toHopMon", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<NganhToHop> nganhToHops = new ArrayList<>();

  @Column(name = "mon1")
  private String mon1;

  @Column(name = "mon2")
  private String mon2;

  @Column(name = "mon3")
  private String mon3;

  @Column(name = "tentohop")
  private String tentohop;

}