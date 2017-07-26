package model;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/*provides a list of KAnalyze k-mer counter tool output file
 * Two lists are provided, one contains the k-mers (as strings).
 * The other list contains the counter of the k-mers (as integers)
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
        while ((line = in.readLine())!= null)
            {
            	String words [] = line.split("\t");
            	           	
            	kmers.add(words[0]);
            	counts.add(Integer.parseInt(words[1]));
             }//end-while
			  
            in.close();
	}
	
	public ArrayList<String> getKmersList ()
	{
		return this.kmers;		
	}
	
	public int getKmersListSize()
	{
		
		return this.kmers.toArray().length;
	}
	
	public ArrayList<Integer> getCountsList ()
	{
		return this.counts;
	}
	
	
	/*For testing purposes*/
	public static void main(String[] args) throws IOException{
		
		String filename = "D:/Eclipse_WorkSpace/DisCVR_PrototypeIII/src/virusKmersTest_4";
		int k = 4;
		KmersList KL = new KmersList (filename,k);
		
		ArrayList<String> kList  = KL.getKmersList();
		ArrayList<Integer> countList = KL.getCountsList();
		
		for (String kmer: kList)
		{
			System.out.println(kmer);
		}
		
		for (int count: countList)
		{
			System.out.println(count);
		}

	}

}
