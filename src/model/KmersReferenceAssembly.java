package model;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
//import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class KmersReferenceAssembly {
	
	public KmersReferenceAssembly() {		
	}
	
	/*A method to align the kmers in the list with the reference genome, with specific mismatch tolerance. 
	 *For each k-mer in the k-merList, the method iteratively gets a list of the mapping positions. 
	 *If the list of mapping positions > zero, an entry of a VirusK-mer is created and then it is added to the list of VirusKmers
	 *which contains all mapped k-mers with the specific mismatchTolerance. 
	 *If the k-mer is aligned to the genome, the index of the first base where the match happens is recorded as a position.
	 *A k-mer can happen more than once in the reference genome at different positions. Therefore, a list of positions is kept for each aligned k-mer.
	 */	
	public ArrayList<VirusKmers> getVirusKmersListMatchedWithRefGenome (String refGenome, ArrayList<Kmers> kmersList,int mismatchTolerance,String taxaID){
		
		ArrayList<VirusKmers> results = new ArrayList<VirusKmers>();
		
		//gets the positions for each kmer in the kmers List
    	for(Kmers k:kmersList){
    		String kmer = k.getKmer();
    		int count = k.getCount();
    			
    		ArrayList<Integer>positions = getkmersPositionsInRefGenome (refGenome, kmer, mismatchTolerance);
    		
    		//only return k-mers which are mapped to the reference genome with the mismatchTolerance
    		//i.e. k-mers with 0 positins are not considered.
    		Integer[] refPositions = positions.toArray(new Integer[positions.size()]);
    		if(refPositions.length >0){
    			VirusKmers vKmer = new VirusKmers (kmer,count,positions);
        		results.add(vKmer);
        	}
    	}
    	return results;
	}
	
	/* A method to align a k-mer to the reference genome, allowing the specified mismatchTolerance.
	 * The Match in the reference genome with its location is pritned out to a file.
	 * The reference genome is represented as a TreeMap of all K-mers in the reference genome and their positions in the reference genome
	 * If the k-mer is aligned to the genome, the list of positions is retrieved.
	 * A k-mer can happen more than once in the reference genome at different positions. Therefore, a list of positions is kept for each aligned k-mer.
	 */
  public ArrayList<Integer> getkmersPositionsInRefGenome1 (TreeMap<String,ArrayList<Integer>> refGenomeMap, String kmer, int mismatchTolerance) {
		//if the kmer does not exist in the refGenome, positions is null
		ArrayList<Integer> positions = new ArrayList<Integer>();  
		
		//iterate through the TreeMap
		Set<Entry<String, ArrayList<Integer>>> set = refGenomeMap.entrySet(); //get a set of the entries
		Iterator<Entry<String, ArrayList<Integer>>> i = set.iterator(); //get an iterator
		 
		 //Display elements
		 while (i.hasNext()) {
			 Map.Entry me =(Map.Entry)i.next();
			 String refKmer = (String)me.getKey();
			 int missed = 0;
			 for (int patternIndex = 0; patternIndex < kmer.length(); patternIndex++) {
				 final char textChar = refKmer.charAt(patternIndex);
			     final char patternChar = kmer.charAt(patternIndex);
			     if (textChar != patternChar) {
			        missed++;
			      }
			     if (missed > mismatchTolerance) {
			       break;
			     }
			 }
            if (missed <= mismatchTolerance) {
           	ArrayList<Integer> pos = (ArrayList<Integer>) me.getValue();
			    for(int p:pos){
			    	positions.add(p);
			    }			    
			  }
		}
		return positions;
}

public ArrayList<VirusKmers> getVirusKmersListMatchedWithRefGenome (TreeMap<String,ArrayList<Integer>> refGenomeMap, ArrayList<Kmers> kmersList,int mismatchTolerance,String taxaID){
		//This file is created for testing purposes but it can be deleted 
		//String OutputFileName ="MatchedKmers_"+mismatchTolerance+"_"+taxaID;
		
		ArrayList<VirusKmers> results = new ArrayList<VirusKmers>();
		//gets the positions for each kmer in the kmers List
    		for(Kmers k:kmersList){
    			String kmer = k.getKmer();
    			int count = k.getCount();
    			
    			//ArrayList<Integer>positions =getkmersPositionsInRefGenome1 (refGenomeMap,  kmer, mismatchTolerance, writer);
    			ArrayList<Integer>positions =getkmersPositionsInRefGenome1 (refGenomeMap,  kmer, mismatchTolerance);
    			
    			//only return k-mers which are mapped to the reference genome with the mismatchTolerance
    			//i.e. k-mers with 0 positins are not considered.
    			Integer[] refPositions = positions.toArray(new Integer[positions.size()]);
    			if(refPositions.length >0){
    				VirusKmers vKmer = new VirusKmers (kmer,count,positions);
        			results.add(vKmer);        			 			
    			}    			
    		}    		
    		
		return results;
}
	/*
	 * A method to align a k-mer to the reference genome, allowing the specified mismatchTolerance.
	 * The Match in the reference genome with its location is pritned out to a file.
	 * If the k-mer is aligned to the genome, the index of the first base where the match happens is recorded as a position.
	 * A k-mer can happen more than once in the reference genome at different positions. Therefore, a list of positions is kept for each aligned k-mer.
	 */
	public ArrayList<Integer> getkmersPositionsInRefGenome (String refGenome, String kmer, int mismatchTolerance) {
		
		//if the kmer does not exist in the refGenome, positions is null
		ArrayList<Integer> positions = new ArrayList<Integer>(); 		
		
		final int textIndexMax = refGenome.length() - kmer.length() + 1;
		
		for (int textIndex = 0; textIndex < textIndexMax; textIndex++) {
		    int missed = 0;

		    for (int patternIndex = 0; patternIndex < kmer.length(); patternIndex++) {
		        final char textChar = refGenome.charAt(textIndex + patternIndex);
		        final char patternChar = kmer.charAt(patternIndex);
		        if (textChar != patternChar) {
		            missed++;
		            //System.out.println("I am here");
		        }
		        if (missed > mismatchTolerance) {
		        	break;
		        }
		    }

		    if (missed <= mismatchTolerance) {
		        final String match = refGenome.substring(textIndex, textIndex + kmer.length());
		        //System.out.println("Index: " + textIndex + " Match: " + match);		       
		        positions.add(textIndex);
		   }
		}
		
		//System.out.println(positions);		
		return positions;
	}
	
	
	public TreeMap<String,ArrayList<Integer>> getRefGenKmers (String refGen, int kSize){
		//key: K-mer, Values: List of its Positions in the refGen
		TreeMap<String, ArrayList<Integer>> refKmers = new TreeMap<String, ArrayList<Integer>>(); 
			
		//to get through the whole string 
		for(int i=0; i<=refGen.length()-kSize; i++)	{
			//get the substring from the refGenome whose length equals kSize and starts at position i
			String kmer = refGen.substring(i,i+kSize);
			//System.out.println("Kmer: "+kmer+"\t Pos: "+i);
				
			/*add the k-mer to the TreeMap
			 * If it exists in the Tree, add its position to the list
			 * If it does not exist in the Tree, add a new entry into the Tree with the new position
			*/
			if(refKmers.get(kmer) != null) {
				ArrayList<Integer> positions = refKmers.get(kmer);
			    // add if item is not already in list
			    if(!positions.contains(i)) positions.add(i);
			    	
			    refKmers.put(kmer,positions);
			 }
			 else {// key is not in the map
				 ArrayList<Integer> positions = new ArrayList<Integer>(10);
				 positions.add(i);
				 refKmers.put(kmer,positions);
			 }
		}
		return refKmers;		
	}
	
	public void printTreeMap (TreeMap<String, ArrayList<Integer>> map)
	{
		 Set<Entry<String, ArrayList<Integer>>> set = map.entrySet(); //get a set of the entries
		 Iterator<Entry<String, ArrayList<Integer>>> i = set.iterator(); //get an iterator
		 
		 //Display elements
		 while (i.hasNext())
		 {
			 Map.Entry me =(Map.Entry)i.next();
			 System.out.print("K-mer<"+me.getKey() + ">: ");
			 System.out.println(me.getValue());
		 }
		 System.out.println();
	}

	public String getRefGenome(String fileName) throws IOException{
		String text = "";
		
		try {
			BufferedReader br= new BufferedReader(new FileReader(fileName));
			String header =  br.readLine();
			//System.out.println("header: "+header);
			
			String line="";
			while((line=br.readLine())!=null){
				//System.out.println("Squence: "+text);
				text = text+line;
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return text;
	}
	
/*
	public synchronized void addToList(String mapKey, int myItem) {
	    

	    //key is already in the tree, means the list of positions at least has one item
	    if(RefPositions.get(mapKey) != null) {
	    	ArrayList<Integer> itemsList = RefPositions.get(mapKey);
	    	// add if item is not already in list
	        //if(!itemsList.contains(myItem)) itemsList.add(myItem);
	    	itemsList.add(myItem);
	        RefPositions.put(mapKey, itemsList);
	    }
	   else {// key is not in the map
		   ArrayList<Integer> itemsList = new ArrayList<Integer>();
		   itemsList.add(myItem);
		   RefPositions.put(mapKey, itemsList);
	    }
	}*/
	
	public int getIndex(String itemName, ArrayList<Kmers> kmersList)
	{
	    for (int i = 0; i < kmersList.size(); i++)
	    {
	        Kmers k = kmersList.get(i);
	        if (itemName.equals(k.getKmer()))
	        {
	            return i;	          
	        }
	    } 
	    return -1;
	}
	
	
	/*
	 * A method to print the mapped k-mers with the reference genome to a file in the format:
	 * mismatch_index k-mer count positions
	 */
	public void printMappedKmers(TreeMap<Integer,ArrayList<VirusKmers>> map, String fileName){
		
		PrintWriter writer = null;
		try{
			 OutputStream out = new FileOutputStream(fileName);
	            writer = new PrintWriter(out);
			
			Set<Entry<Integer, ArrayList<VirusKmers>>> set = map.entrySet(); //get a set of the entries
			 Iterator<Entry<Integer, ArrayList<VirusKmers>>> i = set.iterator(); //get an iterator
			 
			 //Display elements
			 while (i.hasNext())
			 {
				 Map.Entry me =(Map.Entry)i.next();				 			
				 
				 int mismatchTolerance = (Integer)me.getKey();
				 ArrayList<VirusKmers> vKmers = (ArrayList<VirusKmers>)me.getValue();
				 
				 for(VirusKmers v:vKmers ){
					 String kmer = v.getKmer();
					 int count =v.getCount();
					 ArrayList<Integer> positions= v.getPositions();
					 
					 //System.out.println(mismatchTolerance+"\t"+kmer+"\t"+count+"\t"+positions);
					 
					 writer.print(mismatchTolerance+"\t"+kmer+"\t"+count+"\t");
					 for(int pos:positions)
					   writer.print(pos+"\t");
					 writer.println();
				 }
			 }
			 //System.out.println();
			 writer.flush();
 		     writer.close();
             }
        catch(Exception e)
		{
         e.printStackTrace();
        }
	}
	
	/*
	 *A method to return the number of mapped k-mers with the reference genome per mismatch index
	 */
	public int [] getKmersCoverage (TreeMap<Integer,ArrayList<VirusKmers>> mappedKmers, int maxMismatchTolerance){
		int [] kmersCoverage = new int [maxMismatchTolerance+1];
		//get the size of the virusKmers list for each entry in the treemap
		Set<Entry<Integer, ArrayList<VirusKmers>>> set = mappedKmers.entrySet(); //get a set of the entries
		Iterator<Entry<Integer, ArrayList<VirusKmers>>> i = set.iterator(); //get an iterator
		 
		 //Display elements
		 while (i.hasNext())
		 {
			 Map.Entry me =(Map.Entry)i.next();
			 int mismatchTolerance = (Integer)me.getKey();
			 //System.out.println("Key: "+mismatchTolerance);
			 ArrayList<VirusKmers> vKmers = (ArrayList<VirusKmers>)me.getValue();
			 
			 
			 kmersCoverage [mismatchTolerance]= vKmers.size();	
		 }
		 /*System.out.println("The number of mapped k-mers for each mismatch:");
		 for (int index=0;index < kmersCoverage.length;index++)
			 System.out.println("["+index+"]: "+kmersCoverage[index]);
		 System.out.println();*/
		 
		 return kmersCoverage;
	}
	
	/* 
	 * A method to return the overall mapped areas in the reference genome per mismatchIndex
	 */
	public int [] getOverallMappedArea (long [][] mappedKmersPos) {
		int [] overallMappedArea = new int [mappedKmersPos.length]; 
		
		int numRows = mappedKmersPos.length;
		int numCols = mappedKmersPos[0].length;
		
		for(int row = 0; row< numRows; row++){
			int mapped = 0;
			for (int col=0; col <numCols; col++){
				if (mappedKmersPos[row][col] > 0)
					mapped++;
			}
			overallMappedArea[row]=mapped;
		}
		
		return overallMappedArea;
	}
	
	/*
	 * A method to return the number of k-mers in the list KmersList and their total counts.
	 */
	public int [] getNumClassifiedKmers (ArrayList<Kmers> kmersList)
	{
		int [] numClassifiedKmers = new int [] {0,0};
		
		int counts = 0;
		for (Kmers kmer:kmersList){
			counts = counts+kmer.getCount();
		}
		numClassifiedKmers[0] = kmersList.size();
		numClassifiedKmers[1] = counts;
		return numClassifiedKmers ;
	}
	
	/*A method to get the k-mers that are mapped to the the reference genome, with specific mismatch tolerance. i.e. cut-off is 4 bases (~10% of length of k-mer)
	 *Starting with the 0-mismatch tolerance, the method iteratively passes, a list of k-mers, the classified k-mers to another method
	 *to get the list of mapped k-mers and their positions in the reference genome.
	 *The method returns a list of virusK-mers that are found in the reference genome. 
	 *A virusKmer is composed of a A kmer, its count, its list of positions in the reference genome.
	 *After each iteration, the mapped k-mers are removed from the list of classified k-mers so that it is not mapped again in the next iteration 
	 *with a different mismatch tolerance.
	 *The Treemap is indexed by the mismatch Tolerance.
	 *
	 */	
	
	public TreeMap<Integer,ArrayList<VirusKmers>> getMappedKmers(ArrayList<Kmers> kmersList, int maxMisMatchTolerance, String refGenome,String taxaID){
		
						
		TreeMap<Integer,ArrayList<VirusKmers>> mappedKmers = new TreeMap<Integer,ArrayList<VirusKmers>>();
		for (int i = 0;i <= maxMisMatchTolerance; i++)
		{
			int mismatchTolerance = i;
			//adds an entry to mapped Kmers with the key= i and an empty ArrayList of viruses
			mappedKmers.put(mismatchTolerance, new ArrayList<VirusKmers>());
			
			//how many kmers in the list we want to map to the reference
			System.out.println("mismatch["+mismatchTolerance+"]: (START) The size of Kmers List: "+kmersList.size());
			
			//get the virusList with all 
			ArrayList<VirusKmers> vResult = getVirusKmersListMatchedWithRefGenome (refGenome, kmersList, mismatchTolerance,taxaID);
			
			//remove the k-mers which are mapped for that mismatch index from the original list          
	    	for(VirusKmers k:vResult){
	    		String kmer = k.getKmer();
	    		
	    		//the kmers were mapped to the ref genome with this particular mismatchTolerance so we don't match it again
	    		//return the index for that kmer in the kmersList
	    		int index=getIndex(kmer,kmersList);
	    		//System.out.println("Kmer: "+kmer+" is in position: "+index+" in the kmers List");
	    		kmersList.remove(index);	    					
	    	}
	    		//add the list of virusKmers to the mapped kmers under the key=mismatchTolerance 
    			mappedKmers.put(i, vResult);
    			System.out.println("mismatch["+mismatchTolerance+"]: (END) The size of Kmers List: "+kmersList.size());
	    	
		}
		
		System.out.println("The new List size: "+kmersList.size()); // Prints 1
		System.out.println("The original List size: "+kmersList.size()); // Prints 2

		
		return mappedKmers;
	}
	
	
	public TreeMap<Integer,ArrayList<VirusKmers>> getMappedKmers(ArrayList<Kmers> kmersList, int maxMisMatchTolerance, TreeMap<String,ArrayList<Integer>> refGenomeMap,String taxaID){
		TreeMap<Integer,ArrayList<VirusKmers>> mappedKmers = new TreeMap<Integer,ArrayList<VirusKmers>>();
		for (int i = 0;i <= maxMisMatchTolerance; i++)	{
			int mismatchTolerance = i;
			//adds an entry to mapped Kmers with the key= i and an empty ArrayList of viruses
			mappedKmers.put(mismatchTolerance, new ArrayList<VirusKmers>());
				
			//how many kmers in the list we want to map to the reference
			System.out.println("The size of the matched Kmers List: "+kmersList.size()+" for the mismatch index: "+mismatchTolerance);
				
			ArrayList<VirusKmers> vResult = getVirusKmersListMatchedWithRefGenome (refGenomeMap, kmersList, mismatchTolerance,taxaID);
				
			System.out.println("There are "+vResult.size()+" kmers exists in the reference genome that are mapped to the matched k-mers list with "
						+ " mismatch tolerance: "+mismatchTolerance);
			//This file is for testing purposes and can be deleted later
			//String OutputFileName ="Positions_"+mismatchTolerance+"_"+taxaID;
					
			/*PrintWriter writer=null;
		    try {
		    	OutputStream out = new FileOutputStream(OutputFileName);
		        writer = new PrintWriter(out);*/
		        //gets the positions for each kmer in the kmers List
		    	for(VirusKmers k:vResult){
		    		String kmer = k.getKmer();
		    		int count = k.getCount();
		    		
		    		ArrayList<Integer>positions = k.getPositions();
		    		Integer[] pos =positions.toArray(new Integer[positions.size()]);
		    			
		    		/*writer.print(count+"\t");
		    		for(int p:pos )	    					
		    	    	writer.println(p);
		    		*/	
		    		//the kmers were mapped to the refgenome with this particular mismatchTolerance so we don't match it again
		    		//return the index for that kmer in the kmersList
		    		int index=getIndex(kmer,kmersList);
		    		kmersList.remove(index);	    					
		    	}
		    	//add the list of virusKmers to the mapped kmers under the key=mismatchTolerance 
	    		mappedKmers.put(i, vResult);
		    	System.out.println("The size of Kmers List: "+kmersList.size()+" after mapping k-mers is over for mismatch tolerance: "+mismatchTolerance);
		    	/*writer.flush();
		    	writer.close();
		      }
		      catch(Exception e) {
		            e.printStackTrace();
		      }*/
		  }
		return mappedKmers;
		}
	
	public long [][] getMappedKmersPositions1 (TreeMap<Integer,ArrayList<VirusKmers>> mappedKmers, int maxMismatchTolerance, int refGenomeLen){
		//number of rows starts from 0 up to the max mismatch, 
		//number of col corresponds to the length of refGenome but positions starts at 0 in the array 
		long[][] mappedKmersPositions = new long [maxMismatchTolerance+1][refGenomeLen];
		//set up the 2D array so all positions are set to zeros
		for(int row = 0; row < maxMismatchTolerance+1; row++){
			for(int col=0; col < refGenomeLen; col++){
				mappedKmersPositions [row][col]=0;
			}
		}
		
	   		
		for(int row = 0; row < maxMismatchTolerance+1; row++){
			//get the list of VirusKmers for that index
			ArrayList<VirusKmers> vKmersList = mappedKmers.get(row);
			//Examine each entry in the list
			for (VirusKmers virus: vKmersList){
				String kmer =virus.getKmer();
				int kSize = kmer.length();
				int count = virus.getCount();				
				ArrayList<Integer> positions = virus.getPositions();
				
				//Examine the position list
	            int numOfPosBins = positions.size();
	            long [] countPerPos = getPosAndCount(count, numOfPosBins);
	            
	            //update the positions in the 2D array according to the counts in each bin
	            for(int i=0; i < numOfPosBins; i++ ){
	            	int startPos = positions.get(i);
	            	int endPos = (startPos+kSize)-1;
	            	
	            	for(int col =startPos; col <= endPos; col++){
	            		long in = mappedKmersPositions [row][col];
	            		mappedKmersPositions [row][col]= in + countPerPos[i];
	            	}
	            }
			}
		}
		return mappedKmersPositions;
	}
	
	public long [][] getMappedKmersPositions (String mappedKmersFile, int maxMismatchTolerance, int refGenomeLen){
		//number of rows starts from 0 up to the max mismatch, 
		//number of col corresponds to the length of refGenome but positions starts at 0 in the array 
		long[][] mappedKmersPositions = new long [maxMismatchTolerance+1][refGenomeLen];
		//set up the 2D array so that the first column is the index of mismatchTolerance
		//All positions are set to zeros
		for(int row = 0; row < maxMismatchTolerance+1; row++){
			for(int col=0; col < refGenomeLen; col++){
				mappedKmersPositions [row][col]=0;
			}
		}
		
	    /*//print out the 2D Array to console
		printMappedKmersPos (mappedKmersPositions);*/
		
		/*for(int row = 0; row <= maxMismatchTolerance; row++){
			System.out.print("["+row+"]: ");
			for(int col=0; col < refGenomeLen; col++){
				System.out.print(mappedKmersPositions [row][col]+",");
				
			}
			System.out.println();
		}*/
		
		//open the file to read the mapping positions
		try {
			BufferedReader br= new BufferedReader(new FileReader(mappedKmersFile));
			
			//read the file one line at a time
			String line="";
			int numOfLines = 0;
			while((line=br.readLine())!=null){
				numOfLines++;
				
				//System.out.println("line["+numOfLines+"]: "+line );
				
				//split the line
				String [] words = line.split("\t");
				
				int mismatchTolerance = Integer.parseInt(words[0]);
				String kmer =words[1];
				int kSize = kmer.length();
				int count = Integer.parseInt(words[2]);
				
				ArrayList<Integer> positions = new ArrayList<Integer>();
				for(int i= 3; i<words.length;i++){
					int pos = Integer.parseInt(words[i]);
					positions.add(pos);
				}
				
				/*System.out.println("---- Split by space ------");
				System.out.println("mismatch: "+mismatchTolerance);
				System.out.println("kmer: "+kmer);
				System.out.println("count: "+count);
				System.out.print("positions:\t");
				
				for(int pos:positions)
					System.out.print(pos+"\t");
                System.out.println();*/
                
				//Examine the position list
                int numOfPosBins = positions.size();
                long [] countPerPos = getPosAndCount(count, numOfPosBins);
                
               /* for(int i = 0; i < countPerPos.length; i++){
                	System.out.println("Bin["+i+"]: "+countPerPos[i]);
                	
                }*/
                
                //update the position in the 2D array according to the counts in each bin
                for(int i=0; i < numOfPosBins; i++ ){
                	int startPos = positions.get(i);
                	int endPos = (startPos+kSize)-1;
                	
                	for(int col =startPos; col <= endPos; col++){
                		long in = mappedKmersPositions [mismatchTolerance][col];
                		mappedKmersPositions [mismatchTolerance][col]= in + countPerPos[i];
                	}
                }
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mappedKmersPositions;
	}
	
	public void printMappedKmersPos (long [][] mappedKmersPositions){
		int numOfRows = (mappedKmersPositions.length);// number of mismatches
		int numOfCols = (mappedKmersPositions[0].length);//reference genome length
		
		/*System.out.println("There are "+numOfRows+" rows");
		System.out.println("There are "+numOfCols+" columns");*/
		for(int row = 0; row < numOfRows; row++){
			//System.out.print("["+row+"]: ");
			for(int col=0; col < numOfCols; col++){
				//System.out.print(mappedKmersPositions [row][col]+",");
				
			}
			//System.out.println();
		}
	}
	
	public void printMappedKmersToFile (String fileName,long [][] mappedKmersPositions){
		int numOfRows = (mappedKmersPositions.length);// number of mismatches
		int numOfCols = (mappedKmersPositions[0].length);//reference genome length
		
		PrintWriter printer = null;
		try {
			OutputStream out = new FileOutputStream(fileName);
			printer = new PrintWriter(out);
			
			/*System.out.println("There are "+numOfRows+" rows");
			System.out.println("There are "+numOfCols+" columns");*/
			for(int row = 0; row < numOfRows; row++){
				printer.print("["+row+"]: ");
				//System.out.print("["+row+"]: ");
				for(int col=0; col < numOfCols; col++){
					//System.out.print(mappedKmersPositions [row][col]+",");
					printer.print(mappedKmersPositions [row][col]+",");
					
				}
				//System.out.println();
				printer.println();
			}
			printer.flush();
			printer.close();
           
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
		
	}
	public long [][] getRefGenCoverageForMappedKmers (long [][] mappedKmersPos){
		int numRows = mappedKmersPos.length;
		int numCols = mappedKmersPos[0].length; 
			
		long [][] overallCoverage = new long [numRows][2];
			
		for(int row =0; row<numRows; row++){
			int mappedArea = 0;
			int sum =0;
			for(int col=0;col<numCols;col++){
				if(row ==0 ){
					if(mappedKmersPos[row][col] >0) {
						mappedArea++;
						sum += mappedKmersPos[row][col];
					}
				}
				//other rows					
				else{
					boolean found = false;
					for(int counter = row-1; counter >=0; counter--){
						if(mappedKmersPos[counter][col] >0) {
							found = true; 
							break;
						}
					}
					if(!found && mappedKmersPos[row][col] >0){
						mappedArea++;
						sum += mappedKmersPos[row][col];
					}
				}	
			}
			overallCoverage[row][0]= mappedArea;
			overallCoverage[row][1]= sum;
		}
		return overallCoverage;
	}
		
	public long [] getDepthStats (long[] mappedKmersPos, int numKmers){
		//set the min value to the largest possible number it can be.
		long min = numKmers; //min depth
		long max = 0;     //max depth 
		long sum = 0;     //total depth
		long area =0;     //mapped area
		long average = 0; //average depth
		for (int count=0; count < mappedKmersPos.length; count++){
				//we don't want to consider positions with 0 coverage, cause that contains no depth info
				if (mappedKmersPos[count] >0){
					sum += mappedKmersPos[count];
					area++;
				}
				
				if (mappedKmersPos[count] < min) {
						min= (int)mappedKmersPos[count];	
				}
					
				if (mappedKmersPos[count] > max) {
					max= (int)mappedKmersPos[count];
				}
		}
		
		if(sum == 0) //no coverage at all
				min = 0; //reset min to 0 so that all stats indicate no coverage
			
			
		//System.out.println("Total depth= "+sum+" and the area of mapped genomes= "+area);
			
		average =(long) Math.round((double) sum/ (double)area);
			
		long [] stats = {min,max,average,area};
		return stats;
	}
	
	public long [][] getDepthStatsForMappedKmers (long [][] mappedKmersPos, int numMappedKmers){
		int numRows = mappedKmersPos.length;
		int numCols = 4; //{min,max,average,area} 
		
		long [][] overallDepthStats = new long [numRows][numCols];
		
		for(int index =0; index<numRows; index++){
			long [] kmersPos = mappedKmersPos [index];
			overallDepthStats [index] = getDepthStats (kmersPos,numMappedKmers);
		}
		
		return overallDepthStats;
	}
	
	public String getStatText (int [] classifiedKmers,int [] afterMappingClassifiedKmers,long [][] mappedKmersPos){
		String statsText = "";
		double [] mappedKmersPer = new double [2];
		mappedKmersPer [0] = ((double)(classifiedKmers[0]-afterMappingClassifiedKmers[0])/(double)classifiedKmers[0])*100;
		mappedKmersPer [1] = ((double)(classifiedKmers[1]-afterMappingClassifiedKmers[1])/(double)classifiedKmers[1])*100;
		    
		statsText = statsText+"Genome Length: "+mappedKmersPos[0].length+"\tMapped K-mers (out of Classified): "+String.format("%.2f", mappedKmersPer [1])+"%\n";
		    
		System.out.println("The number of classified k-mers (distinct k-mers): "+classifiedKmers[0]+" and their total counts is "+classifiedKmers[1]);
		System.out.println("The number of mapped k-mers (distinct k-mers)p: "+afterMappingClassifiedKmers[0]+" and their total counts is "+afterMappingClassifiedKmers[1]);
		    
		System.out.println("The percenctage of mapped k-mers (distinct k-mers): "+mappedKmersPer[0]+" %");
		System.out.println("The percenctage of mapped k-mers (total Counts): "+mappedKmersPer[1]+" %");
		
		long [][] overAllCoverage= getRefGenCoverageForMappedKmers (mappedKmersPos);
		statsText = statsText+"\t\tGenome Coverage Percentage: \tAverage Depth:\n";
		for( int row= 0; row < overAllCoverage.length; row++){
			System.out.println("overAllCoverage["+row+"]: "+overAllCoverage[row][0]);
		    System.out.println("Average Depth["+row+"]: "+overAllCoverage[row][1]);
		    double percMapped = ((double)overAllCoverage[row][0]/(double)mappedKmersPos[0].length)*100;
		    long averageDepth =0;
		    if(overAllCoverage[row][0]>0){
		    	averageDepth = overAllCoverage[row][1]/overAllCoverage[row][0];
		    }
		    statsText = statsText+row+"-mismatch:\t\t"+String.format("%.2f", percMapped)+"%\t\t\t"+averageDepth+"\n";
		 }
		return statsText;
	}
	
	/*public String getStatText (int [] classifiedKmers,int [] afterMappingClassifiedKmers,long [][] mappedKmersPos){
		
		String statsText = "";
		
		
	    double [] mappedKmersPer = new double [2];
	    mappedKmersPer [0] = ((double)(classifiedKmers[0]-afterMappingClassifiedKmers[0])/(double)classifiedKmers[0])*100;
	    mappedKmersPer [1] = ((double)(classifiedKmers[1]-afterMappingClassifiedKmers[1])/(double)classifiedKmers[1])*100;
	    
	    statsText = statsText+"Genome Length: "+mappedKmersPos[0].length+"\tMapped K-mers (out of Classified): "+String.format("%.2f",mappedKmersPer[1])+"%\n";
	    
	    System.out.println("The number of classified k-mers (distinct k-mers): "+classifiedKmers[0]+" and their total counts is "+classifiedKmers[1]);
	    System.out.println("The number of mapped k-mers (distinct k-mers)p: "+afterMappingClassifiedKmers[0]+" and their total counts is "+afterMappingClassifiedKmers[1]);
	    
	    System.out.println("The percenctage of mapped k-mers (distinct k-mers): "+mappedKmersPer[0]+" %");
	    System.out.println("The percenctage of mapped k-mers (total Counts): "+mappedKmersPer[1]+" %");
	    
	    //statsText = statsText+"Genome Coverage Percentage: \n";
	    
	    long [][] overallDepthStats = getDepthStatsForMappedKmers (mappedKmersPos, afterMappingClassifiedKmers[1]);
	    
	    for( int row= 0; row < overallDepthStats.length; row++){
	    	System.out.println("Depth Stats["+row+"] (i.e. {min,max,average,area}:");
	    	 for( int col= 0; col < overallDepthStats[0].length; col++){
	    		 System.out.print(overallDepthStats[row][col]+"\t");
	    	 }
	    	 System.out.println();
	    }
	    
	    statsText = statsText+"\t\tGenome Coverage Percentage: \tAverage Depth:\n";
	    for( int row= 0; row < overallDepthStats.length; row++){
	    	
	    	double percMapped = ((double)overallDepthStats[row][3]/(double)mappedKmersPos[0].length)*100;
	    	statsText = statsText+row+"-mismatch:\t\t"+String.format("%.2f", percMapped)+"%\t\t\t"+overallDepthStats[row][2]+"\n";
	    }	
	    
	    
	    return statsText;
	}
	*/
	/*
	 * A method to distribute the k-mer count between the number of positions mapped in the reference genome
	 */
	private long [] getPosAndCount(long count, int numOfPosBins){
		//create an array to hold the counts for each position bin
		long [] countPerPosBin = new long[numOfPosBins];
		
		//fill in the bins with zeros
		for (int i=0;i < countPerPosBin.length; i++)
			countPerPosBin [i] =0;
		
		//There are three situations to consider
		//1 position bin ==> count goes to that bin
		if(numOfPosBins == 1){
			/*System.out.println("There is only one position bin to be filled with the k-mer count!");
			System.out.println("=================================================================");*/			
			 countPerPosBin [0]= count; 
			 //System.out.println("position[0]: "+count);
			 
			 return countPerPosBin;
		}
		else {
			//numOfPosBins is greater than count, e.g. count =2, bins= 3
			//==> fill the first bins with 1 and ignore the rest of binsint index = 0;//to keep track of positions bins
			if(numOfPosBins > count){
				/*System.out.println("The number of position bins is greater the than k-mer count!");
				System.out.println("=================================================================");*/
				int index=0;
				for(long c =1; c<=count; c++){
					//add 1 to that bin
					countPerPosBin[index]++;
					
					//move to next bin
					index++;					
				}
			}
			
			//count is greater than numOfPosBins, e.g. count=4, bins=3
			//==> fill the bins equally and then the first bins will be filled with the remainder
			if(numOfPosBins < count){
				/*System.out.println("The number of position bins is smaller than the k-mre count!");
				System.out.println("=================================================================");*/
				long div = (long) count/numOfPosBins;
				long reminder = (long) count%numOfPosBins;
				/*System.out.println("div: "+div);
				System.out.println("reminder: "+reminder);*/
				
				//fill in the bins with div
				for (int i=0;i < countPerPosBin.length; i++){
					countPerPosBin [i] =div;
				}
				
				//if there is reminder, then fill in the first bins 
				if(reminder>0){
					int Index=0;
					for(long rem =1;rem<=reminder; rem++){
						//add 1 to that bin
						countPerPosBin[Index]++;						
						//move to next bin
						Index++;
				     }
				}
				//return countPerPosBin;
			}
			//count is equal to numOfPosBins, e.g. count=2, bins=2
			//==> each bin gets 1
			if(numOfPosBins == count){
				/*System.out.println("The number of position bins is equal to the k-mer count!");
				System.out.println("=================================================================");*/
				for (int i=0;i < countPerPosBin.length; i++){
					countPerPosBin [i]++;
				}
			}
		}
		return countPerPosBin;
	}
	
	/*	
	public static void main(String[] args) throws IOException{
		
		String dir = "E:\\Eclipse_WorkSpace\\KmersAlignment\\";
        String matchFile = "allMatchedKmers_1B1_S1_L001_R1R2_001withHostFiltering_31";
       
    	
        //String taxaID ="147711"; //for RhinovirusA
        String taxaID ="44131"; //for RhinovirusA49
        //String taxaID ="10298";//for human herpesVirus1
        
        
        String refGenomeFile ="rhinoVirusA49.fasta";
		//String refGenomeFile ="HumanHerpesVirus_1.fasta";
        
        long startTime = System.currentTimeMillis();
        
        KmersReferenceAssembly kRA = new KmersReferenceAssembly ();
    	
        ************************
       * This is the code should be used for classificationOuput2
       ClassificationOutput classOutput = new ClassificationOutput(dir);       
              
        
      classOutput.setallMatchedKmersMap(matchFile) ; 
        
       
        ArrayList<Kmers> kmersList = classOutput.getAllMatchedKmersList();
        
      //create a copy of the kmersList so removal of mapped k-mers does not affect the list of matched k-mers
      	ArrayList<Kmers> kmersListCopy = new ArrayList<Kmers>(kmersList); //use copy-constructor not cloning
      
        int totalCount =0;	
        for(Kmers kmer:kmersList){
        	//System.out.println("Kmer: "+kmer.getKmer()+"\tCount: "+kmer.getCount());
        	totalCount += kmer.getCount();
        }


        System.out.println("There are "+kmersList.size()+" distinct k-mers with the taxaID: "+taxaID+" with total counts= "+totalCount);
        int [] classifiedKmers = kRA.getNumClassifiedKmers (kmersList);
	    
		String refGenome = kRA.getRefGenome(refGenomeFile);
				
		//System.out.println("Reference Genome: "+refGenome);
		
		
		int maxMisMatchTolerance = 3;
		
		//get the mapped kmers 
		//The TreeMap has definite number of keys = maxMisMatchTolerance
		TreeMap<Integer,ArrayList<VirusKmers>> mappedKmers = kRA.getMappedKmers (kmersListCopy,maxMisMatchTolerance, refGenome,taxaID );
		
		//get the number of mapped kmers per mismatch tolerance
		int [] kmersCoverage = kRA.getKmersCoverage (mappedKmers,maxMisMatchTolerance);
	    for(int k =0; k<kmersCoverage.length;k++)
	    	System.out.print("["+k+"]: "+kmersCoverage[k]+"\t");
	    System.out.println();
		
		
		//print the mapped kmers to a file as well as to console
		String OutputFileName2 ="MappedKmers"+"_"+taxaID;
        kRA.printMappedKmers(mappedKmers, OutputFileName2);
        
        int refGenomeLen = refGenome.length();
        //long [][] mappedKmersPos = kRA.getMappedKmersPositions (OutputFileName2, maxMisMatchTolerance, refGenomeLen);
        long [][] mappedKmersPos = kRA.getMappedKmersPositions1 (mappedKmers,maxMisMatchTolerance,refGenomeLen);
        kRA.printMappedKmersPos (mappedKmersPos);
	    
        String fileName = "ReferenceGenomeMappedKmers_"+taxaID;
        kRA.printMappedKmersToFile (fileName,mappedKmersPos);
        
        
        //Test: case numOfPosBins > count
        long count = 13314;
        int numOfPosBins = 5;
        //Test: case numOfPosBins < count
        long count = 2;
        int numOfPosBins = 3;
        //Test: case numOfPosBins == count
        long count = 2;
        int numOfPosBins = 2;
        
        //Test: case numOfPosBins = 1
        long count = 13314;
        int numOfPosBins = 1;
        //test splitting the counts correctly between positions
        long [] countPerPos = kRA.getPosAndCount(count, numOfPosBins);
        
        for(int i = 0; i < countPerPos.length; i++){
        	System.out.println("Bin["+i+"]: "+countPerPos[i]);
        }
		
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("Time taken: "+(0.001*elapsedTime)+" seconds.");
	   
	    int [] afterMappingClassifiedKmers = kRA.getNumClassifiedKmers (kmersListCopy);
	    
	    System.out.println("There are "+afterMappingClassifiedKmers[0]+" (remaining) distinct k-mers for the taxa ID: "+taxaID+" and their total counts is "+afterMappingClassifiedKmers[1]);
	    
	    String statText = kRA.getStatText(classifiedKmers, afterMappingClassifiedKmers,mappedKmersPos);
	    System.out.println("===================================================================");
	    System.out.println("The Stats:");
	    System.out.println(statText);
	}
	
	*/
	
}
