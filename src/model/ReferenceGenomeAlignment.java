package model;


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

}
