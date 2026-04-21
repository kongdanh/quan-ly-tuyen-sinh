package com.tuyensinh.mapper;

import com.tuyensinh.dto.NganhToHopDTO;
import com.tuyensinh.model.Nganh;
import com.tuyensinh.model.NganhToHop;
import com.tuyensinh.model.ToHopMon;

public class NganhToHopMapper {

    // =========================
    // ENTITY -> DTO
    // =========================
    public static NganhToHopDTO toDTO(NganhToHop entity) {
        if (entity == null) return null;

        NganhToHopDTO dto = new NganhToHopDTO();

        dto.setId(entity.getId());

        dto.setMaNganh(
                entity.getNganh() != null ? entity.getNganh().getManganh() : null
        );

        dto.setMaToHop(
                entity.getToHopMon() != null ? entity.getToHopMon().getMatohop() : null
        );

        dto.setThMon1(entity.getThMon1());
        dto.setHsmon1(entity.getHsmon1());

        dto.setThMon2(entity.getThMon2());
        dto.setHsmon2(entity.getHsmon2());

        dto.setThMon3(entity.getThMon3());
        dto.setHsmon3(entity.getHsmon3());

        dto.setTbKeys(entity.getTbKeys());

        dto.setN1(entity.getN1());
        dto.setTo(entity.getTo());
        dto.setLi(entity.getLi());
        dto.setHo(entity.getHo());
        dto.setSi(entity.getSi());
        dto.setVa(entity.getVa());
        dto.setSu(entity.getSu());
        dto.setDi(entity.getDi());
        dto.setTi(entity.getTi());
        dto.setKhac(entity.getKhac());
        dto.setKtpl(entity.getKtpl());

        dto.setDolech(entity.getDolech());

        return dto;
    }

    // =========================
    // DTO -> ENTITY
    // =========================
    public static NganhToHop toEntity(NganhToHopDTO dto) {
        if (dto == null) return null;

        NganhToHop entity = new NganhToHop();

        entity.setId(dto.getId());

        if (dto.getMaNganh() != null) {
            Nganh nganh = new Nganh();
            nganh.setManganh(dto.getMaNganh());
            entity.setNganh(nganh);
        }

        if (dto.getMaToHop() != null) {
            ToHopMon toHop = new ToHopMon();
            toHop.setMatohop(dto.getMaToHop());
            entity.setToHopMon(toHop);
        }

        entity.setThMon1(dto.getThMon1());
        entity.setHsmon1(dto.getHsmon1());

        entity.setThMon2(dto.getThMon2());
        entity.setHsmon2(dto.getHsmon2());

        entity.setThMon3(dto.getThMon3());
        entity.setHsmon3(dto.getHsmon3());

        entity.setTbKeys(dto.getTbKeys());

        entity.setN1(dto.getN1());
        entity.setTo(dto.getTo());
        entity.setLi(dto.getLi());
        entity.setHo(dto.getHo());
        entity.setSi(dto.getSi());
        entity.setVa(dto.getVa());
        entity.setSu(dto.getSu());
        entity.setDi(dto.getDi());
        entity.setTi(dto.getTi());
        entity.setKhac(dto.getKhac());
        entity.setKtpl(dto.getKtpl());

        entity.setDolech(dto.getDolech());

        return entity;
    }
}