package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.dto.DiemCongChiTietDTO;
import com.tuyensinh.service.XetTuyenService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin Dashboard Panel for viewing "Điểm Cộng Chi Tiết" (Bonus Points Detail).
 * Displays aggregated bonus information for candidates including IELTS, HSG, and regional priority bonuses.
 *
 * Features:
 * - Search by CCCD
 * - View all candidates/bonuses if no search filter
 * - Non-editable table with detailed bonus breakdown
 * - Clean layout with proper styling
 */
public class DiemCongChiTietPanel extends JPanel {

    private XetTuyenService xetTuyenService;
    private JTextField searchCccdField;
    private JButton searchButton;
    private JTable diemCongTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    // Table column indices
    private static final int COL_CCCD = 0;
    private static final int COL_HO_TEN = 1;
    private static final int COL_NV_THU = 2;
    private static final int COL_MA_NGANH = 3;
    private static final int COL_MA_TO_HOP = 4;
    private static final int COL_DIEM_TIENG_ANH = 5;
    private static final int COL_DIEM_HSG = 6;
    private static final int COL_TONG_DIEM = 7;

    private static final String[] COLUMN_NAMES = {
        "CCCD", "Họ Tên", "NV Thứ", "Mã Ngành", "Mã Tổ Hợp",
        "Điểm Tiếng Anh", "Điểm HSG", "Tổng Điểm Cộng"
    };

    public DiemCongChiTietPanel(XetTuyenService xetTuyenService) {
        this.xetTuyenService = xetTuyenService;
        this.setLayout(new BorderLayout(5, 5));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ════ TOP PANEL: Search Bar ════
        JPanel topPanel = createSearchPanel();
        this.add(topPanel, BorderLayout.NORTH);

        // ════ CENTER PANEL: Table ════
        // Initialize table model with columns (non-editable)
        this.tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // All cells non-editable
            }
        };

        this.diemCongTable = new JTable(tableModel);
        this.diemCongTable.setDefaultEditor(Object.class, null);  // Disable editing
        this.diemCongTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.diemCongTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        // Set column widths
        diemCongTable.getColumnModel().getColumn(COL_CCCD).setPreferredWidth(100);
        diemCongTable.getColumnModel().getColumn(COL_HO_TEN).setPreferredWidth(120);
        diemCongTable.getColumnModel().getColumn(COL_NV_THU).setPreferredWidth(60);
        diemCongTable.getColumnModel().getColumn(COL_MA_NGANH).setPreferredWidth(100);
        diemCongTable.getColumnModel().getColumn(COL_MA_TO_HOP).setPreferredWidth(100);
        diemCongTable.getColumnModel().getColumn(COL_DIEM_TIENG_ANH).setPreferredWidth(100);
        diemCongTable.getColumnModel().getColumn(COL_DIEM_HSG).setPreferredWidth(80);
        diemCongTable.getColumnModel().getColumn(COL_TONG_DIEM).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(diemCongTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách Điểm Cộng Chi Tiết"));
        this.add(scrollPane, BorderLayout.CENTER);

        // ════ BOTTOM PANEL: Status Bar ════
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.statusLabel = new JLabel("Sẵn sàng...");
        bottomPanel.add(statusLabel);
        this.add(bottomPanel, BorderLayout.SOUTH);

        // ════ LOAD INITIAL DATA ════
        loadDataToTable("");
    }

    /**
     * Creates the search panel (top section)
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Tìm kiếm"));

        // CCCD label and field
        JLabel cccdLabel = new JLabel("CCCD:");
        this.searchCccdField = new JTextField(15);
        this.searchCccdField.setToolTipText("Nhập CCCD để tìm kiếm (để trống để xem tất cả)");

        // Search button
        this.searchButton = new JButton("Lọc/Tìm kiếm");
        this.searchButton.addActionListener(e -> onSearchButtonClicked());

        // Reset button
        JButton resetButton = new JButton("Làm mới");
        resetButton.addActionListener(e -> onResetButtonClicked());

        // Add components to panel
        panel.add(cccdLabel);
        panel.add(searchCccdField);
        panel.add(searchButton);
        panel.add(resetButton);

        return panel;
    }

    /**
     * Loads data from service and populates the table.
     * Clears existing table rows and adds fresh data.
     *
     * @param searchCccd CCCD filter (empty string or null for all records)
     */
    private void loadDataToTable(String searchCccd) {
        try {
            // Clear existing rows
            tableModel.setRowCount(0);

            // Update status
            updateStatus("Đang tải dữ liệu...");

            // Call service to get data
            List<DiemCongChiTietDTO> dataList = xetTuyenService.layDanhSachDiemCong(searchCccd);

            // Populate table
            for (DiemCongChiTietDTO dto : dataList) {
                Object[] row = {
                    dto.getCccd(),
                    dto.getHoTen(),
                    dto.getNguyenVongThu(),
                    dto.getMaNganh(),
                    dto.getMaToHop(),
                    String.format("%.2f", dto.getDiemCongTiengAnh()),
                    String.format("%.2f", dto.getDiemCongHsg()),
                    String.format("%.2f", dto.getTongDiemCong())
                };
                tableModel.addRow(row);
            }

            // Update status with result count
            String filterInfo = searchCccd != null && !searchCccd.trim().isEmpty() 
                ? " (CCCD: " + searchCccd + ")" 
                : " (Tất cả)";
            updateStatus("Tải xong. Tổng: " + dataList.size() + " bản ghi" + filterInfo);

        } catch (Exception ex) {
            String errorMsg = "Lỗi tải dữ liệu: " + ex.getMessage();
            System.err.println("[DiemCongChiTietPanel] " + errorMsg);
            ex.printStackTrace();
            updateStatus("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, errorMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Event handler: Search button clicked
     */
    private void onSearchButtonClicked() {
        String searchCccd = searchCccdField.getText().trim();
        loadDataToTable(searchCccd);
    }

    /**
     * Event handler: Reset button clicked
     */
    private void onResetButtonClicked() {
        searchCccdField.setText("");
        loadDataToTable("");
    }

    /**
     * Update the status label at the bottom
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Test/Demo method to run this panel standalone
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame("Quản Lý Điểm Cộng Chi Tiết");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 600);

            // Initialize service
            XetTuyenService service = new XetTuyenService();

            DiemCongChiTietPanel panel = new DiemCongChiTietPanel(service);
            frame.add(panel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
