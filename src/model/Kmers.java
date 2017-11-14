package model;

/***
 * An instance of this class is a k-mer and its count
 * 
 * @author Maha Maabar
 *
 */
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

}
