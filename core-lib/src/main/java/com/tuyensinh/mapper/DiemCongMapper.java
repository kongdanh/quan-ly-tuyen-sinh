package com.tuyensinh.mapper;

import com.tuyensinh.dto.DiemCongImportDTO;
import com.tuyensinh.model.DiemCong;

public class DiemCongMapper {

    /**
     * Convert DTO từ Excel sang Entity
     * Lưu ý: ThiSinh cần được set riêng từ Service layer
     */
    public static DiemCong toEntity(DiemCongImportDTO dto) {
        if (dto == null) {
            return null;
        }

        DiemCong entity = new DiemCong();
        // ThiSinh sẽ được set từ bên ngoài thông qua tsCccd
        entity.setManganh(dto.getManganh());
        entity.setMatohop(dto.getMatohop());
        entity.setPhuongthuc(dto.getPhuongthuc());
        entity.setDiemCC(dto.getDiemCC());
        entity.setDiemUtxt(dto.getDiemUtxt());
        entity.setDiemTong(dto.getDiemTong());
        entity.setGhichu(dto.getGhichu());
        entity.setDcKeys(dto.getDcKeys());

        return entity;
    }

    /**
     * Convert Entity sang DTO
     */
    public static DiemCongImportDTO toDTO(DiemCong entity) {
        if (entity == null) {
            return null;
        }

        DiemCongImportDTO dto = new DiemCongImportDTO();
        dto.setTsCccd(entity.getThiSinh() != null ? entity.getThiSinh().getCccd() : null);
        dto.setManganh(entity.getManganh());
        dto.setMatohop(entity.getMatohop());
        dto.setPhuongthuc(entity.getPhuongthuc());
        dto.setDiemCC(entity.getDiemCC());
        dto.setDiemUtxt(entity.getDiemUtxt());
        dto.setDiemTong(entity.getDiemTong());
        dto.setGhichu(entity.getGhichu());
        dto.setDcKeys(entity.getDcKeys());

        return dto;
    }
}
