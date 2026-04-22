package com.tuyensinh.mapper;

import com.tuyensinh.dto.NganhDTO;
import com.tuyensinh.model.Nganh;

/**
 * NganhMapper - Chuyển đổi giữa Nganh entity và NganhDTO.
 * Tất cả method là static, không cần khởi tạo.
 */
public class NganhMapper {

    private NganhMapper() {}

    // ================================================================
    // entity → DTO
    // ================================================================

    public static NganhDTO toDTO(Nganh n) {
        if (n == null) return null;
        return new NganhDTO(
                n.getId(),
                n.getManganh(),
                n.getTennganh(),
                n.getNTohopgoc(),
                n.getNChitieu(),
                n.getNDiemsan(),
                n.getNDiemtrungtuyen(),
                n.getNTuyenthang(),
                n.getNDgnl(),
                n.getNThpt(),
                n.getNVsat(),
                n.getSlXtt(),
                n.getSlDgnl(),
                n.getSlVsat(),
                n.getSlThpt()
        );
    }

    // ================================================================
    // DTO → entity
    // ================================================================

    public static Nganh toEntity(NganhDTO dto) {
        if (dto == null) return null;
        Nganh n = new Nganh();
        n.setId(dto.getId());
        n.setManganh(dto.getManganh());
        n.setTennganh(dto.getTennganh());
        n.setNTohopgoc(dto.getNTohopgoc());
        n.setNChitieu(dto.getNChitieu());
        n.setNDiemsan(dto.getNDiemsan());
        n.setNDiemtrungtuyen(dto.getNDiemtrungtuyen());
        n.setNTuyenthang(dto.getNTuyenthang());
        n.setNDgnl(dto.getNDgnl());
        n.setNThpt(dto.getNThpt());
        n.setNVsat(dto.getNVsat());
        n.setSlXtt(dto.getSlXtt());
        n.setSlDgnl(dto.getSlDgnl());
        n.setSlVsat(dto.getSlVsat());
        n.setSlThpt(dto.getSlThpt());
        return n;
    }

    /**
     * Copy dữ liệu từ DTO vào entity đã có sẵn (dùng khi update,
     * giữ nguyên ID + các field không có trong form)
     */
    public static void updateEntity(Nganh target, NganhDTO source) {
        if (target == null || source == null) return;
        // Không ghi đè ID
        target.setManganh(source.getManganh());
        target.setTennganh(source.getTennganh());
        target.setNTohopgoc(source.getNTohopgoc());
        target.setNChitieu(source.getNChitieu());
        target.setNDiemsan(source.getNDiemsan());
        target.setNDiemtrungtuyen(source.getNDiemtrungtuyen());
        target.setNTuyenthang(source.getNTuyenthang());
        target.setNDgnl(source.getNDgnl());
        target.setNThpt(source.getNThpt());
        target.setNVsat(source.getNVsat());
        target.setSlXtt(source.getSlXtt());
        target.setSlDgnl(source.getSlDgnl());
        target.setSlVsat(source.getSlVsat());
        target.setSlThpt(source.getSlThpt());
    }
}