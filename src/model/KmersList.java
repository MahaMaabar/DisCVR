package model;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/***
 * Creates two lists from KAnalyze output.
 * One list contains k-mers 
 * Another list contains their corresponding counts.
 * 
 * @author Maha Maabar
 *
 */

public class KmersList {
	
	ArrayList <String> kmers;
	ArrayList <Integer> counts;
	
	public KmersList (String filename, int k) throws IOException
	{
		setLists (filename, k);
	}

	public void setLists (String file, int kmerSize) throws IOException
	{
		kmers = new ArrayList<String>();
		counts = new ArrayList<Integer>();
		
		BufferedReader in     = new BufferedReader(new FileReader(file));            
           
		/*populate the kmers and the counts lists*/
        String line;
        while ((line = in.readLine())!= null){
            	String words [] = line.split("\t");
            	           	
            	kmers.add(words[0]);
            	counts.add(Integer.parseInt(words[1]));
             }
			  
            in.close();
	}
	
	public ArrayList<String> getKmersList (){
		return this.kmers;		
	}
	
	public int getKmersListSize(){
		
		return this.kmers.toArray().length;
	}
	
	public ArrayList<Integer> getCountsList ()	{
		return this.counts;
	}	
	

}
