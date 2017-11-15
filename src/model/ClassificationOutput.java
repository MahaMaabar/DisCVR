package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import java.net.URL;
import controller.VirusResult;
/***
 * Creates treemaps from the classification output
 * 
 * @author Maha Maabar
 *
 */

public class ClassificationOutput {
	public static final String DIR_PROPERTY_NAME = "discvrJAR.rootDir";
	public static final String currentDir = System.getProperty("user.dir");
	//treemap<key,value>
	private TreeMap<Integer,String>  virusNameMap; //<taxID,virusName>
	private TreeMap<Integer, String> virusRankMap; //<taxID,virusRank>
	private TreeMap<Integer, Integer> totalCountsDistinctKmersMap; //<taxID,total counts of distinct k-mers>
    private TreeMap<Integer, Integer> distinctKmerMap; //<taxID, number of distinct k-mers>
    private TreeMap<Integer, Integer> sharedKmerMap;   //<taxID, number of shared k-mers>
    private TreeMap<Integer, Integer> DBtotalCountsMap; //<taxID, total counts of k-mers in DB>
    private TreeMap<Integer, Integer> DBdistinctKmerMap;//<taxID, number of distinct k-mers in DB>
    private String [] highestScoresNames ;
    private int [] highestScoresSpecific;
    private int [] highestScoresShared;
    //key:K-mer (matched k-mer in the sample, value: its count in the sample
    private TreeMap<String, Integer> allMatchedKmers; 
        
  //constructor to set the virus names and ranks using appropriate dmps files
  public ClassificationOutput(String dbOption) {
    	String namesFile ="";
    	String nodesFile ="";
    	
    	if(dbOption.equalsIgnoreCase("BuiltInDB")){
    		namesFile = "/resources/names.dmp";  
    		nodesFile = "/resources/nodes.dmp";    		
    	}
    	
    	if(dbOption.equalsIgnoreCase("customisedDB")){
    		String actualPath = System.getProperty(DIR_PROPERTY_NAME, currentDir );
    		namesFile = actualPath+"/customisedDB/names.dmp";  
    		nodesFile = actualPath+"/customisedDB/nodes.dmp";    		
    	}		
		createNamesMap(namesFile,dbOption);
		createRankMap (nodesFile,dbOption);
}

//prints a treemap to the console
public void printKmersTreeMap (TreeMap<Integer, Integer> map) {
		 Set<Entry<Integer, Integer>> set = map.entrySet(); //get a set of the entries
		 Iterator<Entry<Integer, Integer>> i = set.iterator(); //get an iterator
		 
		 int num =0;
		 //Display elements
		 while (i.hasNext()) {
			 Map.Entry me =(Map.Entry)i.next();
			 num++;
			 
			 System.out.print("["+num+"] TaxID<"+me.getKey() + ">: ");
			 System.out.println(me.getValue());
		 }		 
 }
	
//populates the treemaps from the results of the classification i.e. from the match file
public int [] setKmersTrees(String matchFileName) 	{
		this.totalCountsDistinctKmersMap = new TreeMap<Integer,Integer>(); 
	    this.distinctKmerMap = new TreeMap<Integer,Integer>(); 
	    this.sharedKmerMap = new TreeMap<Integer,Integer>();
	    this.allMatchedKmers= new TreeMap<String,Integer>();
		
	    int numOfSpecificKmers = 0;
	    int numOfSharedKmers =0;
	    int [] numOfKmers = new int [2];
	    
	   	//reads the match file     
	    try (BufferedReader bf = new BufferedReader(new FileReader(matchFileName))) {
	    	String line;
	 		 while ((line = bf.readLine()) != null) {
	 			 //one line: kmer count taxID(s) lengthOfTaxID(s)
	 			 String [] words = line.split("[ \t]");
	 			 int wordsLen = words.length;
	 			 
	 			 String kmer = words[0];
	 			 int count = Integer.parseInt(words[1]);
	 			 
	 			 //add the k-mer and its count to the allMatchedKemrs
	 			 allMatchedKmers.put(kmer, count);	 			 
	 			 int idListLen = Integer.parseInt(words[wordsLen-1]);
	 			 
	 			 if(idListLen ==1) {//it is a specific kmer ==> adds to the distinct k-mers
	 				String idList = words[2];
	 				int taxID = Integer.parseInt(idList);
	 				
	 				if(distinctKmerMap.get(taxID) != null) {//key exists in the map
	 					int value = distinctKmerMap.get(taxID).intValue();
	 					distinctKmerMap.put(taxID,value+1);
	 					//update the total count map
	 					int countSum = totalCountsDistinctKmersMap.get(taxID).intValue();
	 					totalCountsDistinctKmersMap.put(taxID, countSum+count);
	 				}
	 				else {//create a new entry
	 					distinctKmerMap.put(taxID,1);
	 					totalCountsDistinctKmersMap.put(taxID, count);
	 				 }
	 				numOfSpecificKmers++;
	 			 }	 	    	
	 			 else {//it is a shared k-mer between multiple taxIDS
	 				for (int i=2; i< wordsLen-1;i++){
	 					int taxID = Integer.parseInt(words[i]);
	 					if(sharedKmerMap.get(taxID) != null) {//key exists in the map
		 					int value = sharedKmerMap.get(taxID).intValue();
		 					sharedKmerMap.put(taxID,value+1);		 					
		 				 }
		 				 else {//create a new entry
		 					sharedKmerMap.put(taxID,1);		 					
		 				 }
	 				}
	 				numOfSharedKmers++;
	 			 }	 		     
	 		   } 	 
	 		 	 		 
	 		 bf.close();  
	 	}catch (IOException ex) {
	   	     System.out.println("Errors reading the file "+matchFileName);
	    }	    
	    numOfKmers[0]=numOfSpecificKmers;
	    numOfKmers[1]= numOfSharedKmers;
	    return numOfKmers; 
	    	   
}

//gets a list of k-mers and their counts from the allMatchedKmers Treemap
public ArrayList<Kmers> getAllMatchedKmersList () {
	ArrayList<Kmers> kmersList = new ArrayList<Kmers>();
	/*iterate over the treemap*/
	Set<Entry<String,Integer>> set = allMatchedKmers.entrySet(); //get a set of the entries
	Iterator<Entry<String,Integer>> i = set.iterator(); //get an iterator
	 
	 //Display elements
	 while (i.hasNext())
	 {
		 Map.Entry me =(Map.Entry)i.next();		 
		 String kmerString = (String)me.getKey();
		 int count = (Integer) me.getValue();
		 
		 Kmers akmer = new Kmers(kmerString,count);
		 
		 if(!kmersList.contains(akmer)) kmersList.add(akmer);
		
	 }
	return kmersList;
}

//sets the treemaps for distinct kmers and their totals from the k-mers database file
public void setDBIDsMap(String dbFileName, String dbOption){
	//<key:taxID,value: total counts of k-mers with that taxID>
	this.DBtotalCountsMap = new TreeMap<Integer,Integer>(); 
	//<key:taxID,value: total number of distinct k-mers with that taxID>
	this.DBdistinctKmerMap = new TreeMap<Integer,Integer>(); 
	
	BufferedReader bf;
	try{
		
	   if(dbOption.equals("customisedDB")){
		 bf = new BufferedReader( new FileReader(dbFileName));  
	   }
	   else{
		   URL dbFileURL = getClass().getResource(dbFileName);
		   bf = new BufferedReader(new InputStreamReader(dbFileURL.openStream()));
	   }
	   String line; 		    
	   while ((line = bf.readLine()) != null) {
		    //one line: kmer count taxaID(s) lengthOfTaxaID(s)
			String [] words = line.split("[ \t]"); 
			int count = Integer.parseInt(words[1])	;
		 	int idListLen = Integer.parseInt(words[3]);
		 	
		 	if(idListLen == 1) {//looking for only specific(=distinct) k-mers in the db
		 		int taxID = Integer.parseInt(words[2]) ;
		 		//key is already in the tree but it is a new kmer cause each line  in the file is a distinct k-mer
		 	    if(DBdistinctKmerMap.get(taxID) != null) {
		 	    	int value =DBdistinctKmerMap.get(taxID).intValue();
			        DBdistinctKmerMap.put(taxID, value+1);
		            //update the count in the DB total ID counts. 
		            int countSum = DBtotalCountsMap.get(taxID).intValue();
		  			DBtotalCountsMap.put(taxID,countSum+count);
			     }
                 else { //a new key
                	 DBdistinctKmerMap.put(taxID,1); //create a new key and initialise it to 1 in DB distinct k-mers
			         DBtotalCountsMap.put(taxID,count);//create a new key and initialise it to count of the k-mer
			     }
			  }
		  } 
		  bf.close(); 
	}
	catch (IOException ex)	{
		 System.out.println("Errors reading the file "+dbFileName);
	}
}

//sets the virus names treemap from the appropriate dmp file
public void createNamesMap (String namesDmpFile, String dbOption) {
	//<key:taxID, value: its scientific name in the file>
	virusNameMap = new TreeMap<Integer,String>();
		
	BufferedReader br = null;
	try	{
		if(dbOption.equalsIgnoreCase("BuiltInDB")){
			URL namesFileURL = getClass().getResource(namesDmpFile);
			br = new BufferedReader(new InputStreamReader(namesFileURL.openStream()));
		}
		if(dbOption.equalsIgnoreCase("customisedDB")){
			br = new BufferedReader (new FileReader(namesDmpFile));
		}
		String nameLine;
	  			
	  	while((nameLine= br.readLine())!= null)	{
	  		String [] names = nameLine.split("\t\\|\t");	  			  				
	  		//add the nodes to the parent map//
	  		if (names[3].contains("scientific"))  {
	  			virusNameMap.put(Integer.parseInt(names[0]), names[1]);
	  		 }
		 }	
	  	br.close();
	 }
	 catch (IOException ex)	{
			System.out.println("Errors reading file "+namesDmpFile);
	}
 }

//sets the virus rank treemap from the appropriate dmp file
public void createRankMap (String nodedDmpFile, String dbOption) {
	//<key:taxID, value:its rank on the taxonomy tree>
	virusRankMap = new TreeMap<Integer,String>();
		
	BufferedReader br = null;
	try {
		if(dbOption.equalsIgnoreCase("BuiltInDB")){
			URL namesFileURL = getClass().getResource(nodedDmpFile);
			br = new BufferedReader(new InputStreamReader(namesFileURL.openStream()));
		}
		if(dbOption.equalsIgnoreCase("customisedDB")){
			br = new BufferedReader (new FileReader(nodedDmpFile));
		}
		String nodeLine;	  			
	  	while((nodeLine= br.readLine())!= null)	{
	  		String [] words = nodeLine.split("\t\\|\t");
	  		virusRankMap.put(Integer.parseInt(words[0]), words[2]);
	  	}		
	  	br.close();
	}
	catch (IOException ex)	{
		System.out.println("Errors reading file "+nodedDmpFile);
	}
}

//returns a list of virus information from the distinct kmers treemap
//Each entry in the the distinct kmers treemap is the taxID of a virus which has at least one distinct k-mer in the matching file
public ArrayList<VirusResult> getVirusMapResults () {
	ArrayList<VirusResult> virusResults = new ArrayList<VirusResult>();
	
	Set<Entry<Integer, Integer>> set = distinctKmerMap.entrySet(); //get a set of the entries
	Iterator<Entry<Integer, Integer>> i = set.iterator(); //get an iterator
	 
	 //Display elements
	 while (i.hasNext()) {
		 Map.Entry me =(Map.Entry)i.next();
		 String name = virusNameMap.get(me.getKey());
		 int taxaID = (Integer)me.getKey();
		 int distNum = (Integer) me.getValue();
		 int totalNum = totalCountsDistinctKmersMap.get(me.getKey());		
		 String rank = virusRankMap.get(me.getKey());
		 
		 int dbTotNum= DBdistinctKmerMap.get(me.getKey()); 
		 int dbDisNum=DBtotalCountsMap.get(me.getKey());
		 
		 VirusResult vr = new VirusResult(name, ""+taxaID, distNum, totalNum,rank,dbDisNum,dbTotNum);
		 virusResults.add(vr);
	 }
	
	return virusResults;
}

//summaries the information in a treemap for the viruses, with distinct and shared k-mers, in the matching
public String getSummaryMaps () {
	String summary ="";
	TreeMap<Integer, Integer>sumMap = getSumKmers();
	    
	int virusNameLen = 0;
	String result ="";
		 
	int numHighestScore = 0;
	if(distinctKmerMap.size()<3) {
		numHighestScore = distinctKmerMap.size();
		highestScoresNames= new String[distinctKmerMap.size()];
		highestScoresSpecific= new int[distinctKmerMap.size()];
		highestScoresShared = new int[distinctKmerMap.size()];
	}
	else {
		numHighestScore = 3;
		highestScoresNames= new String[numHighestScore];
		highestScoresSpecific= new int[numHighestScore];
		highestScoresShared = new int[numHighestScore];
	}
	
	int numOfScores =0;
	//sort the values in the distinct k-mers treemap so the taxID with highest num of k-mers is first
	Map sortedMap = sortByValues(distinctKmerMap);
	    
	Set set = sortedMap.entrySet();
	Iterator i = set.iterator();
	    
	while(i.hasNext()) {
		Map.Entry me = (Map.Entry)i.next();
	    numOfScores++;
	         
	     int taxID = (Integer)me.getKey();
	     int total = sumMap.get(taxID);
	     int specific = distinctKmerMap.get(taxID);
	         	         
	     int shared =0;
	     if(sharedKmerMap.get(taxID) != null){
	    	 shared = sharedKmerMap.get(taxID);
	     }
	     String virusName = (String) virusNameMap.get(taxID);
	     if(virusName.length() > virusNameLen)
	       	virusNameLen = virusName.length();
	         
	     result = result+virusName+":"+specific+":"+shared+":"+total+"\n";
	         
	     if(numOfScores<=numHighestScore){
	       	this.highestScoresNames[numOfScores-1] = virusName;
	        this.highestScoresSpecific[numOfScores-1] = specific;
	       	this.highestScoresShared[numOfScores-1] = shared;
	      }	         
	   }
	   summary = summary+virusNameLen+"\n"+result;
		return summary;
}

//formats the summary text to be printed on the summary panel of the GUI
public String formatSummaryText(String text){
		 String formattedText = "";		 
		 String [] line = text.split("\n");
		 int virusNameLen = Integer.parseInt(line[0]);
		 
		 int tabbedSpace = 5;
		 int spaces=virusNameLen+tabbedSpace;
		 
		 formattedText +=getSpace(spaces," ");
		 formattedText +=getSpace(tabbedSpace," ");
		 
		 formattedText +="No. Classified K-mers\n";
		 
		 formattedText +=getSpace(spaces+"No. Classified K-mers".length()+2*tabbedSpace+"Total".length(),"=");
		 formattedText +="\n";
		 
		 formattedText +=formatHeadings (virusNameLen);
		 
		 formattedText +=getSpace(spaces+"No. Classified K-mers".length()+2*tabbedSpace+"Total".length(),"=");
		 formattedText +="\n";
		 
		 int [] headingsLength = {virusNameLen,"Specific".length(),"Non-specific".length(),"Total".length()};
		 for (int i =1; i<line.length;i++)	{
			 formattedText += formatRows (line[i], headingsLength);		 
		 }	
		 
		 return formattedText;
}

//prints all the info for the viruses in the matching results to a text
public String printVirusMapResults (){
	String results = "";
		
	Set<Entry<Integer, Integer>> set = distinctKmerMap.entrySet(); //get a set of the entries
	Iterator<Entry<Integer, Integer>> i = set.iterator(); //get an iterator
		 
	while (i.hasNext()) {
		Map.Entry me =(Map.Entry)i.next();
		String name = virusNameMap.get(me.getKey());
		int taxID = (Integer) me.getKey();
		int distNum = (Integer) me.getValue();
		int totalNum = totalCountsDistinctKmersMap.get(me.getKey());		
		String rank = virusRankMap.get(me.getKey());
			 
		int dbTotNum= DBdistinctKmerMap.get(me.getKey()); 
		int dbDisNum=DBtotalCountsMap.get(me.getKey());
			 
		String result = name+": "+taxID+": "+dbTotNum+": "+dbDisNum+": "+distNum+": "+totalNum+": "+rank+"\n";
		results = results + result;
	 }
		
	return results;
}

public String[]getHighestScoresName(){
    return this.highestScoresNames;		 
}

public int[]getHighestScoresSpecific(){
    return this.highestScoresSpecific;
}

public int[]getHighestScoresShared(){
    return this.highestScoresShared;
}

public TreeMap<Integer,Integer>getSharedKmersMap(){
	return this.sharedKmerMap;
}

public TreeMap<Integer,Integer>getDistinctKmersMap(){
	return this.distinctKmerMap;
}

public TreeMap<Integer,Integer>getTotalCountsMap(){
	return this.totalCountsDistinctKmersMap;
}

public TreeMap<String,Integer> getAllMatchedKmers(){
	return this.allMatchedKmers;
}	
	
public TreeMap<Integer, Integer> getDBDisIDsMap(){
    return this.DBdistinctKmerMap;
}
    
 public TreeMap<Integer, Integer> getDBtotKmerMap(){
   	 return this.DBtotalCountsMap;
 }

 /***********************helpers methods *****************/
 
//returns a treemap for a virus which has distinct and shared k-mers in the matching 
private TreeMap<Integer, Integer>getSumKmers(){
	//<key:taxID, value:sum of specific+shared k-mers>
	TreeMap<Integer,Integer> sumDisKmers = new TreeMap<Integer,Integer>();
	
	Set set = this.distinctKmerMap.entrySet();
	Iterator i = set.iterator();
	
	while(i.hasNext()) {
		Map.Entry me = (Map.Entry)i.next();
		int taxID = (Integer)me.getKey();
		int specific = (Integer)me.getValue();
		int shared =0;
		if(sharedKmerMap.get(taxID) != null ){
			shared = sharedKmerMap.get(taxID).intValue();		    
		 }
		 sumDisKmers.put(taxID,specific+shared);
		}	
		return sumDisKmers;
}

//formats the heading of the summary text that sums up specific+shard k-mers for the matching results
private String formatHeadings (int nameLen){
	String [] headings = {"Virus Name","Specific","Non-specific","Total"};
		
	String text =headings[0];
	int space = 5;
	    
	text +=getSpace(nameLen-headings[0].length()+space," ");
	for(int head=1;head<headings.length;head++){
		text +=headings[head];			 
		text +=getSpace(space," ");
	}
	text +="\n";
	return text;
		 
}

//formats a single row in the summary text
private String formatRows (String line, int [] headingsLength){
	String text="";
	int space = 5;
	String [] virusInfo = line.split(":");
			
	for(int i=0;i<virusInfo.length;i++ ){
		text +=virusInfo[i];
		text +=getSpace(headingsLength[i]-virusInfo[i].length()+space," ");			
	 }
	 text +="\n";
	 return text;
}

//creates an empty line with certain number of spaces
private String getSpace(int spaces, String text){
	String spaceText="";
	for(int j=1;j<=spaces;j++) {
			 spaceText +=text;
	}
	return spaceText;
}

//sorts the values in a treemap 
private <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
	Comparator<K> valueComparator =  new Comparator<K>() {
		public int compare(K k1, K k2) {
			int compare = map.get(k2).compareTo(map.get(k1));// to sort descendingly
	        if (compare == 0) 
	           return 1;
	        else 
	          return compare;
	     }
	   };

	   Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
	   sortedByValues.putAll(map);
	   return sortedByValues;
    }

}