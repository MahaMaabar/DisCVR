package gui;
import javax.swing.SwingUtilities;

/***
 * The main class that launches DisCVR's GUI to run a classification process
 *  implementation is inspired by an example provided by udemy.com
 *  It follows the model-view-controller framework
 *  
 *  @author Maha Maabar
 *         
 */
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
