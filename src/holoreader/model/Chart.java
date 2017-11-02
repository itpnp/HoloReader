/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holoreader.model;

import holoreader.gui.MainPage;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author USER
 */
public class Chart {
    private MainPage main;
    private List<Long> data = null;
    private Double index = 1.0;
    private Double volt = 0.0;
    private Double time;
    private DecimalFormat dFormat = new DecimalFormat("##.##");
    public Chart(MainPage main) {
        this.main = main;
    }

    public List<Long> getData() {
        return data;
    }

    public void setData(List<Long> data) {
        this.data = data;
    }
    
    public void showChart(){
        JPanel panel = createChartPanel();
        main.getjPanel2().removeAll();
        main.getjPanel2().setSize(763, 509);
        main.getjPanel2().add(panel);
        panel.setVisible(true);
        main.validate();
    }
    private JPanel createChartPanel() {
        String chartTitle = "Holo Reader Graph";
        String xAxisLabel = "TIME (S)";
        String yAxisLabel = "VOLT";
        
        XYDataset dataset = createDataset();
        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel,  dataset);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        XYSplineRenderer render = new XYSplineRenderer();
        render.setSeriesPaint(0, Color.BLUE);
        render.setSeriesShape(0, new Ellipse2D.Double(-1, -1, 2, 2));
        chart.getXYPlot().setRenderer(render);
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        NumberAxis time = (NumberAxis) plot.getDomainAxis();
        range.setRange(0.0, 5.0);
        time.setRange(0.00,0.08);
        time.setTickUnit(new NumberTickUnit(0.01));
        range.setTickUnit(new NumberTickUnit(1.0));
        return new ChartPanel(chart);
    }
    private XYDataset createDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("Sample Data");
        if(data.size() > 0 ){
          for(int i =0; i<data.size(); i++){
            volt = data.get(i)*0.019608;
            time = Double.valueOf((dFormat.format((index/3800))).replace(",", "."));
            series1.add((index/3800),volt);
//            System.out.println(time);
            index++;
          }
      }
        dataset.addSeries(series1);
        return dataset;
    }

}
