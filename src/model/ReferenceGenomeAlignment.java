package model;


import java.io.File;
import java.util.List;

public class ReferenceGenomeAlignment {
	
	private List<Integer> refGenomeScores;
	private String alignementStats ; 
	private ReferenceGenomeFile rgf;
	
	private String outputPath = System.getProperty("user.dir");
	
	public ReferenceGenomeAlignment()
	{
		//alignToRefGenome (parameters);
		refGenomeScores= null;
		alignementStats ="";
		rgf = new ReferenceGenomeFile();
	}
	
	public boolean refGenExists (String taxID, String referenceGenomesFile, String dbOption){
		if(rgf.setRefGenome(referenceGenomesFile, taxID,dbOption)){
			//System.out.println("The reference file with taxa ID: "+taxaID);
			return true;
		} else {
				//System.out.println("The reference file with taxaID: "+taxaID+" does not exist!");
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
		//System.out.println("SAM files: "+refFileOutput);
		int [] readsStats = rgPositions.setGenomePositions(refFileOutput);
		int numReads = rgPositions.getNumOfReads();
				
		double percMapped = rgPositions.calculatePerc(readsStats[0], numReads);
		double percUnMapped = rgPositions.calculatePerc(readsStats[1], numReads);
		System.out.println("Number of reads= "+numReads+"\tMapped reads= "+readsStats[0]+" ("+percMapped+"%)"
				                +"\tUnmapped reads= "+readsStats[1]+" ("+percUnMapped+"%)"); 			           
			
		long startTime = System.currentTimeMillis();
		
		//Four numbers: {minimum depth, maximum depth, average depth, mapped area}
		int [] depthStats = rgPositions.getDepthStats(); 
				
		long endTime = System.currentTimeMillis();
		//System.out.println("Time taken= "+(endTime-startTime));
		/*System.out.println("Minimum depth= "+depthStats[0]+"\tMaximum depth= "+depthStats[1]
						          +"\tAverage depth= "+depthStats[2]+"\tMapped Area= "+depthStats[3]);
		*/	
		
		//Percentage of coverage= (mapped Area / genome length)*100
		double percCoverage = rgPositions.calculatePerc(depthStats[3], genomeLen);
		//System.out.println("Percentage of genome Coverage= "+percCoverage+"%");
			
		alignementStats = "Genome Length: "+genomeLen+"\tMappedArea: "+depthStats[3]+"\tGenome Coverage: "+String.format("%.2f", percCoverage)+"%\n"+
			           "Mapped Reads: "+readsStats[0]+" ("+String.format("%.2f", percMapped)+"%)\tUnmapped Reads: "+readsStats[1]+" ("+String.format("%.2f", percUnMapped)+"%)\n"+
	                "Minimum Depth: "+depthStats[0]+"\tMaximum Depth: "+depthStats[1]+"\tAverage Depth: "+depthStats[2];

	     System.out.println("Stats Text: "+alignementStats);
	     refGenomeScores = rgPositions.getPosList();
		
	}
			
	public List<Integer> getRefGenomeScores() {
		return refGenomeScores;
	}

	public String getAlignementStats() {
		return alignementStats;
	}
	
	public static void main(String[] args) {
		
		String taxID = args[0]; //e.g. 186538
		String referenceGenomesFile = args[1]; //e.g. "referenceGenomes"
		String sampleFile = args [2];//e.g. E:\Datasets\HumanData\SRA_EbolaData\SRR1735246_concet.fq
		String dbOption = args[3]; //e.g. "customisedDB" or "BuiltInDB";
		
		 ReferenceGenomeAlignment rga = new ReferenceGenomeAlignment();
			
	     boolean found = rga.refGenExists (taxID, referenceGenomesFile,dbOption);
	     if (found){
	    	 
	    	  String outputPath = System.getProperty("user.dir")+"/temp_"+taxID+"/";
	    	  String refFile = "refGenome_"+taxID+".fa";
			  System.out.println("RefGenome file: "+refFile);
			  String refFileOutput ="referenceAlignement_"+taxID+".sam";
			  System.out.println("SAM output file: "+refFileOutput);
			  rga.alignToRefGenome(taxID,sampleFile,refFile,refFileOutput, outputPath);
	    	
			  //List<Integer> refGenomeScores = rga.getRefGenomeScores();
	    	  String	alignementStats = rga.getAlignementStats();
	    		
	    	  System.out.println("The results are: "+alignementStats);
	     }
	     
	}

}
