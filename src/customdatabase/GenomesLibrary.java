package customdatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/***
 * creates the reference genome library file from downloaded data
 * 
 * @author Maha Maabar
 *
 */
public class GenomesLibrary {

	public static void main(String[] args) {
		/*input parameters */
		//input file is a list of non-duplicates taxIDs and the accession number(s) of their reference sequence, if exists.  
		String taxIDFile = args[0];
		//name of the database
		String dbName = args[1];
		//directory contains the reference sequences fasta files and their info 
		String refSeqDir = args[2];
		
		/*output files */        	
		String outFile = dbName+"_referenceGenomesLibrary";
		
		/*delete the output file if it already exists*/
		File fileToDelete = new File(refSeqDir+outFile);
		if (fileToDelete.exists()) {
			fileToDelete.delete();     
		}
		
		GenomesLibrary gl = new GenomesLibrary();
		
		/*Step1:
		 * creates two treeMaps from the information in  the taxIDFile
		 * One TreeMap has the taxIDs and their corresponding accNums, if exits
		 * One TreeMap has the accNums and the list of their corresponding taxIDs in the file
		 */
		TreeMap<String,String> taxIDsMap = gl.getTaxIDsMap(taxIDFile);
		gl.printTaxIDsMap(taxIDsMap);
		TreeMap<String,ArrayList<String>> accNumsMap = gl.getAccNumsMap(taxIDFile);
				
		gl.processRefSeqInfo(taxIDsMap, refSeqDir, outFile, accNumsMap);

	}
	
	private TreeMap<String,String> getTaxIDsMap(String fileName){
		TreeMap<String,String> taxIDsMap = new TreeMap<String,String>();
		
		//Read the file, each line corresponds to a taxID
		//Add the taxID to the map only if an accNum exists
		 try(BufferedReader br= new BufferedReader(new FileReader(fileName))){
			 String line;
			 int numLines = 1;
			 while((line=br.readLine())!=null){
				 String words[]=line.split("\t");				 
				 if(words.length == 2){
					 taxIDsMap.put(words[0], words[1]);
				 }				 
			 numLines++;	 
			 }			 
		 } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return taxIDsMap;
	}
	
	private TreeMap<String,ArrayList<String>> getAccNumsMap(String fileName){
		TreeMap<String,ArrayList<String>> accNumsMap = new TreeMap<String,ArrayList<String>>();
		
		//Read the file, each line corresponds to a taxID
		//Add the taxID to the map only if an accNum exists
		 try(BufferedReader br= new BufferedReader(new FileReader(fileName))){
			 String line;
			 int numLines = 1;
			 while((line=br.readLine())!=null){
				 String words[]=line.split("\t");
				 
				 if(words.length == 2){
					 //search for the accNum string is already in the map
					 String taxID= words[0];
					 String accNumStr= words[1];
					 if(accNumsMap.get(accNumStr)== null){
						ArrayList<String> taxIDsList = new ArrayList<String>();
					    taxIDsList.add(taxID);
					    accNumsMap.put(accNumStr, taxIDsList);
					 }
					 else{
						 //get the already existing list of taxIDs in the map
						 ArrayList<String>taxIDsList = (ArrayList<String>)accNumsMap.get(accNumStr);
						 taxIDsList.add(taxID);
						 accNumsMap.put(accNumStr, taxIDsList);
					 }
				 }				 
			 numLines++;	 
			 }			 
		 } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return accNumsMap;
	}
	
	/*get the information for the reference sequences from the list of taxaIDs Map	 */
	public void processRefSeqInfo(TreeMap<String,String> taxaIDsMap, String directory, String outFile, 
			TreeMap<String, ArrayList<String>> accNumsMap){
		//get an iterator
		Set<Entry<String,ArrayList<String>>> set = accNumsMap.entrySet(); //get a set of the entries
		Iterator<Entry<String,ArrayList<String>>> i = set.iterator(); //get an iterator
		 
		int num =1;
		 //Display elements
		 while (i.hasNext())
		 {
			 Map.Entry me =(Map.Entry)i.next();
			 num++;
			 String accNumStr  = (String)me.getKey();
			 ArrayList<String> taxIDList = (ArrayList<String>)accNumsMap.get(accNumStr);
			 			 
			 //creates the string to hold the taxaID(s) for the refseq
			 String taxaIDStr= "@"+taxIDList.get(0);
			 
			 if(taxIDList.size()>1){
				 for(int index=1;index<taxIDList.size();index++){
				     taxaIDStr += "|"+taxIDList.get(index); 
				 } 
			 }			 
			 			 
			 System.out.println("taxIDStr: "+taxaIDStr);
			 //get the list of accNums
			 String [] accList = accNumStr.split(",");
			 System.out.println("AccList size: "+accList.length);
			 //choose one taxID from the list of taxIDs
			 String taxID = taxIDList.get(0);
			 printRefSeq(taxID, accList, directory, outFile,taxaIDStr);
		 }
	}

/*prints the refSeq header and sequence to a file with the taxID
 * Segmented viruses have a string of 300 Ns added between the segments*/	
 private void printRefSeq(String taxID, String [] accList, String directory, String outFile, String taxaIDStr){
		
		String virusSeqFile= directory+"/Virus_"+taxID+"_RefSeq.fa";
		String refSeqLibFile = directory+"/"+outFile;
		
		//read the file into memory in an array
    	ArrayList<String> headers = new ArrayList<String>();
    	ArrayList<String> seqs = new ArrayList<String>();
    	
    	
	    try(BufferedReader br= new BufferedReader(new FileReader(virusSeqFile));
	    	PrintWriter pw = new PrintWriter(new FileWriter(refSeqLibFile,true))){
	    	
	    	int index =0;
	    	String line;
	    	while((line=br.readLine()) != null){
	    		if (line.charAt(0) =='>'){
	    			String header = line.trim();
	    			String sequence = br.readLine().trim();
	    			headers.add(header);
					seqs.add(sequence);
					index++;	
	    		}
	    	}
	    		
	        //print the ref seq to the file
	    	String refSeq = "";
	    	String oneHeader = "";
    		String oneSeq = "";
    		String nString = createString ('N', 300);
			
			if(accList.length ==1){
				int refSeqIndex = getIndex(headers, accList[0]);
				oneHeader=headers.get(refSeqIndex);
				oneSeq=seqs.get(refSeqIndex);
			}
			else{
				//go through each accNum in the list and format the string
				for(int i=0; i<accList.length;i++){
					if(i==0){
						int refSeqIndex = getIndex(headers, accList[i]);
						oneHeader=headers.get(refSeqIndex)+",";
						oneSeq=seqs.get(refSeqIndex)+nString;
					}
					//last element
					else if(i==accList.length-1){
						int refSeqIndex = getIndex(headers, accList[i]);
						oneHeader=oneHeader+headers.get(refSeqIndex);
						oneSeq=oneSeq+seqs.get(refSeqIndex);
					}
					//other elements
					else{
						int refSeqIndex = getIndex(headers, accList[i]);
						oneHeader=oneHeader+headers.get(refSeqIndex)+",";
						oneSeq=oneSeq+seqs.get(refSeqIndex)+nString;
					}
				}
			}
			
			refSeq=taxaIDStr+"@"+oneHeader;
			refSeq=refSeq+"@"+oneSeq;
	    	
	    	pw.println(refSeq);
	    	br.close();
	    	
	    	pw.flush();
	    	pw.close();
		
	 } catch (IOException e) {
			e.printStackTrace();
	 }
		
}
 /*returns the index of the header with the accNum from the list, if exists
  *If it does not exist, returns -1
*/
 private int getIndex(ArrayList<String> headers, String accNum){
		int index = -1;
		for (int i=0;i<headers.size();i++) {
			//split the headers into parts. 
			//Accession Number comes after >
			String [] headerParts = headers.get(i).split(" ");			
			String headerAcc = headerParts[0].substring(1).trim();
						
			if (headerAcc.equals(accNum.trim())) {
				index = i;
				break;
			}
		}		
		return index;
	}
 
 private String createString (char c, int len){
		String st="";
		for(int i=0;i<len;i++)
			st=st+c;
		return st;
	}
private void printTaxIDsMap(TreeMap<String,String> taxIDsMap){
		//get an iterator
		Set<Entry<String,String>> set = taxIDsMap.entrySet(); //get a set of the entries
		Iterator<Entry<String,String>> i = set.iterator(); //get an iterator
		
		int num=1;
		while (i.hasNext())
		 {
			 Map.Entry me =(Map.Entry)i.next();
			 String taxID  = (String)me.getKey();
			 String accNumStr = (String)me.getValue();
			 			 
			 System.out.println("["+num+"]: <"+taxID+" , "+accNumStr+">");
			 num++;
		 }
	}	

}
