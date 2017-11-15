package model;


import java.io.File;
import java.util.List;
/***
 * Carries out the process of reference genome alignment in relation to the sample reads.
 * Generates stats for coverage and depth from the sam file.
 * 
 * @author Maha Maabar
 *
 */
public class ReferenceGenomeAlignment {
	
	private List<Integer> refGenomeScores;
	private String alignementStats ; 
	private ReferenceGenomeFile rgf;	
	
	public ReferenceGenomeAlignment()	{
		refGenomeScores= null;
		alignementStats ="";
		rgf = new ReferenceGenomeFile();
	}
	
	public boolean refGenExists (String taxID, String referenceGenomesFile, String dbOption){
		if(rgf.setRefGenome(referenceGenomesFile, taxID,dbOption)){
			return true;
		} else {
				return false;
		}			
	}
	
	public void alignToRefGenome (String taxID, String sampleFile, String refFile, String refFileOutput, String logDir){		
		/*First step: create the reference file */
		rgf.createRefFile(refFile);
		
		/*Second Step: run Tanoti and generates the SAM file
		 * parms[0] = sample file (.fq or .fastq)
		 * parms[1] = reference file (.fa or .fasta )
		 * parms[2] = output file (.sam)
		 */
		String [] refAssParms = {sampleFile, refFile,refFileOutput,logDir};
		new ReferenceAssembly (refAssParms);
			
		/*Third Step: process the SAM file to get the coverage and depth information */
		ReferenceGenome rgPositions = new ReferenceGenome();				
		rgPositions.setGenomeLen (refFile);
		int genomeLen = rgPositions.getGenomeLength();
				
		rgPositions.setToDefaultPos();
				
		//Three numbers: {num of reads, num of mapped reads, num of unmapped reads}
		int [] readsStats = rgPositions.setGenomePositions(refFileOutput);
		int numReads = rgPositions.getNumOfReads();
				
		double percMapped = rgPositions.calculatePerc(readsStats[0], numReads);
		double percUnMapped = rgPositions.calculatePerc(readsStats[1], numReads);
		
		//Four numbers: {minimum depth, maximum depth, average depth, mapped area}
		int [] depthStats = rgPositions.getDepthStats(); 
				
		//Percentage of coverage= (mapped Area / genome length)*100
		double percCoverage = rgPositions.calculatePerc(depthStats[3], genomeLen);
			
		alignementStats = "Genome Length: "+genomeLen+"\tMappedArea: "+depthStats[3]+"\tGenome Coverage: "+String.format("%.2f", percCoverage)+"%\n"+
			           "Mapped Reads: "+readsStats[0]+" ("+String.format("%.2f", percMapped)+"%)\tUnmapped Reads: "+readsStats[1]+" ("+String.format("%.2f", percUnMapped)+"%)\n"+
	                "Minimum Depth: "+depthStats[0]+"\tMaximum Depth: "+depthStats[1]+"\tAverage Depth: "+depthStats[2];
	     
	     refGenomeScores = rgPositions.getPosList();		
	}
			
	public List<Integer> getRefGenomeScores() {
		return refGenomeScores;
	}

	public String getAlignementStats() {
		return alignementStats;
	}
	
	private static void deleteTempFolder(File dir) {
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
	
//to be used for validation output when using command line to run DisCVR classification
public static void main(String[] args) {
		
		String taxID = args[0]; //e.g. 186538
		String referenceGenomesFile = args[1]; //e.g. "referenceGenomes"
		String sampleFile = args [2];//e.g. full/path/to/sample.fq
		String dbOption = args[3]; //e.g. "customisedDB" or "BuiltInDB";
		
		 ReferenceGenomeAlignment rga = new ReferenceGenomeAlignment();
			
	     boolean found = rga.refGenExists (taxID, referenceGenomesFile,dbOption);
	     if (found){
	    	 
	    	  String currentDir = System.getProperty("user.dir");
	    	  String outputPath = currentDir+"/temp_"+taxID+"/";
	    	  
	    	  File directory = new File(outputPath);			
				
			  if (directory.mkdir()) {
					System.out.println("Directory is created!");				
				} else {
					System.out.println("Failed to create directory!");
				}
	    	  
	    	  String refFile = "refGenome_"+taxID+".fa";
			  System.out.println("RefGenome file: "+refFile);
			  String refFileOutput ="referenceAlignement_"+taxID+".sam";
			  System.out.println("SAM output file: "+refFileOutput);
			  rga.alignToRefGenome(taxID,sampleFile,refFile,refFileOutput, outputPath);
	    	
			  String	alignementStats = rga.getAlignementStats();
	    		
	    	  System.out.println("The results are: "+alignementStats);
	    	  
	    	  deleteTempFolder(directory);
	    	  
	    	  //delete temp files
	    	  deleteFiles (currentDir, ".sam");
	    	  deleteFiles (currentDir, ".fa");
	    	  
	     }
     }

private static void deleteFiles (String dirName, String fileN)
	{
		final File dir = new File(dirName);
		    final String[] allFiles = dir.list();
		    for (final String file : allFiles) {
		        if (file.endsWith(fileN)) {
		           new File(dirName + "/" + file).delete();
		        }
		    }
	}
}
