package customdatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;

//import model.PermutationFiles;
import utilities.PermutationFiles;
public class KmersDatabaseBuild{
	
	KmersDatabaseBuild (String [] args){
		build(args);
	}

	public static void main(String[] args) {
		/*Parameters = {virusFilesDir, hostFilesName, virusKmersOutputFileName, kSize, numThreads, 
                 entropyThrshld,  fileCounter}
		 String virusFilesDir = parameters[0]; //Directory conatins a list of fasta files for virus sequences
		 String  hostFilesName = parameters[1];//name of the fasta file contains the host sequences
		 String virusKmersOutputFileName =  parameters[2]; //full path for the virus-specific kmers database file
		 int kSize = Integer.parseInt(parameters[3]); // k-mer size
		 int numThreads = Integer.parseInt(parameters[4]);//number of threads to run virus k-mers counting
		 double entropyThrshld= Double.parseDouble (parameters[5]);//Entropy threshold to filter out low-complexity virus k-mers
                 int fileCounter=Integer.parseInt(parameter[6]);//number of virus k-mers files to process at once
		 */
		System.out.println("=================================================================");
		System.out.println();
		
		for (String s:args) System.out.println(s);
			
		new KmersDatabaseBuild (args);
			
		
		System.out.println("Done!");
        
		System.out.println("==============================================");

	}
	
	private void build(String [] args){
		 /*Parameters = {virusFilesDir, hostFilesName, virusKmersOutputFileName, kSize, numThreads,
                  entropyThrshld,fileCounter}
		 String virusFilesDir = parameters[0]; //Directory conatins a list of fasta files for virus sequences
		 String  hostFilesName = parameters[1];//name of the fasta file contains the host sequences
		 String virusKmersOutputFileName =  parameters[2]; //full path for the virus-specific kmers database file
		 int kSize = Integer.parseInt(parameters[3]); // k-mer size
		 int numThreads = Integer.parseInt(parameters[4]);//number of threads to run virus k-mers counting
		 double entropyThrshld= Double.parseDouble (parameters[5]);//Entropy threshold to filter out low-complexity virus k-mers
		 int fileCounter=Integer.parseInt(parameter[6]);//counter to process virus k-mers files
		 */
		
		 /*Static Parameters
		  * The number of permutation files was decided upon after careful consideration of the size of the 
		  * file (i.e number of k-mers to be uploaded to memory).
		  * Host files are large in size and it is found that having a perm size of 5 which gives 1024 perms files is 
		  * the best choice when dealing with the human genomes
		  */
		 int permSize=5;
		 
		 String workingdirectory = System.getProperty("user.dir");
		 String kAnalyzeDir = workingdirectory+"/lib";
		 
		 String tempDir = workingdirectory+"/temp/"; //to hold all intermediate files
		 createDir(tempDir);
		 
		 
		 long startTime = System.currentTimeMillis();
			
	         System.out.println("=================================================================");
		 System.out.println();
		 
		 /*step1:
		  * Process Host files:
		  * Count host k-mers
		  * Split k-mers into perms files
		  */
		  
		 String hostKmersOutputDir = tempDir+"hostKmers/";
		 createDir(hostKmersOutputDir);
		 
		 String [] hostKmersParms = {args[1],args[3],tempDir,hostKmersOutputDir,kAnalyzeDir,""+permSize};
		 System.out.println("Step 1: Host K-mers Processing ...");	
		 HostKmersCounting hKC = new  HostKmersCounting(hostKmersParms);
		 System.out.println("Finished Host k-mers Processing!");
		
		 deleteDirectory(new File(hostKmersOutputDir));
	
	     /*step2:
		  * Process Virus files:
		  * Count virus k-mers
		  * Annotate k-mers with virus taxIDs, remove low entropy k-mers and Split k-mers into perms files
		  * sort virus k-mers alphabetically in perms files
		  */
		  
		 String [] virusKmersParms = {args[0],args[3],tempDir,kAnalyzeDir,args[4],""+permSize,args[5],args[6]};
		 System.out.println("Step 2: Virus K-mers Processing ...");	
		 new VirusKmersCounting(virusKmersParms);
		 System.out.println("Finished Virus k-mers Processing!");
		 
		 /*step3:
		  * Remove Host Genomes:
		  * Identify Virus-specific k-mers; exist in the virus k-mers files but not in the host k-mers files		    
		  */
		 String [] perms =getPermsArray(permSize);
		 String tempFile = tempDir+"VirusSpecificDB_temp";
		 
		 System.out.println("Step 3: Identifying virus-specific k-mers ...");	
		 new KmersMatching(tempFile,Integer.parseInt(args[3]), perms,tempDir);
		 
		 System.out.println("Finished writing virus-specific k-mers database file!");
		 
		 /*step4: 
		  * identify the k-mers with single taxIDs and those with multiple taxIDs
		  * Each k-mer is given, at the end of the line, the number of taxdIDs that are represented by this k-mer
		  */
		 //String virusSpecificKmersFile = workingdirectory+"/"+args[2];
                 String virusSpecificKmersFile = workingdirectory+"/customisedDB/"+args[2];
		 printKmersNumOfTaxIDs (tempFile, virusSpecificKmersFile);
		 
		 /*step4:
		  * Delete temp folder and its contents
                  */
		 
		 System.out.println("Deleting temp folder ...");
		 deleteDirectory(new File(tempDir));  
	 
		 long endTime = System.currentTimeMillis();
		 long time=endTime-startTime;
			
		 int seconds = (int)(time / 1000) % 60 ;
		 int minutes = (int)((time / (1000*60)) % 60);
		 int hours = (int)((time / (1000*60*60)) % 24);
			
		 String timeText = String.format("%03d:%02d:%02d", hours, minutes, seconds);
		 System.out.println("Time taken to build the virus-specific database: "+timeText);
	}

	//prints the number of taxIDs that are represented by the k-mer next to the k-mer in the file
	private void printKmersNumOfTaxIDs (String inputFile, String outputFile){
		
		int numOfSingleID = 0;
		int numOfMultipleID = 0;
		int numOfLines =0;
		
		try(BufferedReader br = new BufferedReader (new FileReader(inputFile));
		    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))){
			String line;
			while((line = br.readLine())!= null){
				numOfLines++;
				String [] info = line.split("\t");//k-mer = info[0], count = info[1]
					
				String [] allIDs = info[2].split(" ");//list of all taxa IDs assigned to the k-mer
				int [] idList = new int[allIDs.length];
                for (int i=0; i<allIDs.length; i++)	{
				   idList[i] = Integer.parseInt(allIDs[i]); 
				   //System.out.println(i+": "+idList[i]);
				}
				             			
			    if(idList.length==1) {//only single ID
			    	//1 to indicate the k-mer is originally assigned a single taxa ID
                    pw.println(line+1);                                  
                    numOfSingleID++;
			    }			
			    else {//multiple ID
			    	//length of idList indicates how many taxa IDS were assigend to the k-mer originally
                    pw.println(line+idList.length);
				    numOfMultipleID++;
			    }		
		    }
			
			br.close();
			pw.flush();
			pw.close();
			
			System.out.println();
			System.out.println("There are "+numOfLines+" k-mers in the virus-specific K-mers database file.");
			System.out.println();
			System.out.println("There are "+numOfSingleID+" k-mers are assigned a single taxa ID so they are assigned to that taxa ID.");
			System.out.println();
			System.out.println("There are "+numOfMultipleID+" k-mers are assigned multiple taxa ID.");
			System.out.println();
			
		} catch (NumberFormatException e) {
				e.printStackTrace();
		} catch (IOException e) {
				e.printStackTrace();
		}
	}
	
	private void createDir(String dirPath){
		File tmpDir = new File (dirPath);
	    if(! tmpDir.exists()){
	    	if(tmpDir.mkdir()){
	    		System.out.println(dirPath+" Directory is created!");
	    	}
	    }
	}
	
	private boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
	
private String[] getPermsArray(int permSize){
		
	    char set[] = {'A', 'C', 'G', 'T'};
		
		PermutationFiles PF = new PermutationFiles(permSize,set.length);
		String [] perms = PF.getPermsArray();			
	    PF.printAllKLength(set,permSize,perms); //Populate the permutations array
	      
	     return perms;		
	}

}
