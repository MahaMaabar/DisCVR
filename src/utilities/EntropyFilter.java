package utilities;

import java.util.HashMap;
import java.util.Map;

/*** calculates the tri-nucleotide k-mer entropy using Shannon entropy
 * 
 * @author Maha Maabar
 *
 */
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
		      double prob = (double) entry.getValue() / n;
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

}
