package com.tuyensinh.mapper;

import com.tuyensinh.dto.ToHopMonImportDTO;
import com.tuyensinh.model.ToHopMon;

/**
 * ToHopMonMapper - Chuyển đổi giữa ToHopMon entity và ToHopMonImportDTO.
 * Tất cả method là static, không cần khởi tạo.
 */
public class ToHopMonMapper {

    private ToHopMonMapper() {}

    // ================================================================
    // entity → DTO
    // ================================================================

    public static ToHopMonImportDTO toDTO(ToHopMon entity) {
        if (entity == null) return null;
        return new ToHopMonImportDTO(
                entity.getMatohop(),
                entity.getMon1(),
                entity.getMon2(),
                entity.getMon3(),
                entity.getTentohop()
        );
    }

    // ================================================================
    // DTO → entity
    // ================================================================

    public static ToHopMon toEntity(ToHopMonImportDTO dto) {
        if (dto == null) return null;
        ToHopMon entity = new ToHopMon();
        entity.setMatohop(dto.getMatohop());
        entity.setMon1(dto.getMon1());
        entity.setMon2(dto.getMon2());
        entity.setMon3(dto.getMon3());
        entity.setTentohop(dto.getTentohop());
        return entity;
    }

    /**
     * Copy dữ liệu từ DTO vào entity đã có sẵn (dùng khi update,
     * giữ nguyên ID + các field không có trong form)
     */
    public static void updateEntity(ToHopMon target, ToHopMonImportDTO source) {
        if (target == null || source == null) return;
        // Không ghi đè ID
        target.setMatohop(source.getMatohop());
        target.setMon1(source.getMon1());
        target.setMon2(source.getMon2());
        target.setMon3(source.getMon3());
        target.setTentohop(source.getTentohop());
    }
}
