package com.tuyensinh.mapper;

import com.tuyensinh.dto.BangQuyDoiImportDTO;
import com.tuyensinh.model.BangQuyDoi;

public class BangQuyDoiMapper {

    /**
     * Convert DTO từ Excel sang Entity
     */
    public static BangQuyDoi toEntity(BangQuyDoiImportDTO dto) {
        if (dto == null) {
            return null;
        }

        BangQuyDoi entity = new BangQuyDoi();
        entity.setDPhuongthuc(dto.getPhuongthuc());
        entity.setDTohop(dto.getTohop());
        entity.setDMon(dto.getMon());
        entity.setDDiema(dto.getDiemA());
        entity.setDDiemb(dto.getDiemB());
        entity.setDDiemc(dto.getDiemC());
        entity.setDDiemd(dto.getDiemD());
        entity.setDMaquydoi(dto.getMaquydoi());
        entity.setDPhanvi(dto.getPhanvi());

        return entity;
    }

    /**
     * Convert Entity sang DTO
     */
    public static BangQuyDoiImportDTO toDTO(BangQuyDoi entity) {
        if (entity == null) {
            return null;
        }

        BangQuyDoiImportDTO dto = new BangQuyDoiImportDTO();
        dto.setPhuongthuc(entity.getDPhuongthuc());
        dto.setTohop(entity.getDTohop());
        dto.setMon(entity.getDMon());
        dto.setDiemA(entity.getDDiema());
        dto.setDiemB(entity.getDDiemb());
        dto.setDiemC(entity.getDDiemc());
        dto.setDiemD(entity.getDDiemd());
        dto.setMaquydoi(entity.getDMaquydoi());
        dto.setPhanvi(entity.getDPhanvi());

        return dto;
    }
}
