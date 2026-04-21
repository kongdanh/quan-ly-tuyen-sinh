package com.tuyensinh.service;

import com.tuyensinh.dao.NhomQuyenDAO;
import com.tuyensinh.model.NhomQuyen;
import com.tuyensinh.util.HibernateUtil;

import java.util.List;

import org.hibernate.Session;

public class NhomQuyenService {
    private final NhomQuyenDAO nhomQuyenDAO = new NhomQuyenDAO();

    public List<NhomQuyen> findAllSync() {
        return nhomQuyenDAO.findAllSync();
    }

    public NhomQuyen findById(Integer id) {
        if (id == null) return null;
        return nhomQuyenDAO.findById(id); 
    }

    public void save(NhomQuyen nq) { nhomQuyenDAO.save(nq); }
    public void update(NhomQuyen nq) { nhomQuyenDAO.update(nq); }


    // validate 
    public boolean isMaNhomExists(String maNhom) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        Long count = session.createQuery("SELECT count(n) FROM NhomQuyen n WHERE n.maNhom = :ma", Long.class)
                            .setParameter("ma", maNhom).uniqueResult();
        return count > 0;
    }
}
}