package gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/***
 * sets the filter for the input file extension.
 * 
 * @author Maha Maabar
 *
 */
public class InputFileFilter extends FileFilter {
	public boolean accept(File file) {
		
		if(file.isDirectory()) {
			return true;
		}
		
		String name = file.getName();
		
		if(name.endsWith(".fa")||name.endsWith(".fasta")
		 ||name.endsWith(".fq")||name.endsWith(".fastq")
		 ||name.endsWith(".fa.gz")||name.endsWith(".fasta.gz")
		 ||name.endsWith(".fq.gz")||name.endsWith(".fastq.gz"))
		
		{
			return true;
		}
			
		else
			return false;
	}

	public String getDescription() {
		return "Sequence files (*.fasta;*.fastq;*.fasta.gz;*.fastq.gz)\n";
	}

}
