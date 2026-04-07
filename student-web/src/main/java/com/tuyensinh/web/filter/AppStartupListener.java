package com.tuyensinh.web.filter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        // getContextPath() trả về đường dẫn gốc của ứng dụng web, ví dụ: "/student-web"
        String contextPath = sce.getServletContext().getContextPath();
        
        // create full URL: http://localhost:8080/student-web
        String fullUrl = "http://localhost:8080" + contextPath;

        // start -> path localhost:8080 -> welcome.jsp -> index.jsp -> servlet -> jsp
        System.out.println("\n=========================================================");
        System.out.println(" SERVER STARTED! ");
        System.out.println(" RUNNING ON: " + fullUrl);
        System.out.println("=========================================================\n");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Chạy khi tắt server
        System.out.println(" SERVER STOPPED! ");
    }
}