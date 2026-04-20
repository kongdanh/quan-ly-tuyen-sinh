package com.tuyensinh.admin.ui.dialog;

import com.tuyensinh.admin.ui.components.CustomTable;
import com.tuyensinh.admin.util.AdminSession;
import com.tuyensinh.admin.util.UIConstants;
import com.tuyensinh.model.ThiSinh;
import com.tuyensinh.model.YeuCauCapNhat;
import com.tuyensinh.service.YeuCauCapNhatService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DanhSachYeuCauDialog extends JDialog {

    private CustomTable        table;
    private DefaultTableModel  tableModel;
    private final YeuCauCapNhatService service = YeuCauCapNhatService.getInstance();
    private List<YeuCauCapNhat> cachedList;

    public DanhSachYeuCauDialog(Frame parent) {
        super(parent, "Danh sách yêu cầu sửa hồ sơ", true);
        setSize(820, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadData();
    }

    // ----------------------------------------------------------------
    // BUILD UI
    // ----------------------------------------------------------------

    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        header.setBackground(Color.decode(UIConstants.COLOR_BLUE));

        JLabel lbl = new JLabel("Danh sách yêu cầu cập nhật thông tin đang chờ duyệt");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 14f));
        header.add(lbl);
        return header;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "CCCD", "Điện thoại mới", "Email mới", "Khu vực", "Thời gian gửi", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new CustomTable();
        table.setModel(tableModel);

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (e.getClickCount() == 2 && row != -1 && cachedList != null) {
                    YeuCauCapNhat yc = cachedList.get(row);
                    openDuyetDialog(yc);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new EmptyBorder(8, 8, 8, 8));
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        footer.setBackground(Color.decode(UIConstants.COLOR_BG));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode(UIConstants.COLOR_BORDER)));

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setPreferredSize(new Dimension(100, 30));
        btnRefresh.addActionListener(e -> loadData());

        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 30));
        btnClose.addActionListener(e -> dispose());

        footer.add(btnRefresh);
        footer.add(btnClose);
        return footer;
    }

    private void loadData() {
        service.layDanhSachChoDuyet().thenAccept(list -> SwingUtilities.invokeLater(() -> {
            cachedList = list;
            tableModel.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (YeuCauCapNhat yc : list) {
                tableModel.addRow(new Object[]{
                    yc.getId(),
                    yc.getCccd(),
                    yc.getDienThoai() != null ? yc.getDienThoai() : "—",
                    yc.getEmail()     != null ? yc.getEmail()     : "—",
                    yc.getKhuVuc()    != null ? yc.getKhuVuc()    : "—",
                    yc.getNgayTao()   != null ? yc.getNgayTao().format(fmt) : "—",
                    yc.getTrangThai()
                });
            }
        }));
    }

    // ----------------------------------------------------------------
    // ACTION & UI SO SÁNH
    // ----------------------------------------------------------------

    private void openDuyetDialog(YeuCauCapNhat yc) {
        service.layThongTinGoc(yc.getCccd()).thenAccept(tsGoc -> {
            SwingUtilities.invokeLater(() -> {
                showDetailPopup(yc, tsGoc);
            });
        });
    }

    private void showDetailPopup(YeuCauCapNhat yc, ThiSinh tsGoc) {
        // Tạo Panel chứa nội dung
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        
        // Bảng HTML so sánh
        JLabel lblTable = new JLabel(buildComparisonHtml(yc, tsGoc));
        panel.add(lblTable, BorderLayout.CENTER);

        // btn Xem Minh Chứng
        JPanel pnlMinhChung = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnXemAnh = new JButton("Xem ảnh minh chứng đính kèm");
        
        btnXemAnh.addActionListener(e -> {
            String fileName = yc.getMinhChungUrl();
            if (fileName == null || fileName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có ảnh minh chứng.");
                return;
            }

            // Tạo một Dialog con để xem ảnh nhanh
            JDialog viewer = new JDialog(this, "Xem minh chứng: " + yc.getCccd(), true);
            viewer.setSize(900, 700);
            viewer.setLocationRelativeTo(this);
            viewer.setLayout(new BorderLayout());

            // 2. Sử dụng component vừa tạo ở Bước 1
            com.tuyensinh.admin.ui.components.ImagePreviewPanel preview = new com.tuyensinh.admin.ui.components.ImagePreviewPanel();
            preview.loadLocalImage(fileName);

            viewer.add(preview, BorderLayout.CENTER);

            // 3. Thêm nút đóng 
            JButton btnClose = new JButton("Đóng xem trước");
            btnClose.addActionListener(ev -> viewer.dispose());
            JPanel pnlBtn = new JPanel();
            pnlBtn.add(btnClose);
            viewer.add(pnlBtn, BorderLayout.SOUTH);

            viewer.setVisible(true);
        });
        
        pnlMinhChung.add(btnXemAnh);
        panel.add(pnlMinhChung, BorderLayout.SOUTH);

        // 2. Hiển thị Dialog
        int choice = JOptionPane.showOptionDialog(
            this, panel,
            "Duyệt yêu cầu — CCCD: " + yc.getCccd(),
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            new String[]{"Chấp nhận duyệt", "Từ chối", "Đóng"},
            "Đóng"
        );

        String currentAdmin = AdminSession.getInstance().getCurrentUsername();

        if (choice == 0) { // Chấp nhận
            if (service.duyetYeuCau(yc.getId(), null, currentAdmin)) {
                JOptionPane.showMessageDialog(this, "Đã duyệt và cập nhật hồ sơ cho TS: " + yc.getCccd());
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi! Không thể duyệt yêu cầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else if (choice == 1) { // Từ chối
            String lyDo = JOptionPane.showInputDialog(this, "Nhập lý do từ chối (bắt buộc):");
            if (lyDo != null && !lyDo.trim().isEmpty()) {
                if (service.tuChoiYeuCau(yc.getId(), lyDo, null, currentAdmin)) {
                    JOptionPane.showMessageDialog(this, "Đã từ chối yêu cầu của TS: " + yc.getCccd());
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi! Không thể từ chối yêu cầu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else if (lyDo != null) {
                JOptionPane.showMessageDialog(this, "Bạn phải nhập lý do để thí sinh biết!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // vẽ bảng so sánh
    private String buildComparisonHtml(YeuCauCapNhat yc, ThiSinh ts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><table style='border-collapse: collapse; width: 450px;' border='1' cellpadding='5'>");
        sb.append("<tr style='background-color: #e2e8f0;'><th align='left'>Trường dữ liệu</th><th align='left'>Thông tin Cũ</th><th align='left'>Yêu cầu Mới</th></tr>");

        appendHtmlRow(sb, "Điện thoại", ts.getDienThoai(), yc.getDienThoai());
        appendHtmlRow(sb, "Email", ts.getEmail(), yc.getEmail());
        appendHtmlRow(sb, "Khu vực", ts.getKhuVuc(), yc.getKhuVuc());
        appendHtmlRow(sb, "Nơi sinh", ts.getNoiSinh(), yc.getNoiSinh());
        appendHtmlRow(sb, "Đối tượng", ts.getDoiTuong(), yc.getDoiTuong());

        sb.append("</table><br><i style='color: #64748b;'>Chú ý: Dữ liệu màu đỏ là dữ liệu có sự thay đổi.</i></html>");
        return sb.toString();
    }

    // Hàm phụ trợ so sánh và highlight màu đỏ nếu khác nhau
    private void appendHtmlRow(StringBuilder sb, String label, String oldVal, String newVal) {
        String o = (oldVal != null && !oldVal.isBlank()) ? oldVal : "—";
        String n = (newVal != null && !newVal.isBlank()) ? newVal : "—";
        
        // Nếu trường mới có dữ liệu và khác trường cũ -> In đậm màu Đỏ
        String nDisplay = (!o.equals(n) && !n.equals("—")) ? "<b style='color:#dc2626;'>" + n + "</b>" : n;

        sb.append("<tr>")
          .append("<td><b>").append(label).append("</b></td>")
          .append("<td>").append(o).append("</td>")
          .append("<td>").append(nDisplay).append("</td>")
          .append("</tr>");
    }
}