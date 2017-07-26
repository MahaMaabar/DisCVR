package model;
import java.util.ArrayList;

public class Kmers {

	private String kmer;
	private int count;
	
	public Kmers (String kmer, int count){
		this.kmer = kmer;
		this.count = count;
	}
	
	
	public String getKmer() {
		return kmer;
	}

	public void setKmer(String kmer) {
		this.kmer = kmer;
	}


	public int getCount() {
		return count;
	}


	public void setCount(int count) {
		this.count = count;
	}


	public static void main(String[] args) {
		String [] kmersName = {"AAAA","CCCC","GGGG","TTTT"};
	    int [] counts = {2,4,5,1};
	    
	    ArrayList<Kmers> kmersList = new ArrayList<Kmers>();
	    
	    for(int i=0 ; i< kmersName.length;i++)
	    {
	    	Kmers kmer = new Kmers(kmersName[i],counts[i]);
	    	kmersList.add(kmer);
	    }
        
	    for(Kmers k:kmersList){
	    	System.out.println("Kmer: "+k.getKmer()+"\tCount: "+k.getCount());
	    }
	}

}
