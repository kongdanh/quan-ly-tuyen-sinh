    package com.tuyensinh.admin.ui.panels;

    import java.awt.Color;
    import java.awt.Component;
    import java.awt.Dimension;
    import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
    import java.util.List;
    import java.util.Map;
    import java.util.concurrent.CompletableFuture;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
    import javax.swing.JPanel;
    import javax.swing.JProgressBar;
    import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;

import com.tuyensinh.admin.ui.base.BaseTablePanel;
import com.tuyensinh.admin.ui.dialog.NganhToHopDialog;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.dto.NganhDTO;
import com.tuyensinh.model.Nganh;
    import com.tuyensinh.service.NganhService;

    public class NganhToHopPanel extends BaseTablePanel<NganhDTO> {

        private final NganhService nganhService = new NganhService();

        public NganhToHopPanel() {
            super("Ngành tuyển sinh", "Quản lý danh sách ngành và chỉ tiêu tuyển sinh");
            loadTableData();
        }

        // ================================================================
        // COLUMNS
        // ================================================================
        @Override
        protected String[] getColumnNames() {
            return new String[]{
                    "Mã ngành",
                    "Tên ngành",
                    "Khoa",
                    "Chỉ tiêu",
                    "Đã đăng ký",
                    "Tỉ lệ (%)",
                    "Trạng thái",
                    "Thao tác"
            };
        }

        @Override
        protected Object[] toTableRow(NganhDTO n) {
            return new Object[]{
                    n.getMaNganh(),
                    n.getTenNganh(),
                    n.getKhoa() == null ? "—" : n.getKhoa(),
                    n.getChiTieu(),
                    n.getDaDangKy(),
                    n.getTiLe(),
                    n.getTrangThai(),
                    ""
            };
        }

        // ================================================================
        // DATA (async wrapper)
        // ================================================================
        @Override
        protected CompletableFuture<List<NganhDTO>> fetchPage(String keyword, Map<String, Object> filters, int page, int pageSize) {
            return CompletableFuture.supplyAsync(nganhService::getAllWithStats);
        }

        @Override
        protected CompletableFuture<Long> fetchCount(String keyword, Map<String, Object> filters) {
            return CompletableFuture.supplyAsync(() ->
                    (long) nganhService.getAllWithStats().size()
            );
        }

        // ================================================================
        // CRUD
        // ================================================================
        @Override
        protected void showAddDialog() {
            Nganh newNganh = new Nganh(); 
            Window window = SwingUtilities.getWindowAncestor(this);

            Frame parentFrame = (window instanceof Frame) ? (Frame) window : null;

            NganhToHopDialog dialog = new NganhToHopDialog(parentFrame, newNganh, true);
            dialog.setVisible(true);
            
            if (dialog.isSaved()) {
                showSuccess("Thêm ngành mới thành công!");
                loadTableData();
            }
        }

        @Override
        protected void showEditDialog(int row) {
            String maNganh = getMaNganhFromRow(row);

            Nganh currentNganh = nganhService.findByMA(maNganh).orElse(null);

            if (currentNganh != null) {
                Window parent = SwingUtilities.getWindowAncestor(this);
                NganhToHopDialog dialog = new NganhToHopDialog((Frame) parent, currentNganh, false);
                dialog.setVisible(true);

                if (dialog.isSaved()) {
                    showSuccess("Cập nhật thông tin thành công!");
                    loadTableData();
                }
            }
        }

        @Override
        protected void deleteRecord(int row) {
            String maNganh = getMaNganhFromRow(row);

            if (!confirmDelete(maNganh)) return;

            // TODO: gọi service delete
            showSuccess("Đã xóa ngành: " + maNganh);
        }

        // ================================================================
        // KEY HANDLING (THAY THẾ ID INT)
        // ================================================================
        private String getMaNganhFromRow(int row) {
            return (String) getCellValue(row, 0);
        }

        @Override
        protected String getNameFromRow(int row) {
            Object val = getCellValue(row, 1);
            return val != null ? val.toString() : "";
        }

        // ================================================================
        // TABLE CONFIG
        // ================================================================
        @Override
        protected void configureColumns() {

            table.getColumnModel().getColumn(0).setPreferredWidth(120);
            
            // center renderer
            DefaultTableCellRenderer center = new DefaultTableCellRenderer();
            center.setHorizontalAlignment(JLabel.CENTER);

            int[] centerCols = {3, 4, 5, 6};
            for (int col : centerCols) {
                table.getColumnModel().getColumn(col).setCellRenderer(center);
            }

            table.getColumnModel().getColumn(5).setCellRenderer(new ProgressRenderer());
            table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        }

        // ================================================================
        // PROGRESS RENDERER
        // ================================================================
       class ProgressRenderer extends JPanel implements javax.swing.table.TableCellRenderer {

    private final JProgressBar bar = new JProgressBar(0, 100);
    private final JLabel label = new JLabel();

    public ProgressRenderer() {
        // Sử dụng GridBagLayout để kiểm soát vị trí chính xác
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Cấu hình thanh Bar   
        bar.setPreferredSize(new Dimension(150, 6)); // Độ rộng 150, cao 6 (mỏng)
        bar.setMinimumSize(new Dimension(50, 6));
        bar.setBackground(Color.decode("#EEEEEE"));
        bar.setForeground(Color.decode(UIConstants.COLOR_NAVY));
        bar.setBorderPainted(false); // Xóa viền để trông thanh thoát hơn

        // Thêm thanh Bar vào Layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Chiếm không gian ngang
        gbc.fill = GridBagConstraints.HORIZONTAL; // Chỉ giãn ngang, không giãn dọc
        gbc.insets = new Insets(0, 5, 0, 5); // Cách lề trái phải
        add(bar, gbc);

        // Cấu hình nhãn Label
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridx = 1;
        gbc.weightx = 0; // Không chiếm thêm không gian
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 8); // Cách lề phải 8px
        add(label, gbc);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        int progress = (value instanceof Integer) ? (int) value : 0;
        bar.setValue(progress);
        label.setText(progress + "%");

        // Màu nền khi được chọn (để trông chuyên nghiệp hơn)
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
        } else {
            setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
        }
        
        return this;
    }
}

        // ================================================================
        // STATUS RENDERER
        // ================================================================
        class StatusRenderer extends DefaultTableCellRenderer {
            private final JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
            private final RoundBadge badge = new RoundBadge(20); // Bán kính 20 cho tròn hơn

            public StatusRenderer() {
                container.add(badge);
            }

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {

                String status = (value != null) ? value.toString() : "";
                badge.setText(status);

                // Xác định màu sắc
                Color bg;
                if ("Đang tuyển".equals(status)) {
                    bg = Color.decode(UIConstants.BTN_SUCCESS_BG);
                } else if ("Đã đủ chỉ tiêu".equals(status)) {
                    bg = Color.decode(UIConstants.BADGE_DANGER_BG);
                } else {
                    bg = Color.decode(UIConstants.BADGE_WARNING_BG);
                }

                // Cấu hình Badge
                badge.setBackground(bg);
                badge.setForeground(Color.WHITE);
                badge.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

                // Cấu hình Container (phần nền của cả ô Table)
                if (isSelected) {
                    container.setBackground(table.getSelectionBackground());
                } else {
                    // Đảm bảo nền khớp với màu dòng bảng (trắng hoặc sọc - striped)
                    container.setBackground(table.getBackground());
                }

                return container;
            }
        }

        class RoundBadge extends JLabel {
            private int radius = 15;

            public RoundBadge(int radius) {
                this.radius = radius;
                setOpaque(false); // Bắt buộc false để thấy góc bo
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ nền bo tròn
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                
                g2.dispose();
                super.paintComponent(g); // Vẽ chữ lên trên nền đã bo
            }
        }
}