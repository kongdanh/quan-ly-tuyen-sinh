package com.tuyensinh.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.tuyensinh.model.KetQuaXetTuyen;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PdfExportUtil {

    public static void exportGiayBaoTrungTuyen(File file, List<KetQuaXetTuyen> listData) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Cấu hình Font (Nên dùng font hỗ trợ Tiếng Việt trong thực tế, ở đây dùng font mặc định tạm thời)
        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.TIMES_ROMAN, 13, Font.NORMAL);

        boolean isFirst = true;

        for (KetQuaXetTuyen kq : listData) {
            if (!"TRUNG_TUYEN".equals(kq.getTrangThai())) continue;

            if (!isFirst) {
                document.newPage(); // Sang trang mới cho thí sinh tiếp theo
            }
            isFirst = false;

            // Header (Tiêu đề Quốc hiệu)
            Paragraph header1 = new Paragraph("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", boldFont);
            header1.setAlignment(Element.ALIGN_CENTER);
            Paragraph header2 = new Paragraph("Độc lập - Tự do - Hạnh phúc", boldFont);
            header2.setAlignment(Element.ALIGN_CENTER);
            
            document.add(header1);
            document.add(header2);
            document.add(new Paragraph("----------------------", normalFont));
            document.add(new Paragraph("\n"));

            // Tiêu đề chính
            Paragraph title = new Paragraph("GIẤY BÁO TRÚNG TUYỂN ĐẠI HỌC", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n\n"));

            // Nội dung
            document.add(new Paragraph("Hội đồng Tuyển sinh thông báo:", normalFont));
            document.add(new Paragraph("Thí sinh: " + kq.getHoSo().getThiSinh().getHo() + " " + kq.getHoSo().getThiSinh().getTen(), boldFont));
            document.add(new Paragraph("Số CCCD: " + kq.getHoSo().getThiSinh().getCccd(), normalFont));
            document.add(new Paragraph("Mã Hồ sơ: " + kq.getHoSo().getMaHoSo(), normalFont));
            document.add(new Paragraph("\nĐÃ TRÚNG TUYỂN VÀO NGÀNH: " + kq.getNganh().getTennganh(), boldFont));
            document.add(new Paragraph("Điểm xét tuyển: " + kq.getDiemXetTuyen(), normalFont));
            
            document.add(new Paragraph("\nĐề nghị anh/chị hoàn tất thủ tục nhập học theo đúng thời gian quy định.", normalFont));
        }

        document.close();
    }
}