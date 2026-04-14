package com.tuyensinh.util;

public class PaginationHelper {

    /**
     * Tính tổng số trang dựa trên tổng số bản ghi và kích thước trang
     */
    public static int calculateTotalPages(long totalRecords, int pageSize) {
        if (totalRecords == 0) return 1;
        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    /**
     * Tính vị trí bắt đầu (Offset) cho câu lệnh SQL (LIMIT, OFFSET)
     * Trang 1 -> Offset 0
     * Trang 2 -> Offset 20
     */
    public static int calculateOffset(int pageIndex, int pageSize) {
        return (pageIndex - 1) * pageSize;
    }

    /**
     * Tính Số Thứ Tự (STT) hiển thị trên UI cho từng dòng
     * @param pageIndex Trang hiện tại
     * @param pageSize Số dòng mỗi trang
     * @param rowInPage Chỉ số dòng trong trang hiện tại (0 -> pageSize-1)
     */
    public static int calculateSTT(int pageIndex, int pageSize, int rowInPage) {
        return (pageIndex - 1) * pageSize + (rowInPage + 1);
    }

    /**
     * Kiểm tra xem trang hiện tại có phải trang đầu/cuối không (Dùng để disable nút UI)
     */
    public static boolean isFirstPage(int pageIndex) {
        return pageIndex <= 1;
    }

    public static boolean isLastPage(int pageIndex, long totalRecords, int pageSize) {
        return pageIndex >= calculateTotalPages(totalRecords, pageSize);
    }
}