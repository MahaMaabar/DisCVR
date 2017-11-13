package customdatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import utilities.PermutationFiles;

/***
 * Count k-mers from the host file and split them into small files so
 * that k-mers starts with the same prefix are all in one file.
 *  
 * @author Maha Maabar
 *
 */
public class HostKmersCounting {

	public HostKmersCounting(String [] parms)	{
	       
		  String hostFileName =parms[0];
		  String kSize = parms[1];
		  String tempDir = parms[2];
		  String hostKmersDir = parms[3];
		  String kAnalyzeDir = parms[4];
		  String permSize = parms[5];
		  
          //setup dir and file names to hold host k-mers		  
		  String hostKmersOutputFile = hostKmersDir+"hostKmers_"+parms[1];
		  String fileNamePrefix =tempDir+"hKmers_"; //example of host k-mer name "hKmers_AAAAA"
		 
		  String [] hostKmersParms = {hostFileName,kSize,hostKmersOutputFile,kAnalyzeDir};
		  
		  System.out.println("Host K-mers Counting ...");
		  runHostKmersCounting(hostKmersParms);
		 
		  String[] hostPermsFiles =createPermFiles(fileNamePrefix,Integer.parseInt(permSize));
		  //writing k-mers to perms files
		  System.out.println("Writing Host K-mers to permutation files ...");
		  writeHostKmersToPermsFiles(hostKmersOutputFile, tempDir, hostPermsFiles[0], hostPermsFiles[hostPermsFiles.length-1]);
	}
/*runs KAnalyze to count k-mers from the host genomes*/	
private void runHostKmersCounting(String [] parms) 	{
		String inputFile = parms[0];
		int kSize = Integer.parseInt(parms[1]);
		String kmersOutputFile = parms[2];
		String kAnalyzeDir = parms[3];
			
	    Runtime rt = Runtime.getRuntime();
        Process proc;  
        int interVal = -1;

        //human genomes is big. It is better to use 3G for java heap
        String command = "java -Xmx3g -jar "+kAnalyzeDir+"/kanalyze.jar count  -k "+kSize+
    			" -o "+ kmersOutputFile+" -f fasta"+" "+inputFile+" -rcanonical";
       
        try {
			proc = rt.exec(command);
			 // Wait for the command to complete.
	        interVal = proc.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        if (interVal != 0)
        	System.out.println("Host K-mers counting encountered some errors: "+interVal);
                    
	}
	
private String[] createPermFiles(String fileNamePrefix,int permSize){
	char set[] = {'A', 'C', 'G', 'T'};
	PermutationFiles PF = new PermutationFiles(permSize,set.length);
	String [] perms = PF.getPermsArray();			
	PF.printAllKLength(set,permSize,perms); //Populate the permutations array
	        	 
	for(int i=0; i<perms.length;i++){
		String permsFile =fileNamePrefix+(perms[i]);
	    try {
	    	PrintWriter pw =  new PrintWriter(new BufferedWriter(new FileWriter(permsFile)));
		} catch (IOException e) {
			e.printStackTrace();
		} 
	 }
	 return perms;		
}


 private void writeHostKmersToPermsFiles(String file, String directory, String firstPerm, String lastPerm){
				
		int permSize = firstPerm.length();
		long recordSize=0;
		List<String> records = new ArrayList<String>();
		String perm = firstPerm;
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))){
		    for (String line = null; (line = br.readLine()) != null;) {
		    	line += "\n";
		    	byte[] buffer = line.getBytes();
		    	
		    	String kmerPerm = line.substring(0, permSize);
		    	
		    	//if the k-mer's perm is the same as perm
				if(kmerPerm.equals(perm)){
					records.add(line);
					recordSize += buffer.length;
					
				}
				else{ //the k-mer's perm is different
					  //write the buffer to a perm file
					
					File permFile = new File(directory+"/hKmers_"+perm);
					
					write(permFile, records,recordSize);
					perm = kmerPerm;		
				    recordSize =0;
				    
				    //new list of records and add current k-mer to it
				    records = new ArrayList<String>();
				    records.add(line);
					recordSize += buffer.length;					
				}		        
		    }
		    
		    //write the records for the last permFile
			File permFile = new File(directory+"/hKmers_"+perm);
						
			write(permFile, records,recordSize);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	

  }
	
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
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
