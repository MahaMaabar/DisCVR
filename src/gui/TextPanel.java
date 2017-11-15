package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/***
 * A class that formats the text areas in DisCVR's GUI.
 * It is used for the summary and progress information panels.
 * 
 * @author Maha Maabar
 *
 */
public class TextPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	
	public TextPanel(String title, Font font, Color color ){
		Dimension dim = getPreferredSize();
		dim.height = 200;
		setPreferredSize(dim);
		setMinimumSize(dim);
				
		textArea = new JTextArea (20,20);
		textArea.setFont(font);
		textArea.setForeground(color);
				
		setLayout (new BorderLayout ());
				
		Border innerBorder = BorderFactory.createTitledBorder(null,title,TitledBorder.LEFT, TitledBorder.TOP, new Font("Verdana",Font.BOLD,12), Color.BLACK);		
		Border outerBorder = BorderFactory.createEmptyBorder(5,5,5,5);
		setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
				
		add (new JScrollPane (textArea), BorderLayout.CENTER);		
	}
	
	public TextPanel (){
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
		
		//add a scrollpane to scroll horizontally and vertically
		add (new JScrollPane (textArea), BorderLayout.CENTER);		
	}
	public void appendText (String text){
		textArea.append(text);				
	}
	
	//reset the text area to null
	public void setText (){
		textArea.setText("");
	}
 }
	
	


