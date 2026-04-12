package com.tuyensinh.web.servlet;

import com.tuyensinh.web.util.WebConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // 1. Get current session without creating a new one
        HttpSession session = req.getSession(false);
        
        if (session != null) {
            // 2. Clear all session data and destroy the session object
            session.invalidate();
        }

        // 3. Prevent browser from caching protected pages after logout
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        resp.setHeader("Pragma", "no-cache"); // HTTP 1.0
        resp.setDateHeader("Expires", 0); // Proxies

        // 4. Redirect to login page using the constant defined
        resp.sendRedirect(req.getContextPath() + WebConstants.LOGIN_PAGE);
    }
}