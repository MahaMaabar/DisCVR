package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Map.Entry;


//to get the positions of the reads that match with the refrence genome from a sam file
public class ReferenceGenome {
	
	private static final Pattern VALID_PATTERN = Pattern.compile("[\\d]+[a-zA-Z|=]"); //first is a number of any digit, second is a letter or = 
	private int genomeLength;
	private int [] genomePositions;
	private int numOfReads;
	private int currentPosition;
				
	public ReferenceGenome(){
				
	}
	
	
	public static void main(String[] args) {
		
		String refFile = args[0];
		String samFile = args[1];
		
		ReferenceGenome positions = new ReferenceGenome();	
			
		positions.setGenomeLen (refFile);
		int genomeLen = positions.getGenomeLength();
			
		System.out.println("The length of the reference genome is "+genomeLen+" bases.");
			
		positions.setToDefaultPos();
			
		//Three numbers: {num of reads, num of mapped reads, num of unmapped reads}
		int [] readsStats = positions.setGenomePositions (samFile);
		int numReads = positions.getNumOfReads();
			
		double percMapped = positions.calculatePerc(readsStats[0], numReads);
		double percUnMapped = positions.calculatePerc(readsStats[1], numReads);
		System.out.println("Number of reads= "+numReads+"\tMapped reads= "+readsStats[0]+" ("+percMapped+"%)"
			                  +"\tUnmapped reads= "+readsStats[1]+" ("+percUnMapped+"%)");
					           
		long startTime = System.currentTimeMillis();
		//Four numbers: {minimum depth, maximum depth, average depth, mapped area}
		int [] depthStats = positions.getDepthStats(); 
			
		long endTime = System.currentTimeMillis();
		System.out.println("Time taken= "+(endTime-startTime)+" ms");
		System.out.println("Minimum depth= "+depthStats[0]+"\tMaximum depth= "+depthStats[1]
					          +"\tAverage depth= "+depthStats[2]+"\tMapped Area= "+depthStats[3]);
			
		//Percentage of coverage= (mapped Area / genome length)*100
		double percCoverage = positions.calculatePerc(depthStats[3], genomeLen);
		System.out.println("Percentage of genome Coverage= "+percCoverage+"%");
			
	}
		
	public int getCurrentPos(){
			return currentPosition;
	}
		
	public void setCurrentPosition(int pos){
			this.currentPosition = pos;
	}
		
	public int getGenomeLength(){
			return this.genomeLength;
	}
		
	//at the beginning, fill the positions all with zero
	public void setToDefaultPos(){
		this.genomePositions = new int[this.genomeLength];
		for (int i=0;i < genomeLength; i++)
			genomePositions [i] =0;
	}
		
	public void setGenomeLen (String file){	
		this.genomeLength=0;
		
		BufferedReader bf;
		
		try {
			bf = new BufferedReader( new FileReader(file));
			String    line ;
			while((line = bf.readLine()) !=null)
				{
	                if (line.length()>0 && line.charAt(0)!='>'){
	                	this.genomeLength += line.length();
					}
				}
			//System.out.println("The length of the reference genome is "+length+" bases.");
			bf.close();
			} catch (IOException ex) {
				 System.out.println("Error reading the file: "+file+".");
				 ex.printStackTrace();
			}		
		}
		
	public int getNumOfReads(){
			return this.numOfReads;
	}
		
	public int [] getDepthStats (){
		//set the min value to the largest possible number it can be.
		int min = this.numOfReads; //min depth
		int max = 0;     //max depth 
		int sum = 0;     //total depth
		int area =0;     //mapped area
		int average = 0; //average depth
		for (int count=0; count <genomePositions.length; count++){
				//we don't want to consider positions with 0 coverage, cause that contains no depth info
				if (genomePositions[count] >0){
					sum += genomePositions[count];
					area++;
				}
				
				if (genomePositions[count] < min) {
						min=genomePositions[count];	
				}
					
				if (genomePositions[count] > max) {
					max=genomePositions[count];
				}
		}
		
		if(sum == 0) //no coverage at all
				min = 0; //reset min to 0 so that all stats indicate no coverage
			
			
		//System.out.println("Total depth= "+sum+" and the area of mapped genomes= "+area);
			
		average =(int) Math.round((double) sum/ (double)area);
			
		int [] stats = {min,max,average,area};
		return stats;
	}
		
	public double calculatePerc(int num1, int num2){
			//System.out.println("num1: "+num1+"\tnum2: "+num2+"\tAverage: "+(double) num1/ (double)num2);
			return  ((double) num1/ (double)num2)*100;
	}
		
	public int [] setGenomePositions(String samFile){
		this.numOfReads =0;
			
		int numOfMapped=0;
		int numOfUnMapped=0;
		BufferedReader bf;
		
		try {
				bf = new BufferedReader(new FileReader(samFile));
				String    line ;
				while((line = bf.readLine()) !=null){
					
                    /*each line in sam file which corresponds to sequences does not start with @*/
					if (line.charAt(0)!='@') {
						numOfReads++;
						String [] words = line.split("\t");
						/*for(String w:words)
						   System.out.println(w);*/
						int readPos = Integer.parseInt(words[3]); //positions is the 4 the column in sam file
						
						//this is what I get in sam file but in my array it corresponds to the previous one 
						//because the array indices start at 0 
						 
					    if (readPos>0){ //means the read maps to the genome
					    	setCurrentPosition (0); //reset the current position
					    	//pw1.println(line);
					    	numOfMapped++;
							String cigarVal = words[5];
													
							//get the CIGAR value into a list of values
							List<String> cigarValues= parse(cigarVal);
							
							/*for (String s: cigarValues)
								System.out.println("v:"+s.toString());*/
							  
							setCurrentPosition(readPos-1);//we start at the position indicated by the read
							decodeCIGAR(cigarValues);		
					    }
					    
					    else{
					    	numOfUnMapped++;
					    }				
							
					}//end-if 
									  
				}//end-while
				
				/*System.out.println("Results:");
				for (int pos=1; pos <= genomePositions.length;pos++)
					System.out.print("("+pos+")"+genomePositions[pos-1]);
				System.out.println();
				System.out.println("================================================");
					*/
				  
			/*System.out.println ("There are "+numOfMapped+" mapped reads in the sam file.");
			System.out.println ("There are "+numOfUnMapped+" unmapped reads in the sam file.");*/
				
			bf.close();
			/*pw1.close();
			pw2.close();*/
		} catch (IOException ex) {
			 System.out.println("Error reading the file: "+samFile+".");
			 ex.printStackTrace();
		}
			int [] readsStats = {numOfMapped,numOfUnMapped};
			return readsStats;
	}
		
	private void decodeCIGAR(List<String> cigarValues){
			for(int i=0;i<cigarValues.size();i++){
				String cVal = cigarValues.get(i);
				//System.out.println("The length of this value:"+cVal.length());
				//int num = Character.getNumericValue(cVal.charAt(0));
				int cLen = Integer.parseInt(cVal.substring(0,cVal.length()-1));
				//char letter = cVal.toUpperCase().charAt(1);
				char cLetter = cVal.toUpperCase().charAt(cVal.length()-1);
			    //System.out.println(cVal.charAt(0)+"\t"+cVal.charAt(1));
			    //System.out.println(cLen+"\t"+cLetter);
			    
				
				switch (cLetter){
				case 'M': //In this case we start at the position and for that length we increase the 
				case 'X': //positions in the reference genome.
				case '=':
					int start = currentPosition; int end = (start+cLen)-1;
					//System.out.println("It is Mapping at position:"+currentPosition+" for "+cLen+" positions. So ends mapping at: "+end);
					
					for (int pos=start; pos<=end;pos++)
					{
						genomePositions[pos]++;
						setCurrentPosition(currentPosition+1);
						
						//System.out.println(p+","+pos+","+genomePositions[pos]);
					}
					
					break;
				case 'I': //In this case, we don't do anything to the reference genome positions
				case 'S':
				case 'H':
				case 'P':
					//System.out.println("It is Doing Nothing");
					break;
				case 'N':
				case 'D':
					//System.out.println("It is Moving position from "+currentPosition+" by "+cLen+" positions to "+(currentPosition +cLen));
					setCurrentPosition(currentPosition +cLen);
					break;
				default:
					//System.out.println("Wrong Character");					
				}							
				//System.out.println("Position Now= "+startPos); 
		  }	
			
	}
	private List<String> parse(String toParse) {
			ArrayList<String> chunks = new ArrayList<String>();
		    Matcher matcher = VALID_PATTERN.matcher(toParse);
		    while (matcher.find()) {
		        chunks.add( matcher.group() );
		        //System.out.println("matcher:"+matcher.group().toString());
		    }
		    return chunks;
	}	
			
	public int getPosValue(int pos){
			return this.genomePositions[pos];
	}
		
	public List<Integer> getPosList(){
		return Arrays.stream(genomePositions)
			      .boxed()
			      .collect(Collectors.toList());
	}
		
	public int getMinPos (List<Integer> list){
			Object obj = Collections.min(list);
		    //System.out.println(obj);
		    return (int) obj;
	}
		
	public int getMaxPos (List<Integer> list){
			Object obj = Collections.max(list);
		    //System.out.println(obj);
		    return (int) obj;
	}
		
	public void saveToFile(String fileName){
			PrintWriter pw;
			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
				
				int depth = 0;
				int mappedArea =0;
				for(int i=0;i<genomeLength;i++)
				{
					if(genomePositions[i]!=0){
						mappedArea++;
						depth += genomePositions[i];
						pw.println((i+1)+"\t"+genomePositions[i]+"\t"+mappedArea+"\t"+depth);
					}
					
					//pw.println((i+1)+"\t"+genomePositions[i]);
				}

				pw.println("Depth= "+depth);
				pw.println("MappedArea= "+mappedArea);
				 pw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			 
			
	}
		
	public void formatGenome(String infile, String outfile){
			int genlength=0;
			BufferedReader bf;
			PrintWriter pw;
			try {
				bf = new BufferedReader( new FileReader(infile));
				pw = new PrintWriter(new BufferedWriter(new FileWriter(outfile)));
				String    line ;
				while((line = bf.readLine()) !=null)
				  {
					//System.out.println("LINE= "+line);
					
					if (line.length()>0){
						
						if (line.charAt(0)!='>'){
							//System.out.println("line:"+line);
							pw.print(line.trim());
							genlength = genlength+line.length();
							//System.out.println("len="+genlength);
						}
						else{
							//System.out.println("header:"+line);
							pw.println(line.trim());
						}
					}
				  }
				//System.out.println("The length of the reference genome is "+length+" bases.");
				bf.close();
				pw.close();
			} catch (IOException ex) {
				 System.out.println("Error reading the file: "+infile+".");
				 ex.printStackTrace();
			}
			
		}
		
		
	}
