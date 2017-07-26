package customdatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import utilities.EntropyFilter;
import utilities.ExecutorTask;
import utilities.PermutationFiles;
//import model.PermutationFiles;

public class VirusKmersCounting {


	public VirusKmersCounting (String [] parms)	{
		/*parms={virusFilesDir,kSize,tempDir,kAnalyzeDir,numThreads,permSize,entropyThrshld,fileCounter};*/
		 String [] inputFiles = createInputFilesList(parms[0]);
		  
		  //create subdirectories to hold the virus kmers files
		  //each subdirectory is named with the taxID from the virus file name	
		  String [] outputFiles =createOutputFilesList (parms[2], inputFiles, Integer.parseInt(parms[1]));
                  
          //run k-mers counting on all virus files using multi-threading
		 String [] virusKmersCountParms = {parms[0],parms[1],parms[2],parms[3],parms[4]};
		 runVirusKmersCounting(virusKmersCountParms,inputFiles,outputFiles);
		 System.out.println("Finished Virus K-mers counting!");	
		
		
		String outputDir = parms[2];
	
		//split virus k-mers from all files into small perm files
	
		int counter = Integer.parseInt(parms[7]);
		String fileNamePrefix =outputDir+"vKmers_";////example of virus k-mer name "vKmers_AAAAA"
		String [] virusPermsFiles = createPermFiles(fileNamePrefix,Integer.parseInt(parms[5]));
			
		/*annotates the k-mers by assigning them to the taxID from their file name
		 * filter out the k-mers with entropy less than entropy threshold,
		 * then write the k-mers to the corresponding perm file
		 * To address memory issues, only 500 virus k-mers files are processed at a time
		 */		
	
		System.out.println("Labelling and removing low-entropy Virus k-mers...");
		runKmersFiltering(outputFiles, counter, Double.parseDouble(parms[6]), outputDir, virusPermsFiles);
		
		/*sort the k-mers in the virus perms files to be able to run binary search on them*/
		System.out.println("Sorting virus k-mers...");
		for(int i=0; i<virusPermsFiles.length;i++){
	        String permsFile =fileNamePrefix+(virusPermsFiles[i]);
	        sortPermsFile(permsFile);
	    }
			
	}

/*runs kAnalyze to count k-mers from virus files
 *The process can be multithreaded in case of huge number of files
 */
 private void runVirusKmersCounting(String [] parms, String[]inputFiles,String[]outputFiles){
	  String inputFile = parms[0];
	  int kSize = Integer.parseInt(parms[1]);
	  String outputDir = parms[2];
	  String kAnalyzeDir = parms[3];
	  int numThread = Integer.parseInt(parms[4]);
	  int size = outputFiles.length;
          
       /*for(String p:parms)
		  System.out.println("parameters for run k-mers counting: "+p);
	   */
	
	  /*split the list of files into chunks 
	   *each chunk will use numThread to process virus files. 
	   *Each thread carries k-mer counting from a single file and the output file is put 
	   *in its corresponding output subdirectory*/
	  if(size <= numThread){
		  //System.out.println("Number of files is less than "+numThread);
		
		  int exitValue=-1;
		  try {
			  String command [] = new String [size];
			  Thread task [] = new Thread [size];
			
			  for (int j=0; j<size; j++)	{				
				command [j]="java -Xmx1024m -Xss500k -jar "+kAnalyzeDir+"/kanalyze.jar count  -k "+kSize+
		    			" -o "+ outputFiles[j]+" -f fasta"+" "+inputFiles[j]+" -rcanonical";
	 			//System.out.println(command[j]);
	 			task [j]= new Thread(new ExecutorTask(command [j],outputDir));
	 			task[j].start();
			  }
			
			//wait for all thread to finish
			for(Thread t : task){
				t.join();
				exitValue++;					
			}
		}catch (InterruptedException e) {
	        System.err.println("k-mer counting is interrrupted");
	        Thread.currentThread().interrupt();
	        e.printStackTrace();
	    }	 
		
		//System.out.println("K-mers counting exit value: "+exitValue);
		
		if(exitValue <0 || exitValue < size-1){
			
			System.out.println("K-mers counting didn't finish properly for:"+size);
		}
		
	}
    else{//process k-mers counting in batches 			
		
		int numChunk = size/numThread;
		int extraChunk = size%numThread;
		System.out.println("Number of batches to process with "+numThread+" thread(s) is:"+numChunk);
		if(extraChunk > 0)
			System.out.println("The rest of files will be using "+extraChunk+" thread(s) to process");
		
				
		int batch =1;
		int index = 0;
		while (batch <= numChunk){
			System.out.println("Batch :"+batch);
			//System.out.println("File Index: "+index);
			
			int exitValue=-1;
			try {
				String command [] = new String [numThread];
				Thread task [] = new Thread [numThread];
				
				for (int j=0; j<numThread; j++)	{
					
					command [j]="java -Xmx1024m -Xss500k -jar "+kAnalyzeDir+"/kanalyze.jar count  -k "+kSize+
			    			" -o "+ outputFiles[j+index]+" -f fasta "+" "+inputFiles[j+index]+" -rcanonical";
		 			//System.out.println(command[j]);
		 			task [j]= new Thread(new ExecutorTask(command [j],outputDir));
		 			task[j].start();
				}
				//wait for all thread to finish
				for(Thread t : task){
					t.join();
					exitValue++;					
				}
			}catch (InterruptedException e) {
		        System.err.println("k-mer counting is interrrupted");
		        Thread.currentThread().interrupt();
		        e.printStackTrace();
		    }	 
			//System.out.println("K-mers counting exit value: "+exitValue);
			
			if(exitValue <0 || exitValue < numThread-1){
				System.out.println("K-mers counting didn't finish properly for batch: "+batch);
			}
			
			batch++;
			index += numThread;			
		}		
		
		if(batch > numChunk && extraChunk >0){
			System.out.println("Batch (Last batch to process):"+batch);
			//System.out.println("File Index: "+index);
			int exitValue=-1;
			try {
				String command [] = new String [extraChunk];
				Thread task [] = new Thread [extraChunk];
				
				for (int j=0; j<extraChunk; j++)	{
					
					//System.out.println("File index to process: "+(index+j));
					//System.out.println("File to process: "+inputFiles[j+index]);
					
					command [j]="java -jar "+kAnalyzeDir+"/kanalyze.jar count  -k "+kSize+
			    			" -o "+ outputFiles[j+index]+" -f fasta "+" "+inputFiles[j+index]+" -rcanonical";;
		 			task [j]= new Thread(new ExecutorTask(command [j],outputDir));
		 			task[j].start();
		 			
				}
				//wait for all thread to finish
				for(Thread t : task){
					t.join();
					exitValue++;					
				}
			}catch (InterruptedException e) {
		        System.err.println("k-mer counting is interrrupted");
		        Thread.currentThread().interrupt();
		        e.printStackTrace();
		    }	 
			//System.out.println("K-mers counting exit value: "+exitValue);
			
			if(exitValue <0 || exitValue < extraChunk-2){
				System.out.println("k-mers counting didn't finish properly for Last batch: "+batch);
			}
		}
	}
	
  }
  
  /*get the list of virus file names
   * Assumption: The file name format is Virus_<taxID>.fa
   */
  private String [] createInputFilesList (String directory){
		ArrayList<String> filesList = new ArrayList<String>();
		File[] files = new File(directory).listFiles();
		for(File file: files){
			if(file.isFile() && file.getName().endsWith(".fa")){
			  filesList.add(file.getAbsolutePath());
			}				
		}
		
		String [] fileNames= filesList.toArray(new String[filesList.size()]);
		
		return fileNames;
	}

  /*creates a subdirectory in the outputDir, for each virus file, to hold the k-mers output files
   *This is to avoid mixed results from KAnalyse when done in one directory
   *returns an array with the name of the virus k-mers output file
   *Format of the output file name: <taxID>_kmers<kSize>
   */
 private String [] createOutputFilesList (String outputDir, String [] inputFiles,int kSize){
		String [] outputFiles = new String [inputFiles.length];
		/*System.out.println("OutputDir:"+outputDir);
		System.out.println("Number of inputFiles "+inputFiles.length);
		System.out.println("K size:"+kSize);*/
		for(int i =0; i < outputFiles.length; i++){
			//get the taxID from the file name "/Virus_taxID.fa"
			File f = new File(inputFiles[i]);
			String fileName = f.getName();
			String taxID = fileName.substring(fileName.lastIndexOf("_")+1, fileName.indexOf("."));
			
			//create a folder with the same taxID to hold k-mers counting results			
			outputFiles[i] = outputDir+taxID+"/"+taxID+"_kmers"+kSize;
			
			String folderName=outputDir+"/"+taxID+"/";
			File tmpDir = new File (folderName);
		    if(! tmpDir.exists()){
		    	if(tmpDir.mkdir()){
		    	    File outFile = new File(outputFiles[i]);
                            try{
                              outFile.createNewFile();
                            }catch(IOException e){
                               System.out.println("Failed to create the file "+outFile.getName());
                            }
		    	}		    	
		    }
		}
		
		return outputFiles;
	}

 /*gets a list of the names of the output k-mers file in a directory */ 
 private void getKmersOutputFile (String directory,ArrayList<File> files ){
	 File[] fList = new File(directory).listFiles();
	 for(File file: fList){
		 if(file.isFile()){
			
			if(file.getName().contains("_kmers"))
			   files.add(file);
		 }
		 else if(file.isDirectory()) {
			getKmersOutputFile(file.getAbsolutePath(),files);
		 }
	 }
}

  /*returns an array from a list*/
  private String [] getFileNames(ArrayList<File> filesList ){
		String [] fileNames= new String[filesList.size()];
		
		for(int i=0; i<filesList.size();i++){
			File file = filesList.get(i);
			if(file.isFile() && file.getName().contains("_kmers")){
			   fileNames[i]=file.getAbsolutePath();				
			}				
		}
		return fileNames;
  }
 
  /*creates perms files with the fileNamePrefix */
 private String[] createPermFiles(String fileNamePrefix,int permSize){
		
	    char set[] = {'A', 'C', 'G', 'T'};
		
		PermutationFiles PF = new PermutationFiles(permSize,set.length);
		String [] perms = PF.getPermsArray();			
	    PF.printAllKLength(set,permSize,perms); //Populate the permutations array
	        	 
	    for(int i=0; i<perms.length;i++){
	    	String permsFile =fileNamePrefix+(perms[i]);
	        try (PrintWriter pw =  new PrintWriter(new BufferedWriter(new FileWriter(permsFile)))) {
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
	    }
	     return perms;		
 }
 
 /* Filters all virus k-mers so low entropy k-mers are removed
  * The virus k-mers are processed in batches equal to counter
  * The process invloves uploading the files into a treemap, annotating the k-mers with the taxID from
  * their file name, updates their counts.
  * Only k-mers with entropy > entropy threshold are added to the treemap
  */
private void runKmersFiltering(String [] fileNames, int counter, double entropyThreshold, String outputDir, String []perms){
	System.out.println("Number of virus Files:"+fileNames.length);
	if(fileNames.length <= counter){
		   System.out.println("Number of files are less than counter!");
		   readViruKmersFiles(fileNames,entropyThreshold,outputDir,perms[0],perms[perms.length-1]);
	   }
	   else{
		   //divide the num of files into batches so that 
		   int numChunk = fileNames.length / counter;
		   int extraChunk = fileNames.length % counter;
		   		   
		   System.out.println("Number of batches to process virus k-mers files are: "+numChunk+" / "+counter+" files per batch ");
		   
		   int lastBatchSize = fileNames.length - (numChunk*counter);
		   if(extraChunk > 0){			   
			   System.out.println("Last batch will process "+lastBatchSize+" virus k-mers files.");
			   
		   }
			   
		   int index=0;
		   int batch =1;
		   for(int i = 0;i<numChunk;i++){
			int firstIndex = index;
		    	int lastIndex = index+(counter-1);
		    	
	            /* System.out.println("Number of files to process in batch "+batch+" is "+counter);
	 	     System.out.println("First index ["+firstIndex+"]:"+fileNames[firstIndex]);
                     System.out.println("Last index ["+lastIndex+"]:"+fileNames[lastIndex]);*/
	             
                     String [] filesToProcess = new String [counter];
                     
                     for(int n=0;n<counter;n++){
	            	 filesToProcess [n] = fileNames[firstIndex+n];	
                                  	 
	             }
                     
	             readViruKmersFiles(filesToProcess,entropyThreshold,outputDir,perms[0],perms[perms.length-1]);
	            
                     index += counter; 
	              
	             batch++;
		    }
		   //process last batch
		   
		   System.out.println("Processing last batch of size "+lastBatchSize);
		   /*System.out.println("starting at index: "+index+
				              " and ends at index "+(index+lastBatchSize-1));*/
		   /*System.out.println("Number of files to process in batch "+batch+" is "+lastBatchSize);
		   System.out.println("First index ["+index+"]:"+fileNames[index]);
		   System.out.println("Last index ["+(index+lastBatchSize-1)+"]:"+fileNames[(index+lastBatchSize-1)]);*/
            String [] filesToProcess = new String [lastBatchSize];
            for(int n=0;n<lastBatchSize;n++){
           	 filesToProcess [n] = fileNames[index+n]; 
                 	            	 
            }
            readViruKmersFiles(filesToProcess,entropyThreshold,outputDir,perms[0],perms[perms.length-1]);
	   }
 }

/*uploads a set of files into a treemap
 * Treemap<String, ArrayList<Integer>
 * Key: k-mer, Value: first integer is the k-mer count, the rest are taxIDs which the k-mer comes from 
 * The treemap is printed into virus perms files 
 */
private void readViruKmersFiles(String [] files, double entropyThrshld,String directory, String firstPerm, String lastPerm){
	TreeMap<String,ArrayList<Integer>> virusKmersMap = new TreeMap<String,ArrayList<Integer>>();
	int index = 1;
	for (String file: files){
	    File f = new File(file);
	    String fileName = f.getName();
		
	    int lastIndex = fileName.lastIndexOf("_kmers");
		int taxID = Integer.parseInt(fileName.substring(0,lastIndex));
		
		
		Pattern newLine = Pattern.compile("\n");
		String [] tokens = null;
		CharBuffer charBuffer = null;
		String charEncoding = null;
		MappedByteBuffer mappedByteBuffer = null;
		
		try{//mappedByteBuffer is used because it is fast
			charEncoding = System.getProperty("file.encoding");
			
			//Read a file
			try(FileChannel fileChannel = new FileInputStream(file).getChannel()){
				mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
				 if(mappedByteBuffer != null){
				    try{
					charBuffer = Charset.forName(charEncoding).decode(mappedByteBuffer);
                                        
                                        StringBuffer sb = new StringBuffer(charBuffer);
					
					tokens = newLine.split(sb);
					
                                        mergeKmers (virusKmersMap, tokens, entropyThrshld, taxID);   

                                        }catch (IllegalArgumentException ex) {
                                            if(charBuffer == null){
                                               System.out.println("charBuffer for taxID: "+taxID+" is null!");
                                            }
                                            else{
                                               System.out.println("charBuffer size for taxID: "+taxID+" is "+charBuffer.length());
                                            }
                                           
                                        }
					
				}

			}catch (IOException ioe){
				ioe.printStackTrace();
			}catch (ArrayIndexOutOfBoundsException aio )	{
				System.out.println("No virus k-mers from the virus file with taxID: "+taxID);
				System.out.println("The file is empty or the length of the sequence is less than the k-mer size");
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
	index++;	
	}

	printVirusKmersToPermsFile(directory, virusKmersMap, firstPerm, lastPerm);	
}

/*updates k-mers information in the treemap when k-mer is added 
 * tokens is an array of lines: each line consists of a k-mer and its count
 * k-mers are added to the virusKmersMap, their counts is updated and they all assigned the taxID
 * Only k-mers with entropy > threshold are added to the virusKmersMap
 * */
private void mergeKmers (TreeMap<String,ArrayList<Integer>> virusKmersMap, String [] tokens, double entropyThrshld, int taxID){
	if(tokens.length == 0) {//No virusKmers output from that taxID
		System.out.println("No virus k-mers found from taxID file: "+taxID);
	}
	else{
		for (int i= 0; i< tokens.length;i++){			
			String [] words = tokens[i].split("\t");
			String kmer = words[0];
                        
                        int count = Integer.parseInt(words[1]);
						
			//only k-mers with entropy > threshold are added to the tree
			double entropy=getEntropy(kmer);
			if(entropy >  entropyThrshld){
				if(virusKmersMap.get(kmer) !=null){
					ArrayList<Integer> values= (ArrayList<Integer>) virusKmersMap.get(kmer);
					
					//add the count at the start of the list
					int oldCount = values.get(0);
					oldCount += count;
					values.remove(0);
					values.add(0, oldCount);
					values.add(taxID);//add the taxID to the list
                                        
                }
				else{
					//create a list of the count and taxID
					ArrayList<Integer> values = new ArrayList<Integer>();
					values.add(0,count);
					values.add(taxID);
					
					virusKmersMap.put(kmer,values);
				}
			}	
		}
		
	}
	
}

/*prints the treemap to virus k-mers perms files
 * Each k-mer is written to the perms files that starts with its perm
 */
private void printVirusKmersToPermsFile(String Directory, TreeMap<String,ArrayList<Integer>> virusKmersMap, String firstPerm, String lastPerm) {
	Set<Entry<String,ArrayList<Integer>>> set = virusKmersMap.entrySet(); //get a set of the entries
	Iterator<Entry<String,ArrayList<Integer>>> i = set.iterator(); //get an iterator
	int num=0;
	int permSize = firstPerm.length();
	long recordSize=0;
	List<String> records = new ArrayList<String>();
	String perm = firstPerm;
	while (i.hasNext()){
		num++;
		Map.Entry me =(Map.Entry)i.next();
		String kmer = (String)me.getKey();
		
		ArrayList<Integer> values = (ArrayList<Integer>)me.getValue();
		int count = values.get(0);
		//get the first letters from the k-mer that are the size of the perm
		String kmerPerm = kmer.substring(0, permSize);
		
		//create a record from the element
		String record =kmer+"\t"+count+"\t";
		for(int index=1; index<values.size(); index++){
			record +=values.get(index)+" ";
		}
		record +="\n";
		byte[] buffer = record.getBytes();
		//if the k-mer's perm is the same as perm
		if(kmerPerm.equals(perm)){
			records.add(record);
			recordSize += buffer.length;			
		}
		else{ //the k-mer's perm is different
			  //write the buffer to a perm file
			
			File permFile = new File(Directory+"/vKmers_"+perm);
			
			write(permFile, records,recordSize);
			perm = kmerPerm;		
		    recordSize =0;
		    
		    //new list of records and add current k-mer to it
		    records = new ArrayList<String>();
		    records.add(record);
			recordSize += buffer.length;			
		}			
	}
	//write the records for the last permFile
	File permFile = new File(Directory+"/vKmers_"+perm);
	write(permFile, records,recordSize);  	
}

/*writes a list of virus k-mers and their information to a file
 * Uses FileChannel because it is faster than bufferedWriter
 */
private void write(File file, List<String> records, long recordSize){
	
	try {
			final RandomAccessFile raf = new RandomAccessFile(file,"rw");
			raf.seek(raf.length());
			final FileChannel fc = raf.getChannel();
			final MappedByteBuffer mbf = fc.map(FileChannel.MapMode.READ_WRITE,fc.position(), recordSize);
			fc.close();
			for(int i=0;i<records.size();i++){
				final byte [] recordBytes = records.get(i).getBytes(Charset.forName("ISO-8859-1"));
				mbf.put(recordBytes);
			}
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}			
	
}

/*caluclates the shannon entropy of a k-mer using the tri-nucleotide base*/
private double getEntropy(String kmer){
	int bases = 3; // bases to use for the entropy i.e. di- or tri-nucleotide
	EntropyFilter ef = new EntropyFilter();
	double entropy = ef.calculateShannonEntropy(kmer,bases);
	return entropy;
}

/*Reads a virus k-mers perm file into a treemap, 
 *Writes the treemap to a file, this will print the virus k-mers sorted alphabetically
 */
private void sortPermsFile(String fileName){
	TreeMap<String,ArrayList<Integer>> virusKmers = new TreeMap<String,ArrayList<Integer>>();
	int numLines=0;
	String line;
	try(BufferedReader br= new BufferedReader(new FileReader(fileName))){
		while((line=br.readLine()) != null){
			numLines++;
			String [] words = line.split("\t");
			String kmer = words[0];
			int count = Integer.parseInt(words[1]);
			String taxIDsInfo = words[2];
			
			//split the taxIDs into a list
			String [] taxIDs = taxIDsInfo.split(" ");
			
			
			//upload the line onto the tree, updating the k-mers count and its taxID list				
			if(virusKmers.get(kmer) !=null){
				ArrayList<Integer> values= (ArrayList<Integer>) virusKmers.get(kmer);
				
				//add the count to the list
				int oldCount = values.get(0);
				oldCount += count;
				values.remove(0);
				values.add(0, oldCount);
				
				//add the taxID to the list
				for(String taxID:taxIDs){
					int id = Integer.parseInt(taxID);
                                         
                                        //no duplicates taxIDs
					if (!values.contains(id))
						values.add(id);
                                       //if taxID is the same as count
                                       if (id ==count)
						values.add(id);
				}
				
			}
			else{
				//create a list of the count and taxID
				ArrayList<Integer> values = new ArrayList<Integer>();
				values.add(0,count);
				
				for(String taxID:taxIDs){
					int id = Integer.parseInt(taxID);
					//no duplicates taxIDs
					if (!values.contains(id))
						values.add(id);
                                       //if taxID is the same as count
                                       if (id ==count)
						values.add(id);
				}
									
				virusKmers.put(kmer,values);
				
			}
			
		}
		br.close();
	} catch (IOException e) {
	
	e.printStackTrace();
    }
	
	//print the sorted virus k-mers to a different file
	String sortedFile = fileName+"_sorted";//if writing to the same file, it will wipe out some k-mers
	printSortedKmersToPermsFile(sortedFile, virusKmers);
	
	}

/*prints the k-mers int the treemap to the file*/
private void printSortedKmersToPermsFile(String fileName, TreeMap<String,ArrayList<Integer>> virusKmersMap) {
	//open the file to write to it
	try (PrintWriter pw =  new PrintWriter(new BufferedWriter(new FileWriter(fileName)))){
		
		//iterate through the kmers map
		Set<Entry<String,ArrayList<Integer>>> set = virusKmersMap.entrySet(); //get a set of the entries
    	Iterator<Entry<String,ArrayList<Integer>>> i = set.iterator(); //get an iterator
	    int num=0;
	    while (i.hasNext()){
	    	num++;
		    Map.Entry me =(Map.Entry)i.next();
		    String kmer = (String)me.getKey();
		
		    ArrayList<Integer> values = (ArrayList<Integer>)me.getValue();
		    int count = values.get(0);
		    
		    pw.print(kmer+"\t"+count+"\t"); //write the k-mer to the file
			for(int index=1; index<values.size(); index++){
				pw.print(values.get(index)+" ");
			}
			pw.println();
	    }	
		pw.flush();
		pw.close();
	 } catch (IOException e) {
			e.printStackTrace();
	}		 
	
	}

}
