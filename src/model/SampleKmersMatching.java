package model;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import utilities.PermutationFiles;

/***
 * Carries out exact matching between sample k-mers and database k-mers:
 * Sample k-mers are split into smaller files according to their first five bases.
 * e.g. all k-mers start with AAAAA are in one file and k-mers start with AAAAC are in another file and so on.
 * Database k-mers are split in the same manner. 
 * Matches between corresponding files is carried out and only k-mers found in both database and sample
 * files are written to the output file along with their taxonomy lables taken from the database file.
 *  
 * @author Maha Maabar
 *
 */
public class SampleKmersMatching {
	
	private String[] getPermsArray(int k ){
		char set[] = {'A', 'C', 'G', 'T'};
		
		PermutationFiles PF = new PermutationFiles(k,set.length);
		String [] perms = PF.getPermsArray();

    	/*Populate the permutations array*/
		PF.printAllKLength(set,k,perms);
		return perms;
	}
    
	//find k-mers which exists in both sample k-mers files and db k-mers files
	public int [] searchForKmersMatches (String dir, int kmerLen, int k, String prefixName) {
		int []numOfMatches={0,0};
		
		try(PrintWriter pw =  new PrintWriter(new BufferedWriter(new FileWriter(dir+"allMatchedKmers_"+prefixName+"_"+kmerLen)))){
			String [] allPerms = getPermsArray(k);
            int numOfNonMatches =0; //counts number of distinct k-mers that are in the sample but not in the database

			for(int i=0; i < allPerms.length; i++) {//There are 256 files
				String sampleFileName = dir+"sKmers_"+allPerms[i];

				File f = new File(sampleFileName);
				if(!f.exists()) {//No need to carry search with the corresponding file from the database
					continue;
				}

				/*get the corresponding database file */
		        String dbFileName = dir+"dbKmers_"+allPerms[i];

				File hF = new File(dbFileName);
				/*no database file matches the virus file so all k-mers here are labelled unclassified
				 * We do that by adding -1 to the position list
				 */
				if(!hF.exists()){
					/*No need to carry matching, just add the list of sample kmers to the no match list */
					BufferedReader    bf = new BufferedReader( new FileReader(sampleFileName));
					String    kmerInstance ;

					while((kmerInstance = bf.readLine()) !=null){
						numOfNonMatches++;
					 }
					bf.close();
	             }
				 else {
					VirusKmers vDB = new VirusKmers();
					ArrayList<VirusKmers> kmers = vDB.getVirusKmersList(dbFileName, kmerLen);
					ArrayList<String> dbKmersList = vDB.getKmersList(kmers);

	                /*carries out a search for matches
				  	 * Open the sample file and read it line by line.
				  	 * Extract the kmer and search for it in the list of database k-mers.
				  	 * If it exists, write its info to the output file.
				  	 * Info is: k-mer, count in the sample file, taxID(s) from the database file
				  	 */
					BufferedReader bf = new BufferedReader( new FileReader(sampleFileName));
					String kmerInstance ;

					while((kmerInstance = bf.readLine()) !=null){
						String[]words = kmerInstance.split("\t");
						String kmer = words[0];
						int count = Integer.parseInt(words[1]);

	                    int index= Collections.binarySearch(dbKmersList, kmer);
						if(index >=0){
							VirusKmers v = vDB.getVirusKmerFromList(kmers,index);
	                        v.setCount(count);
						    pw.println(v.virusKmerInfo ());
							numOfMatches[0]++;
							numOfMatches[1]=numOfMatches[1]+count;
	                    }
						else{
							numOfNonMatches++;
	                    }
	                 }
					bf.close();
				}
			}
			pw.close();
			
			//delete temp files
			deleteAllFiles (dir,"dbKmers_");
			deleteAllFiles (dir,"sKmers_");
			
		}
		catch (IOException ex){
			System.out.println("Errors reading/writing to files.");
		}		
		return numOfMatches;
	}	
/************** Helper methods **************/
	//deletes all files in the dir with a certain prefix
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
	
}
