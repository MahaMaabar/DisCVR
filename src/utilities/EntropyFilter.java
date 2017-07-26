package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class EntropyFilter {
	
	 public double calculateShannonEntropy(String s,int base) {
		   Map<String, Integer> numOfOccurrence = new HashMap<>();
		 
		    for (int index = 0; index <= s.length()-base; index +=base) {
		    	
		      String occurrence = s.substring(index,index+base);
		      if (numOfOccurrence.containsKey(occurrence)) {
		    	  numOfOccurrence.put(occurrence, numOfOccurrence.get(occurrence) + 1);
		      } else {
		    	  numOfOccurrence.put(occurrence, 1);
		      }
		    }
		 
		    int n = s.length() / base;
		    double entropy = 0.0;
		    for (Map.Entry<String, Integer> entry : numOfOccurrence.entrySet()) {
		      String symbol = entry.getKey();
		      double prob = (double) entry.getValue() / n;
		      //System.out.println("String: "+symbol+"\tIts probability: "+prob);
		      entropy += prob * log2(prob);
		    }
		    if (entropy == 0)
		    	return 0;
		    else
		    	return -entropy;
		  }
	 
    private double log2(double a) {
	     return Math.log(a) / Math.log(2);
	}

	public static void main(String[] args) {
		
		String inputFile = args[0];
		String outputFile = args[1];
		double threshold = Double.parseDouble(args[2]);
		
		long startTime = System.currentTimeMillis();
		
		int bases = 3; // bases to use for the entropy i.e. di- or tri-nucleotide
		EntropyFilter ef = new EntropyFilter();
		
	   
	    try{
			BufferedReader textReader = new BufferedReader( new FileReader (inputFile));
			PrintWriter pw =  new PrintWriter(new BufferedWriter(new FileWriter(outputFile))); 
			String line = null;
			
			long numKmers=0;
			int lowEKmers =0;
			int hiEKmers =0;
			TreeMap<String,String> lowEKmersMap= new TreeMap<String,String>();
			while((line = textReader.readLine())!=null){
				if(line.isEmpty()){
					System.out.println("Empty line");
					break;
				}
				
				String [] words = line.split("\t");
				String kmer= words[0];
				String taxaID = words [2];
				double entropy = ef.calculateShannonEntropy(kmer,bases);	   
				
				if(entropy < threshold){
					lowEKmers++;
					String taxaAndEntropy = taxaID+" "+entropy;
					lowEKmersMap.put(kmer, taxaAndEntropy);
				}
				else{
					hiEKmers++;
					pw.println(line);
				}
					
				
				numKmers++;
			 }
		
			textReader.close();
			pw.flush(); pw.close();
			
			long endTime = System.currentTimeMillis();
			long time=endTime-startTime;
			
			/*calculate run classification time*/
			
			int seconds = (int)(time / 1000) % 60 ;
			int minutes = (int)((time / (1000*60)) % 60);
			int hours = (int)((time / (1000*60*60)) % 24);
			
			String timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
			
			System.out.println("There are "+numKmers+" Distinct k-mers in the file: "+inputFile);
			System.out.println("There are "+lowEKmers+" k-mers with entropy less than "+threshold);
			System.out.println("There are "+hiEKmers+" k-mers with entropy equal or greater than "+threshold);
			
			System.out.println("Time taken to de-noise: "+timeText);
			
			
		  }catch(IOException e){
			  
		}
		
		
	
	}

}
