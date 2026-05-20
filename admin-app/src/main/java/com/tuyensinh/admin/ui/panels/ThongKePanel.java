// admin-app/src/main/java/com/tuyensinh/admin/ui/panels/ThongKePanel.java
package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.components.CustomComboBox;
import com.tuyensinh.model.DotTuyenSinh;
import com.tuyensinh.service.DotTuyenSinhService;
import com.tuyensinh.service.ThongKeService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class ThongKePanel extends JPanel {
    
    private final ThongKeService tkService = new ThongKeService();
    private final DotTuyenSinhService dotService = new DotTuyenSinhService();
    
    private CustomComboBox<DotTuyenSinh> cbDot;
    private JTabbedPane mainTabs;
    
    // Components cho các tab
    private JPanel pnlCards;
    private JPanel pnlOverviewCharts;
    private JPanel pnlStudentCharts;
    private JPanel pnlMajorCharts;
    private JTable tblNganh;
    private DefaultTableModel modelNganh;
    private JTable tblPhuongThuc;
    private DefaultTableModel modelPhuongThuc;
    private JPanel pnlTopTinhThanh;
    private JTable tblTinhThanh;
    private DefaultTableModel modelTinhThanh;
    private JPanel pnlDoiTuong;
    private JTable tblDoiTuong;
    private DefaultTableModel modelDoiTuong;
    
    public ThongKePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));
        initHeader();
        initMainTabs();
        loadInitialData();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                reloadAllData();
            }
        });
    }
    
    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        com.tuyensinh.admin.ui.components.HeaderPanel headerTitle = new com.tuyensinh.admin.ui.components.HeaderPanel(
            "THỐNG KÊ TUYỂN SINH", "Dữ liệu thống kê thời gian thực theo các đợt xét tuyển");
        header.add(headerTitle, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        JLabel lblDot = new JLabel("Đợt tuyển sinh:");
        lblDot.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rightPanel.add(lblDot);
        
        cbDot = new CustomComboBox<>(new DotTuyenSinh[0]);
        cbDot.setPreferredSize(new Dimension(280, 32));
        cbDot.addActionListener(e -> reloadAllData());
        rightPanel.add(cbDot);
        
        header.add(rightPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }
    
    private void initMainTabs() {
        mainTabs = new JTabbedPane();
        mainTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainTabs.setBorder(new EmptyBorder(10, 15, 15, 15));
        
        mainTabs.addTab("TỔNG QUAN", buildOverviewTab());
        mainTabs.addTab("THÍ SINH", buildStudentTab());
        mainTabs.addTab("NGÀNH HỌC", buildMajorTab());
        
        add(mainTabs, BorderLayout.CENTER);
    }
    
    private JPanel buildOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        
        // 4 thẻ thống kê chính
        pnlCards = new JPanel(new GridLayout(1, 4, 15, 0));
        pnlCards.setOpaque(false);
        pnlCards.setPreferredSize(new Dimension(0, 120));
        panel.add(pnlCards, BorderLayout.NORTH);
        
        // 2 biểu đồ tròn cho tổng quan
        pnlOverviewCharts = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlOverviewCharts.setOpaque(false);
        panel.add(pnlOverviewCharts, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel buildStudentTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        
        // Hàng 1: Giới tính + Khu vực (2 biểu đồ tròn)
        JPanel row1 = new JPanel(new GridLayout(1, 2, 15, 0));
        row1.setOpaque(false);
        panel.add(row1, BorderLayout.NORTH);
        pnlStudentCharts = row1;
        
        // Hàng 2: Top tỉnh thành + Đối tượng ưu tiên (2 table)
        JPanel row2 = new JPanel(new GridLayout(1, 2, 15, 0));
        row2.setOpaque(false);
        row2.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        // Bảng Tỉnh/Thành
        String[] colsTT = {"Tỉnh/Thành", "Số lượng thí sinh"};
        modelTinhThanh = new DefaultTableModel(colsTT, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTinhThanh = new JTable(modelTinhThanh);
        tblTinhThanh.setRowHeight(30);
        tblTinhThanh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblTinhThanh.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblTinhThanh.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        JScrollPane scrollTT = new JScrollPane(tblTinhThanh);
        scrollTT.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        JPanel wrapperTT = new JPanel(new BorderLayout());
        wrapperTT.setBackground(Color.WHITE);
        wrapperTT.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(12, 12, 12, 12)
        ));
        JLabel lblTT = new JLabel("Top Tỉnh/Thành có nhiều thí sinh");
        lblTT.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTT.setForeground(new Color(33, 37, 41));
        lblTT.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrapperTT.add(lblTT, BorderLayout.NORTH);
        wrapperTT.add(scrollTT, BorderLayout.CENTER);
        
        pnlTopTinhThanh = new JPanel(new BorderLayout());
        pnlTopTinhThanh.setOpaque(false);
        pnlTopTinhThanh.add(wrapperTT, BorderLayout.CENTER);
        row2.add(pnlTopTinhThanh);
        
        // Bảng Đối tượng ưu tiên
        String[] colsDt = {"Đối tượng ưu tiên", "Số lượng thí sinh"};
        modelDoiTuong = new DefaultTableModel(colsDt, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDoiTuong = new JTable(modelDoiTuong);
        tblDoiTuong.setRowHeight(30);
        tblDoiTuong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblDoiTuong.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        tblDoiTuong.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        JScrollPane scrollDt = new JScrollPane(tblDoiTuong);
        scrollDt.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        JPanel wrapperDt = new JPanel(new BorderLayout());
        wrapperDt.setBackground(Color.WHITE);
        wrapperDt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(12, 12, 12, 12)
        ));
        JLabel lblDt = new JLabel("Thống kê theo Đối tượng");
        lblDt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDt.setForeground(new Color(33, 37, 41));
        lblDt.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrapperDt.add(lblDt, BorderLayout.NORTH);
        wrapperDt.add(scrollDt, BorderLayout.CENTER);
        
        JPanel pnlDoiTuongWrapper = new JPanel(new BorderLayout());
        pnlDoiTuongWrapper.setOpaque(false);
        pnlDoiTuongWrapper.add(wrapperDt, BorderLayout.CENTER);
        row2.add(pnlDoiTuongWrapper);
        
        panel.add(row2, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel buildMajorTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        
        // Hàng trên: Top ngành + Top tổ hợp môn
        JPanel topRow = new JPanel(new GridLayout(1, 2, 15, 0));
        topRow.setOpaque(false);
        topRow.setPreferredSize(new Dimension(0, 320));
        pnlMajorCharts = topRow;
        panel.add(topRow, BorderLayout.NORTH);
        
        JTabbedPane centerTabs = new JTabbedPane();
        centerTabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Bảng chi tiết ngành
        String[] cols = {"Mã ngành", "Tên ngành", "Chỉ tiêu", "Số ĐK", "Trúng tuyển", "Lấp đầy", "Trạng thái"};
        modelNganh = new DefaultTableModel(cols, 0) {
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };
        tblNganh = new JTable(modelNganh);
        tblNganh.setRowHeight(30);
        tblNganh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblNganh.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Căn giữa các cột số
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 2; i <= 5; i++) {
            tblNganh.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scroll = new JScrollPane(tblNganh);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        centerTabs.addTab("Chi tiết lấp đầy ngành", scroll);

        // Bảng phương thức
        String[] colsPt = {"Tên ngành", "Phương thức xét tuyển", "Số lượng trúng tuyển"};
        modelPhuongThuc = new DefaultTableModel(colsPt, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPhuongThuc = new JTable(modelPhuongThuc);
        tblPhuongThuc.setRowHeight(30);
        tblPhuongThuc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblPhuongThuc.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane scroll2 = new JScrollPane(tblPhuongThuc);
        scroll2.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), new EmptyBorder(10, 10, 10, 10)
        ));
        centerTabs.addTab("Trúng tuyển theo phương thức", scroll2);

        panel.add(centerTabs, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadInitialData() {
        dotService.findPageWithFilters("", List.of(), Map.of(), 1, 100).thenAccept(dots -> {
            SwingUtilities.invokeLater(() -> {
                DotTuyenSinh all = new DotTuyenSinh();
                all.setId(0);
                all.setTenDot("-- Tất cả các đợt --");
                cbDot.addItem(all);
                for (DotTuyenSinh d : dots) {
                    cbDot.addItem(d);
                }
                if (cbDot.getItemCount() > 0) {
                    cbDot.setSelectedIndex(0);
                }
                reloadAllData();
            });
        });
    }
    
    private void reloadAllData() {
        DotTuyenSinh sel = (DotTuyenSinh) cbDot.getSelectedItem();
        if (sel == null) return;
        Integer idDot = (sel.getId() != null && sel.getId() > 0) ? sel.getId() : null;
        
        // Tab Tổng quan
        loadCards(idDot);
        loadTrangThaiXetTuyen(idDot);
        loadPhuongThucXetTuyen(idDot);
        
        // Tab Thí sinh
        loadGioiTinh(idDot);
        loadKhuVuc(idDot);
        loadTopTinhThanh(idDot);
        loadDoiTuongUuTien(idDot);
        
        // Tab Ngành
        loadTopNganh(idDot);
        loadTopToHopMon(idDot);
        loadChiTietNganh(idDot);
        loadTrungTuyenTheoPhuongThuc(idDot);
    }
    
    // ==================== LOAD DỮ LIỆU TỔNG QUAN ====================
    
    private void loadCards(Integer idDot) {
        tkService.getTongQuanStats(idDot).thenAccept(stats -> SwingUtilities.invokeLater(() -> {
            pnlCards.removeAll();
            
            addStatCard("Tổng hồ sơ", formatNumber(stats.get("tongHoSo")), new Color(59, 130, 246));
            addStatCard("Hồ sơ hợp lệ", formatNumber(stats.get("tongHoSoHopLe")), new Color(16, 185, 129));
            addStatCard("Trúng tuyển", formatNumber(stats.get("tongTrungTuyen")), new Color(245, 158, 11));
            addStatCard("Tổng chỉ tiêu", formatNumber(stats.get("tongChiTieu")), new Color(139, 92, 246));
            
            pnlCards.revalidate();
            pnlCards.repaint();
        }));
    }
    
    private void addStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(new Color(100, 100, 100));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(color);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        
        card.add(lblTitle, BorderLayout.WEST);
        card.add(lblValue, BorderLayout.EAST);
        
        pnlCards.add(card);
    }
    
    private void loadTrangThaiXetTuyen(Integer idDot) {
        tkService.getTrangThaiXetTuyenThiSinh(idDot).thenAccept(data -> SwingUtilities.invokeLater(() -> {
            JPanel wrapper = createPieChartWrapper("Trạng thái xét tuyển của thí sinh", data);
            replaceOrAddChart(pnlOverviewCharts, wrapper, 0);
        }));
    }
    
    private void loadPhuongThucXetTuyen(Integer idDot) {
        tkService.getPhanBoTheoPhuongThuc(idDot).thenAccept(data -> SwingUtilities.invokeLater(() -> {
            JPanel wrapper = createPieChartWrapper("Phân bố theo phương thức xét tuyển", data);
            replaceOrAddChart(pnlOverviewCharts, wrapper, 1);
        }));
    }
    
    // ==================== LOAD DỮ LIỆU THÍ SINH ====================
    
    private void loadGioiTinh(Integer idDot) {
        tkService.getThongKeGioiTinh(idDot).thenAccept(data -> SwingUtilities.invokeLater(() -> {
            JPanel wrapper = createPieChartWrapper("Cơ cấu giới tính", data);
            replaceOrAddChart(pnlStudentCharts, wrapper, 0);
        }));
    }
    
    private void loadKhuVuc(Integer idDot) {
        tkService.getThongKeKhuVuc(idDot).thenAccept(data -> SwingUtilities.invokeLater(() -> {
            JPanel wrapper = createPieChartWrapper("Phân bố theo khu vực", data);
            replaceOrAddChart(pnlStudentCharts, wrapper, 1);
        }));
    }
    
    private void loadTopTinhThanh(Integer idDot) {
        tkService.getTopTinhThanh(idDot, 10).thenAccept(data -> SwingUtilities.invokeLater(() -> {
            modelTinhThanh.setRowCount(0);
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                modelTinhThanh.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
        }));
    }
    
    private void loadDoiTuongUuTien(Integer idDot) {
        tkService.getThongKeDoiTuongUuTien(idDot).thenAccept(data -> SwingUtilities.invokeLater(() -> {
            modelDoiTuong.setRowCount(0);
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                modelDoiTuong.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
        }));
    }
    
    // ==================== LOAD DỮ LIỆU NGÀNH ====================
    
    private void loadTopNganh(Integer idDot) {
        tkService.getTopNganhTheoNguyenVong(idDot, 10).thenAccept(data -> SwingUtilities.invokeLater(() -> {
            JPanel wrapper = createBarChartWrapper("Top 10 ngành có nhiều nguyện vọng nhất", data, "Ngành học", "Số nguyện vọng", false);
            replaceOrAddChart(pnlMajorCharts, wrapper, 0);
        }));
    }
    
    private void loadTopToHopMon(Integer idDot) {
        tkService.getTopToHopMon(idDot, 10).thenAccept(data -> SwingUtilities.invokeLater(() -> {
            JPanel wrapper = createBarChartWrapper("Top 10 tổ hợp môn được chọn nhiều nhất", data, "Tổ hợp môn", "Số nguyện vọng", false);
            replaceOrAddChart(pnlMajorCharts, wrapper, 1);
        }));
    }
    
    private void loadChiTietNganh(Integer idDot) {
        tkService.getChiTietLapDayNganh(idDot).thenAccept(data -> SwingUtilities.invokeLater(() -> {
            modelNganh.setRowCount(0);
            for (Object[] row : data) {
                modelNganh.addRow(row);
            }
            // Custom renderer cho cột trạng thái
            tblNganh.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        }));
    }
    
    private void loadTrungTuyenTheoPhuongThuc(Integer idDot) {
        tkService.getSoLuongTrungTuyenTheoPhuongThuc(idDot).thenAccept(data -> SwingUtilities.invokeLater(() -> {
            modelPhuongThuc.setRowCount(0);
            for (Object[] row : data) {
                modelPhuongThuc.addRow(row);
            }
        }));
    }
    
    // ==================== CHART HELPERS ====================
    
    private void replaceOrAddChart(JPanel container, JPanel newChart, int index) {
        SwingUtilities.invokeLater(() -> {
            if (container.getComponentCount() > index) {
                container.remove(index);
                container.add(newChart, index);
            } else {
                container.add(newChart);
            }
            container.revalidate();
            container.repaint();
        });
    }
    
    private JPanel createPieChartWrapper(String title, Map<String, Long> data) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(12, 12, 12, 12)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(33, 37, 41));
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrapper.add(lblTitle, BorderLayout.NORTH);
        
        JFreeChart chart = createPieChart(data);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(0, 280));
        wrapper.add(chartPanel, BorderLayout.CENTER);
        
        return wrapper;
    }
    
    private JPanel createBarChartWrapper(String title, Map<String, Long> data, String xLabel, String yLabel, boolean vertical) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(12, 12, 12, 12)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(33, 37, 41));
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrapper.add(lblTitle, BorderLayout.NORTH);
        
        JFreeChart chart = createBarChart(data, xLabel, yLabel, vertical);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(0, 280));
        wrapper.add(chartPanel, BorderLayout.CENTER);
        
        return wrapper;
    }
    
    private JFreeChart createPieChart(Map<String, Long> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            if (entry.getValue() > 0) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
        }
        
        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})", new DecimalFormat("0"), new DecimalFormat("0%")));
        plot.setShadowPaint(null);
        plot.setBackgroundPaint(null);
        plot.setOutlineVisible(false);
        plot.setSimpleLabels(true);
        
        return chart;
    }
    
    private JFreeChart createBarChart(Map<String, Long> data, String xLabel, String yLabel, boolean vertical) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), yLabel, entry.getKey());
        }
        
        PlotOrientation orientation = vertical ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL;
        JFreeChart chart = ChartFactory.createBarChart(null, xLabel, yLabel, dataset, orientation, false, true, false);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(59, 130, 246));
        
        if (!vertical) {
            renderer.setMaximumBarWidth(0.08);
        }
        
        return chart;
    }
    
    // ==================== UTILITIES ====================
    
    private String formatNumber(Object obj) {
        if (obj == null) return "0";
        if (obj instanceof Long) {
            long val = (Long) obj;
            if (val >= 1_000_000) return String.format("%.1fM", val / 1_000_000.0);
            if (val >= 1_000) return String.format("%.1fK", val / 1_000.0);
            return String.valueOf(val);
        }
        return obj.toString();
    }
    
    private Color getStatusColor(String status) {
        if (status.contains("Đã đủ")) return new Color(220, 53, 69);
        if (status.contains("Gần đủ")) return new Color(255, 193, 7);
        if (status.contains("Đang tuyển")) return new Color(25, 135, 84);
        return new Color(13, 110, 253);
    }
    
    // Cell renderer cho cột trạng thái
    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected && value != null) {
                String status = value.toString();
                c.setBackground(getStatusColor(status));
                c.setForeground(Color.WHITE);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            return c;
        }
    }
}