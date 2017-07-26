package customdatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
                /*String namesFile = actualPath+"/resources/names.dmp";
    	String nodesFile = actualPath+"/resources/nodes.dmp";*/		
		String namesFile = actualPath+"/customisedDB/names.dmp";
    	String nodesFile = actualPath+"/customisedDB/nodes.dmp";
    	
    	//System.out.println("Actual Path: "+actualPath);
			   
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
		TreeMap<Integer,ArrayList<Integer>> parentsIDsMap =ds.getParentsList(nodesFile,files,taxIdSeqMap,parentFile);
		
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
				 //System.out.println("taxID<"+taxID+"> has "+taxIDsHeaders.size()+" sequence(s)");
			 }  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*remove common sequences from the ancestors'
			 * 		
			 */
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
							 //notCommon++;
						 }
						 if(line.charAt(0) =='>' && taxIDsHeaders.contains(line)){
							 shared++;
							 //System.out.println("Shared header["+shared+"]:<"+taxID+","+id+">"+line);
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
		//System.out.println("ID: "+id);
		//get the num of sequences 
		String fileName = directory+"/Virus_"+id+".fa";
		int seqCount = 0;
		
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
			String line;
			while ((line=br.readLine()) != null){
				
				//dealing with exception thrown at empty sequence 
				if(line.length()==0) continue;
				if(line.charAt(0)=='>'){
					//System.out.println("["+seqCount+"]"+line);
					seqCount++;
				}
						
				
			}
			br.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		String name = taxIDsNameMap.get(id);
		String rank = taxIDsRankMap.get(id);
		
		String accNumStr="";
		//check if id has ref seq
		if(accNumMap.get(id) != null){
			ArrayList<String> accNum = accNumMap.get(id);
			//System.out.println("AccNum["+id+"]: has "+accNum.size());
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
			//System.out.println("AccNumStr["+id+"]:"+accNumStr);
		}
		String idInfo=id+","+seqCount+","+name+","+rank+","+accNumStr;
		//System.out.println("line["+id+"]:"+ idInfo);
		taxIDsInfo[index++]=idInfo;
	}
	return taxIDsInfo;
}

/*prints the information for all taxIDs in the directory to a file
 * 
 */
public void printVirusInfo(String [] virusInfo, String fileName){
	try(PrintWriter pw = new PrintWriter(new FileWriter(fileName))){
		for(int i=0; i < virusInfo.length;i++){
			pw.println(virusInfo[i]);
		}
		pw.flush();
		pw.close();
	}catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
 
 /* copies tmp file to original file and then delete the tmp file
  * 
  */
  private void copyFile(String sourcePath, String destinationPath) {
	  
	    try {
			Files.copy(Paths.get(sourcePath), new FileOutputStream(destinationPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    File sourceFile = new File(sourcePath);
	    if(sourceFile.delete()){
			//System.out.println("Temp File: "+sourceFile.getName() + " is deleted!");
		}else{
			//System.out.println("Delete operation is failed.");
		}
	    
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
		
	/*
	 * returns a map of all taxaIDs in a file and their names, taken from the VirusNamesMap
	 */	
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
	
	/*
	 * get a list of all files in a directory
	 */
	private String[] getFilesList(File dir) {
		//System.out.println("Listing files in the directory... "); 
    	     
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
	  		  }//end-while		
	  			
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
			}//end-while		
	  		br.close();
		}
		catch (IOException ex)
		{
			System.out.println("Errors reading file "+filename);
		}
		
		System.out.println("Size of the virus ranks map :"+virusRankMap.size());
	}
	
		/*get a map of taxaIDs and their number of sequences
		 * 
		 */
		private TreeMap<Integer,Integer> getTaxaIDs (String directory, String [] files){
			TreeMap<Integer,Integer> taxaIDmap = new TreeMap<Integer,Integer>();
			for(int i=0;i<files.length;i++){
				String fileName =directory+"/"+files[i];
				//System.out.println("File("+i+") "+fileName);
				if(fileName.endsWith("_Info")){
					String [] words = files[i].split("_");
					//System.out.println(words[0]+"\t"+words[1]+"\t"+words[2]);
					int taxaID = Integer.parseInt(words[1]);
					
					//read the first line in the file = number of sequences for that taxaID
					try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
						String line =br.readLine().trim();
						int numSeq = Integer.parseInt(line);
						
						//System.out.println("["+i+"]: TaxaID = "+taxaID+" Num Of Seq. = "+numSeq);
						
						taxaIDmap.put(taxaID, numSeq);
							        
					 br.close();
					 } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				   }
			    
				}//if-end
				
			  }//for-end
		
			return taxaIDmap;		
		}
	   
		/* returns a list of all keys in the map
		 * 
		 */
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
							
						    //System.out.println("line: "+line);
							
						    //get the list of taxIDs in the string
					        int firstIndex = line.indexOf('@');
					        int nextIndex = line.indexOf('>');
					        String taxIDString = line.substring(firstIndex+1, nextIndex-1);
					        //System.out.println("TaxaIDString: "+taxIDString);
					        
					        
					        String [] taxIDList = taxIDString.split("\\|");
					        //System.out.println("There are "+taxIDList.length+" taxID(s) in the header");
					        
					        /*for(String s: taxIDList){
					        	System.out.print(s+",");
					        }
					        System.out.println();*/
					        
					        //get the list of acc Nums
					        int lastIndex = line.lastIndexOf('@');
					        
					        //System.out.println("first Index = "+firstIndex+"\t next Index = "+nextIndex+"\t last Index = "+lastIndex);
					        
					        String accNumString = line.substring(nextIndex,lastIndex);
					        //System.out.println("AccNumString: "+accNumString);
					        
					        String [] words = accNumString.split(",>");
						    
						    //System.out.println("The length of words in the Acc Num String is "+words.length);
						   
						    for(int i=0; i<words.length;i++){
						    	String [] info = words[i].split(" ");
							    //System.out.println("Info: "+info[0]);
							    String accNum = info[0].replace(">", "");
							    //System.out.println("Acc Num: "+accNum);
							    accList.add(accNum);
							}
						    
						    for(int index =0; index<taxIDList.length;index++){
						    	accNumMap.put(Integer.parseInt(taxIDList[index]), accList);
						    }
						 } 
					}      
		        
				 br.close();
				
			    
			    } catch (IOException e) {
				// TODO Auto-generated catch block
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
				//System.out.println("key - "+id);
				
				ArrayList<Integer> parentsList = getFullLineage (id, virusParentMap);
				
				//check the parents list is not empty
				if(parentsList == null){
					//System.out.println("The parent list for taxaID: "+id+" is null");
				}
				
				else{
					//System.out.println("The size of the parent list is "+parentsList.size());
					ArrayList<Integer> pList = new ArrayList<Integer>();
					//examine if any from the parent list exists in the taxaIdSet
					//System.out.print("Parents List: ");
					for(int parentId: parentsList){
						//System.out.print(parentId+"\t");
						if(taxaIdSet.contains(parentId)){
							pList.add(parentId);
							num++;
							//System.out.println("*****");
						}
					}
					//System.out.println();
					
					if(pList.size()> 0){
						parentsIDList.put(id,pList);
					}
					
					//System.out.println("<"+id+"> has "+pList.size()+" id(s) from the files in its parents list");
					
				}
			}
			
			//System.out.println("Number of parents found: "+num);
			
			//printParentingTreeMap (parentsIDList);
			
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
				//System.out.print("["+num+"] TaxaID<"+me.getKey() + ">: ");
				for(int v:values){
					// System.out.println("parent:"+v);
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
		int aParent = parent_map.get(taxaID).intValue();
	           
	    ArrayList<Integer> a_path = new ArrayList<Integer>(100);
	    a_path.add(aParent);
	    //System.out.print("TaxaID<"+taxaID+"-->"+aParent);
	    
	    
	    while(aParent > 1) //The top parent in the tree is 1
	    {
	    	//a_path.add(aParent);
	    	
	    	aParent = parent_map.get(aParent).intValue();
	    	//System.out.print("-->"+aParent);	    	
	    	
	    	a_path.add(aParent);	    	
	    }
	    
	    //System.out.println();
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
			//System.out.println("Reading file: "+virusFile);
			try(PrintWriter pw = new PrintWriter(new FileWriter(fileName,true));
				BufferedReader br = new BufferedReader (new FileReader(virusFile))){
				String line;
				while((line=br.readLine())!= null){
					//dealing with exception thrown at empty sequence 
					if(line.length()==0) continue;
					if(line.charAt(0)=='>'){	
						//System.out.println("["+numSeqInFile+"]"+line);
						numSeqInFile++;
					}
					pw.println(line);
				}
				//System.out.println("There are "+numSeqInFile+" sequences in the virus file: "+virusFile);
				
				numAllSeqs += numSeqInFile;
				br.close();
				
				pw.flush();
				pw.close();					
			} 
		    catch (IOException ex)	{
			   ex.printStackTrace();
		    }
			//System.out.println("There are "+numAllSeqs+" sequences in the database file: "+fileName);
		}
                System.out.println("There are "+numAllSeqs+" sequences in the database file: "+fileName);		
	}

}
