package model;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/*Represents a virus k-mer in the DisCVR tool. 
 *It contains k-mer, its count in the file and list of positions the k-mer appears at 
 *int the virus sequence file.
 */

public class VirusKmers {
	
	/*class Attributes*/
	String kmer;                  //stores the kmer letters
	int count;                    //stores the count of the kmers (number of times the kmer occurs)
	ArrayList <Integer> positions;//stores the position in file where the kmers occur. 
	                              //This corresponds to the line number of the sequence in the file.
	
	/*constructors*/
	public VirusKmers()
	{
		
	}
	
	public VirusKmers (String str, int num)
	{
		kmer = str;
		count = num;
		positions = new ArrayList<Integer>();		
	}
	
	public VirusKmers (String str, int num, ArrayList<Integer> pos)
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
	
	public ArrayList<Integer> getPositions ()
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
		this.positions.add(pos);
	}
	
	public int getPositionsSize ()
	{
		return this.positions.size();
	}
	
	public ArrayList<VirusKmers> getVirusKmersList (String filename, int kmerSize) throws IOException
	{
		ArrayList<VirusKmers> vKmers = new ArrayList<VirusKmers>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while((line = br.readLine())!=null)
		{
			
			String [] words = line.trim().split("\t");
			/*System.out.println("line: "+line);
			System.out.println("Number of words: "+words.length);
			for(String w:words){
				  System.out.println("word:"+w);
			}*/
			ArrayList<Integer> kmerPos = new ArrayList<Integer>();
			String [] positions = words[2].split(" ");
			//System.out.println("Number of positions "+positions.length);
			for(int i=0; i<positions.length; i++){
			  String p = positions[i];
			  //System.out.println("p["+i+"]:"+p);
			  if(!p.equals("")){
				  kmerPos.add(Integer.parseInt(p));	
			  }
			}
			VirusKmers v = new VirusKmers(words[0],Integer.parseInt(words[1]),kmerPos);
			vKmers.add(v);				
		}
		br.close();
		return vKmers;
		
	}
	
   public VirusKmers getVirusKmerFromList(ArrayList<VirusKmers> vList, int index)
   {
	   return (vList.get(index));
   }
	
   public ArrayList<String> getKmersList(ArrayList<VirusKmers> vList)
   {
	   ArrayList<String> kmers = new ArrayList<String>(500);
	   for(VirusKmers v:vList)
	   {
		   kmers.add(v.getKmer());
	   }
	   
	   return kmers;
   }
	
	public void printVirusKmers ()
	{
		
		String s = this.virusKmerInfo ();
		System.out.println(s);
	}	
	
	public String virusKmerInfo ()
	{ 
		String str = this.getKmer()+"\t"+this.getCount()+"\t";
		for (int pos: this.positions)
		{
			str = str+pos+" ";
		}
		return str;
	}
	
	public String virusPosIDs ()
	{ 
		String str = "";
		for (int pos: this.positions)
		{
			str = str+pos+" ";
		}
		return str;
	}
	
	
	
	public static void main(String[] args) throws IOException{
		
	    String filename = args[0]; //File contains Virus-Specific Kmers
	    int kSize = Integer.parseInt(args[1]); //size of the kmer
	    
	    /*
	    ArrayList <String> virusK = new ArrayList<String>();
	    int numOfLines = 0;
	    
	    
		BufferedReader br = null;
        String sCurrentLine = null;
        try
        {
        	//String filename ="D:/Eclipse_WorkSpace/DisCVR_PrototypeIII/src/NonVirusSpecificKmers_8";
        	//String filename ="D:/KAnalyze_WorkSpace/humanChromosomes_Kmers_31";
        	//String filename ="D:/Eclipse_WorkSpace/DisCVR_PrototypeIII/src/Results Data/VirusSpecificKmers_30";
            br = new BufferedReader(new FileReader(filename));
            while ((sCurrentLine = br.readLine()) != null)
            {
            	numOfLines++;
            	virusK.add(sCurrentLine);
                //System.out.println(sCurrentLine);                
            }
            
            br.close();
            System.out.println("There are "+numOfLines+" lines in the file.");
        }
        
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        //Populate the array of virus kmers//
        
        int index = 0;
        
		VirusKmers [] vReads = new VirusKmers [numOfLines];
		
		for (String item : virusK)
		{
		   	int countIndex = item.indexOf("\t");
            int count =Integer.parseInt(item.substring(countIndex+1));
            vReads[index] = new VirusKmers (item.substring(0,kSize),count);
		    
            System.out.println("Kmer["+index+"]:"+item.substring(0,kSize)+"\t"+count);
            
		    index++;
		}
		
		virusK = null;
		
		for (VirusKmers v : vReads)
		{
			v.printVirusKmers ();
		}
		*/
	    
	    
	    VirusKmers v = new VirusKmers();
	    ArrayList<VirusKmers> kmers = v.getVirusKmersList(filename, kSize);
	    
	    for (VirusKmers item : kmers)
		{
	    	item.printVirusKmers ();
		}
		
	    System.out.println ("There are "+kmers.size()+" kmers in the file! ");
	    
	    System.out.println ("The kmer at 52 is: "+v.getVirusKmerFromList(kmers, 51).getKmer()+" kmers in the file! ");
		System.out.println("Done!");
		
	    /*
	    VirusKmers v = new VirusKmers();
	    ArrayList<VirusKmers> vList = v.getVirusKmersList(filename, kSize);
	    ArrayList<String> kmers = v.getKmersList(vList);
	    
	    for (String k : kmers)
		{
	    	System.out.println (k);
		}
		
	    System.out.println ("There are "+kmers.size()+" kmers in the list! ");
	    
	   System.out.println ("The kmer at 52 is: "+v.getVirusKmerFromList(vList, 51).getKmer()+" kmers in the file! ");
		System.out.println("Done!");
		*/
       

	}	
	
}
