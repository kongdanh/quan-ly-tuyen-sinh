package com.tuyensinh.web.servlet;

import com.tuyensinh.dao.*;
import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.model.*;
import com.tuyensinh.service.DiemService;
import com.tuyensinh.service.NguyenVongService;
import com.tuyensinh.service.ThiSinhService;
import com.tuyensinh.service.NguyenVongService.SaveResult;
import com.tuyensinh.web.util.WebConstants;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize       = 1024 * 1024 * 10,
    maxRequestSize    = 1024 * 1024 * 20
)
@WebServlet("/thisinh/*")
public class ThiSinhController extends HttpServlet {

    // Dependencies
    private final ThiSinhService     thiSinhService = new ThiSinhService();
    private final DiemThiXetTuyenDAO diemDAO     = new DiemThiXetTuyenDAO();
    private final NguyenVongDAO      nvDAO       = new NguyenVongDAO();
    private final NganhDAO           nganhDAO    = new NganhDAO();
    private final YeuCauCapNhatDAO   ycDAO       = new YeuCauCapNhatDAO();
    private final DiemService        diemService = new DiemService();
    private final NguyenVongService  nvService   = new NguyenVongService();
    private final ThiSinhAccountDAO accDAO = new ThiSinhAccountDAO();

    // GET
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ThiSinhSessionDTO user = getUser(req);
        if (user == null) { redirectLogin(req, resp); return; }

        // Attribute public
        req.setAttribute("user",     user);
        req.setAttribute("thongTin", thiSinhService.findByCccd(user.getCccd()).orElse(null));

        switch (getPath(req)) {
            case "/dashboard" -> handleDashboard(req, resp, user.getCccd());
            case "/scores"    -> handleScores(req, resp, user.getCccd());
            case "/profile"   -> forward(req, resp, "/WEB-INF/views/profile.jsp");
            case "/wishes"    -> handleWishes(req, resp, user.getCccd());
            case "/results"   -> handleResults(req, resp, user.getCccd());
            default           -> resp.sendRedirect(req.getContextPath() + "/thisinh/dashboard");
        }
    }

    // POST
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        ThiSinhSessionDTO user = getUser(req);
        if (user == null) { redirectLogin(req, resp); return; }

        switch (getPath(req)) {
            case "/profile/update" -> handleProfileUpdate(req, resp, user);
            case "/wishes/save"    -> handleWishSave(req, resp, user);
            case "/wishes/delete"  -> handleWishDelete(req, resp, user);
            case "/wishes/reorder" -> handleWishReorder(req, resp, user);
            case "/change-password"-> handleChangePassword(req, resp, user);
            default                -> resp.sendRedirect(req.getContextPath() + "/thisinh/dashboard");
        }
    }

    // GET HANDLERS

    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp, String cccd)
            throws ServletException, IOException {
        DiemThiXetTuyen diem = diemDAO.findByCccd(cccd).orElse(null);
        req.setAttribute("diem",     diem);
        req.setAttribute("maxToHop", diemService.tinhMaxToHop(diem));
        req.setAttribute("wishes",   nvDAO.findByCccd(cccd));

        // KIỂM TRA MẬT KHẨU MẶC ĐỊNH
        boolean isDefaultPassword = false;
        ThiSinh thongTin = thiSinhService.findByCccd(cccd).orElse(null);
        
        ThiSinhAccount acc = accDAO.findByCccd(cccd).orElse(null); 
        
        if (thongTin != null && acc != null) {
            if (org.mindrot.jbcrypt.BCrypt.checkpw(thongTin.getNgaySinh(), acc.getPasswordHash())) {
                isDefaultPassword = true;
            }
        }
        req.setAttribute("isDefaultPassword", isDefaultPassword);

        forward(req, resp, "/WEB-INF/views/dashboard.jsp");
    }

    private void handleScores(HttpServletRequest req, HttpServletResponse resp, String cccd)
            throws ServletException, IOException {
        DiemThiXetTuyen diem     = diemDAO.findByCccd(cccd).orElse(null);
        var             listToHop = diemService.tinhListToHop(diem);
        req.setAttribute("diem",      diem);
        req.setAttribute("listToHop", listToHop);
        req.setAttribute("maxToHop",  listToHop.stream()
            .max(java.util.Comparator.comparingDouble(m -> (Double) m.get("total")))
            .orElse(null));
        forward(req, resp, "/WEB-INF/views/scores.jsp");
    }

    private void handleWishes(HttpServletRequest req, HttpServletResponse resp, String cccd)
            throws ServletException, IOException {
        req.setAttribute("wishes",    nvDAO.findByCccd(cccd));
        req.setAttribute("listNganh", nganhDAO.findAllSync());
        forward(req, resp, "/WEB-INF/views/wishes.jsp");
    }

    private void handleResults(HttpServletRequest req, HttpServletResponse resp, String cccd)
            throws ServletException, IOException {
        List<NguyenVong> wishes = nvDAO.findByCccd(cccd);

        NguyenVong trungTuyen = wishes.stream()
            .filter(nv -> "TRUNG_TUYEN".equalsIgnoreCase(nv.getNvKetqua())
                       || "yes".equalsIgnoreCase(nv.getNvKetqua()))
            .findFirst().orElse(null);

        boolean daCoKetQua = wishes.stream()
            .anyMatch(nv -> nv.getNvKetqua() != null
                         && !"CHO".equalsIgnoreCase(nv.getNvKetqua()));

        req.setAttribute("trungTuyen", trungTuyen);
        req.setAttribute("daCoKetQua", daCoKetQua);
        forward(req, resp, "/WEB-INF/views/results.jsp");
    }

    // POST HANDLERS

    private void handleProfileUpdate(HttpServletRequest req, HttpServletResponse resp,
                                     ThiSinhSessionDTO user)
            throws ServletException, IOException {

        String fileName = saveUploadedFile(req, user.getCccd());

        YeuCauCapNhat yc = YeuCauCapNhat.builder()
            .cccd(user.getCccd())
            .dienThoai(req.getParameter("dienThoai"))
            .email(req.getParameter("email"))
            .noiSinh(req.getParameter("noiSinh"))
            .khuVuc(req.getParameter("khuVuc"))
            .doiTuong(req.getParameter("doiTuong"))
            .minhChungUrl(fileName)
            .trangThai("PENDING")
            .build();

        ycDAO.save(yc);
        resp.sendRedirect(req.getContextPath() + "/thisinh/profile?success=true");
    }

    private void handleWishSave(HttpServletRequest req, HttpServletResponse resp,
                                ThiSinhSessionDTO user)
            throws IOException {

        String  manganh = req.getParameter("manganh");
        String  idnvStr = req.getParameter("idnv");
        Integer idnv    = (idnvStr != null && !idnvStr.isBlank())
                          ? Integer.parseInt(idnvStr) : null;

        DiemThiXetTuyen diem   = diemDAO.findByCccd(user.getCccd()).orElse(null);
        SaveResult      result = nvService.saveWish(user.getCccd(), manganh, idnv, diem);

        String redirect = switch (result) {
            case OK            -> "/thisinh/wishes?success=add";
            case NOT_QUALIFIED -> "/thisinh/wishes?error=not_qualified";
            case MAX_REACHED   -> "/thisinh/wishes?error=max";
            case FORBIDDEN     -> "/thisinh/wishes?error=forbidden";
        };
        resp.sendRedirect(req.getContextPath() + redirect);
    }

    private void handleWishDelete(HttpServletRequest req, HttpServletResponse resp,
                                  ThiSinhSessionDTO user)
            throws IOException {
        try {
            int idnv    = Integer.parseInt(req.getParameter("idnv"));
            boolean ok  = nvService.deleteWish(idnv, user.getCccd());
            String param = ok ? "success=delete" : "error=forbidden";
            resp.sendRedirect(req.getContextPath() + "/thisinh/wishes?" + param);
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/thisinh/wishes?error=delete_failed");
        }
    }

    private void handleWishReorder(HttpServletRequest req, HttpServletResponse resp,
                                   ThiSinhSessionDTO user)
            throws IOException {
        String idsParam = req.getParameter("ids");
        if (idsParam == null || idsParam.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        nvService.reorderByIds(idsParam.split(","), user.getCccd());
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // PRIVATE UTILITIES

    /** Xử lý upload file minh chứng, trả về tên file đã lưu hoặc null */
    private String saveUploadedFile(HttpServletRequest req, String cccd) {
        try {
            if (req.getContentType() == null
                    || !req.getContentType().toLowerCase().startsWith("multipart/")) return null;

            Part filePart = req.getPart("fileMinhChung");
            if (filePart == null || filePart.getSize() == 0) return null;

            String orig     = filePart.getSubmittedFileName().replaceAll("[^a-zA-Z0-9.-]", "_");
            String fileName = "TS_" + cccd + "_" + System.currentTimeMillis() + "_" + orig;
            String dir      = getServletContext().getRealPath("") + File.separator + WebConstants.UPLOAD_DIR;
            new File(dir).mkdirs();
            filePart.write(dir + File.separator + fileName);
            return fileName;
        } catch (Exception e) {
            System.out.println("[Upload] Lỗi: " + e.getMessage());
            return null;
        }
    }

    /** Lấy ThiSinhSessionDTO từ session */
    private ThiSinhSessionDTO getUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null
               : (ThiSinhSessionDTO) session.getAttribute(WebConstants.SESSION_THISINH);
    }

    /** Lấy pathInfo, mặc định /dashboard */
    private String getPath(HttpServletRequest req) {
        String p = req.getPathInfo();
        return (p == null || p.equals("/")) ? "/dashboard" : p;
    }

    /** Forward đến JSP */
    private void forward(HttpServletRequest req, HttpServletResponse resp, String path)
            throws ServletException, IOException {
        req.getRequestDispatcher(path).forward(req, resp);
    }

    /** Redirect về trang login */
    private void redirectLogin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + WebConstants.LOGIN_PAGE);
    }

    // change pwd
    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp, ThiSinhSessionDTO user) throws IOException {
        String oldPass = req.getParameter("oldPassword");
        String newPass = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");

        if (newPass == null || !newPass.equals(confirm)) {
            resp.sendRedirect(req.getContextPath() + "/thisinh/dashboard?error=pwd_mismatch");
            return;
        }

        ThiSinhAccount acc = accDAO.findByCccd(user.getCccd()).orElse(null);
        
        if (acc != null) {
            // Kiểm tra mật khẩu cũ có đúng không
            if (org.mindrot.jbcrypt.BCrypt.checkpw(oldPass, acc.getPasswordHash())) {
                // Hash mật khẩu mới và lưu DB
                String hashedNewPass = org.mindrot.jbcrypt.BCrypt.hashpw(newPass, org.mindrot.jbcrypt.BCrypt.gensalt(12));
                acc.setPasswordHash(hashedNewPass);
                accDAO.update(acc);
                resp.sendRedirect(req.getContextPath() + "/thisinh/dashboard?success=pwd_changed");
            } else {
                resp.sendRedirect(req.getContextPath() + "/thisinh/dashboard?error=wrong_old_pwd");
            }
        } else {
            resp.sendRedirect(req.getContextPath() + "/thisinh/dashboard?error=acc_not_found");
        }
    }

}