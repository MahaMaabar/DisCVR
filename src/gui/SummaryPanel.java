package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import model.VirusResult;

public class SummaryPanel extends JPanel {
	
private JTextArea textArea;
	
	//constructor 
	public SummaryPanel (){
		
		
		//settings for the dimension of the panel
		Dimension dim = getPreferredSize();
		dim.height = 200;
		setPreferredSize(dim);
		setMinimumSize(dim);
				
		textArea = new JTextArea (20,20);
		
		
		Font font = new Font("Verdana", Font.BOLD, 12);
		textArea.setFont(font);
		textArea.setForeground(Color.BLACK);
				
		setLayout (new BorderLayout ());
				
		Border innerBorder = BorderFactory.createTitledBorder(null,"Analysis Summary",TitledBorder.LEFT, TitledBorder.TOP, new Font("Verdana",Font.BOLD,12), Color.BLACK);		
		Border outerBorder = BorderFactory.createEmptyBorder(5,5,5,5);
		setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
				
		//add a scrollpane to scroll horizontally and vertically
		add (new JScrollPane (textArea), BorderLayout.CENTER);
		
	}
	
	public void setData(String text){
		String str = "Hello\n\n";
		textArea.append(str);
		
		
			textArea.append("Word: "+text+"\n");
		/*System.out.println("There are "+vDB.size()+" viruses in the sample (TablePanel)");
		for(VirusResult v:vDB)
			System.out.println(v.getName()+" :: "+v.getTaxaID()+" :: "+v.getDisKmers()+" :: "+v.getTotKmers()+"\n");
        */
	}

	public static void main(String[] args) {
		
		SummaryPanel panel = new SummaryPanel();
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("Hi");
		list.add("Bye");
		
		for(String l:list)
		panel.setData(l.toString());
		
		JFrame main = new JFrame();
		main.add(panel);
		main.setVisible(true);
		main.pack();

	}

}
