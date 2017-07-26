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

public class ReferenceGenomeFile {
	
	private String header;
	private String sequence;
	
	public ReferenceGenomeFile () {
		header = "";
		sequence = "";
	}

	/*public static void main(String[] args) {
		String genomesFile = args[0];
		String taxaID = args [1];
		
		
		String genomesFile = "E:\\Eclipse_WorkSpace\\DisCVR_OnlineVersion\\resources\\referenceGenomes_1";
		String taxaID = "11599";
		
		ReferenceGenomeFile rgf = new ReferenceGenomeFile();
		rgf.setRefGenome(genomesFile, taxaID);
		
		String refFile = "./TempFiles/refFile_"+taxaID;
		rgf.createRefFile(refFile);
		
		System.out.println("Ref Genome Header: "+rgf.getHeader());
		System.out.println("Ref Genome Sequence: "+rgf.getSequence().length());

	}*/
	
	
	public String getHeader() {
		return header;
	}

	public String getSequence() {
		return sequence;
	}
    
	//Check if the taxaID for the virus exists in the reference Genomes library. and set the 
	//If yes, set up the header and sequence global variable 
	public boolean setRefGenome(String genomesFile, String taxaID, String dbOption){
		//System.out.println("The reference genome File: "+genomesFile);
		//System.out.println("The db Option: "+dbOption);
		
		BufferedReader br = null;
		try{
			if (dbOption.equals("BuiltInDB")){
				 //System.out.println("Genomes File in Resource: "+genomesFile);
				 URL refGenURL = getClass().getResource(genomesFile);
				 
				 if(refGenURL == null) {
						System.err.println("Unable to load the genome file: "+ genomesFile);
					}
				 br = new BufferedReader(new InputStreamReader(refGenURL.openStream()));
				 
			 }
			 if(dbOption.equals("customisedDB")){
				 //System.out.println("Genomes File in customised DB: "+genomesFile);
				 br = new BufferedReader(new  FileReader(genomesFile));
			 }
		
		 String line = null;
		 while((line = br.readLine()) != null){
				//numOfLines++;	
				
				//System.out.println("Line ["+numOfLines+"(length:"+line.length()+"]:"+line);
				
				StringTokenizer st = new StringTokenizer(line,"@");
                //int numToken =0;
				//System.out.println("---- Split by @ ------");
				while (st.hasMoreElements()) {
					//numToken++;
					String nextStr = st.nextElement().toString();
					//System.out.println("Token["+numToken+"]: "+nextStr);
					if(nextStr.contains("|")){
					   String [] ids = nextStr.split("\\|");
					   //System.out.println("Number of taxaIDs: "+ids.length);
					   for(int i=0; i<ids.length;i++){
						 //System.out.println("ids["+i+"]: "+ids[i]);
							if(ids[i].equals(taxaID)){
								//System.out.println("[if] Found Taxa ID: "+taxaID);
								header =st.nextElement().toString();
								//System.out.println("Header("+header.length()+"): "+header);
								sequence=st.nextElement().toString();
								//System.out.println("seq("+sequence.length()+"): "+sequence);
								break;
							}
					   }
					}
					else{
						
						if(nextStr.equals(taxaID)){
							//System.out.println("[else] Found Taxa ID: "+taxaID);
							header =st.nextElement().toString();
							//System.out.println("Header("+header.length()+"): "+header);
							sequence=st.nextElement().toString();
							//System.out.println("seq("+sequence.length()+"): "+sequence);
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
			//System.out.println("No reference Genome for the taxa ID "+taxaID+" exists in our database!");
			return false;
		}
		else 
			return true;
		
	}
	
	//prints out the header and sequence global variables to a file (i.e. prints the ref seq to a file)
	public void createRefFile(String refFile){
		//After getting the information for the reference genome. create a fasta file to
		//have the header and seq
		if(header.length() > 0 && sequence.length()> 0)
		{
			System.out.println("Creating the refFile for assembly!");
			System.out.println("Header: "+header+"\tThe sequence is of length: "+sequence.length());
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
