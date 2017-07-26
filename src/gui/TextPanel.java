package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;


//public class TextPanel extends JPanel implements Consumer{
public class TextPanel extends JPanel {
	
	private JTextArea textArea;
	
	public TextPanel(String title, Font font, Color color ){
		Dimension dim = getPreferredSize();
		dim.height = 200;
		setPreferredSize(dim);
		setMinimumSize(dim);
				
		textArea = new JTextArea (20,20);
		
		//font = new Font("Verdana", Font.BOLD, 12);
		textArea.setFont(font);
		textArea.setForeground(color);
				
		setLayout (new BorderLayout ());
				
		Border innerBorder = BorderFactory.createTitledBorder(null,title,TitledBorder.LEFT, TitledBorder.TOP, new Font("Verdana",Font.BOLD,12), Color.BLACK);		
		//Border innerBorder = BorderFactory.createTitledBorder("Progress Information");
		Border outerBorder = BorderFactory.createEmptyBorder(5,5,5,5);
		setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
				
		//add a scrollpane to scroll horizontally and vertically
		add (new JScrollPane (textArea), BorderLayout.CENTER);
		
	}
	
	//constructor 
	public TextPanel (){
		/*textArea = new JTextArea();
		
		setLayout(new BorderLayout ());
		
		//add a scrollpane to scroll horizontally and vertically
		add(new JScrollPane(textArea), BorderLayout.CENTER);*/
		
		//settings for the dimension of the panel
		Dimension dim = getPreferredSize();
		dim.height = 200;
		setPreferredSize(dim);
		setMinimumSize(dim);
				
		textArea = new JTextArea (20,20);
		
		
		Font font = new Font("Verdana", Font.BOLD, 18);
		textArea.setFont(font);
		Color color = Color.GREEN;
		textArea.setForeground(color);
		
						
		setLayout (new BorderLayout ());
				
		Border innerBorder = BorderFactory.createTitledBorder(null,"Progress Information",TitledBorder.LEFT, TitledBorder.TOP, new Font("Verdana",Font.BOLD,12), Color.BLACK);		
		//Border innerBorder = BorderFactory.createTitledBorder("Progress Information");
		Border outerBorder = BorderFactory.createEmptyBorder(5,5,5,5);
		setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
				
		//add a scrollpane to scroll horizontally and vertically
		add (new JScrollPane (textArea), BorderLayout.CENTER);
		
	}
	
	//a method to append text to the text area
	public void appendText (String text){
		textArea.append(text);
				
	}
	
	//a method to remove text from the text area
		public void setText (){
			textArea.setText("");
					
		}
     
     
 }
	
	


