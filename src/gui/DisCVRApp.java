package gui;
import javax.swing.SwingUtilities;


public class DisCVRApp {
	public static void main(String[] args) {
		//to run the application being threads-safe
		SwingUtilities.invokeLater(new  Runnable(){
			public void run() {	
				new DisCVRApplicationFrame();
			}
		});			
	}   
}
