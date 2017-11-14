package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;
import utilities.EntropyFilter;

/***
 * Uses KAnalyze tool to extract k-mers and their counts from a sample file.
 * Removes low entropy and single copies k-mers from the output.
 * 
 * @author Maha Maabar
 *
 */
public class KmersCounting {

	private int numOfReads;
	private int goodKmers;
	private int badKmers;
	private int lowEntropyKmers;
	private long numKmers;
	private long totalKmersCounts;
	private long totalGoodKmers;

	public KmersCounting (String [] parms) 	{
		setNumOfReads(parms[0]);
		
		numKmers =0;
		totalKmersCounts =0;		
		badKmers =0;
		goodKmers =0;
		totalGoodKmers =0;		
		double entropyThreshold = Double.parseDouble(parms[5]);
		
			
		/*denoise k-mers:
		 * 1st: remove any k-mer in the file with count is 1 (single-copies k-mers)
		 * 2nd: remove any k-mer in the file with entropy <= entropythreshold (low-entropy k-mers) 
		 */
		
		String [] tempParms = {parms[0],parms[1],parms[2],"TempFiles/TempKmers",parms[4]}; //TempKmers contains the results of KAnalyze
			
		runKmersCounting(tempParms);
			
		String outFile1 = "TempFiles/badKmers";
		denoiseKmers(tempParms[3], outFile1, parms[3],entropyThreshold);
				
		//delete temporary files
		deleteFiles("TempFiles","badKmers");
		deleteFiles("TempFiles","TempKmers");		
		
	}
	
	//runs KAnalyze to count k-mers
   	private void runKmersCounting(String [] parms) {
		String inputFile = parms[0];
		int kSize = Integer.parseInt(parms[1]);
		String inputFormat = parms[2];
		String outputFile = parms[3];
		String kAnalyzeDir = parms[4];		
		
		Runtime rt = Runtime.getRuntime();
        Process proc; 
        
        // This has to be the default settings for KAnalyze, otherwise it will not run on machine with small RAM
        String command ="java -jar -Xmx1024m "+kAnalyzeDir+"/kanalyze.jar count  -k "+kSize+
        		            " -o "+ outputFile+" -f "+inputFormat+" "+inputFile+" -rcanonical";
        try {
        	proc = rt.exec(command);

            int interVal = proc.waitFor();
            if (interVal == 0){
            }
            else{
              System.out.println("K-mers counting encounted some errors: "+interVal);
            }   	
        }
        catch (Exception e)
        {
        	System.out.println("Error in running KAnalyze");
        }
        
	}
   	
    private void deleteFiles (String dirName, String fileN)
   	{
   		final File dir = new File(dirName);
   		    final String[] allFiles = dir.list();
   		    for (final String file : allFiles) {
   		        if (file.startsWith(fileN)) {
   		           new File(dirName + "/" + file).delete();
   		        }
   		    }
   	}
	
    //counts the number of reads in the file
	private void setNumOfReads(String file){
		//open the input file and counts the headers
		//fasta, header starts with >
		//fastq, header starts with @
		//fastqgz, zipped file starts with @ 	
		
		BufferedReader r;		
        try {
        	if(file.endsWith(".gz")) {//reading a compressed file
    			final int DEFAULT_BUFFER_SIZE=5096;
    			r=new BufferedReader(new InputStreamReader(new GZIPInputStream(
    			  new FileInputStream(file),DEFAULT_BUFFER_SIZE)),DEFAULT_BUFFER_SIZE);
    		}    		
    		else {
    			r= new BufferedReader(new FileReader(file));
    		}
        	 numOfReads =1;
        	         	 			 
        	 String firstLine= r.readLine();   //get the first character from the header
        	 String headerPrefix = firstLine.substring(0,1).trim();
			 	
			 String hostLine=""; 				
			 while((hostLine = r.readLine()) != null) {
				 if(hostLine.length()>headerPrefix.length()  && 
					(hostLine.substring(0,1)).equals(headerPrefix)){
						numOfReads++;	
				} 	
              }
			  r.close();		
		 } 
		 catch (IOException ex)
	     {
	    	System.out.println("Errors reading from "+file);	   
	     }
		
	}
	
   //opens the k-mers output file and prints the k-mers with count > 1 and whose tri-nucleotide entropy > threshold
   //to the output file
   private void denoiseKmers(String inFile, String outFile1, String outFile2, double threshold){
	   int bases = 3; // bases to use for the entropy i.e. di- or tri-nucleotide
	   EntropyFilter ef = new EntropyFilter();
			
	   lowEntropyKmers =0;
	   	
	   try(BufferedReader kmerFile  = new BufferedReader(new FileReader(inFile));
		   PrintWriter pw1 =  new PrintWriter(new BufferedWriter(new FileWriter(outFile1))); 
		   PrintWriter pw2 =  new PrintWriter(new BufferedWriter(new FileWriter(outFile2)));){
		   
		   String kmerLine=""; 				
				
			while((kmerLine = kmerFile.readLine()) != null)	{
				String [] words =kmerLine.split("\t");
				String kmer= words[0];
				int count = Integer.parseInt(words[1]);
				numKmers++;	
				totalKmersCounts += count ;
					
				if (count ==1){
					pw1.println(kmerLine);
					badKmers++;
				}
				else { //if its count > 1 and its entropy is > threshold
					double entropy = ef.calculateShannonEntropy(kmer,bases);
					if(entropy < threshold){
						lowEntropyKmers++;						
					}	
					else{	//count k-mers with entropy >threshold
						pw2.println(kmerLine);							
						//count k-mers with count >1
						goodKmers++;
						totalGoodKmers += count;							
					}
				}
			 }
				
			kmerFile.close();	
			pw1.close();
			pw2.close();			
		  }
		  catch (IOException ex)  {
		    	System.out.println("Errors reading from "+inFile);	   
		  }
	}
	
	public int getNumOfReads()
	{
		return numOfReads;
	}	
    public int getGoodKmers() {
		return goodKmers;
	}
	public int getBadKmers() {
		return badKmers;
	}	
    public long getNumKmers() {
		return numKmers;
	}		
	public long getTotalKmersCounts() {
		return totalKmersCounts;
	}	
	public long getTotalGoodKmers() {
		return totalGoodKmers;
	}	
	public long getLowEntropyKmers() {
		return lowEntropyKmers;
	}

}
