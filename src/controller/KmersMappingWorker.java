package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import gui.KmersMappingPanel;
import gui.TextPanel;
import model.Kmers;
import model.KmersAssembly;
import model.MappedKmers;
import model.ReferenceGenomeFile;

/***
 * A class to pass the results for reference genome alignment by the matched k-mers from 
 * sample classification output to the GUI.
 * 
 * @author Maha Maabar
 *
 */

public class KmersMappingWorker extends SwingWorker <int [][] , String>{
	
	private int [][] mappingResults ;
	private String assemblyText ;
	private TextPanel textPanel;
	private String virusName;
	private ClassificationWorker classifier; 	
	private String [] assemblyPrams;
	private boolean refGenFound;	
	

	public KmersMappingWorker (final String [] assemblyPrams, final TextPanel textPanel, final ClassificationWorker classifier){
		this.textPanel = textPanel;	
		this.assemblyPrams = assemblyPrams;		
		this.classifier = classifier; 
		assemblyText = null;
		refGenFound= false;	
	}

	@Override
	protected int [][] doInBackground() throws Exception {		
		long startTime = System.currentTimeMillis();
		
		String taxID = assemblyPrams [0]; 
		String referenceGenomesFile = assemblyPrams [1];
		virusName = assemblyPrams [2];
		String dbOption = assemblyPrams[3];
		
		//First: get the reference genome from the reference genome library in DisCVR 
		//into a separate file in a temp directory
		ReferenceGenomeFile rgf = new ReferenceGenomeFile();
        
		//check if the ref genome exists in the library
		boolean found = rgf.setRefGenome(referenceGenomesFile,taxID,dbOption);
		
		if (found){	
			
			String actualPath = System.getProperty("user.dir");
			String outputPath = actualPath+"/KmersMappingTemp_"+taxID+"/";
        	
			//if taxaID exists in the referenceGenomes file
			//create a directory in the current directory to hold temporary files
			File directory = new File(outputPath);
			
			if (directory.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.err.println("Failed to create directory!");
			}
        	        	
        	textPanel.appendText("\n\n**************************************************************");
			publish ("\n\n**************************************************************");
			
			textPanel.appendText("\nStarting k-mers Assembly to Reference Genome ("+virusName+")\n");
			publish("\nStarting K-mers Mappring to Reference Genome.\n");
			
			//make a fasta file to hold the sequence corresponds to the tax ID
			String refFile = outputPath+"refGenome_"+taxID+".fa";
			
			rgf.createRefFile(refFile);			
			int maxMismatchTolerance = 3;
			
			//get all the matched k-mers into a list of k-mers and their counts
	        KmersAssembly kA = new KmersAssembly();
	        ArrayList<Kmers> kmersList = classifier.getAllMatchedKmers ();
	        int [] classifiedKmers = kA.getNumClassifiedKmers (kmersList);
		        
		    String refGenome = kA.getRefGenome(refFile);
		    int refGenomeLen = refGenome.length();
		     
		     if(refGenomeLen >0){
		    	 this.refGenFound = true;
		     }
		     
		     /*--------------Assembly Using String Matching/Lowest Minimum Distance-------------------*/
			 TreeMap<Integer,ArrayList<MappedKmers>> mappedResults = kA.getAssembledKmersString (refGenome,kmersList,maxMismatchTolerance);
			 int mappeKmersSize = mappedResults.size();
			 	
			 if(mappeKmersSize == 0){
			 }
				
			else{
				//Publish results of the alignment stats on the GUI
				mappingResults= kA.getMappedKmersPositions(mappedResults,maxMismatchTolerance, refGenomeLen);
				int [] afterMappingClassifiedKmers = kA.getNumMappedKmers (mappedResults);
				    
				assemblyText = kA.getStatText(classifiedKmers, afterMappingClassifiedKmers,mappingResults);
					
			}				
		    
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
    		
    		deleteTempFolder(directory);
        }	
		
		return mappingResults;
	}
	
		
	@Override
	protected void done() {
		if(mappingResults != null){
			SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            //show a graph of coverage and depth of the reference genome by matched k-mers 
        	  new KmersMappingPanel (mappingResults,virusName,assemblyText).setVisible(true);             
          }
        });
	  }
	  //reference genome does not exist in database
	  if(!this.refGenFound ){
		  JOptionPane.showMessageDialog(null, "Our database does not have a complete reference genome for the "+virusName,
							"Reference Genome Coverage", JOptionPane.INFORMATION_MESSAGE);
		}		
		//reference genome exists but mapping results are null
        else if(assemblyText== null && mappingResults == null){
			JOptionPane.showMessageDialog(null, "No classified k-mers is mapped to the complete reference genome for the "+virusName,
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
