package com.tuyensinh.web.servlet;

import com.tuyensinh.dto.ThiSinhSessionDTO;
import com.tuyensinh.service.AuthService;
import com.tuyensinh.web.util.WebConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = AuthService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(WebConstants.SESSION_THISINH) != null) {
            resp.sendRedirect(req.getContextPath() + "/thisinh/dashboard");
            return;
        }
        resp.sendRedirect(req.getContextPath() + WebConstants.LOGIN_PAGE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String cccd    = req.getParameter("cccd");
        String password = req.getParameter("password");

        if (cccd != null) cccd = cccd.trim();
        if (password != null) password = password.trim();

        ThiSinhSessionDTO thisinh = authService.loginThiSinh(cccd, password);

        if (thisinh != null) {
            // del old session if exists
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) oldSession.invalidate();

            HttpSession session = req.getSession(true);
            session.setAttribute(WebConstants.SESSION_THISINH, thisinh);
            session.setMaxInactiveInterval(30 * 60); // time out 30min

            resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath() + "/thisinh/dashboard"));
        } else {
            req.setAttribute("error", "Sai CCCD hoặc mật khẩu!");
            req.getRequestDispatcher(WebConstants.LOGIN_PAGE).forward(req, resp);
        }
    }
}