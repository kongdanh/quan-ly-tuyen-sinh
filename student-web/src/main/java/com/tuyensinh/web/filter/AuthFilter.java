package com.tuyensinh.web.filter;

import com.tuyensinh.web.util.WebConstants;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final String[] PUBLIC_PATHS = {
        "/login.jsp", "/LoginServlet", "/assets/", "/uploads/"
    };

    private static final String[] PUBLIC_EXTENSIONS = {
        ".css", ".js", ".png", ".jpg", ".jpeg", ".webp", ".svg", ".ico"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getServletPath();

        // accept public paths
        for (String pub : PUBLIC_PATHS) {
            if (path.equals(pub) || path.startsWith(pub)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // accept public extensions
        String lower = path.toLowerCase();
        for (String ext : PUBLIC_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                chain.doFilter(req, res);
                return;
            }
        }

        // check session
        HttpSession session = request.getSession(false);
        Object user = (session != null) ? session.getAttribute(WebConstants.SESSION_THISINH) : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + WebConstants.LOGIN_PAGE);
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override public void init(FilterConfig fc) throws ServletException {}
    @Override public void destroy() {}
}