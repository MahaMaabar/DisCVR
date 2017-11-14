package model;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
/***
 * Represents a virus k-mer in DisCVR's virus-specific k-mers database. 
 * An instance contains k-mer, its count in the file and list of virus taxIDs the k-mer appears at 
 * their sequences.
 * 
 * @author Maha Maabar
 *
 */
public class VirusKmers {
	
	/*class Attributes*/
	String kmer;                  //stores the kmer letters
	int count;                    //stores the count of the kmers (number of times the kmer occurs)
	ArrayList <Integer> ids;//stores the taxID(s) of the viruses where the kmer is originated from. 
	
	/*constructors*/
	public VirusKmers(){
		
	}
	
	public VirusKmers (String str, int num)	{
		kmer = str;
		count = num;
		ids = new ArrayList<Integer>();		
	}
	
	public VirusKmers (String str, int num, ArrayList<Integer> id){
		kmer = str;
		count = num;
		ids = id;
	} 
	
	public String getKmer (){
		return this.kmer;
	}
	
	public int getCount (){
		return this.count;
	}
	
	public ArrayList<Integer> getPositions (){
		return this.ids;
	}
		
	public void setKmer (String str){
		this.kmer = str;
	}
	
	public void setCount (int num){
		this.count= num;
	}

	public void setIds (int id){
		this.ids.add(id);
	}
	
	public int getIdsSize (){
		return this.ids.size();
	}
	
	//gets a list of virusKmers from the database file 
	public ArrayList<VirusKmers> getVirusKmersList (String filename, int kmerSize){
		ArrayList<VirusKmers> vKmers = new ArrayList<VirusKmers>();
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String line;
		    while((line = br.readLine())!=null)	{
		    	String [] words = line.trim().split("\t");
			    ArrayList<Integer> kmerIds = new ArrayList<Integer>();
			    String [] idList = words[2].split(" ");
			for(int i=0; i<idList.length; i++){
			  String p = idList[i];
			  if(!p.equals("")){
				  kmerIds.add(Integer.parseInt(p));	
			  }
			}
			VirusKmers v = new VirusKmers(words[0],Integer.parseInt(words[1]),kmerIds);
			vKmers.add(v);				
		}
		br.close();
		}catch (IOException e){
			e.printStackTrace();
			System.out.println("Error reading the file: "+filename);
		}
		return vKmers;		
	}

	//gets a virusKmers at position index from the list of virusKmers
   public VirusKmers getVirusKmerFromList(ArrayList<VirusKmers> vList, int index) {
	   return (vList.get(index));
   }
	
   public ArrayList<String> getKmersList(ArrayList<VirusKmers> vList)  {
	   ArrayList<String> kmers = new ArrayList<String>(500);
	   for(VirusKmers v:vList)  {
		   kmers.add(v.getKmer());
	   }
	   return kmers;
   }
	
	public void printVirusKmers ()	{		
		String s = this.virusKmerInfo ();
		System.out.println(s);
	}	
	
	public String virusKmerInfo ()	{ 
		String str = this.getKmer()+"\t"+this.getCount()+"\t";
		for (int id: this.ids){
			str = str+id+" ";
		}
		return str;
	}
	
	public String virusIDs (){ 
		String str = "";
		for (int id: this.ids){
			str = str+id+" ";
		}
		return str;
	}
}
