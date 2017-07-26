package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import gui.AlignmentPanel;
import gui.TextPanel;
import model.ReferenceGenomeAlignment;
//import model.ReferenceGenomeFile;

public class AlignmentWorker extends SwingWorker <List<Integer>, String> {		
	private List<Integer> assemblyResults ; 
	private String assemblyText ;
	private TextPanel textPanel;
	private String virusName;	
	private String [] assemblyPrams;
	
public AlignmentWorker (final String [] assemblyPrams, final TextPanel textPanel, String virusName){
	this.textPanel = textPanel;	
	this.assemblyPrams = assemblyPrams;
	this.virusName = virusName;
		
	assemblyResults = null;		
		
}
@Override
protected List<Integer> doInBackground() throws Exception {
		
		long startTime = System.currentTimeMillis();
		
		String taxaID = assemblyPrams [0]; 
		String referenceGenomesFile = assemblyPrams [1];
		String sampleFile = assemblyPrams [2];
		String dbOption = assemblyPrams[3];
		
		ReferenceGenomeAlignment rga = new ReferenceGenomeAlignment();
        boolean found = rga.refGenExists (taxaID, referenceGenomesFile,dbOption);
        if (found){        	
        	//if the taxaID exists in the referenceGenomes file
			//create a directory in the current directory to hold temporary files
			//System.out.println("Creating Temp folder for alignment...");
			String outputPath = System.getProperty("user.dir")+"/temp_"+taxaID+"/";
			File directory = new File(outputPath);
			
			if (directory.mkdir()) {
				System.out.println("Directory is created!");
				
			} else {
				System.out.println("Failed to create directory!");
			}
        	        	
        	textPanel.appendText("\n\n**************************************************************");
			publish ("\n\n**************************************************************");
			
			textPanel.appendText("\nStarting Reads Assembly to Reference Genome ("+virusName+")\n");//update text panel
			publish("\nStarting Reference Genome Alignment.\n");//update text panel
			
			// if directory is created, make a fasta file to hold the sequence corresponds to the taxa ID
			String refFile = outputPath+"refGenome_"+taxaID+".fa";
			//System.out.println("RefGenome file: "+refFile);
			String refFileOutput =outputPath+"referenceAlignement_"+taxaID+".sam";
			//System.out.println("SAM output file: "+refFileOutput);
			rga.alignToRefGenome(taxaID,sampleFile,refFile,refFileOutput,outputPath);
			
			assemblyResults = rga.getRefGenomeScores();
			assemblyText = rga.getAlignementStats();
    		
    		//System.out.println("The results are: "+assemblyText);
    		
    		
    		textPanel.appendText("\nFinished Reference Alignment.\n");
    		
            publish("\nFinished Reference Alignment.\n");
    		
    		long endTime = System.currentTimeMillis();
    		long time=endTime-startTime;
    		    		
    		int seconds = (int)(time / 1000) % 60 ;
    		int minutes = (int)((time / (1000*60)) % 60);
    		int hours = (int)((time / (1000*60*60)) % 24);
    		
    		String text = String.format("%02d:%02d:%02d", hours, minutes, seconds);
    		
    		
    		textPanel.appendText("\nTime taken (hh:mm:ss): "+text+"\n");
    		publish("\nTime taken (hh:mm:ss): "+text+"\n");
    		textPanel.appendText("\n**************************************************************\n");
    		
    		//deleteTempFolder(directory);
        }	
		
		return assemblyResults;
	}
	
	@Override
	protected void process(final List<String> chunks) {
		
	  }
	
	@Override
	  protected void done() {
		if(assemblyResults != null){
			SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AlignmentPanel(assemblyResults,assemblyText,virusName).setVisible(true);
            }
           });		
	    }
	    else {
	    	JOptionPane.showMessageDialog(null, "Our database does not have a complete reference genome for the "+virusName,
				"Reference Genome Coverage", JOptionPane.INFORMATION_MESSAGE);
	  }
  }
	
	
 private  void deleteTempFolder(File dir) {
		String[] files;    
        if(dir.isDirectory()){
            files = dir.list();
            for (int i=0; i<files.length; i++) {
               File aFile = new File(dir,files[i]); 
               aFile.delete();
             }
        }
        dir.delete();
	 }

	
}
