package customdatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/***
 * Filters out common sequences from downloaded virus data
 * 
 * @author MahaMaabar
 *
 */
public class DataSequences {

	public static final String DIR_PROPERTY_NAME = "discvrJAR.rootDir";
	public static final String currentDir = System.getProperty("user.dir");

		
	private TreeMap<Integer,String>  virusNameMap; 
	private TreeMap<Integer, String> virusRankMap;
	
	public DataSequences(String namesFile, String nodesFile){
		createNamesMap(namesFile);		
		createRankMap(nodesFile);
	}

	public static void main(String[] args) {
		/*input parameters */
		//directory which contains the data sequences files; the file name is the taxID of the data  
		String directory = args [0];
		//path to the reference genome library file which corresponds to the data in the directory
		String referenceGenomeFile = args[1];
		//name of database
		String dbName = args[2];
		
		/*setting up the path for the NCBI files*/
		String actualPath = System.getProperty(DIR_PROPERTY_NAME, currentDir );
               	
		String namesFile = actualPath+"/customisedDB/names.dmp";
    	String nodesFile = actualPath+"/customisedDB/nodes.dmp";
    	System.out.println(namesFile);
		System.out.println(nodesFile);
    	
    	
    	/*output files */
    	String parentFile = directory+"/"+dbName+"_lineageIDs.txt";
		String infoFileName = directory+"/"+dbName+"_DataInformation.csv";
		String allSeqsFile =  directory+"/"+dbName+"_allSeqsData";
					   
		DataSequences ds = new DataSequences(namesFile,nodesFile);
		
		/*Step1: 
		 * get the list of all files in the directory 
		 */
		String [] files = ds.getFilesList(new File (directory));
		
		/*Step2: 
		 * create a treemap of all the taxIDs in the directory and their number of downloaded sequences 
		 * key:virus taxID, value: number of Sequences
		 */ 
		TreeMap<Integer,Integer> taxIdSeqMap = ds.getTaxaIDs (directory, files);
			   
		//Make a list of all the taxIDs in the directory
		ArrayList<Integer> taxIDsList= ds.getTaxIDsList(taxIdSeqMap);
			  
		/*Step 3:
		 * create a treemap to contain the taxIDs in the list and their virus Names
		 * key: virus taxID, value: virus name
		 */
		TreeMap<Integer,String> taxIDsNameMap = ds.getTaxIDsNameMap(taxIdSeqMap);
		
		/*Step 4:
		 * create a treemap to contain the taxIDs in the list and their rank
		 * key: virus taxID, value: virus rank
		 */
		TreeMap<Integer,String> taxIDsRankMap = ds.getTaxIDsRankMap (taxIdSeqMap);
		
		/*Step 5:
		 * create a treemap of all the accession numbers of the refseq, if found, for the taxIDS in the directory
		 * key: virus taxID, value: a list of the accession numbers 
		 */
		TreeMap<Integer,ArrayList<String>> accNumMap = ds.getAccNumbers(referenceGenomeFile);
			   
		/*Step 6:
		 * create a treemap of all the virus parents and prints it to a file
		 * key: virus TaxID, value: a list of its ancestors in the taxonomy tree
		 * to be used for filtering common sequences
		 */	
		System.out.println("Printing virus ancestor's list...");
		TreeMap<Integer,ArrayList<Integer>> parentsIDsMap = ds.getParentsList(nodesFile,files,taxIdSeqMap,parentFile);
		
		/*Step 7:
		 * filter out common sequences between a virus taxID and its LCA
		 */
		System.out.println("Removing common sequences from ancestor's sequences....");
		ds.filterDataSeq(parentsIDsMap, directory);
		
		/*Step 8:
		 * print out to a file information about the taxIDs in the directory
		 * Each line contains: taxID, num of sequences, virus name, virus rank, accNum of their ref seq if found		 *  
		 */
		String [] virusInfo = ds.getVirusInfo (taxIDsNameMap,taxIDsRankMap,accNumMap,taxIDsList,directory);
		/*for(String str:virusInfo)
			System.out.println(str);*/
		System.out.println("Printing virus information .....");
		ds.printVirusInfo(virusInfo,infoFileName);
		
		/*Step 9:
		 * print out to a file all the viruses sequences in the directory
		 * Each line contains: a header followed by its corresponding sequence  
		 */
		System.out.println("Printing all virus sequences to one file ...");
		ds.printSeqToFile(allSeqsFile,taxIDsList, directory);
		
 }

	/*removes shared sequences, between a taxID and its direct ancestor, from the ancestor's sequences
	 *  
	 */
	public void filterDataSeq(TreeMap<Integer,ArrayList<Integer>> parentsIDsMap, String directory){
		//Iterate over the parents map 
		 Set<Entry<Integer,ArrayList<Integer>>> set = parentsIDsMap .entrySet(); //get a set of the entries
		 Iterator<Entry<Integer,ArrayList<Integer>>> i = set.iterator(); //get an iterator
			
		 //Display elements
		 while (i.hasNext()) {
			 
			 Map.Entry me =(Map.Entry)i.next();
			 int taxID  = (Integer)me.getKey();
			 ArrayList<Integer> parentsIDs = (ArrayList<Integer>)me.getValue();
			 int numOfParents = parentsIDs.size();
			 
			 String taxIDFile=directory+"/Virus_"+taxID+".fa";
			 ArrayList<String> taxIDsHeaders = new ArrayList<String>();
			 
			 try(BufferedReader br = new BufferedReader (new FileReader(taxIDFile))){
				 String line;
				 while((line= br.readLine())!= null){
					 if(line.charAt(0) =='>'){
						 taxIDsHeaders.add(line) ;
					 }
				 }
			}  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*remove common sequences from the ancestors' */
			for(int id: parentsIDs){
				String parentIDFile=directory+"/Virus_"+id+".fa";
				String tempFile=directory+"/Virus_"+id+"_temp.fa";
				
				//int notCommon =0;
				int shared=0;
				 
				 try(BufferedReader br = new BufferedReader (new FileReader(parentIDFile));
					 PrintWriter pw = new PrintWriter(new FileWriter(tempFile))){
					 
					 String line;					 
					 while((line= br.readLine())!= null){
						 if(line.charAt(0) =='>' && !taxIDsHeaders.contains(line)){
							
							 pw.println(line) ;//print header
							 pw.println(br.readLine()) ;//print sequence
						 }
						 if(line.charAt(0) =='>' && taxIDsHeaders.contains(line)){
							 shared++;
						 }
					 }
					 System.out.println("Number of shared sequences between the taxID<"+taxID+"> and its ancestor <"+id+">: "+shared);
					 br.close();
					 pw.flush();
					 pw.close();
					 
					 //origianl file is replaced with contents of temp file 
					 copyFile(tempFile, parentIDFile);
					
				 }  catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 }
		 }
	}

/*
 * load the information of each taxID in the list into an array of strings
 * Each string consists of taxID, numOfSeqs, virusName, virusRank, AccNum(s) for ref seq
 */
public String [] getVirusInfo (TreeMap<Integer,String> taxIDsNameMap,TreeMap<Integer,String> taxIDsRankMap,
		TreeMap<Integer,ArrayList<String>> accNumMap,ArrayList<Integer> taxIDsList,String directory){
	String []taxIDsInfo = new String [taxIDsList.size()] ;
	int index=0;
	for(int id:taxIDsList){
		
		String fileName = directory+"/Virus_"+id+".fa";
		int seqCount = 0;
		
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
			String line;
			while ((line=br.readLine()) != null){
				
				//dealing with exception thrown at empty sequence 
				if(line.length()==0) continue;
				if(line.charAt(0)=='>'){
					seqCount++;
				}
			}
			br.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
				
		String name = taxIDsNameMap.get(id);
		String rank = taxIDsRankMap.get(id);
		
		String accNumStr="";
		//check if id has ref seq
		if(accNumMap.get(id) != null){
			ArrayList<String> accNum = accNumMap.get(id);
			if (accNum.size()==1){
				accNumStr+=accNum.get(0);
			}
			else{
				for(int i=0; i<accNum.size()-1;i++)
				{
					accNumStr+=accNum.get(i)+"+";
				}
				accNumStr+=accNum.get(accNum.size()-1);
			}
		}
		String idInfo=id+","+seqCount+","+name+","+rank+","+accNumStr;
		taxIDsInfo[index++]=idInfo;
	}
	return taxIDsInfo;
}

/*prints the information for all taxIDs in the directory to a file */
public void printVirusInfo(String [] virusInfo, String fileName){
	try(PrintWriter pw = new PrintWriter(new FileWriter(fileName))){
		for(int i=0; i < virusInfo.length;i++){
			pw.println(virusInfo[i]);
		}
		pw.flush();
		pw.close();
	}catch (IOException e) {
		e.printStackTrace();
	}
}
 
 /* copies tmp file to original file and then delete the tmp file  */
  private void copyFile(String sourcePath, String destinationPath) {
	    try {
			Files.copy(Paths.get(sourcePath), new FileOutputStream(destinationPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    File sourceFile = new File(sourcePath);
	    // Does not seem to delete the sourceFile after instantiating it.
	    sourceFile.delete();
	}

	/*returns a map of all taxIDs in a file and their rank, taken from the VirusRankMap
	 * 
	 */	
	public TreeMap<Integer,String> getTaxIDsRankMap (TreeMap<Integer,Integer> taxIDSeqMap){
		TreeMap<Integer,String> taxIdRankMap = new TreeMap<Integer,String>();
		
		//get an iterator
		Set<Entry<Integer,Integer>> set = taxIDSeqMap.entrySet(); //get a set of the entries
		Iterator<Entry<Integer,Integer>> i = set.iterator(); //get an iterator
				
		while (i.hasNext()) {
			Map.Entry me =(Map.Entry)i.next();
			int taxID  = (Integer)me.getKey();
			String rank = (String)virusRankMap.get(taxID);
			taxIdRankMap.put(taxID, rank);
		}
		
		return taxIdRankMap;
 	}
		
	/*returns a map of all taxaIDs in a file and their names, taken from the VirusNamesMap */	
	public TreeMap<Integer,String> getTaxIDsNameMap (TreeMap<Integer,Integer> taxIDSeqMap){
		TreeMap<Integer,String> taxIDsNameMap = new TreeMap<Integer,String>();
		
		//get an iterator
		Set<Entry<Integer,Integer>> set = taxIDSeqMap.entrySet(); //get a set of the entries
		Iterator<Entry<Integer,Integer>> i = set.iterator(); //get an iterator
						
		while (i.hasNext()) {
			Map.Entry me =(Map.Entry)i.next();
			int taxID  = (Integer)me.getKey();
			String name = (String)virusNameMap.get(taxID);
			taxIDsNameMap.put(taxID, name);
		}
		return taxIDsNameMap;
 	}
	
	/* get a list of all files in a directory */
	private String[] getFilesList(File dir) {
		String[] files = null;    
         if(dir.isDirectory()){
             files = dir.list();
         }
         return files;	 
  }
	
	/*Populates the global virusNameMap with all taxIDs (in names.dmp) and their scientific names
	 * The key is the taxID and the value is its name
	 */
	private void createNamesMap  (String filename) {
		virusNameMap = new TreeMap<Integer,String>();
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String nameLine;
	  			
	  		while((nameLine= br.readLine())!= null)
	  		{
	  		    //System.out.println("name line: "+nameLine);
	  			String [] names = nameLine.split("\t\\|\t");
	  			
	  			//add the nodes to the parent map//
	  		    if (names[3].contains("scientific")) {
	  		    	virusNameMap.put(Integer.parseInt(names[0]), names[1]);
	  		    }
	  		  }		
	  			
	  		br.close();
		}
		catch (IOException ex)
		{
			System.out.println("Errors reading file "+filename);
		}
		
		System.out.println("Size of the virus names map :"+virusNameMap.size());
	}
	
	/*Populates the global virusRankMap with all taxIDs (in nodes.dmp) and their rank in the taxonomy tree
	 * The key is the taxID and the value is its rank
	 */
	private void createRankMap  (String filename) {
		virusRankMap = new TreeMap<Integer,String>();
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String nodeLine;
	  			
	  		while((nodeLine= br.readLine())!= null){
	  			String [] words = nodeLine.split("\t\\|\t");	  			
	  		    virusRankMap.put(Integer.parseInt(words[0]), words[2]);
			}		
	  		br.close();
		}
		catch (IOException ex)
		{
			System.out.println("Errors reading file "+filename);
		}
		
		System.out.println("Size of the virus ranks map :"+virusRankMap.size());
	}
	
		/*get a map of taxaIDs and their number of sequences */
		private TreeMap<Integer,Integer> getTaxaIDs (String directory, String [] files){
			TreeMap<Integer,Integer> taxaIDmap = new TreeMap<Integer,Integer>();
			for(int i=0;i<files.length;i++){
				String fileName =directory+"/"+files[i];
				if(fileName.endsWith("_Info")){
					String [] words = files[i].split("_");
					int taxaID = Integer.parseInt(words[1]);
					
					//read the first line in the file = number of sequences for that taxaID
					try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
						String line =br.readLine().trim();
						int numSeq = Integer.parseInt(line);
						
						taxaIDmap.put(taxaID, numSeq);
							        
					 br.close();
					 } catch (IOException e) {
					    e.printStackTrace();
				   }
				}
			 }
			return taxaIDmap;		
		}
	   
		/* returns a list of all keys in the map  */
	   public ArrayList<Integer> getTaxIDsList(TreeMap<Integer,Integer>taxaIDmap){
			Set<Integer> taxaIdSet =taxaIDmap.keySet();
			ArrayList<Integer> taxaIDsList = new ArrayList<Integer>();
			
			for(int i:taxaIdSet)
				taxaIDsList.add(i);
			
			return taxaIDsList;
		}
	   
	 private TreeMap<Integer,ArrayList<String>> getAccNumbers (String fileName){
			TreeMap<Integer,ArrayList<String>> accNumMap = new TreeMap<Integer,ArrayList<String>>();
			
			try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
				int num = 0;
				    // For each ref seq in the file, the format:
				    // @taxaID@header@seq
				    String line ;
					while((line=br.readLine()) !=null ){
															
						if(line.contains("@>")){
							num++;							
							ArrayList<String> accList = new ArrayList<String>();
							
						    //get the list of taxIDs in the string
					        int firstIndex = line.indexOf('@');
					        int nextIndex = line.indexOf('>');
					        String taxIDString = line.substring(firstIndex+1, nextIndex-1);
					        
					        String [] taxIDList = taxIDString.split("\\|");
					        				        
					        //get the list of acc Nums
					        int lastIndex = line.lastIndexOf('@');
					        
					        String accNumString = line.substring(nextIndex,lastIndex);
					        
					        String [] words = accNumString.split(",>");
						    
						    for(int i=0; i<words.length;i++){
						    	String [] info = words[i].split(" ");
							    String accNum = info[0].replace(">", "");
							    accList.add(accNum);
							}
						    
						    for(int index =0; index<taxIDList.length;index++){
						    	accNumMap.put(Integer.parseInt(taxIDList[index]), accList);
						    }
						 } 
					}      
		        
				 br.close();
				
			    
			    } catch (IOException e) {
				  e.printStackTrace();
			   }			
			return accNumMap;
		}
	  
	
  /*creates a treemap of viruses in the directory and their ancestors
   * key: virus taxID, values: List of all taxIDs in the directory which are the virus taxID's ancestors
   */
  public TreeMap<Integer,ArrayList<Integer>> getParentsList(String nodesFile,String [] files, TreeMap<Integer,Integer> taxIDSeqMap,String parentFile){
	        //create a treemap of all the virus parents
			TreeMap<Integer,Integer> virusParentMap = createParentMap  (nodesFile);
			
			TreeMap<Integer,ArrayList<Integer>> parentsIDList = new TreeMap<Integer,ArrayList<Integer>>();
						
			Set<Integer> taxaIdSet =taxIDSeqMap.keySet();
			int num =0; //to keep track of number of parents
			for(int id : taxIDSeqMap.keySet()){
				System.out.println(id);
				ArrayList<Integer> parentsList = getFullLineage(id, virusParentMap);
				
				//check the parents list is not empty
				if(parentsList == null){
				}
				else{
					ArrayList<Integer> pList = new ArrayList<Integer>();
					for(int parentId: parentsList){
						if(taxaIdSet.contains(parentId)){
							pList.add(parentId);
							num++;
							}
					}
					
					if(pList.size()> 0){
						parentsIDList.put(id,pList);
					}
					
				}
			}
			
			printMapToFile(parentFile,parentsIDList);
			
			return parentsIDList;
		}
	 
    /* prints a treemap to console
	 * key:taxID, values: a list of its parents	 
	 */
	 public void printParentingTreeMap (TreeMap<Integer,ArrayList<Integer>> map)
		 {
			 Set<Entry<Integer,ArrayList<Integer>>> set = map.entrySet(); //get a set of the entries
			 Iterator<Entry<Integer,ArrayList<Integer>>> i = set.iterator(); //get an iterator
			 
			 int num =0;
			 //Display elements
			 while (i.hasNext())
			 {
				 Map.Entry me =(Map.Entry)i.next();
				 num++;
				 int taxaID  = (Integer)me.getKey();
				 ArrayList<Integer> values =(ArrayList<Integer>) me.getValue();
				 
				 System.out.print("["+num+"] TaxaID<"+me.getKey() + ">: ");
				 for(int v:values)
				   System.out.print(v+"\t");
				 System.out.println();
			 }
			 
		 }
	/* prints a treemap  to a file
	 * key:taxID, values: a list of its parents	 
	 */
	public void printMapToFile(String fileName, TreeMap<Integer,ArrayList<Integer>> parentsIDMap){
		Set<Entry<Integer,ArrayList<Integer>>> set = parentsIDMap.entrySet(); //get a set of the entries
		Iterator<Entry<Integer,ArrayList<Integer>>> i = set.iterator(); //get an iterator
				 
		try(PrintWriter pw = new PrintWriter(new FileWriter(fileName,true))){
			int num =0;
			//Display elements
			while (i.hasNext()) {
				Map.Entry me =(Map.Entry)i.next();
				num++;
				int taxaID  = (Integer)me.getKey();
				pw.print(taxaID+"\t");
						 
				ArrayList<Integer> values =(ArrayList<Integer>) me.getValue();						
				for(int v:values){
					pw.print(v+"\t");
				}
				pw.println();
			 }
			pw.flush();
			pw.close();
		   }catch (IOException ex)	{
			   ex.printStackTrace();
		}
	}

	/*returns an arrayList contains the full lineage of a taxaID from a tree map.*/
	public ArrayList<Integer> getFullLineage (int taxaID, TreeMap<Integer,Integer> parent_map)
	{
		System.out.println(taxaID);
		System.out.println(parent_map.get(taxaID));
		int aParent = parent_map.get(taxaID).intValue();
	           
	    ArrayList<Integer> a_path = new ArrayList<Integer>(100);
	    a_path.add(aParent);
	    
	    while(aParent > 1) {//The top parent in the tree is 1
	    	aParent = parent_map.get(aParent).intValue();
	    	a_path.add(aParent);	    	
	    }
	    return a_path;
	  }

	/*Returns a tree that contains all the taxaID in the file with their parents
	 * The key is the taxa ID and the value is its parent
	 */
	public TreeMap<Integer,Integer> createParentMap  (String filename) 	{
		TreeMap<Integer,Integer> parentMap = new TreeMap<Integer,Integer>();
		try(BufferedReader br = new BufferedReader (new FileReader(filename))){
		
		String nodeLine;
			
		while((nodeLine= br.readLine())!= null)
		{
			String [] nodes = nodeLine.split("\t\\|\t");				
			/*add the nodes to the parent map*/
			parentMap.put(Integer.parseInt(nodes[0]), Integer.parseInt(nodes[1]));				
		 }				
		br.close();
		}
		catch(IOException ex){
			System.out.println("Errors reading file "+filename);
		}
		return parentMap;
	 }
	
	/*put all the sequences in one file*/
	public void printSeqToFile(String fileName,ArrayList<Integer> taxIDsList, String directory ){
		int numAllSeqs=0;
		for(int id: taxIDsList){
			int numSeqInFile=0;
			//open virus file
			String virusFile = directory+"/Virus_"+id+".fa";
			try(PrintWriter pw = new PrintWriter(new FileWriter(fileName,true));
				BufferedReader br = new BufferedReader (new FileReader(virusFile))){
				String line;
				while((line=br.readLine())!= null){
					//dealing with exception thrown at empty sequence 
					if(line.length()==0) continue;
					if(line.charAt(0)=='>'){	
						numSeqInFile++;
					}
					pw.println(line);
				}
				numAllSeqs += numSeqInFile;
				br.close();
				
				pw.flush();
				pw.close();					
			} 
		    catch (IOException ex)	{
			   ex.printStackTrace();
		    }
		}
                System.out.println("There are "+numAllSeqs+" sequences in the database file: "+fileName);		
	}

}
