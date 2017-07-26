package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class ProgressPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
	
	
	public ProgressPanel () {
		progressBar = new JProgressBar();
		
		progressBar.setMaximum(4); // 4 messages to be retrieved 
		
		progressBar.setStringPainted(true);		
		progressBar.setString("Classifying Sample...");
		
		setLayout(new FlowLayout());	
			
		Dimension size = new Dimension(400,30);//Dimension (width,height)	
		progressBar.setPreferredSize(size);
		
		Border innerBorder = BorderFactory.createTitledBorder(null,"Progress Bar",TitledBorder.LEFT, TitledBorder.TOP, new Font("Verdana",Font.BOLD,12), Color.BLACK);
		
		Border outerBorder = BorderFactory.createEmptyBorder(5,5,5,5);
		setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		
		add(progressBar);
		
	}
	
	public void setMaximum (int value) {
		progressBar.setMaximum(value);
	}

	public void setValue(int value) {		
		int progress = 100*value/progressBar.getMaximum();
		progressBar.setString(String.format("%d%% complete",progress));
		
		progressBar.setValue(value);
	}
	
	public int getValue(){
		return progressBar.getValue();
	}
	

}
