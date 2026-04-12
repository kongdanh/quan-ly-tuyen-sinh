package com.tuyensinh.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private PasswordUtil() {}

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
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
        String myHash = hash(myPassword);
        System.out.println("===============================================");
        System.out.println("Mật khẩu gốc: " + myPassword);
        System.out.println("Mã Hash chuẩn (Copy chuỗi này): " + myHash);
        System.out.println("Test verify lại: " + verify(myPassword, myHash));
        System.out.println("===============================================");
    }
}