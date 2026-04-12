package com.tuyensinh.web.filter;

import com.tuyensinh.util.HibernateUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 1. Initialize Hibernate Connection Pool early
        System.out.println("\n[System] Initializing Database Connection...");
        try {
            HibernateUtil.getSessionFactory();
            System.out.println("[System] Database connected successfully!");
        } catch (Exception e) {
            System.err.println("[System] Database connection FAILED!");
            e.printStackTrace();
        }

        // 2. Display Server Info
        String contextPath = sce.getServletContext().getContextPath();
        String fullUrl = "http://localhost:8080" + contextPath;

        System.out.println("\n=========================================================");
        System.out.println("   SERVER STARTED SUCCESSFULLY! ");
        System.out.println("   ACCESS APP AT: " + fullUrl);
        System.out.println("=========================================================\n");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Close Hibernate connections when server stops to prevent memory leaks
        System.out.println("\n[System] Closing Database connections...");
        HibernateUtil.shutdown();
        System.out.println(" SERVER STOPPED! ");
    }
}