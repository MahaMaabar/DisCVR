package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.StringTokenizer;

/***
 * Gets the reference genome to be used in the reference assembly into a separate file.
 * The file has the header on the first line and the sequence on the second line.
 * 
 * @author Maha Maabar
 *
 */
public class ReferenceGenomeFile {
	
	private String header;
	private String sequence;
	
	public ReferenceGenomeFile () {
		header = "";
		sequence = "";
	}
	public String getHeader() {
		return header;
	}
	public String getSequence() {
		return sequence;
	}
    
	//Check if the taxaID for the virus exists in the reference Genomes library.  
	//If yes, set up the header and sequence global variable 
	public boolean setRefGenome(String genomesFile, String taxaID, String dbOption){
		
		BufferedReader br = null;
		try{
			if (dbOption.equals("BuiltInDB")){
				 URL refGenURL = getClass().getResource(genomesFile);
				 
				 if(refGenURL == null) {
						System.err.println("Unable to load the genome file: "+ genomesFile);
					}
				 br = new BufferedReader(new InputStreamReader(refGenURL.openStream()));
				 
			 }
			 if(dbOption.equals("customisedDB")){
				 br = new BufferedReader(new  FileReader(genomesFile));
			 }
		
		 String line = null;
		 while((line = br.readLine()) != null){
				StringTokenizer st = new StringTokenizer(line,"@");
                
				while (st.hasMoreElements()) {					
					String nextStr = st.nextElement().toString();
					if(nextStr.contains("|")){
					   String [] ids = nextStr.split("\\|");
					   for(int i=0; i<ids.length;i++){
						 if(ids[i].equals(taxaID)){
								header =st.nextElement().toString();
								sequence=st.nextElement().toString();
								break;
							}
					   }
					}
					else{						
						if(nextStr.equals(taxaID)){
							header =st.nextElement().toString();
							sequence=st.nextElement().toString();
							break;
						}						
					}
				}
			}
		br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(header.length()==0 && sequence.length()==0){
			return false;
		}
		else 
			return true;		
	}
	
	//prints out the header and sequence global variables to a file (i.e. prints the ref seq to a file)
	public void createRefFile(String refFile){
		//After getting the information for the reference genome. create a fasta file to
		//have the header and seq
		if(header.length() > 0 && sequence.length()> 0)	{
			try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(refFile)))){
				pw.println(header);
				pw.println(sequence);
			     
				pw.flush();
				pw.close();				
			}catch (Exception ex) {
		        	ex.printStackTrace();
		    } 
		}
		else
			System.err.println("Cannot create reference file because reference genome information is missing!");
		}

}
