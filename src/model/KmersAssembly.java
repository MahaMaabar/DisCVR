package model;
import java.awt.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;


public class KmersAssembly {
	
	/*get the reference genome into a String from a file */
	public String getRefGenome(String fileName) throws IOException{
		String text = "";
		
		try {
			BufferedReader br= new BufferedReader(new FileReader(fileName));
			String line =  br.readLine();
			//System.out.println("header: "+line);
			while((line=br.readLine())!=null){
				text = text+line;
				//System.out.println("Squence: "+text);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return text;
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
	
	/*
	 * calculates the distance between two strings (k-mers) of the same length.
	 * If distance is greater than tolerance index, no need to carry out full 
	 * length comparison.
	 */
	public int distance (String refKmer, String matchKmer, int mismatchTolerance){
		int missedCounter = 0;
		for (int refKmerIndex = 0; refKmerIndex < refKmer.length(); refKmerIndex++) {
		   char refChar = refKmer.charAt(refKmerIndex);
		   char matchChar = matchKmer.charAt(refKmerIndex);
		   if (refChar != matchChar) {
		      missedCounter++;
		   }
		   if (missedCounter > mismatchTolerance) {
		      break;
		   }		    
		}
		return missedCounter;
	}
	
	
	
	//public ArrayList<Integer> getLowestDistancePositions(String aKmer, TreeMap<String, ArrayList<Integer>> refKmers, int maxMismatchTolerance ){
	public int [] getLowestDistancePositions(String aKmer, TreeMap<String, ArrayList<Integer>> refKmers, int maxMismatchTolerance ){
				
		ArrayList<Integer> mappedPos = new ArrayList<Integer>();
		int min = maxMismatchTolerance+1; //sets the min to the highest possible distance
		
		//String aKmer = kmersList.get(kmersIndex).getKmer();
		
		//Need to iterate through the treemap
		Set<Entry<String, ArrayList<Integer>>> set = refKmers.entrySet(); //get a set of the entries
		Iterator<Entry<String, ArrayList<Integer>>> iter = set.iterator(); //get an iterator
		 
		 //Display elements
		 while (iter.hasNext()) {
			 Map.Entry me =(Map.Entry)iter.next();
			 String refKmer= (String)me.getKey();
			 
			 int distance = distance(refKmer, aKmer, maxMismatchTolerance);
			 //System.out.println("matched k-mers: "+aKmer+" reference K-mer: "+ refKmer+" distance: "+distance);
			 
			 //make a new virusKmers, only if the distance is within the mismatch tolerance
			 if(distance < maxMismatchTolerance+1){
				//set up the virusKmers attributes 
				 ArrayList<Integer> positions = (ArrayList<Integer>) me.getValue();
				 
				 //Always check this first, otherwise the list will have duplicates of the first element
				 //min was already changed
				 if(distance == min && min!=maxMismatchTolerance+1){
					 //add the position to the list
					 //mapped.add(mappedKmer);
					 for(int pos:positions)
						 mappedPos.add(pos);
				 }
				
				//found a lower mismatch
				 if(distance < min ){
					//need to clear the virusKmers list 
					mappedPos.clear();
					//add the new VirusKmer to the list
					for(int pos:positions)
						 mappedPos.add(pos);
					 
					min = distance;//reset the min
				 }	
			}
		 }
		 
		 /*System.out.println("matched k-mers: "+aKmer+" lowest mismatch: "+ min);
		 System.out.println("========================================");*/
		 
		 int[] refPositions = new int[mappedPos.size()+1];
		 //first element in the array is the lowest mismatch
		 refPositions[0]=min;
		 //copy the list of positions to the array
		 for(int i=1;i<refPositions.length;i++){
			 refPositions[i]= mappedPos.get(i-1);
		 }
		
		 return refPositions;
	}
	
	
	
	
	public String getStatText (int [] classifiedKmers,int [] afterMappingClassifiedKmers,int [][] mappedKmersPos){
		String statsText = "";
		double [] mappedKmersPer = new double [2];
				
		mappedKmersPer [0] = ((double)(afterMappingClassifiedKmers[0])/(double)classifiedKmers[0])*100;
		mappedKmersPer [1] = ((double)(afterMappingClassifiedKmers[1])/(double)classifiedKmers[1])*100;
		
		statsText = statsText+"Genome Length: "+mappedKmersPos[0].length+"\tMapped K-mers (out of Classified): "+String.format("%.2f", mappedKmersPer [1])+"%\n";
				
		int refGenomeLength = mappedKmersPos[0].length;
		int [] overAllCoverage=  getCoverage (mappedKmersPos);
		double [] accumlatedCoverage= getAccumlatedCoverage (overAllCoverage,refGenomeLength);
		
		int [][] overallDepth = getDepth(mappedKmersPos);
		int [] averageDepth= getAverageDepth (overallDepth);
		
		statsText = statsText+"\t\t\tGenome Coverage Percentage: \tAverage Depth:\n";
		for( int row= 0; row < accumlatedCoverage.length; row++){
			if(row == 0){
		    	statsText = statsText+row+"-mismatch:\t\t\t"+String.format("%.2f", accumlatedCoverage[row]*100)+"%\t\t\t"+averageDepth[row]+"\n";
		    }
		    else{
		      statsText = statsText+"0-to-"+row+"-mismatch:\t\t"+String.format("%.2f", accumlatedCoverage[row]*100)+"%\t\t\t"+averageDepth[row]+"\n";
		    }
		    
		 }
		return statsText;
	}
	
	
	
	public TreeMap<Integer,ArrayList<MappedKmers>> getAssembledKmers (TreeMap<String,ArrayList<Integer>> refKmers, ArrayList<Kmers>kmersList, int maxMismatchTolerance){
		
		/*TreeMap to hold results of mapped Kmers
		 * Key: mismatchIndex 
		 * Value: mappedK-mers from the kmersList that are mapped to refKmers. 
		 * The form of Value a VirusKmers: K-mer<from KmersList>,count<form KmersList>, positions<from refKmers>
		 */
		
		TreeMap<Integer,ArrayList<MappedKmers>> mappedResults = new TreeMap<Integer,ArrayList<MappedKmers>>();
				
		for(int kmersIndex=0; kmersIndex<kmersList.size();kmersIndex++){
			String aKmer = kmersList.get(kmersIndex).getKmer();
		
			int[] mappedPos = getLowestDistancePositions(aKmer, refKmers, maxMismatchTolerance );
			int lowestMismatch = mappedPos[0];
			//System.out.println("mismatch: "+lowestMismatch);
			//System.out.println("*******************************");
			if(mappedPos.length >1){
				
				int [] mappedPosCopy = new int [mappedPos.length-1];
				System.arraycopy(mappedPos, 1, mappedPosCopy, 0, mappedPos.length-1);//5 is the length to copy
				 
				MappedKmers mappedKmer= new MappedKmers(aKmer,kmersList.get(kmersIndex).getCount(), mappedPosCopy);
				//System.out.println(mappedKmer.mappedKmerInfo());
				
				//add the mapped k-mers to the treemap, indexed by the mismatch
				if(mappedResults.get(lowestMismatch) != null) {
			    	ArrayList<MappedKmers> mKmers = mappedResults.get(lowestMismatch);
			    	mKmers.add(mappedKmer) ;	
			    	mappedResults.put(lowestMismatch,mKmers);
			    }
			   else {// key is not in the map
				   ArrayList<MappedKmers> mKmers = new ArrayList<MappedKmers> (10);
				   mKmers.add(mappedKmer);
				   mappedResults.put(lowestMismatch,mKmers);
			    }			
			}
						
		}
		//System.out.println("There are "+mappedResults.size()+" mapped K-mers to the reference genome");
		
		return mappedResults;
	}
	
	public void printMappedKmers (TreeMap<Integer, ArrayList<MappedKmers>> map)
	{
		 Set<Entry<Integer, ArrayList<MappedKmers>>> set = map.entrySet(); //get a set of the entries
		 Iterator<Entry<Integer, ArrayList<MappedKmers>>> i = set.iterator(); //get an iterator
		 
		 
		 //Display elements
		 while (i.hasNext())
		 {
			 Map.Entry me =(Map.Entry)i.next();
			 System.out.print("<"+me.getKey() + "-mismatch>: ");
			 ArrayList<MappedKmers> mkmers=(ArrayList<MappedKmers>)me.getValue();
			 int counter =1;
			 for(MappedKmers k:mkmers){
				 System.out.println("["+counter+"]:"+k.mappedKmerInfo());
				 counter++;
			 }
				 
			 System.out.println("+++++++++++++++++++++++++++++++++");
		 }
		
	}
	
	/*A method to align the kmers in the list with the reference genome, with specific mismatch tolerance. 
	 *For each k-mer in the k-merList, the method iteratively gets a list of the mapping positions. 
	 *If the list of mapping positions > zero, an entry of a VirusK-mer is created and then it is added to the list of VirusKmers
	 *which contains all mapped k-mers with the specific mismatchTolerance. 
	 *If the k-mer is aligned to the genome, the index of the first base where the match happens is recorded as a position.
	 *A k-mer can happen more than once in the reference genome at different positions. Therefore, a list of positions is kept for each aligned k-mer.
	 */	
	public ArrayList<MappedKmers> getVirusKmersListMatchedWithRefGenome (String refGenome, ArrayList<Kmers> kmersList,int mismatchTolerance){
		
		ArrayList<MappedKmers> results = new ArrayList<MappedKmers>();
		
		//gets the positions for each kmer in the kmers List
    	for(Kmers k:kmersList){
    		String kmer = k.getKmer();
    		int count = k.getCount();
    			
    		ArrayList<Integer>positions = getkmersPositionsInRefGenome (refGenome, kmer, mismatchTolerance);
    		
    		int [] refPositions = new int[positions.size()];
    		for(int i=0; i<positions.size();i++){
    			refPositions[i] = positions.get(i);
    		}
    		
    		//only return k-mers which are mapped to the reference genome with the mismatchTolerance
    		//i.e. k-mers with 0 positins are not considered.
    		//Integer[] refPositions = positions.toArray(new Integer[positions.size()]);
    		if(refPositions.length >0){
    			MappedKmers vKmer = new MappedKmers (kmer,count,refPositions);
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
	
	public TreeMap<Integer,ArrayList<MappedKmers>> getMappedKmers(ArrayList<Kmers> kmersList, int maxMisMatchTolerance, String refGenome){
		
						
		TreeMap<Integer,ArrayList<MappedKmers>> mappedKmers = new TreeMap<Integer,ArrayList<MappedKmers>>();
		for (int i = 0;i <= maxMisMatchTolerance; i++)
		{
			int mismatchTolerance = i;
			//adds an entry to mapped Kmers with the key= i and an empty ArrayList of viruses
			
			//how many kmers in the list we want to map to the reference
			System.out.println("mismatch["+mismatchTolerance+"]: (START) The size of Kmers List: "+kmersList.size());
			
			//get the virusList with all 
			ArrayList<MappedKmers> vResult = getVirusKmersListMatchedWithRefGenome (refGenome, kmersList, mismatchTolerance);
			
			//remove the k-mers which are mapped for that mismatch index from the original list          
	    	for(MappedKmers k:vResult){
	    		String kmer = k.getKmer();
	    		
	    		//the kmers were mapped to the ref genome with this particular mismatchTolerance so we don't match it again
	    		//return the index for that kmer in the kmersList
	    		int index=getIndex(kmer,kmersList);
	    		//System.out.println("Kmer: "+kmer+" is in position: "+index+" in the kmers List");
	    		kmersList.remove(index);	    					
	    	}
	    		//add the list of virusKmers to the mapped kmers under the key=mismatchTolerance 
	    	    if(vResult.size() >0){
	    	    	mappedKmers.put(i, vResult);
	    			System.out.println("mismatch["+mismatchTolerance+"]: (END) The size of Kmers List: "+kmersList.size());
	    	    }
    			
	    	
		}
		
		System.out.println("The new List size: "+kmersList.size()); // Prints 1
		System.out.println("The original List size: "+kmersList.size()); // Prints 2

		
		return mappedKmers;
	}
	
	
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
	 * returns the number of mapped k-mers out of the matched k-mers list
	 * The first entry is number of DISTINCT mapped k-mers
	 * The second entry is the total counts of the mapped k-mers
	 */
	public int [] getNumMappedKmers (TreeMap<Integer,ArrayList<MappedKmers>> mappedKmer){
		int [] numMappedKmers = new int [2];
		
		
		//need to iterate through the map
		Set<Entry<Integer, ArrayList<MappedKmers>>> set = mappedKmer.entrySet(); //get a set of the entries
		Iterator<Entry<Integer, ArrayList<MappedKmers>>> i = set.iterator(); //get an iterator
		 
		 //Display elements
		 while (i.hasNext())
		 {
			 Map.Entry me =(Map.Entry)i.next();
			 //System.out.print("<"+me.getKey() + "-mismatch>: ");
			 ArrayList<MappedKmers> mkmers=(ArrayList<MappedKmers>)me.getValue();
			 //System.out.println(mkmers.size()+" mapped k-mers");
			 numMappedKmers[0] += mkmers.size();		 
			 
			 for(MappedKmers k:mkmers){
				 numMappedKmers[1] += k.getCount();
			 }
		 }
		  return numMappedKmers;
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
	
	
	public int [] getLowestDistancePositionsString(String aKmer, String refGenome, int maxMismatchTolerance ){
		
		ArrayList<Integer> mappedPos = new ArrayList<Integer>();
		int min = maxMismatchTolerance+1; //sets the min to the highest possible distance
		
		int kSize = aKmer.length();
		for(int refIndex=0; refIndex< refGenome.length()-kSize+1;refIndex++){
			String refKmer= refGenome.substring(refIndex,refIndex+kSize);
			//System.out.println("ref-Kmer: "+refKmer);
			
			 int distance = distance(refKmer, aKmer, maxMismatchTolerance);
			 //System.out.println("matched k-mers: "+aKmer+" reference K-mer: "+ refKmer+" distance: "+distance);
			 
			//make a new virusKmers, only if the distance is within the mismatch tolerance
			 if(distance < maxMismatchTolerance+1){
				//get the positions in the reference genome  
				int position = refIndex;
				 
				 //Always check this first, otherwise the list will have duplicates of the first element
				 //min was already changed
				 if(distance == min && min!=maxMismatchTolerance+1){
					 //add the position to the list
					 mappedPos.add(position);
				 }
				
				//found a lower mismatch
				 if(distance < min ){
					//need to clear the virusKmers list 
					mappedPos.clear();
					//add the new VirusKmer to the list
					mappedPos.add(position);
					 
					min = distance;//reset the min
				 }	
			}
			
		}
			 int[] refPositions = new int[mappedPos.size()+1];
			 //first element in the array is the lowest mismatch
			 refPositions[0]=min;
			 //copy the list of positions to the array
			 for(int i=1;i<refPositions.length;i++){
				 refPositions[i]= mappedPos.get(i-1);
			 }
		
		 return refPositions;
	}
	
	/* to generate random numbers that can be used to update the reference genome in the case 
	 * where the count is less than the kSize.
	 * Ensures that positions are not duplicates.
	 */
	private int[] getRandomPositions(int kSize, int count){
		int [] randomPositions = new int [count];
		int i=0;
		
		while(i < count){
			//no seed is used in the constructor to avoid generating the same random number
			Random generator = new Random();
			//produces a value in the range of 0 to kSize, by adding 1, it is shifted in the range 1 to kSize
			int d = 1+generator.nextInt(kSize); 
			//System.out.println("Random number: "+d);
			//only add the number, if it does not exist 
			if(!findItem(randomPositions,d)){
				randomPositions[i]=d;
				i++;
			}
				
		}
		return randomPositions;
	}
	
	/*
	 * to search an array for an item
	 */
	private boolean findItem(int [] countArray, int number){
		boolean found = false;
		for(int i=0;i<countArray.length;i++){
			if (countArray[i] == number)
				found = true;
		}
		return found;
	}
	
	
public TreeMap<Integer,ArrayList<MappedKmers>> getAssembledKmersString (String refGenome, ArrayList<Kmers>kmersList, int maxMismatchTolerance){
		
		/*TreeMap to hold results of mapped Kmers to a reference genome
		 * Key: mismatchIndex 
		 * Value: mappedK-mers from the kmersList that are mapped to refKmers. 
		 * The form of Value a VirusKmers: K-mer<from KmersList>,count<form KmersList>, positions<from refKmers>
		 */
		
		TreeMap<Integer,ArrayList<MappedKmers>> mappedResults = new TreeMap<Integer,ArrayList<MappedKmers>>();
				
		for(int kmersIndex=0; kmersIndex<kmersList.size();kmersIndex++){
			String aKmer = kmersList.get(kmersIndex).getKmer();
			//System.out.println("matched-kmer: "+aKmer);
			
			int[] mappedPos = getLowestDistancePositionsString(aKmer, refGenome, maxMismatchTolerance );	
			
			
			/* the first element indicates the mismatch index
			 * the rest lists the positions in the reference genome where the aKmer is found.*/
		    int lowestMismatch = mappedPos[0];
			//System.out.println("mismatch: "+lowestMismatch);
			//System.out.println("*******************************");
			if(mappedPos.length >1){//there are mapped k-mers 
				
				/*We do not want to consider index 0 - it is the mismatch index
				 * So copy the elements in the array from index 1 to the end into another array to get the mapped Kmers
				 */
				int [] mappedPosCopy = new int [mappedPos.length-1];
				System.arraycopy(mappedPos, 1, mappedPosCopy, 0, mappedPos.length-1);
				 
				MappedKmers mappedKmer= new MappedKmers(aKmer,kmersList.get(kmersIndex).getCount(), mappedPosCopy);
				//System.out.println(mappedKmer.mappedKmerInfo());
				
				//add the mapped k-mers to the treemap, indexed by the mismatch
				if(mappedResults.get(lowestMismatch) != null) {
			    	ArrayList<MappedKmers> mKmers = mappedResults.get(lowestMismatch);
			    	mKmers.add(mappedKmer) ;	
			    	mappedResults.put(lowestMismatch,mKmers);
			    }
			   else {// key is not in the map
				   ArrayList<MappedKmers> mKmers = new ArrayList<MappedKmers> (10);
				   mKmers.add(mappedKmer);
				   mappedResults.put(lowestMismatch,mKmers);
			    }			
			}
						
		}
		//System.out.println("There are "+mappedResults.size()+" mapped K-mers to the reference genome");
		
		return mappedResults;
	}

/*
 * Distributes the count between the positions in order to update the mapped kmers positions 
 * This is used in the case where list of positions contains only 1 item.
 * The region is defined as all the sites from the positions[0] till the kSize
 * Then the count is distributed among these sites.
 */
private int [] updatePositions1(int [] mappedKmersPositions, int [] positions, int kSize,int count){	 
	
	int start = positions[0];	
	
	int [] regionPositions = new int[kSize];
	for(int index=0;index < regionPositions.length;index++){
		regionPositions[index]=start+index;
	}
	
	//We have 3 cases 
	//case1: count = kSize
	if (count == kSize){
		//increment each site by 1
		for (int i=0;i<regionPositions.length;i++){
			int site = regionPositions[i];
			mappedKmersPositions[site] += 1;
		}
			
	}
	
	//case2: count < kSize
	if (count < kSize){
		//pick random positions in the region and increment ONLY those positions by 1
		int [] randomSites =getRandomPositions(kSize,count);
		
		/*System.out.print("Random Sites: [");
		for(int j=0; j<randomSites.length;j++)
			System.out.print(randomSites[j]+" ");
		System.out.println("]");*/
		
		for(int i=0; i<randomSites.length;i++){
			int positionIndex =  randomSites[i];
			int site = regionPositions[positionIndex-1];
			mappedKmersPositions[site] += 1;
		}
	}
	
	//case3: count > kSize
	if (count > kSize){
		//calculate remainder and count per site
		int countPerSite = count/kSize;
		//increment each site in the region by countPerSite
		for (int i=0;i<regionPositions.length;i++){
			int site = regionPositions[i];
			mappedKmersPositions[site] += countPerSite;
		}
		//consider remainder, if any choose random sites within the region and increment by 1
		int numOfRandomSites = count % kSize;
		
		if(numOfRandomSites >0){
			int [] randomSites =getRandomPositions(kSize,numOfRandomSites);
			
			/*for(int j=0; j<randomSites.length;j++)
				System.out.print(randomSites[j]+" ");
			System.out.println("]");*/
			
			for(int i=0; i<randomSites.length;i++){
				int positionIndex =  randomSites[i];
				int site = regionPositions[positionIndex-1];
				mappedKmersPositions[site] += 1;
			}
		}
		
	}
	return mappedKmersPositions;
}

/*
 * Distributes the count between the positions in order to update the mapped kmers positions 
 * This is used in the case where list of positions contains more than 1 item.
 * The region is defined as all the sites from the position lists with kSize. Overlapped is removed.
 * Then the count is distributed among these sites.
 */
private int [] updatePositions2(int [] mappedKmersPositions, int kSize,int count, int []positions){
	//we need to identify all the regions according to the positions
	ArrayList<Integer> regionPositions = new ArrayList<Integer>(kSize); //we start by kSize
	//remove overlaps
	for(int index =0; index< positions.length;index++){
		int pos = positions[index];
		for (int i =pos; i<pos+kSize; i++){
			if(!regionPositions.contains(i))
				regionPositions.add(i);
		}
	}
	
	int regionSize = regionPositions.size();
	
	/*System.out.println("The size of regionPositions: "+ regionSize);						
	System.out.println("These are:(");
	for(int pos:regionPositions)
		System.out.print(pos+" ");
	System.out.println(")");*/
	
	//Consider the three cases:
	//case1: count == number of sites in the regions
	if(count == regionSize){
		//increment each site in the regions by 1
		for (int site:regionPositions)
			mappedKmersPositions[site] += 1;
	}
	
	//case2: count < number of sites in the regions
	if(count < regionSize){
		//pick random positions in the region and increment ONLY those positions by 1
		int [] randomSites =getRandomPositions(regionSize,count);
		
		/*for(int j=0; j<randomSites.length;j++)
			System.out.print(randomSites[j]+" ");
		System.out.println("]");*/
		
		for(int i=0; i<randomSites.length;i++){
			int positionIndex = randomSites[i];
			int index =regionPositions.indexOf(positionIndex);
			//indexing in the regionPositions starts at 0
			int site = regionPositions.get(positionIndex-1);
			mappedKmersPositions[site] += 1;
		}
	}
	
	//case3: count > number of sites in the regions
	if(count > regionSize){
		//calculate remainder and count per site
		int countPerSite = count/regionSize;
		//increment each site in the region by countPerSite
		for (int site:regionPositions)
			mappedKmersPositions[site] += countPerSite;
		//consider remainder, if any choose random sites within the regions and increment by 1
		int numOfRandomSites = count % regionSize;
		
		if(numOfRandomSites >0){
			int [] randomSites =getRandomPositions(regionSize,numOfRandomSites);
			
			/*for(int j=0; j<randomSites.length;j++)
				System.out.print(randomSites[j]+" ");
			System.out.println("]");*/
			
			for(int i=0; i<randomSites.length;i++){
				int site = randomSites[i];
				mappedKmersPositions[site] += 1;
			}
		}
		
	}
	return mappedKmersPositions;
}



public int [][] getMappedKmersPositions1 (TreeMap<Integer,ArrayList<MappedKmers>> mappedKmers, int maxMismatchTolerance, int refGenomeLen){
	//number of rows starts from 0 up to the max mismatch, 
	//number of col corresponds to the length of refGenome but positions starts at 0 in the array 
	int[][] mappedKmersPositions = new int [maxMismatchTolerance+1][refGenomeLen];
	//set up the 2D array so all positions are set to zeros
	for(int r = 0; r < maxMismatchTolerance+1; r++){
		for(int col=0; col < refGenomeLen; col++){
			mappedKmersPositions [r][col]=0;
			//System.out.println("["+row+"]["+col+"]:"+mappedKmersPositions [row][col]);
		}		
	}
	
	int mappedKmerSize = mappedKmers.size();
	//System.out.println("The size of the elements in mapped kmers: "+mappedKmerSize);

	/*iterate through the mapped Kmers to populate the mapped positions
	 * key indicates the row for the mapped positions (which corresponds in turn to the mismatch index.
	 */
	Set<Entry<Integer, ArrayList<MappedKmers>>> set = mappedKmers.entrySet(); //get a set of the entries
	Iterator<Entry<Integer, ArrayList<MappedKmers>>> iter = set.iterator(); //get an iterator
		 
	while (iter.hasNext()) {
		Map.Entry me =(Map.Entry)iter.next();
		int row = (int) me.getKey();
		//System.out.print("<"+row+">: ");
		ArrayList<MappedKmers>vKmersList =(ArrayList<MappedKmers>)me.getValue();
		//System.out.println(vKmersList .size()+" mapped k-mers");
			 
		 		
		//Examine each entry in the list only when the list is not empty
		if(vKmersList.size() >0){
			for (MappedKmers virus: vKmersList){
				String kmer =virus.getKmer();
				int kSize = kmer.length();
				int count = virus.getCount();				
				int [] positions = virus.getPositions();
				
				/*System.out.print("K-mer: "+kmer+"\tCount: "+count+"\tPositions: [");
				for(int i=0; i< positions.length;i++)
					System.out.print(positions[i]+" ");
				System.out.println("]");*/
				
				
				//First, we consider the case where positions is only 1
				if(positions.length==1){					
					//System.out.println("["+row+"]: one single position!");
					mappedKmersPositions [row]=updatePositions1(mappedKmersPositions [row], positions,kSize,count);					
				}
				
				//Second Case: if positions.length > 1
				else{					
					//System.out.print("["+row+"]: "+positions.length+" positions!");
					mappedKmersPositions[row] =updatePositions2(mappedKmersPositions[row], kSize,count, positions);
				}
	           
			}
			
		}		
		
	}
		
	return mappedKmersPositions;
}

/*
 * Returns the coverage information for the reference genome taking into account the mismatch index
 * Each row in the input array corresponds to a certain mismatch index.
 * The cols in the input array corresponds to a single site in the reference genome.
 * The output array contains the information for the coverage for each mismatch index.
 * Each row indicates the number of new mapped sites in the reference genome. 
 */
public int [] getCoverage (int [][] mappedKmersPos){
	int numRows = mappedKmersPos.length;
	int numCols = mappedKmersPos[0].length;
	
	int [] overallCoverage = new int [numRows];
	
	for(int row=0;row<numRows; row++){
		int mappedArea =0;
		
		for(int col=0; col<numCols; col++){
			if (row ==0) {//special case is the first row
				if(mappedKmersPos[row][col] >0){
					mappedArea++;
				}
			}
			
			//other rows, need to traverse back the array looking for new mapped area
			else{
				boolean found = false;
				//System.out.println("At the start:");
				for(int counter=row-1; counter >=0; counter--){
					if (mappedKmersPos[counter][col]>0){ //site has been already populated
						//System.out.println("I am There at["+counter+"]["+col+"]");
						found = true;
						break;
					}
				}
				if(!found && mappedKmersPos[row][col]>0){
					//System.out.println("I am here at["+row+"]["+col+"]");
					mappedArea++;
				}
			}
		}
		//System.out.println("MappedArea ["+row+"]:"+mappedArea);
		overallCoverage [row]= mappedArea;
		
	}
	
	return overallCoverage;
}


/*
 * Get the accumulated coverage for the reference genome taking into account the mismatch index
 * The input array has the information in each row for the accumulated coverage up to a certain mismatch index.
 * The output calculates the accumulated coverage for all rows. 
 */
public double [] getAccumlatedCoverage (int [] overallCoverage, int refGenomeLength){
	int numRows = overallCoverage.length;
	double [] accumlatedCoverage = new double [numRows];
	
	double accCoverage = 0;
	for(int row =0; row< numRows; row++){
		double coverage = (double)overallCoverage[row]/(double)refGenomeLength;
		accCoverage += coverage;
		
		accumlatedCoverage[row] = accCoverage;
	}		
	return accumlatedCoverage;
}

/*
 * Get the depth information for the k-mers mapping to the reference genome taking into account the mismatch index
 * Each row in the input array corresponds to a certain mismatch index. Each column corresponds to a site in the genome.
 * Each row in the output array gives the information depth for each mismatch index. 
 * The first column counts all the mapped area from the first row and the second row calculates their sum.
 */
public int [][] getDepth (int [][] mappedKmersPos){
	int numRows = mappedKmersPos.length;
	int numCols = mappedKmersPos[0].length;
	
	int [][] overallDepth = new int [numRows][2];
	int allMappedArea =0;
	int sum =0;
	
	for(int row=0;row <numRows; row++){
		for(int col=0;col<numCols;col++){
			if (mappedKmersPos[row][col]>0){
				allMappedArea++;
				sum += mappedKmersPos[row][col];
			}
		}
		overallDepth[row][0]= allMappedArea;
		overallDepth[row][1]= sum;
	}
	
	return overallDepth;		
}

/*
 * calculates the average depth of k-mers mapping to the reference genome.
 * The input array contains the information for each mismatch. 
 * The first column contains the mapped area and the second column contains their sum.
 * Each row in the output array contains the average depth for the accumlated depth.
 */
public int [] getAverageDepth (int [][] overallDepth){
	int numRows = overallDepth.length;
	int [] averageDepth = new int [numRows];
	
	for(int row=0; row< numRows; row++){
		System.out.println("mapped area ["+row+"]:"+overallDepth[row][1]+"\t"+"sum mapped area ["+row+"]:"+overallDepth[row][0]);
		double result = (double)overallDepth[row][1]/(double)overallDepth[row][0];
		System.out.println("Result (mappedArea/sum)["+row+"]: "+result);
		int average = (int)Math.round(result);
		System.out.println("Average (rounded)["+row+"]: "+average);
		
		averageDepth[row]= average;
	}
	return averageDepth;
}

/*	public static void main(String[] args) throws IOException{
		String dir = "E:\\Eclipse_WorkSpace\\KmersAlignment\\";
        //String matchFile = "allMatchedKmers_1B1_S1_L001_R1R2_001withHostFiltering_31";
        //String matchFile = "allMatchedKmers_SRR1735246_EbolaSamplewithHostFiltering_31";
        String matchFile = "allMatchedKmers_Test";
    	
        //String refGenomeFile ="zaireEbola.fa";
        //String refGenomeFile ="zaireEbolavirus.fasta";
        //String refGenomeFile ="rhinoVirusA49.fasta";
		//String refGenomeFile ="HumanHerpesVirus_1.fasta";
		String refGenomeFile ="test.fasta";
        
		int kSize = 4;
        //int kSize = 31;
        
        int maxMismatchTolerance = 3;
        
        long startTime = System.currentTimeMillis();
        
        KmersAssembly kA = new KmersAssembly();
         
        get all the matched k-mers into a list of k-mers and their counts
         * 
         
        ClassificationOutput classOutput = new ClassificationOutput(dir);
        classOutput.setallMatchedKmersMap(matchFile) ;
        
        ArrayList<Kmers> kmersList = classOutput.getAllMatchedKmersList();
        
        
        int totalCount =0;	
        for(Kmers kmer:kmersList){
        	totalCount += kmer.getCount();
        }
        
        int [] classifiedKmers = kA.getNumClassifiedKmers (kmersList);
        
        System.out.println("There are "+classifiedKmers[0]+" DISTINCT CLASSIFIED k-mers with total counts= "+classifiedKmers[1]);
        
        for(Kmers matchedK:kmersList){
        	System.out.println("Kmer: "+matchedK.getKmer()+"\tCount: "+matchedK.getCount());
        }
        
                
        String refGenome = kA.getRefGenome(refGenomeFile);
        int refGenomeLen = refGenome.length();
        System.out.println("Reference Genome Length: "+refGenomeLen);
        
        
        
        
        ------------------------------Assemblying Using TreeMap ---------------------
             	
         get the reference genome into a TreeMap<Kmer,List of Positions>
         * 
         
		TreeMap<String,ArrayList<Integer>> refKmers = kA.getRefGenKmers(refGenome, kSize);	
		System.out.println("There are "+refKmers.size()+" distinct K-mers in the reference genome.");
        
		//kA.printTreeMap (refKmers);		
		
		TreeMap<Integer,ArrayList<MappedKmers>> mappedResults =kA.getAssembledKmers (refKmers,kmersList, maxMismatchTolerance);
		
		
		
        ------------------------------Assemblying Using TreeMap ---------------------
		 //create a copy of the kmersList so removal of mapped k-mers does not affect the list of matched k-mers
      	ArrayList<Kmers> kmersListCopy = new ArrayList<Kmers>(kmersList); //use copy-constructor not cloning
      	TreeMap<Integer,ArrayList<MappedKmers>> mappedResults = kA.getMappedKmers (kmersListCopy,maxMismatchTolerance, refGenome );
      	
		
		
		//kA.printMappedKmers (mappedResults);
		
		 * get the coverage and depth of the reference genome
		 
		int mappeKmersSize = mappedResults.size();
		System.out.println("The size of the mapped k-mers treemap: "+mappeKmersSize);
		
		if(mappeKmersSize == 0){
			System.out.println("No mapped K-mers to the reference Genome");
		}
		
		else{
			int [][] refMappedPositions = kA.getMappedKmersPositions(mappedResults,maxMismatchTolerance, refGenomeLen);
			
			int numRows = refMappedPositions.length;
			int numCols = refMappedPositions[0].length;
			
			for(int row=0;row < numRows;row++){
				System.out.print(row+"-mismatch positions:[");
				for(int col=0; col< numCols; col++){
					System.out.print(refMappedPositions[row][col]+",");
				}
				System.out.println("]");
			}
			
			int [] afterMappingClassifiedKmers = kA.getNumMappedKmers (mappedResults);
			
	        System.out.println("There are "+afterMappingClassifiedKmers[0]+" (mapped to reference genome) distinct k-mers and their total counts is "+afterMappingClassifiedKmers[1]);
		    
		    String statText = kA.getStatText(classifiedKmers, afterMappingClassifiedKmers,refMappedPositions);
		    System.out.println("===================================================================");
		    System.out.println("The Stats:");
		    System.out.println(statText);
			
		}
		
		
		
		
		------------------------------Assemblying Using String Matching/Lowest Minimum Distance---------------------
		
     	TreeMap<Integer,ArrayList<MappedKmers>> mappedResults = kA.getAssembledKmersString (refGenome,kmersList,maxMismatchTolerance );
		
		//kA.printMappedKmers (mappedResults);
		
		  get the coverage and depth of the reference genome
		 
		int mappeKmersSize = mappedResults.size();
		System.out.println("The size of the mapped k-mers treemap: "+mappeKmersSize);
		
		
		//kA.printMappedKmers (mappedResults);
		if(mappeKmersSize == 0){
			System.out.println("No mapped K-mers to the reference Genome");
		}
		
		else{
			
			int [][] refMappedPositions = kA.getMappedKmersPositions1(mappedResults,maxMismatchTolerance, refGenomeLen);
			//int [][] refMappedPositions = kA.getMappedKmersPositions(mappedResults,maxMismatchTolerance, refGenomeLen);
			
			int numRows = refMappedPositions.length;
			int numCols = refMappedPositions[0].length;
			
			for(int row=0;row < numRows;row++){
				System.out.print(row+"-mismatch positions:[");
				for(int col=0; col< numCols; col++){
					System.out.print(refMappedPositions[row][col]+",");
				}
				System.out.println("]");
			}
			
			int [] afterMappingClassifiedKmers = kA.getNumMappedKmers (mappedResults);
			
	        System.out.println("There are "+afterMappingClassifiedKmers[0]+" (mapped to reference genome) distinct k-mers and their total counts is "+afterMappingClassifiedKmers[1]);
		    
		    String statText = kA.getStatText(classifiedKmers, afterMappingClassifiedKmers,refMappedPositions);
		    System.out.println("===================================================================");
		    System.out.println("The Stats:");
		    System.out.println(statText);
			
		}
		
		 long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    
		    System.out.println("Time taken: "+(0.001*elapsedTime)+" seconds.");
		    
		    int seconds = (int)(elapsedTime / 1000) % 60 ;
			int minutes = (int)((elapsedTime / (1000*60)) % 60);
			int hours = (int)((elapsedTime / (1000*60*60)) % 24);
			
			
			String text = String.format("%02d:%02d:%02d", hours, minutes, seconds);
			System.out.println("Time taken (hh:mm:ss): "+text);
		
		
	}*/

}
