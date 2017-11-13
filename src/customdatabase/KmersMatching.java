package customdatabase;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.io.File;

/*** 
 *Runs matching between host k-mers and virus k-mers, common k-mers are removed
 *Only k-mers that are virus-specific are kept.
 *
 *@author Maha Maabar 
 */
public class KmersMatching {
	
	public KmersMatching(String fileName, int kSize, String[] permFiles,String dirName) {
		searchForKmersMatches(fileName,kSize,permFiles,dirName);
		
	}

  /*searches for virus-specific k-mers (i.e. exists in virus k-mers files but not in the host k-mers files
   *Uses binary search because it is the fastest search algorithm and only carries searching between 
   *corresponding perms files. 
   */
  private void searchForKmersMatches (String fileName, int kmerLen, String [] permFiles, String outputDir) {
	 try(PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)))){
		int numOfVirusSpecKmers =0; //adds counts of the virus-specific Kmers 
		int numOfNonVirusSpecKmers =0; //adds counts of the non virus-specific kmers
		
		/*iterate through each perm file, upload the host file and search for each virus k-mers in the host
		 * if found, it is non virus-specific k-mer
		 * else, it is a virus-specific k-mer
		 */
		for(int i=0;i< permFiles.length;i++)  
			{
				//matches are done on the sorted virus k-mers
				String virusFileName = outputDir+"vKmers_"+permFiles[i]+"_sorted";//get the virus file
								
				File f = new File(virusFileName); 
				if(!f.exists()) //No need to carry search with the corresponding file from the host
				{
					System.out.println("The file "+virusFileName+" Does NOT Exists!");
					continue;				
				}			
				
				String hostFileName = outputDir+"hKmers_"+permFiles[i];	//get the corresponding host file
				
				File hF = new File(hostFileName);
				// No host file matches the virus file so all k-mers in the virus file are virus-specific
				if(!hF.exists()) {
					try(BufferedReader bf= new BufferedReader(new FileReader(virusFileName))){
						String kmerInstance ;	       
					    while((kmerInstance = bf.readLine()) !=null){
						    pw.println(kmerInstance);
						    numOfVirusSpecKmers++;
					    }
					    bf.close();				    
				     }catch(IOException io){
				    	 io.printStackTrace();					
				     }
				}				
				else{
                                      
					// hostFileName exists so a matching is carried out	
					
					//get a list of host k-mers
					ArrayList<String> hostKmers = getHostKmersList(hostFileName,kmerLen);

					/*Open the virus file and read it line by line to search for it in the host list
					 * If there is no match found for the virus k-mer in the host list, then it is a virus-specific k-mer
					 * The whole line (i.e. k-mer, its count and its taxID(s)) is written to the file
					 * If there is a match, the k-mer is non-specific k-mer
					 */	 		
		             BufferedReader bf     = new BufferedReader( new FileReader(virusFileName));
				    String    kmerInstance ;	
				    while((kmerInstance = bf.readLine()) !=null) {
				    	String kmer = kmerInstance.substring(0,kmerLen);
						int index= Collections.binarySearch(hostKmers, kmer);
					    if(index >=0) { //There is a match i.e. k-mer is non-virus-specific
					    	numOfNonVirusSpecKmers++;					    	
						 }else{   
						       
                                                     pw.println(kmerInstance);//keep the virus k-mer as it is
						     numOfVirusSpecKmers++;
						 } 
					}//end-while
					bf.close();
				}//end-else
			}//end-for iterating through all perms files
			pw.close();
				
			System.out.println("*****************************************************************" );  		
		  	System.out.println("There are "+numOfVirusSpecKmers+"  VIRUS-SPECIFIC kmers in the file." );
		  	System.out.println("There are "+numOfNonVirusSpecKmers+" NON-VIRUS-SPECIFIC kmers in the file." );
		  	System.out.println("*****************************************************************");
		
	   }catch(IOException e){
		    e.printStackTrace();
		}    
  }
				
/*get a list of k-mers only from a file 
 * Note: Some files are really huge about 1.3G (33,629,222 kmers) so using BufferedReader to read the file 
 * is better than using FileChannel which reads chunks of file into memory 
 */
 private ArrayList<String> getHostKmersList (String file, int kmerLen){
		ArrayList<String> hostKmers = new ArrayList<String>();
		
		try(BufferedReader bf= new BufferedReader(new FileReader(file))){
			String line;
			int index =0;
			while((line=bf.readLine()) != null){
				index++;
				String kmer = line.substring(0, kmerLen);
				hostKmers.add(kmer);
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return hostKmers;
	}
	
	/*delete all files which starts with a specific string in a directory */
	private void deleteAllFiles (String dirName, String fileN)
	{
		final File dir = new File(dirName);
		    final String[] allFiles = dir.list();
		    for (final String file : allFiles) {
		        if (file.startsWith(fileN)) {
		            new File(dirName + "/" + file).delete();
		        }
		    }
	}
	
	/*delete a directory and its contents */
	private void deleteDirectory(File directory) {
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
	   
	}
}
