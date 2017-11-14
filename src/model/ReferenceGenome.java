package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/***
 * Gets the positions of the reads that mapped to a reference genome from Tanoti output 
 * file (i.e. sam file).
 * 
 * @author Maha Maabar
 *
 */

public class ReferenceGenome {
	
	private static final Pattern VALID_PATTERN = Pattern.compile("[\\d]+[a-zA-Z|=]"); //first is a number of any digit, second is a letter or = 
	private int genomeLength;
	private int [] genomePositions;
	private int numOfReads;
	private int currentPosition;
				
	public int getCurrentPos(){
			return currentPosition;
	}		
	public void setCurrentPosition(int pos){
			this.currentPosition = pos;
	}		
	public int getGenomeLength(){
			return this.genomeLength;
	}
		
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
			
		average =(int) Math.round((double) sum/ (double)area);
			
		int [] stats = {min,max,average,area};
		return stats;
	}
		
	public double calculatePerc(int num1, int num2){
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
						int readPos = Integer.parseInt(words[3]); //positions is the 4 the column in sam file
						
						if (readPos>0){ //means the read maps to the genome
					    	setCurrentPosition (0); //reset the current position
					    	numOfMapped++;
							String cigarVal = words[5];
													
							//get the CIGAR value into a list of values
							List<String> cigarValues= parse(cigarVal);
							  
							setCurrentPosition(readPos-1);//we start at the position indicated by the read
							decodeCIGAR(cigarValues);		
					    }					    
					    else{
					    	numOfUnMapped++;
					    }								
					} 									  
				}				
				
			bf.close();
			
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
				int cLen = Integer.parseInt(cVal.substring(0,cVal.length()-1));
				char cLetter = cVal.toUpperCase().charAt(cVal.length()-1);
			    
				switch (cLetter){
				case 'M': //In this case we start at the position and for that length we increase the 
				case 'X': //positions in the reference genome.
				case '=':
					int start = currentPosition; int end = (start+cLen)-1;
					
					for (int pos=start; pos<=end;pos++)					{
						genomePositions[pos]++;
						setCurrentPosition(currentPosition+1);						
					}
					
					break;
				case 'I': //In this case, we don't do anything to the reference genome positions
				case 'S':
				case 'H':
				case 'P':
					break;
				case 'N':
				case 'D':
					setCurrentPosition(currentPosition +cLen);
					break;
				default:									
				}				
		  }				
	}
	private List<String> parse(String toParse) {
			ArrayList<String> chunks = new ArrayList<String>();
		    Matcher matcher = VALID_PATTERN.matcher(toParse);
		    while (matcher.find()) {
		        chunks.add( matcher.group() );		        
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
		    return (int) obj;
	}
		
	public int getMaxPos (List<Integer> list){
			Object obj = Collections.max(list);
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
				}
				pw.println("Depth= "+depth);
				pw.println("MappedArea= "+mappedArea);
				 pw.close();
			} catch (IOException e) {
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
				while((line = bf.readLine()) !=null)				  {
					
					if (line.length()>0){
						
						if (line.charAt(0)!='>'){
							pw.print(line.trim());
							genlength = genlength+line.length();
						}
						else{
							pw.println(line.trim());
						}
					}
				  }
				bf.close();
				pw.close();
			} catch (IOException ex) {
				 System.out.println("Error reading the file: "+infile+".");
				 ex.printStackTrace();
			}			
		}			
	}
