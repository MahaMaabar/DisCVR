package gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/*To save the output results from classification to a file with csv foramt */

public class ClassificationResultFilter extends FileFilter {

	@Override
	public boolean accept(File file) {
		
		if(file.isDirectory()) {
			return true;
		}
		
		String name = file.getName();
		
		String extension = Utils.getFileExtension(name);
		
		if (extension == null) {
			return false;
		}
		
		if (extension.equals("csv")) {
			return true;
		} 
		
		return false;
	}

	@Override
	public String getDescription() {
		return "Classification Result files (*.csv)";
	}

}
