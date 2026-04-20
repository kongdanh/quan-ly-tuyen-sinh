    package com.tuyensinh.service;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
import java.util.Optional;

import com.tuyensinh.dao.NganhDAO;
    import com.tuyensinh.dao.NguyenVongDAO;
    import com.tuyensinh.dto.NganhDTO;
    import com.tuyensinh.model.Nganh;

    public class NganhService {

        private NganhDAO nganhDAO = new NganhDAO();
        private NguyenVongDAO nvDAO = new NguyenVongDAO();

        public List<NganhDTO> getAllWithStats() {

            List<Nganh> nganhList = nganhDAO.findAll();
            List<Object[]> countList = nvDAO.countByNganh();

            // convert countList → map
            Map<String, Long> countMap = new HashMap<>();
            for (Object[] row : countList) {
                countMap.put((String) row[0], (Long) row[1]);
            }

            List<NganhDTO> result = new ArrayList<>();

            for (Nganh n : nganhList) {
                int daDangKy = countMap.getOrDefault(n.getManganh(), 0L).intValue();
                int chiTieu = n.getnChitieu();

                int tiLe = chiTieu == 0 ? 0 : (daDangKy * 100 / chiTieu);

                String trangThai;
                if (daDangKy >= chiTieu) {
                    trangThai = "Đã đủ chỉ tiêu";
                } else {
                    trangThai = "Đang tuyển";
                }

                result.add(new NganhDTO(
                    n.getManganh(),
                    n.getTennganh(),
                    "null", // khoa tạm thời
                    chiTieu,
                    daDangKy,
                    tiLe,
                    trangThai
                ));
            }

            return result;
        }

        public Optional<Nganh> findByMA(String maNganh) {
            return nganhDAO.findByMaNganh(maNganh);
        }        

        public static void main(String[] args) {
            NganhService service = new NganhService();
            List<NganhDTO> list = service.getAllWithStats();

            for (NganhDTO n : list) {
                System.out.println(
                    n.getMaNganh() + " | " +
                    n.getTenNganh() + " | " +
                    n.getKhoa() + " | " +
                    n.getChiTieu() + " | " +
                    n.getDaDangKy() + " | " +
                    n.getTiLe() + "% | " +
                    n.getTrangThai()
                );
            }
        }
    }

