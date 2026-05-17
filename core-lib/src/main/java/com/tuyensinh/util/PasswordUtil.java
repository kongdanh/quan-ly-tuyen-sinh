package com.tuyensinh.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private PasswordUtil() {}

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(4));
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    // debug password hashing
    public static void main(String[] args) {
        // test pwd
        String myPassword = "25/07/2007";
        String adminPW = "admin123";
        String myHash = hash(myPassword);
        String adminHash = hash(adminPW);
        String userHash = hash("user123");
        System.out.println("===============================================");
        System.out.println("Mật khẩu gốc: " + myPassword);
        System.out.println("Hash thisinh: " + myHash);
        System.out.println("Hash admin: " + adminHash);
        System.out.println("Hash user: " + userHash);
        System.out.println("Test verify lại: " + verify(myPassword, myHash));
        System.out.println("===============================================");
    }
}