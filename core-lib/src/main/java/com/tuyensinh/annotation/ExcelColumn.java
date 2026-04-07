package com.tuyensinh.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {
    /**
     * Tên chính của cột Excel
     */
    String value();

    /**
     * Các tên thay thế (alias) cho cột này
     */
    String[] aliases() default {};

    /**
     * Có bắt buộc không
     */
    boolean required() default false;
}
