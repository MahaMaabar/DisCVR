package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
 
public class AlignmentPanel extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public AlignmentPanel (List<Integer> scores, String text, String name){
		 super("Reference Genome Alignment");		 
		 
		 
         StyledDocument document = setStyle(text);
         
         JTextPane textPane = new JTextPane(document);
         textPane.setEditable(false);
         
         JTextArea textArea = new JTextArea(text, 5, 5);
         textArea.setPreferredSize(new Dimension(50,50));
         textArea.setLineWrap(true);         

         Font font = new Font("verdana", Font.BOLD, 16);
         textArea.setFont(font);
         textArea.setForeground(Color.BLACK);         
             
         add(textArea, BorderLayout.NORTH);
		 
	     JPanel chartPanel = createChartPanel(scores,name);
	     add(chartPanel, BorderLayout.CENTER);
	     
	     //set icon image for the frame
	     setIconImage(createIcon("/resources/cvr_logo.gif"));
	 
	     setSize(640, 480);
	     setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	     setLocationRelativeTo(null);		 
		
	}
	
	 //creates the panel to hold the graph	 
	 private JPanel createChartPanel (List<Integer> scores, String name){
		 String chartTitle = "Sample Reads Mapping to "+name;
		 String yAxisLabel = "Genome Positions";
		 String xAxisLabel = "Number of Reads";
		 
		
		 final XYDataset dataset =createDataset(scores,name);
		 
		 
		 JFreeChart chart = ChartFactory.createXYLineChart(chartTitle,
		                    yAxisLabel, xAxisLabel, dataset, PlotOrientation.VERTICAL, false, true, false);
		
		 //customise the graph
		 chart.setBackgroundPaint(Color.white);
        
		 // get a reference to the plot for further customisation...
	     final XYPlot plot = chart.getXYPlot();
	     plot.setBackgroundPaint(Color.lightGray);
	   
	     plot.setDomainGridlinePaint(Color.white);
	     plot.setRangeGridlinePaint(Color.white);
	      
	     // change the auto tick unit selection to integer units only...
	     final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	     rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	        
	        
	     rangeAxis.setAutoRangeIncludesZero(true);
	        
	     final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
         xAxis.setAutoRange(true);
         
         /*===============================================================*/
         //set the font for the axes titles
 	     Font font = new Font("Dialog", Font.BOLD,25); 
 	     plot.getDomainAxis().setLabelFont(font);
 	     plot.getRangeAxis().setLabelFont(font);
 	     	     

 	     xAxis.setTickLabelFont(new Font("verdana",Font.BOLD, 15));
 	     rangeAxis.setTickLabelFont(new Font("verdana",Font.BOLD,15));
          
         
         /*======================================================================*/
        
         
         
         return new ChartPanel(chart);
	}
	 
	//sets the dataset for the graph
	private XYDataset createDataset(List<Integer> scores, String name) {
		final XYSeries series = new XYSeries(name);
	        
		for(int i =0;i<scores.size();i++){
			series.add(i,scores.get(i));
		}
	      
	    final XYSeriesCollection dataset = new XYSeriesCollection();
	    dataset.addSeries(series);	       
	                
	    return dataset;
	        
	}
	 
	 
	/*sets the icon for the frame to be the CVR logo */
	private Image createIcon (String path) {
		URL url = getClass().getResource(path);
			
		if(url == null) {
			System.err.println("Unable to load image: "+ path);
		}
			
		Image icon = new ImageIcon (url).getImage();
		return icon;
	}
	 
	private static StyledDocument setStyle(String text){
		StyleContext context = new StyleContext();
		StyledDocument document = new DefaultStyledDocument(context);

		Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
		StyleConstants.setFontSize(style, 12);
		StyleConstants.setSpaceAbove(style, 4);
		StyleConstants.setSpaceBelow(style, 2);
		        
		try {
			document.insertString(document.getLength(), text, style);
		} catch (BadLocationException badLocationException) {
			System.err.println("Oops");
		}
		return document;
	}
   
 }