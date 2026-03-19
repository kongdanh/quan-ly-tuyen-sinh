package com.tuyensinh.util;

/**
 * Hằng số toàn cục – Phần Mềm Quản Lý Tuyển Sinh – Đại học Sài Gòn
 * Database: xettuyen2026 | SGU Design System v1.0.0
 */
public class Constants {

    // ================================================================
    // 1. APP INFO
    // ================================================================
    public static final String APP_TITLE        = "Phần Mềm Quản Lý Tuyển Sinh - Đại học Sài Gòn";
    public static final String APP_TITLE_SHORT  = "Tuyển Sinh SGU";
    public static final String APP_VERSION      = "1.0.0";
    public static final String APP_UNIVERSITY   = "Đại học Sài Gòn";
    public static final String APP_SHORT_NAME   = "SGU";
    public static final String APP_DB_NAME      = "xettuyen2026";

    // ================================================================
    // 2. PHÂN TRANG
    // ================================================================
    public static final int PAGE_SIZE           = 20;
    public static final int PAGE_SIZE_SMALL     = 10;
    public static final int PAGE_SIZE_LARGE     = 50;
    public static final int PAGE_FIRST          = 1;

    // ================================================================
    // 3. UI – WINDOW & LAYOUT
    // ================================================================
    public static final int WINDOW_WIDTH        = 1280;
    public static final int WINDOW_HEIGHT       = 768;
    public static final int WINDOW_MIN_WIDTH    = 1024;
    public static final int WINDOW_MIN_HEIGHT   = 600;

    // ================================================================
    // 4. UI – SIDEBAR
    // ================================================================
    public static final int    SIDEBAR_WIDTH              = 220;
    public static final String SIDEBAR_BG_COLOR           = "#112347";
    public static final String SIDEBAR_ITEM_COLOR         = "rgba(255,255,255,0.65)";
    public static final String SIDEBAR_ITEM_HOVER_BG      = "rgba(255,255,255,0.07)";
    public static final String SIDEBAR_ITEM_ACTIVE_BG     = "rgba(37,99,235,0.25)";
    public static final String SIDEBAR_ITEM_ACTIVE_BORDER = "#3b82f6";
    public static final int    SIDEBAR_ITEM_HEIGHT        = 36;
    public static final int    SIDEBAR_ITEM_PADDING_X     = 14;
    public static final int    SIDEBAR_LABEL_FONT_SIZE    = 9;
    public static final int    SIDEBAR_ITEM_FONT_SIZE     = 12;

    // ================================================================
    // 5. UI – HEADER
    // ================================================================
    public static final int    HEADER_HEIGHT        = 48;
    public static final String HEADER_BG_COLOR      = "#1a3560";
    public static final int    HEADER_AVATAR_SIZE   = 28;
    public static final int    HEADER_FONT_SIZE     = 12;
    public static final int    HEADER_PADDING_X     = 16;

    // ================================================================
    // 6. UI – BUTTON
    // ================================================================
    public static final int BTN_HEIGHT          = 32;
    public static final int BTN_HEIGHT_SM       = 26;
    public static final int BTN_HEIGHT_LG       = 42;
    public static final int BTN_PADDING_X       = 14;
    public static final int BTN_PADDING_X_SM    = 10;
    public static final int BTN_FONT_SIZE       = 12;
    public static final int BTN_BORDER_RADIUS   = 6;
    public static final String BTN_PRIMARY_BG           = "#1a3560";
    public static final String BTN_PRIMARY_HOVER        = "#2a4a7f";
    public static final String BTN_PRIMARY_TEXT         = "#ffffff";
    public static final String BTN_SUCCESS_BG           = "#16a34a";
    public static final String BTN_SUCCESS_TEXT         = "#ffffff";
    public static final String BTN_DANGER_BG            = "#dc2626";
    public static final String BTN_DANGER_TEXT          = "#ffffff";
    public static final String BTN_WARNING_BG           = "#d97706";
    public static final String BTN_WARNING_TEXT         = "#ffffff";
    public static final String BTN_OUTLINE_BG           = "#ffffff";
    public static final String BTN_OUTLINE_BORDER       = "#e2e8f0";
    public static final String BTN_OUTLINE_TEXT         = "#1e293b";
    public static final String BTN_OUTLINE_HOVER_BORDER = "#2563eb";
    public static final String BTN_OUTLINE_HOVER_TEXT   = "#2563eb";

    // ================================================================
    // 7. UI – INPUT / FORM
    // ================================================================
    public static final int    INPUT_HEIGHT         = 34;
    public static final int    INPUT_HEIGHT_SM      = 28;
    public static final int    INPUT_PADDING_X      = 10;
    public static final int    INPUT_BORDER_RADIUS  = 6;
    public static final String INPUT_BORDER         = "#e2e8f0";
    public static final String INPUT_BORDER_FOCUS   = "#2563eb";
    public static final String INPUT_BG             = "#f8fafc";
    public static final int    INPUT_FONT_SIZE      = 12;
    public static final int    LABEL_FONT_SIZE      = 11;
    public static final int    TEXTAREA_HEIGHT      = 70;
    public static final int    SEARCH_WIDTH         = 200;
    public static final int    SEARCH_HEIGHT        = 32;
    public static final int    FORM_GAP             = 14;
    public static final int    FORM_ROW_COLS        = 2;

    // ================================================================
    // 8. UI – TABLE
    // ================================================================
    public static final int    TABLE_ROW_HEIGHT         = 38;
    public static final int    TABLE_HEADER_HEIGHT      = 36;
    public static final int    TABLE_CELL_PADDING_X     = 12;
    public static final int    TABLE_CELL_PADDING_Y     = 9;
    public static final int    TABLE_FONT_SIZE          = 12;
    public static final int    TABLE_HEADER_FONT_SIZE   = 11;
    public static final String TABLE_HEADER_BG          = "#f8fafc";
    public static final String TABLE_ROW_HOVER_BG       = "#f8fafc";
    public static final String TABLE_ROW_BORDER         = "#f1f5f9";
    public static final String TABLE_BORDER_COLOR       = "#e2e8f0";
    public static final int    TABLE_BORDER_RADIUS      = 10;
    public static final int    TABLE_TOOLBAR_HEIGHT     = 48;
    public static final int    TABLE_TOOLBAR_GAP        = 10;
    public static final int    TABLE_TOOLBAR_PADDING_X  = 12;
    public static final int    PAGINATION_HEIGHT        = 40;
    public static final int    PAGINATION_BTN_SIZE      = 28;
    public static final int    PAGINATION_FONT_SIZE     = 11;
    public static final int    COL_STT_WIDTH            = 40;
    public static final int    COL_ACTION_WIDTH         = 110;
    public static final int    COL_BADGE_WIDTH          = 90;
    public static final int    COL_SCORE_WIDTH          = 60;
    public static final int    COL_DATE_WIDTH           = 90;
    public static final int    COL_CODE_WIDTH           = 80;
    public static final double SCORE_MIN                = 0.0;
    public static final double SCORE_MAX                = 10.0;
    public static final double DIEM_XET_MAX             = 30.0;

    // ================================================================
    // 9. UI – CARD / MODAL
    // ================================================================
    public static final int    CARD_PADDING         = 16;
    public static final int    CARD_BORDER_RADIUS   = 10;
    public static final String CARD_BG              = "#ffffff";
    public static final String CARD_BORDER          = "#e2e8f0";
    public static final String CARD_SHADOW          = "0 1px 4px rgba(0,0,0,0.06)";
    public static final int    MODAL_WIDTH          = 520;
    public static final int    MODAL_BORDER_RADIUS  = 12;
    public static final int    MODAL_HEADER_HEIGHT  = 52;
    public static final int    MODAL_PADDING        = 20;
    public static final int    MODAL_FOOTER_HEIGHT  = 52;
    public static final int    CONTENT_PADDING      = 20;
    public static final int    CARDS_GAP            = 14;
    public static final int    SECTION_GAP          = 18;

    // ================================================================
    // 10. UI – COLORS
    // ================================================================
    public static final String COLOR_NAVY           = "#1a3560";
    public static final String COLOR_NAVY_DARK      = "#112347";
    public static final String COLOR_NAVY_LIGHT     = "#2a4a7f";
    public static final String COLOR_BLUE           = "#2563eb";
    public static final String COLOR_BLUE_LIGHT     = "#3b82f6";
    public static final String COLOR_SUCCESS        = "#16a34a";
    public static final String COLOR_WARNING        = "#d97706";
    public static final String COLOR_DANGER         = "#dc2626";
    public static final String COLOR_INFO           = "#0891b2";
    public static final String COLOR_BG             = "#f1f5f9";
    public static final String COLOR_BG_CARD        = "#ffffff";
    public static final String COLOR_BG_INPUT       = "#f8fafc";
    public static final String COLOR_BORDER         = "#e2e8f0";
    public static final String COLOR_TEXT           = "#1e293b";
    public static final String COLOR_TEXT_MUTED     = "#64748b";
    public static final String COLOR_TEXT_WHITE     = "#ffffff";
    public static final String BADGE_ADMIN_BG       = "#fef3c7";
    public static final String BADGE_ADMIN_TEXT     = "#92400e";
    public static final String BADGE_GV_BG          = "#dbeafe";
    public static final String BADGE_GV_TEXT        = "#1e40af";
    public static final String BADGE_SV_BG          = "#dcfce7";
    public static final String BADGE_SV_TEXT        = "#166534";
    public static final String BADGE_SUCCESS_BG     = "#dcfce7";
    public static final String BADGE_SUCCESS_TEXT   = "#15803d";
    public static final String BADGE_WARNING_BG     = "#fef9c3";
    public static final String BADGE_WARNING_TEXT   = "#a16207";
    public static final String BADGE_DANGER_BG      = "#fee2e2";
    public static final String BADGE_DANGER_TEXT    = "#b91c1c";
    public static final String BADGE_INFO_BG        = "#dbeafe";
    public static final String BADGE_INFO_TEXT      = "#1d4ed8";
    public static final String BADGE_GRAY_BG        = "#f1f5f9";
    public static final String BADGE_GRAY_TEXT      = "#475569";

    // ================================================================
    // 11. UI – FONT
    // ================================================================
    public static final String FONT_FAMILY          = "Be Vietnam Pro";
    public static final String FONT_FAMILY_FALLBACK = "Segoe UI, Arial, sans-serif";
    public static final int    FONT_SIZE_XS         = 9;
    public static final int    FONT_SIZE_SM         = 11;
    public static final int    FONT_SIZE_BASE       = 12;
    public static final int    FONT_SIZE_MD         = 13;
    public static final int    FONT_SIZE_LG         = 16;
    public static final int    FONT_SIZE_XL         = 22;
    public static final int    FONT_WEIGHT_NORMAL   = 400;
    public static final int    FONT_WEIGHT_MEDIUM   = 500;
    public static final int    FONT_WEIGHT_SEMIBOLD = 600;
    public static final int    FONT_WEIGHT_BOLD     = 700;

    // ================================================================
    // 12. NHÓM QUYỀN – xt_nhom_quyen.ma_nhom
    // ================================================================
    public static final String NHOM_ADMIN           = "NHOM_ADMIN";
    public static final String NHOM_GIANG_VIEN      = "NHOM_GIANG_VIEN";
    public static final String NHOM_XETTUYEN        = "NHOM_XETTUYEN";

    // ================================================================
    // 13. MÃ CHỨC NĂNG – xt_quyen_chuc_nang.ma_chuc_nang
    // ================================================================
    public static final String QUYEN_NGANH          = "NGANH";
    public static final String QUYEN_TOHOP          = "TOHOP";
    public static final String QUYEN_NGANH_TOHOP    = "NGANH_TOHOP";
    public static final String QUYEN_THI_SINH       = "THI_SINH";
    public static final String QUYEN_DIEM_THI       = "DIEM_THI";
    public static final String QUYEN_DIEM_CONG      = "DIEM_CONG";
    public static final String QUYEN_NGUYEN_VONG    = "NGUYEN_VONG";
    public static final String QUYEN_BANG_QUY_DOI   = "BANG_QUY_DOI";
    public static final String QUYEN_THONG_KE       = "THONG_KE";
    public static final String QUYEN_PHAN_QUYEN     = "PHAN_QUYEN";

    // ================================================================
    // 14. TRẠNG THÁI TÀI KHOẢN – xt_users & xt_thisinh_account
    // ================================================================
    public static final String ACCOUNT_ACTIVE       = "HOAT_DONG";
    public static final String ACCOUNT_LOCKED       = "BI_KHOA";

    // Demo accounts (dev/test only)
    public static final String DEMO_ADMIN_USER      = "admin";
    public static final String DEMO_ADMIN_PASS      = "admin123";
    public static final String DEMO_GV_USER         = "user";
    public static final String DEMO_GV_PASS         = "user123";
    public static final String DEMO_SV_USER         = "sv";
    public static final String DEMO_SV_PASS         = "sv123";

    // ================================================================
    // 15. PHƯƠNG THỨC XÉT TUYỂN
    // Giá trị trong xt_diemthixettuyen.d_phuongthuc
    // ================================================================
    public static final String PT_THPT              = "THPT";
    public static final String PT_VSAT              = "VSAT";
    public static final String PT_DGNL              = "DGNL";
    public static final String PT_TUYEN_THANG       = "XTT";

    // ================================================================
    // 16. KẾT QUẢ XÉT TUYỂN
    // Giá trị trong xt_nguyenvongxettuyen.nv_ketqua
    // ================================================================
    public static final String KQ_CHO_XET           = "CHO";
    public static final String KQ_TRUNG_TUYEN       = "TRUNG_TUYEN";
    public static final String KQ_KHONG_DAT         = "KHONG_DAT";
    public static final String NGANH_DANG_TUYEN     = "Y";
    public static final String NGANH_DUNG_TUYEN     = "N";

    // ================================================================
    // 17. MÔN THI & TỔ HỢP
    // Mã cột trong xt_diemthixettuyen và xt_nganh_tohop
    // ================================================================
    public static final String MON_TOAN             = "TO";
    public static final String MON_VAN              = "VA";
    public static final String MON_VAT_LY           = "LI";
    public static final String MON_HOA_HOC          = "HO";
    public static final String MON_SINH_HOC         = "SI";
    public static final String MON_TIENG_ANH        = "N1";
    public static final String MON_TIENG_ANH_THI    = "N1_THI";
    public static final String MON_TIENG_ANH_CC     = "N1_CC";
    public static final String MON_LICH_SU          = "SU";
    public static final String MON_DIA_LY           = "DI";
    public static final String MON_TIN_HOC          = "TI";
    public static final String MON_KTPL             = "KTPL";
    public static final String MON_CNCN             = "CNCN";
    public static final String MON_CNNN             = "CNNN";
    public static final String MON_NK1              = "NK1";
    public static final String MON_NK2              = "NK2";
    public static final String MON_DGNL_TONG_HOP   = "NL1";

    public static final String TOHOP_A00            = "A00";
    public static final String TOHOP_A01            = "A01";
    public static final String TOHOP_B00            = "B00";
    public static final String TOHOP_C00            = "C00";
    public static final String TOHOP_D01            = "D01";
    public static final String TOHOP_D07            = "D07";
    public static final String TOHOP_M01            = "M01";
    public static final String TOHOP_H00            = "H00";

    public static final int    HS_MON_1             = 1;
    public static final int    HS_MON_3             = 3;
    public static final int    HS_MON_4             = 4;
    public static final int    HS_MON_5             = 5;

    // ================================================================
    // 18. KHU VỰC & ĐỐI TƯỢNG ƯU TIÊN
    // KV_DB_* = giá trị thực lưu trong DB (khu_vuc: '1','2','2NT','3')
    // KV_*    = giá trị hiển thị trên UI
    // ================================================================
    public static final String KV_DB_1             = "1";
    public static final String KV_DB_2             = "2";
    public static final String KV_DB_2NT           = "2NT";
    public static final String KV_DB_3             = "3";
    public static final String KV_1                = "KV1";
    public static final String KV_2_NT             = "KV2-NT";
    public static final String KV_2                = "KV2";
    public static final String KV_3                = "KV3";

    public static final double DIEM_CONG_KV1       = 0.75;
    public static final double DIEM_CONG_KV2NT     = 0.50;
    public static final double DIEM_CONG_KV2       = 0.25;
    public static final double DIEM_CONG_KV3       = 0.00;

    // DT_DB_* = giá trị thực lưu trong DB (doi_tuong: '01','02','03','06a'...)
    public static final String DT_DB_01            = "01";
    public static final String DT_DB_02            = "02";
    public static final String DT_DB_03            = "03";
    public static final String DT_01               = "DT01";
    public static final String DT_02               = "DT02";
    public static final String DT_03               = "DT03";
    public static final String DT_KHONG            = "KHONG";

    public static final double DIEM_CONG_DT01      = 2.00;
    public static final double DIEM_CONG_DT02      = 1.50;
    public static final double DIEM_CONG_DT03      = 0.50;
    public static final double DIEM_CONG_TONG_MAX  = 3.00;

    public static final String GIOI_TINH_NAM       = "Nam";
    public static final String GIOI_TINH_NU        = "Nữ";

    // ================================================================
    // 19. BẢNG QUY ĐỔI – xt_bangquydoi
    // Giá trị trong cột d_phuongthuc
    // ================================================================
    public static final String QD_TIENG_ANH        = "TA";
    public static final String QD_DGNL             = "DGNL";
    public static final String QD_VSAT             = "VSAT";

    public static final String CC_IELTS            = "IELTS";
    public static final String CC_TOEFL_ITP        = "TOEFL_ITP";
    public static final String CC_TOEFL_IBT        = "TOEFL_iBT";
    public static final String CC_VSTEP            = "VSTEP";

    // Giá trị ghichu trong xt_diemcongxetuyen
    public static final String DC_IELTS            = "Tiếng Anh - IELTS";
    public static final String DC_TOEFL_ITP        = "Tiếng Anh - TOEFL ITP";
    public static final String DC_TOEFL_IBT        = "Tiếng Anh - TOEFL iBT";
    public static final String DC_VSTEP            = "Tiếng Anh - VSTEP";

    // ================================================================
    // 20. NGUYỆN VỌNG – xt_nguyenvongxettuyen
    // ================================================================
    public static final int    NV_1                = 1;
    public static final int    NV_2                = 2;
    public static final int    NV_3                = 3;
    public static final int    NV_MAX_HIENTHI      = 3;
    public static final int    NV_MAX_DB           = 9;
    public static final String NV_CHUA_XET         = "CHO";
    public static final String NV_DA_XET           = "DA_XET";
    public static final String NV_TRUNG_TUYEN      = "TRUNG_TUYEN";
    public static final String NV_KHONG_DAT        = "KHONG_DAT";

    // ================================================================
    // 21. TRẠNG THÁI CHUNG
    // ================================================================
    public static final String STATUS_ACTIVE       = "ACTIVE";
    public static final String STATUS_INACTIVE     = "INACTIVE";
    public static final String STATUS_PENDING      = "PENDING";

    // ================================================================
    // 22. TÊN BẢNG – khớp chính xác với database
    // ================================================================
    // Bảng hệ thống
    public static final String TABLE_NHOM_QUYEN    = "xt_nhom_quyen";
    public static final String TABLE_QUYEN_CN      = "xt_quyen_chuc_nang";
    public static final String TABLE_USERS         = "xt_users";
    public static final String TABLE_TS_ACCOUNT    = "xt_thisinh_account";
    // Bảng nghiệp vụ
    public static final String TABLE_NGANH         = "xt_nganh";
    public static final String TABLE_TOHOP         = "xt_tohop_monthi";
    public static final String TABLE_NGANH_TOHOP   = "xt_nganh_tohop";
    public static final String TABLE_THI_SINH      = "xt_thisinhxettuyen25";
    public static final String TABLE_DIEM_THI      = "xt_diemthixettuyen";
    public static final String TABLE_NGUYEN_VONG   = "xt_nguyenvongxettuyen";
    public static final String TABLE_DIEM_CONG     = "xt_diemcongxetuyen";
    public static final String TABLE_BANG_QUY_DOI  = "xt_bangquydoi";
    // Views
    public static final String VIEW_USER_QUYEN     = "v_user_quyen";
    public static final String VIEW_XETTUYEN       = "v_xettuyen_summary";
    public static final String VIEW_THONGKE_NV     = "v_thongke_nguyen_vong";
    public static final String VIEW_DIEM_CONG      = "v_diem_cong_thisinh";
}