package model;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.TreeMap;
import java.nio.file.Path;
import java.nio.file.Paths;

import utilities.PermutationFiles;
import controller.VirusResult;

/***
 * Carries out the process for sample classification.
 * Either through GUI or command line.
 * 
 * @author Maha Maabar
 *
 */
public class SampleClassification {
	
	public static final String DIR_PROPERTY_NAME = "discvrJAR.rootDir";
	public static final String currentDir = System.getProperty("user.dir");
	
	private static SampleClassification sc;
	private KmersCounting kC;
	private SampleKmersMatching kM;	
	private ArrayList<VirusResult> virusResults;	
	private TreeMap<String,Integer> allMatchedKmers;	
	private String summary;   
    private String [] highestScoresNames; 
    private int [] highestScoresSpecific;
    private int [] highestScoresShared;
    
    //classification constructor when using DisCVR's GUI
	public SampleClassification(){		
	}
	
	//classification process when using command line
	public SampleClassification(String []prams) {
		classifySample(prams);
	}
		
	//split sample k-mers into smaller files according to a certain permutation
	public String sampleFileSplitting (String savingDir, String kmersFile, String fileType, String kSize, int permsPower){
		char set[] = {'A', 'C', 'G', 'T'};
		
		PermutationFiles PF = new PermutationFiles(permsPower,set.length);
		String [] perms = PF.getPermsArray();			
        PF.printAllKLength(set,permsPower,perms); //Populate the permutations array
      	
        return PF.printsToSamplePermsFiles(kmersFile,savingDir, fileType, permsPower, perms);       
	}
	
	//split db k-mers into smaller files according to a certain permutation	
	public String dbFileSplitting (String kmersFile, String fileNamePrefix, String dbOption, String type,int permsPower) {
			char set[] = {'A', 'C', 'G', 'T'};
			
			PermutationFiles PF = new PermutationFiles(permsPower,set.length);
			String [] perms = PF.getPermsArray();			
	        PF.printAllKLength(set,permsPower,perms); //Populate the permutations array
	        
	        return PF.printsToDbPermsFiles(kmersFile,fileNamePrefix,  type, dbOption,permsPower, perms);
	       
	}
	
public String getSummaryText(){
		return summary;		
	}	
	public String [] getHighestScoresVirus(){
		return highestScoresNames;
	}	
	public int [] getHighestScoresSpecific(){
		return highestScoresSpecific;
	}	
	public int [] getHighestScoresShared(){
		return highestScoresShared;
	}	
	public TreeMap<String,Integer> getAllMatchedKmers(){
		return this.allMatchedKmers;
	}
	
	public ArrayList<VirusResult> getVirusResultsList (){
		 return virusResults ;
	}
	
	public int getResultSize ()	{
		return virusResults.size();
	}
	
	//get the classification results in terms of virus info and their scores 
	//to be used by DisCVR's GUI
	public String printingMatchingResults(String matchFile,String dbFile,String dbOption) {
		 return setVirusResultsList (dbFile,dbOption,matchFile);
	}
	
	//get the classification results in terms of virus info and their scores
	//to be used when a folder of samples is supplied and not using DisCVR's GUI	
	public void printingMatchingResults2(String matchFile,String sampleInfo, String dbFile,String dbOption, String outFile) {
		setVirusResultsList2 (dbFile,dbOption,matchFile);
		
		//prints out classification results to an output file (comma separated)
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outFile))); 
		    	 
		    //Add info about the reads
		    pw.println(sampleInfo);
		    pw.println("TOTAL_COUNTS_K-MERS"+","+"(%) OF_TOTAL_COUNTS_K-MERS"+","+
		    	       "NUMBER_OF_DISTINCT_K-MERS"+","+"(%) OF_DISTINCT_K-MERS"+","+
		    	       "NUMBER_OF_TOTAL_K-MERS_DB"+","+"TAXONOMY_ID"+","+"TAXONOMY_RANK"+
		    	       ","+"VIRUS_NAME");
		 		 	    		 		
		 	for(VirusResult vResults:virusResults){
		 		String tK = ""+vResults.getTotKmers();
		 		String tKPer = ""+roundTwoDecimals2(vResults.getPercentage(1));
		 		String dK = ""+vResults.getDisKmers();
		 		String dKPer = ""+roundTwoDecimals2(vResults.getPercentage(0));
		 		String tKDB = ""+vResults.getTotKmersDB();
		 		String tI = ""+vResults.getTaxaID();
		 		String vR = ""+vResults.getRank();
		 		String vn = ""+vResults.getName();
		 			
		 		pw.println(tK+","+tKPer+","+dK+","+dKPer+","+tKDB+","+tI+","+vR+","+vn);
		 	 }
		 	 pw.close();
		   }
	       catch (IOException ex)  {
	    	 System.out.println("Errors writing to file "+outFile);	   
	      }
	}

public static SampleClassification getSc() {
	return sc;
}

public static void setDb(SampleClassification sc) {
	SampleClassification.sc = sc;
}

public KmersCounting getkC() {
	return kC;
}

public void setkC(KmersCounting kC) {
	this.kC = kC;
}
public SampleKmersMatching getkC2() {
	return kM;
}

public void setKM(SampleKmersMatching kC2) {
	this.kM = kM;
}

//To run DisCVR's sample classification from command line 
public static void main(String[] args) throws Exception{
	/*args [] =
	   samplesFolder= "D:/path/to/the/samples"
	   kSize ="31"
	   inputFileFormat = "fastq"
	   databaseName= "HaemorrhagicVirusDB" or "D:/path/to/DisCVR/customisedDB/TestDBHaemorrhagic_18" 
	   dbOption= "BuiltInDB" or "customisedDB"
	   entropyThreshold= 2.5 (if dbOption="BuiltInDB") otherwise can be 0 or any number between (0.0-3.0)
	 */
	System.out.println("=================================================================");
	System.out.println();
	
	//for (String s:args) System.out.println(s);
	
	String actualPath = System.getProperty(("user.dir"));
	 
	//make directory to hold temp files
    String savingDir = actualPath+"/TempFiles/";
	File directory = new File(savingDir);
		
    if (!directory.exists()) {
   	  if (directory.mkdir()) {
   		   //System.out.println("Temporary Folder: "+directory+" is created to hold intermediate files.");
	  }
   	  else {
		   System.err.println("Failed to create temporary folder to hold intermediate files.");
	    }
     }
      
      String kAnalyzeDir = actualPath+"/lib";
	 
      //Create the string parameters for sample classification
      String [] sampleClassificationParms = new String [8];
      sampleClassificationParms[0] = savingDir; //to hold classification results
      sampleClassificationParms[1] = "";       //sample file
      sampleClassificationParms[2] = args[1];  //k-size
      sampleClassificationParms[3] = args[2];  //sample file format
      sampleClassificationParms[4] = kAnalyzeDir; 
      sampleClassificationParms[5] = args[3];  //database name
      sampleClassificationParms[6] = args[4];  //database Option (BuiltInDB or customisedDB)
      sampleClassificationParms[7] = args[5];  //entropy threshold for filtering low entropy k-mers
     
	  //runs sample classification on each sample file in the folder 
      String folderPath = args[0];
	  File folder = new File(folderPath);
	  File[] listOfFiles = folder.listFiles();

	  for (int i = 0; i <listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	        System.out.println("File: " + listOfFiles[i].getName());
	        
	        sampleClassificationParms[1] = folderPath+"/"+ listOfFiles[i].getName();
	        setDb(new SampleClassification (sampleClassificationParms));
	      } 
	      System.out.println("=================================================================");
	   }
	 
	  System.out.println("Done!");
	}

/******************** Helpers methods *******************/

//Extracts information from classification output (used with DisCVR's command line)
private void setVirusResultsList2 (String dbFile,String dbOption, String matchFile) {
		virusResults = new ArrayList<VirusResult>();
		
		//sets up the virus names, virus ranks, and dbkmers maps
        ClassificationOutput classOutput = new ClassificationOutput(dbOption);
        classOutput.setDBIDsMap(dbFile,dbOption);
      
       int [] numOfKmers=classOutput.setKmersTrees(matchFile);
      
       allMatchedKmers = classOutput.getAllMatchedKmers();        
       virusResults = classOutput.getVirusMapResults ();
		
		for(VirusResult v:virusResults){
			v.setPercentage(v.getDisKmers(),kC.getGoodKmers(), 0); //First is % of distinct k-mers
			v.setPercentage(v.getTotKmers(), kC.getTotalGoodKmers(), 1); //Second is % of total k-mers
		}		
}


//Extracts information from classification output (used with DisCVR's GUI)
private String setVirusResultsList (String dbFile,String dbOption, String matchFile) {
		virusResults = new ArrayList<VirusResult>();
		
		//sets up the virus names, virus ranks, and db-kmers treemaps
        ClassificationOutput classOutput = new ClassificationOutput(dbOption);
        classOutput.setDBIDsMap(dbFile,dbOption);
      
        //sets up the matchedkmers, distinct(=specific), and shared (=non specific) 
        int [] numOfKmers=classOutput.setKmersTrees(matchFile);
      
        allMatchedKmers = classOutput.getAllMatchedKmers();
      
        virusResults = classOutput.getVirusMapResults ();
		
		for(VirusResult v:virusResults){
			v.setPercentage(v.getDisKmers(),kC.getGoodKmers(), 0); //First is % of distinct k-mers
			v.setPercentage(v.getTotKmers(), kC.getTotalGoodKmers(), 1); //Second is % of total k-mers
		}
		
		String results =classOutput.printVirusMapResults();
        
		String summaryResults = classOutput.getSummaryMaps ();
        summary =classOutput.formatSummaryText(summaryResults);
        
        highestScoresNames = classOutput.getHighestScoresName();
        highestScoresSpecific = classOutput.getHighestScoresSpecific();
        highestScoresShared = classOutput.getHighestScoresShared();

      return results ;    
		
	}

private void classifySample (String []parameters) {
	
	String savingDir = parameters[0]; //e.g. "D:/path/to/savingDir"
	String sampleFile = parameters[1];//e.g. "D:/path/to/samplefile.fq"
	String kSize = parameters[2];     //e.g. "30"
	String inputFileFormat = parameters[3];//"fastq"
	String kAnalyzeDir = parameters[4];//"D:/path/to/KAnalyzeDir"
	String databaseName= parameters[5];//"HaemorrhagicVirusDB"
	String dbOption = parameters [6]; //"BuiltInDB" to use DisCVR's db or "customisedDB" to use user's db
	String entropyThrshld = parameters [7]; //specifies the entropythreshold 
	
	int permsPower = 5; //default setting for  the permutations used to split large files
	String currentDir = System.getProperty("user.dir");
	String outputDir = currentDir+"/ClassificationResults_"+kSize+"_"; //create a directory to hold results output
	long startTime = System.currentTimeMillis();
	
	/*K-mers Counting for the sample*/
	System.out.println("******************************************");
	System.out.println("Counting k-mers from the sample file...");
	
	String sampleKmersFile = savingDir+"SampleKmers_"+kSize;
	String [] samplekmerCountingParms = {sampleFile, kSize,inputFileFormat, sampleKmersFile,kAnalyzeDir,entropyThrshld};

	 kC = new KmersCounting(samplekmerCountingParms);
	 
	 String sampleInfo ="There are "+kC.getNumOfReads()+" reads in the file.\n"
	 +"There are "+kC.getNumKmers()+" distinct K-mers in the file. The sum of their counts is "+kC.getTotalKmersCounts()+".\n"
	 +"There are "+kC.getGoodKmers()+" K-mers in the file with count > 1. The sum of their counts is "+kC.getTotalGoodKmers()+".\n"
	 +"There are "+kC.getBadKmers()+" K-mers in the file with count 1.\n";
	 	 
	 System.out.println("******************************************");
	 System.out.println("Splitting sample and database k-mers into smaller batches...");
	 
	 String statement=sampleFileSplitting (savingDir,sampleKmersFile, "s", kSize, permsPower);
	 //System.out.println(statement);
	 
	 String databaseKmersFile= "";		 
	 if (dbOption.equals("BuiltInDB")){
		 databaseKmersFile= "/resources/"+databaseName;
		 outputDir = outputDir+databaseName;
	 }
	 if(dbOption.equals("customisedDB")){
		databaseKmersFile=databaseName;		
		//extracting only the dbName from the full path
		Path p = Paths.get(databaseName);
		String file = p.getFileName().toString();
		int kSizeIndex = file.indexOf('_');
		String filename = file.substring(0,kSizeIndex);
		outputDir = outputDir+filename;
	 }
		 
	 String fileNamePrefix = savingDir+"db"+"Kmers_"; //prefix for all file names
	 statement=dbFileSplitting (databaseKmersFile, fileNamePrefix, dbOption, "db",permsPower);
	 
	 System.out.println("******************************************");
	 System.out.println("Matching Sample k-mers with the Database k-mers...");
	 //matching sample k-mers with database k-mers in all small files
	 int extIndex = sampleFile.indexOf('.');
	 int fNameIndex = sampleFile.lastIndexOf('/');
	 String filename = sampleFile.substring(fNameIndex+1,extIndex);
	 
	 //create the output Dir 
	 File file = new File(outputDir);
     if (!file.exists()) {
        if (file.mkdir()) {
        }
        else {
           System.out.println("Failed to create directory!");
        }
      }
	  
      String outFile = outputDir+"/"+filename+"_ClassificationOutput_"+kSize+".csv";
	  
	  SampleKmersMatching sKM =new SampleKmersMatching();
	  setKM(sKM);
	  int []numOfMatches = sKM.searchForKmersMatches(savingDir, Integer.parseInt(kSize), permsPower, filename);
	
	  long endTime = System.currentTimeMillis();
	  long duration = (endTime - startTime);  //classification time
	 
	  /*calculate run classification time*/
	  int seconds = (int)(duration / 1000) % 60 ;
	  int minutes = (int)((duration / (1000*60)) % 60);
	  int hours = (int)((duration / (1000*60*60)) % 24);
		
	  String timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		
	  sampleInfo +="Time taken (hh:mm:ss): "+timeText+"\n\n";
	   
	   System.out.println("******************************************");
	   System.out.println("Printing classification results...");
	   String matchFileName = savingDir+"allMatchedKmers_"+filename+"_"+kSize;
	   printingMatchingResults2(matchFileName,sampleInfo, databaseKmersFile,dbOption,outFile);
		
	   //deleting temp folder
	   System.out.println("******************************************");
	   System.out.println("Removing temporary folders...");
	   deleteTempFolder(savingDir);
	   System.out.println("******************************************");
	   System.out.println();
     
}

 private void deleteTempFolder(String dirName) {	 
	   final File dir = new File(dirName);
	   final String[] allFiles = dir.list();   
       for (int i=0; i<allFiles.length; i++) {
               File aFile = new File(dir,allFiles[i]); 
               aFile.delete();
        }
       dir.delete();
	}
	
private double roundTwoDecimals2(double t){
		NumberFormat nf= NumberFormat.getInstance();
		nf.setMaximumFractionDigits(6);
		nf.setMinimumFractionDigits(6);
		nf.setRoundingMode(RoundingMode.HALF_UP);
		return Double.valueOf(nf.format(t));
	}

}
