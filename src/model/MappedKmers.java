package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/*Represents a virus k-mer in the DisCVR tool. 
 *It contains k-mer, its count in the file and list of positions the k-mer appears at the virus sequence file.
 */

public class MappedKmers {
	
	/*class Attributes*/
	String kmer;                  //stores the kmer letters
	int count;                    //stores the count of the kmers (number of times the kmer occurs)
	int [] positions;             //stores the position in a sequence where the kmers occur. 
	                             
	/*constructors*/
	public MappedKmers()
	{
		
	}
	
	public MappedKmers (String str, int num)
	{
		kmer = str;
		count = num;
				
	}
	
	public MappedKmers (String str, int num, int [] pos)
	{
		kmer = str;
		count = num;
		positions = pos;
	} 
	
	public String getKmer ()
	{
		return this.kmer;
	}
	
	public int getCount ()
	{
		return this.count;
	}
	
	public int [] getPositions ()
	{
		return this.positions;
	}
	
	
	public void setKmer (String str)
	{
		this.kmer = str;
	}
	
	public void setCount (int num)
	{
		this.count= num;
	}

	public void setPosition (int pos)
	{
		this.positions = new int[pos];
		
	}
		
	public String mappedKmerInfo ()
	{ 
		String str = this.getKmer()+"\t"+this.getCount()+"\t";
		for (int pos: this.positions)
		{
			str = str+pos+" ";
		}
		return str;
	}
	
	
}
