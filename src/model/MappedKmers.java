package model;

/***
 * Represents a mapped k-mer to a reference genome.
 * An instance contains the k-mer, its count in the sample file and the list of positions in the sequence 
 * where the k-mer occurs.
 * 
 * @author Maha Maabar
 *
 */
public class MappedKmers {
	
	/*class Attributes*/
	String kmer;                  //stores the kmer 
	int count;                    //stores the count of the kmers (number of times the kmer occurs)
	int [] positions;             //stores the position in a sequence where the kmers occur. 
	                             
	/*constructors*/
	public MappedKmers(){
		
	}
	
	public MappedKmers (String str, int num){
		kmer = str;
		count = num;				
	}
	
	public MappedKmers (String str, int num, int [] pos){
		kmer = str;
		count = num;
		positions = pos;
	} 
	
	public String getKmer (){
		return this.kmer;
	}
	
	public int getCount ()	{
		return this.count;
	}
	
	public int [] getPositions ()	{
		return this.positions;
	}
	
	
	public void setKmer (String str){
		this.kmer = str;
	}
	
	public void setCount (int num){
		this.count= num;
	}

	public void setPosition (int pos)	{
		this.positions = new int[pos];
		
	}
		
	public String mappedKmerInfo ()	{ 
		String str = this.getKmer()+"\t"+this.getCount()+"\t";
		for (int pos: this.positions)
		{
			str = str+pos+" ";
		}
		return str;
	}
	
	
}
