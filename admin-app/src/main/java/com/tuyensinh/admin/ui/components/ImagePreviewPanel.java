package com.tuyensinh.admin.ui.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImagePreviewPanel extends JPanel {
    private BufferedImage image;
    private String statusMessage = "Đang tải ảnh...";
    private boolean isError = false;

    public ImagePreviewPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#111827"));
    }

    public void loadImage(String urlString) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                // 1. fake headers
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    // 2. Đọc luồng dữ liệu
                    image = ImageIO.read(conn.getInputStream());
                    
                    if (image == null) {
                        isError = true;
                        statusMessage = "Lỗi: File tồn tại nhưng Java không hỗ trợ đọc định dạng này (.webp). Hãy kiểm tra thư viện Maven.";
                        System.err.println("[ImagePreview] ImageIO trả về null cho URL: " + urlString);
                    }
                } else if (responseCode == 404) {
                    isError = true;
                    statusMessage = "Lỗi 404: Không tìm thấy ảnh trên Server (File có thể đã bị xóa).";
                } else if (responseCode == 403 || responseCode == 401) {
                    isError = true;
                    statusMessage = "Lỗi " + responseCode + ": Bị chặn truy cập. AuthFilter của Web chưa cho phép xem ảnh tĩnh.";
                } else {
                    isError = true;
                    statusMessage = "Lỗi HTTP " + responseCode + " khi kết nối đến Server.";
                }

                // gọi repaint để Swing vẽ lại Panel
                repaint();

            } catch (Exception e) {
                isError = true;
                statusMessage = "Lỗi kết nối: Không thể tải ảnh từ Server.";
                System.err.println("[ImagePreview] Exception: " + e.getMessage());
                e.printStackTrace();
                repaint();
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            double imgWidth = image.getWidth();
            double imgHeight = image.getHeight();
            double containerWidth = getWidth();
            double containerHeight = getHeight();

            double scale = Math.min(containerWidth / imgWidth, containerHeight / imgHeight);
            int targetWidth = (int) (imgWidth * scale);
            int targetHeight = (int) (imgHeight * scale);

            int x = (getWidth() - targetWidth) / 2;
            int y = (getHeight() - targetHeight) / 2;

            g2d.drawImage(image, x, y, targetWidth, targetHeight, null);
        } else {
            // Vẽ thông báo trạng thái hoặc lỗi ra giữa màn hình
            g.setColor(isError ? Color.decode("#ef4444") : Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(statusMessage)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(statusMessage, x, y);
        }
    }

    // hàm load ảnh từ local
    public void loadLocalImage(String fileName) {
        new Thread(() -> {
            try {
                java.io.File file = new java.io.File("student-web/src/main/webapp/uploads/minhchung/" + fileName);
                
                if (!file.exists()) {
                    isError = true;
                    statusMessage = "Lỗi: File không tồn tại trên ổ cứng tại: " + file.getAbsolutePath();
                    repaint();
                    return;
                }

                image = javax.imageio.ImageIO.read(file);
                
                if (image == null) {
                    isError = true;
                    statusMessage = "Lỗi: Không thể giải mã ảnh (Cần kiểm tra lại thư viện WebP).";
                }
                
                repaint();

            } catch (Exception e) {
                isError = true;
                statusMessage = "Lỗi khi đọc file ổ cứng: " + e.getMessage();
                e.printStackTrace();
                repaint();
            }
        }).start();
    }

}