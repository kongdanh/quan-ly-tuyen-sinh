package com.tuyensinh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NganhToHopDTO {

    private Integer id;

    // 🔥 chỉ giữ mã thay vì object
    private String maNganh;
    private String maToHop;

    private String thMon1;
    private Byte hsmon1;

    private String thMon2;
    private Byte hsmon2;

    private String thMon3;
    private Byte hsmon3;

    private String tbKeys;

    private Boolean n1;
    private Boolean to;
    private Boolean li;
    private Boolean ho;
    private Boolean si;
    private Boolean va;
    private Boolean su;
    private Boolean di;
    private Boolean ti;
    private Boolean khac;
    private Boolean ktpl;

    private BigDecimal dolech;

   
}