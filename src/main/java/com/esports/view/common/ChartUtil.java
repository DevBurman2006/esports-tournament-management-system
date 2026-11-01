package com.esports.view.common;

import com.esports.config.ThemeConfig;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.util.Map;

/**
 * Utility for rendering JFreeChart visualizations styled with FlatLaf Cyber Dark theme.
 */
public class ChartUtil {

    private static final Color DARK_BG = ThemeConfig.COLOR_CARD_BG;
    private static final Color TEXT_COLOR = ThemeConfig.COLOR_TEXT_PRIMARY;
    private static final Color GRID_COLOR = new Color(45, 53, 72);

    private static final Color[] PALETTE = {
            ThemeConfig.COLOR_ACCENT_CYAN,
            ThemeConfig.COLOR_ACCENT_PURPLE,
            ThemeConfig.COLOR_SUCCESS,
            ThemeConfig.COLOR_WARNING,
            ThemeConfig.COLOR_DANGER,
            new Color(255, 111, 0),
            new Color(0, 184, 212),
            new Color(156, 39, 176)
    };

    /**
     * Creates a styled Donut / Pie Chart for Tournament Status distribution.
     */
    public static ChartPanel createPieChart(String title, Map<String, Integer> data) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        if (data != null) {
            data.forEach(dataset::setValue);
        }

        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        styleChartCommon(chart);

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(DARK_BG);
        plot.setOutlinePaint(null);
        plot.setLabelBackgroundPaint(new Color(26, 32, 44, 220));
        plot.setLabelPaint(TEXT_COLOR);
        plot.setLabelOutlinePaint(ThemeConfig.COLOR_BORDER);
        plot.setLabelFont(ThemeConfig.FONT_SMALL);

        // Apply palette colors
        int idx = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key, PALETTE[idx % PALETTE.length]);
            idx++;
        }

        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(DARK_BG);
        panel.setOpaque(false);
        return panel;
    }

    /**
     * Creates a styled Bar Chart for Game Popularity or Team/Player rankings.
     */
    public static ChartPanel createBarChart(String title, String categoryAxisLabel, String valueAxisLabel,
                                            Map<String, Integer> data, Color barColor) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if (data != null) {
            data.forEach((cat, val) -> dataset.addValue(val, "Metrics", cat));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                categoryAxisLabel,
                valueAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        styleChartCommon(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(DARK_BG);
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setOutlinePaint(null);

        // Dark axis labels
        plot.getDomainAxis().setTickLabelPaint(ThemeConfig.COLOR_TEXT_MUTED);
        plot.getDomainAxis().setLabelPaint(TEXT_COLOR);
        plot.getDomainAxis().setTickLabelFont(ThemeConfig.FONT_SMALL);
        plot.getRangeAxis().setTickLabelPaint(ThemeConfig.COLOR_TEXT_MUTED);
        plot.getRangeAxis().setLabelPaint(TEXT_COLOR);
        plot.getRangeAxis().setTickLabelFont(ThemeConfig.FONT_SMALL);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setSeriesPaint(0, barColor != null ? barColor : ThemeConfig.COLOR_ACCENT_CYAN);
        renderer.setShadowVisible(false);

        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(DARK_BG);
        panel.setOpaque(false);
        return panel;
    }

    private static void styleChartCommon(JFreeChart chart) {
        chart.setBackgroundPaint(DARK_BG);
        chart.setAntiAlias(true);

        TextTitle chartTitle = chart.getTitle();
        if (chartTitle != null) {
            chartTitle.setPaint(TEXT_COLOR);
            chartTitle.setFont(ThemeConfig.FONT_BOLD);
        }

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(DARK_BG);
            chart.getLegend().setItemPaint(ThemeConfig.COLOR_TEXT_MUTED);
            chart.getLegend().setItemFont(ThemeConfig.FONT_SMALL);
        }
    }
}
