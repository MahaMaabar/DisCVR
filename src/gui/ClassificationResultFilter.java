package gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/***
 * Applies a filter to the file extension when saving classification results to a file.
 * 
 * @author Maha Maabar
 * 
 */
public class ClassificationResultFilter extends FileFilter {
	public boolean accept(File file) {		
		if(file.isDirectory()) {
			return true;
		}		
		String name = file.getName();
		String extension = getFileExtension(name);
		
		if (extension == null) {
			return false;
		}
		
		if (extension.equals("csv")) {
			return true;
		} 
		
		return false;
	}
   
	public String getDescription() {
		return "Classification Result files (*.csv)";
	}
	
	private String getFileExtension (String name) {
		int pointIndex = name.lastIndexOf(".");
		
		if (pointIndex == -1) {
			return null;
		}
			
		if (pointIndex == name.length() -1){
			return null;
		}
		
		return name.substring(pointIndex+1, name.length());
	}


}
