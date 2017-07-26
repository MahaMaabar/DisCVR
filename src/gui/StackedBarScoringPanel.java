package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.text.NumberFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.StandardGradientPaintTransformer;


public class StackedBarScoringPanel extends ApplicationFrame  {
	
	/**
	 * A simple demonstration application showing how to create a stacked bar chart
	 * using data from a {@link CategoryDataset}.
	 */
	
	    public StackedBarScoringPanel(final String title) {
	        super(title);
	        final CategoryDataset dataset = createDataset();
	        final JFreeChart chart = createChart(dataset);
	        final ChartPanel chartPanel = new ChartPanel(chart);
	        chartPanel.setPreferredSize(new java.awt.Dimension(590, 350));
	        setContentPane(chartPanel);
	    }
	    
	    /**
	     * Creates a sample dataset.
	     * 
	     * @return A sample dataset.
	     */
	    private CategoryDataset createDataset() {
	        DefaultCategoryDataset result = new DefaultCategoryDataset();
	        
	        result.addValue(20.3, "Specific", "Human Virus A");
	        result.addValue(27.2, "Specific", "Human Virus A49");
	        result.addValue(19.7, "Specific", "Human Virus C");
	        
	        result.addValue(23.3, "Shared", "Human Virus A");
	        result.addValue(16.2,  "Shared", "Human Virus A49");
	        result.addValue(28.7, "Shared", "Human Virus C");
	       
	        
	       /* result.addValue(20.3, "Product 1 (US)", "Jan 04");
	        result.addValue(27.2, "Product 1 (US)", "Feb 04");
	        result.addValue(19.7, "Product 1 (US)", "Mar 04");
	        result.addValue(19.4, "Product 1 (Europe)", "Jan 04");
	        result.addValue(10.9, "Product 1 (Europe)", "Feb 04");
	        result.addValue(18.4, "Product 1 (Europe)", "Mar 04");
	        result.addValue(16.5, "Product 1 (Asia)", "Jan 04");
	        result.addValue(15.9, "Product 1 (Asia)", "Feb 04");
	        result.addValue(16.1, "Product 1 (Asia)", "Mar 04");
	        result.addValue(13.2, "Product 1 (Middle East)", "Jan 04");
	        result.addValue(14.4, "Product 1 (Middle East)", "Feb 04");
	        result.addValue(13.7, "Product 1 (Middle East)", "Mar 04");

	        result.addValue(23.3, "Product 2 (US)", "Jan 04");
	        result.addValue(16.2, "Product 2 (US)", "Feb 04");
	        result.addValue(28.7, "Product 2 (US)", "Mar 04");
	        result.addValue(12.7, "Product 2 (Europe)", "Jan 04");
	        result.addValue(17.9, "Product 2 (Europe)", "Feb 04");
	        result.addValue(12.6, "Product 2 (Europe)", "Mar 04");
	        result.addValue(15.4, "Product 2 (Asia)", "Jan 04");
	        result.addValue(21.0, "Product 2 (Asia)", "Feb 04");
	        result.addValue(11.1, "Product 2 (Asia)", "Mar 04");
	        result.addValue(23.8, "Product 2 (Middle East)", "Jan 04");
	        result.addValue(23.4, "Product 2 (Middle East)", "Feb 04");
	        result.addValue(19.3, "Product 2 (Middle East)", "Mar 04");

	        result.addValue(11.9, "Product 3 (US)", "Jan 04");
	        result.addValue(31.0, "Product 3 (US)", "Feb 04");
	        result.addValue(22.7, "Product 3 (US)", "Mar 04");
	        result.addValue(15.3, "Product 3 (Europe)", "Jan 04");
	        result.addValue(14.4, "Product 3 (Europe)", "Feb 04");
	        result.addValue(25.3, "Product 3 (Europe)", "Mar 04");
	        result.addValue(23.9, "Product 3 (Asia)", "Jan 04");
	        result.addValue(19.0, "Product 3 (Asia)", "Feb 04");
	        result.addValue(10.1, "Product 3 (Asia)", "Mar 04");
	        result.addValue(13.2, "Product 3 (Middle East)", "Jan 04");
	        result.addValue(15.5, "Product 3 (Middle East)", "Feb 04");
	        result.addValue(10.1, "Product 3 (Middle East)", "Mar 04");*/
	        
	        return result;
	    }
	    
	    /**
	     * Creates a sample chart.
	     * 
	     * @param dataset  the dataset for the chart.
	     * 
	     * @return A sample chart.
	     */
	    private JFreeChart createChart(final CategoryDataset dataset) {

	        final JFreeChart chart = ChartFactory.createStackedBarChart(
	            "Stacked Bar Chart Demo 4",  // chart title
	            "Virus Names",                  // domain axis label
	            "No. of Distinct Classified k-mers",                     // range axis label
	            dataset,                     // data
	            PlotOrientation.HORIZONTAL,    // the plot orientation
	            false,                        // legend
	            false,                        // tooltips
	            false                        // urls
	        );
	        
	       
	        
	       
	        CategoryPlot plot = (CategoryPlot) chart.getPlot();
	      
	        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
	        
	        // change the auto tick unit selection to integer units only...
	        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	       
	        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	        
	        
	        plot.setBackgroundPaint(Color.lightGray);
	 	   
		     plot.setDomainGridlinePaint(Color.white);
		     plot.setRangeGridlinePaint(Color.white);
		        
		     rangeAxis.setAutoRangeIncludesZero(true);
		     
		     //set the font for the axes titles
		     Font font = new Font("Dialog", Font.PLAIN,25); 
		     plot.getDomainAxis().setLabelFont(font);
		     plot.getRangeAxis().setLabelFont(font);
		     
		     CategoryAxis axis = plot.getDomainAxis();

		     ValueAxis axis2 = plot.getRangeAxis();

		     axis.setTickLabelFont(new Font("verdana",Font.BOLD, 15));
		     axis2.setTickLabelFont(new Font("verdana",Font.BOLD,15));
		     
		     //set the font for the legend
		     LegendTitle legend = new LegendTitle(plot.getRenderer());
		     Font newfont = new Font("Arial",0,12); 
		     legend.setItemFont(newfont); 
		     legend.setPosition(RectangleEdge.BOTTOM); 
		     chart.addLegend(legend);

	        
	        
	        BarRenderer renderer = (BarRenderer) plot.getRenderer();
	        /* renderer.setBaseItemLabelGenerator(
	         	    new StandardCategoryItemLabelGenerator(
	         	        "{0}-{1}: {2} {3}", NumberFormat.getInstance()));*/
	         renderer.setBaseItemLabelGenerator(
	         	    new StandardCategoryItemLabelGenerator(
	         	        "{2}", NumberFormat.getInstance()));
	         
	         renderer.setBaseItemLabelFont(new Font("SansSerif", Font.BOLD, 12), true);
	        
	         renderer.setBaseItemLabelsVisible(true);
	         
	        plot.setRenderer(renderer);
	        
	        
	       // plot.setFixedLegendItems(createLegendItems());
	        return chart;
	        
	    }

	    /**
	     * Creates the legend items for the chart.  In this case, we set them manually because we
	     * only want legend items for a subset of the data series.
	     * 
	     * @return The legend items.
	     */
	    private LegendItemCollection createLegendItems() {
	        LegendItemCollection result = new LegendItemCollection();
//	        LegendItem item1 = new LegendItem("US", new Color(0x22, 0x22, 0xFF));
	  //      LegendItem item2 = new LegendItem("Europe", new Color(0x22, 0xFF, 0x22));
	    //    LegendItem item3 = new LegendItem("Asia", new Color(0xFF, 0x22, 0x22));
	      //  LegendItem item4 = new LegendItem("Middle East", new Color(0xFF, 0xFF, 0x22));
//	        result.add(item1);
	  //      result.add(item2);
	    //    result.add(item3);
	      //  result.add(item4);
	        return result;
	    }
	    
	    // ****************************************************************************
	    // * JFREECHART DEVELOPER GUIDE                                               *
	    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
	    // * to purchase from Object Refinery Limited:                                *
	    // *                                                                          *
	    // * http://www.object-refinery.com/jfreechart/guide.html                     *
	    // *                                                                          *
	    // * Sales are used to provide funding for the JFreeChart project - please    * 
	    // * support us so that we can continue developing free software.             *
	    // ****************************************************************************
	    
	    /**
	     * Starting point for the demonstration application.
	     *
	     * @param args  ignored.
	     */
	    public static void main(final String[] args) {
	        final StackedBarScoringPanel demo = new StackedBarScoringPanel("Stacked Bar Chart Demo 4");
	        demo.pack();
	        RefineryUtilities.centerFrameOnScreen(demo);
	        demo.setVisible(true);
	    }

	}


