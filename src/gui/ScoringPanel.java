package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.NumberFormat;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

/***
 * Sets up the scoring panel for DisCVR's GUI.
 * It uses JFreechart package to create a stacked bar graph.
 * 
 * @author Maha Maabar
 *
 */
public class ScoringPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	//format the panel to contain the chart
	public ScoringPanel (String[] names, int[] results, int [] shared){
        final CategoryDataset dataset = createDataset1(names,results, shared);       
	    JPanel chartPanel = createChartPanel(dataset);
	    add(chartPanel, BorderLayout.CENTER);
	    setPreferredSize(new Dimension(1080, 440));
    }
	
	//set the data for the chart
	private static CategoryDataset createDataset1(String [] virusNames, int []scores,int [] shared) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for(int i=0;i<virusNames.length;i++){
        	 dataset.addValue(scores[i],"Specific", virusNames[i]);
        	 dataset.addValue(shared[i],"Non-specific", virusNames[i]);
        }         
        return dataset;
    }
	
	//set the stacked bar chart
	private JPanel createChartPanel(final CategoryDataset dataset) {
        final JFreeChart chart = ChartFactory.createStackedBarChart(	        
            "",                                     // chart title
            "Virus Name",                           // domain (Y-) axis label
            "Number of Distinct classified K-mers", // range (X-) axis label
            dataset,                                // data
            PlotOrientation.HORIZONTAL,             // orientation
            false,                                  // include legend
            false,
            false
         );
         
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();       
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
         plot.setBackgroundPaint(Color.white);
         plot.setDomainGridlinePaint(Color.black);
	     plot.setRangeGridlinePaint(Color.black);    
	    
	     rangeAxis.setAutoRangeIncludesZero(true);
	     
	     //set the fonts for the axes titles
	     Font font = new Font("Dialog", Font.BOLD,25); 
	     plot.getDomainAxis().setLabelFont(font);
	     plot.getRangeAxis().setLabelFont(font);
	     
	     CategoryAxis axis = plot.getDomainAxis();

	     ValueAxis axis2 = plot.getRangeAxis();

	     axis.setTickLabelFont(new Font("verdana",Font.BOLD, 15));
	     axis2.setTickLabelFont(new Font("verdana",Font.BOLD,15));
	     
	     //set the font for the legend
	     LegendTitle legend = new LegendTitle(plot.getRenderer());
	     Font newfont = new Font("Arial",Font.BOLD,16); 
	     legend.setItemFont(newfont); 
	     legend.setPosition(RectangleEdge.BOTTOM); 
	     chart.addLegend(legend);

	      BarRenderer renderer = (BarRenderer) plot.getRenderer();
          renderer.setBaseItemLabelGenerator(
        	    new StandardCategoryItemLabelGenerator(
        	        "{2}", NumberFormat.getInstance()));
        
          renderer.setBaseItemLabelFont(new Font("SansSerif", Font.BOLD, 16), true);       
          renderer.setBaseItemLabelsVisible(true);
        
          renderer.setMaximumBarWidth(.35); // set maximum width to 35% of chart
        	
          final ChartPanel chartPanel = new ChartPanel(chart);
          chartPanel.setBackground(Color.BLACK);
        
          chartPanel.setPreferredSize(new Dimension(1100, 500));
          chartPanel.setMouseWheelEnabled(true);
       
          return chartPanel;     
        
    }

   public static void main(String [] args){
	   SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               JFrame theWindow = new JFrame("Window");
               String[] names = {"Maha","Maabar"};
               int[] results ={1,2};
               int [] shared ={3,4};
               theWindow.getContentPane().add(new ScoringPanel(names,results,shared));
               theWindow.pack();
               theWindow.setLocationByPlatform(true);
               theWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
               theWindow.setVisible(true);
           }
       });
	   
   }
	 
	
 }