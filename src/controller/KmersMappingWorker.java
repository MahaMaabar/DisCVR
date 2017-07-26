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
		
		ReferenceGenomeFile rgf = new ReferenceGenomeFile();
        
		//check if the ref genome exists in the library
		boolean found = rgf.setRefGenome(referenceGenomesFile,taxID,dbOption);
		
		if (found){			
			String actualPath = System.getProperty("user.dir");
			String outputPath = actualPath+"/KmersMappingTemp_"+taxID+"/";
        	
			//if the taxaID exists in the referenceGenomes file
			//create a directory in the current directory to hold temporary files
			File directory = new File(outputPath);
			
			if (directory.mkdir()) {
				//System.out.println("Directory "+outputPath+" is created!");
				//hide(directory);
			} else {
				System.err.println("Failed to create directory!");
			}
        	
        	
        	textPanel.appendText("\n\n**************************************************************");
			publish ("\n\n**************************************************************");
			
			textPanel.appendText("\nStarting k-mers Assembly to Reference Genome ("+virusName+")\n");
			publish("\nStarting K-mers Mappring to Reference Genome.\n");
			
			//make a fasta file to hold the sequence corresponds to the tax ID
			String refFile = outputPath+"refGenome_"+taxID+".fa";
			//System.out.println("RefGenome file: "+refFile);
			
			rgf.createRefFile(refFile);			
			int maxMismatchTolerance = 3;
			
			//get all the matched k-mers into a list of k-mers and their counts
	        KmersAssembly kA = new KmersAssembly();
	        ArrayList<Kmers> kmersList = classifier.getAllMatchedKmers ();
	        //System.out.println("Finished getting all the Matched kmers: "+kmersList.size());     
		    int [] classifiedKmers = kA.getNumClassifiedKmers (kmersList);
		        
		    //System.out.println("There are "+classifiedKmers[0]+" DISTINCT CLASSIFIED k-mers with total counts= "+classifiedKmers[1]);
		    String refGenome = kA.getRefGenome(refFile);
		    int refGenomeLen = refGenome.length();
		     
		     if(refGenomeLen >0){
		    	 this.refGenFound = true;
		    	// System.out.println("Reference Genome Length: "+refGenomeLen+" and the refGenFound is "+this.refGenFound);
		     }
		     
		     /*------------------------------Assemblying Using String Matching/Lowest Minimum Distance---------------------*/
				
		     TreeMap<Integer,ArrayList<MappedKmers>> mappedResults = kA.getAssembledKmersString (refGenome,kmersList,maxMismatchTolerance);
			 int mappeKmersSize = mappedResults.size();
			 //System.out.println("The size of the mapped k-mers treemap: "+mappeKmersSize);
				
			 if(mappeKmersSize == 0){
				 //	System.out.println("No mapped K-mers to the reference Genome");					
			 }
				
			else{
				mappingResults= kA.getMappedKmersPositions1(mappedResults,maxMismatchTolerance, refGenomeLen);
				int [] afterMappingClassifiedKmers = kA.getNumMappedKmers (mappedResults);
					
			    //System.out.println("There are "+afterMappingClassifiedKmers[0]+" (mapped to reference genome) distinct k-mers and their total counts is "+afterMappingClassifiedKmers[1]);
				    
				assemblyText = kA.getStatText(classifiedKmers, afterMappingClassifiedKmers,mappingResults);
				/*System.out.println("===================================================================");
				System.out.println("The Stats:");
				System.out.println(assemblyText);*/
					
			}				
		    
    		textPanel.appendText("\nFinished Reference Alignment.\n");
    		
            publish("\nFinished Reference Alignment.\n");
    		
    		long endTime = System.currentTimeMillis();
    		long time=endTime-startTime;
    		   
    		
    		int seconds = (int)(time / 1000) % 60 ;
    		int minutes = (int)((time / (1000*60)) % 60);
    		int hours = (int)((time / (1000*60*60)) % 24);
    		
    		//System.out.println("Time taken: "+(0.001*time)+" seconds.");
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
        	  new KmersMappingPanel (mappingResults,virusName,assemblyText).setVisible(true);             
          }
        });
	  }
	  //reference genome does not exist in database
	  if(!this.refGenFound ){
		  //System.out.println("Cannot find reference genome information");
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
