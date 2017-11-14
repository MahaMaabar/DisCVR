package controller;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import model.Kmers;
import model.KmersCounting;
import model.SampleClassification;
import model.SampleKmersMatching;
import model.VirusResult;
import model.VirusResultDatabase;
import gui.ProgressPanel;
import gui.TablePanel;
import gui.TextPanel;
import gui.ScoringPanel;

/***
 * A class to take input from the GUI, run sample classification, and then pass the results to the GUI 
 * for display.
 * 
 * @author Maha Maabar
 *
 */
public class ClassificationWorker extends SwingWorker <VirusResultDatabase, String> {
	
	public static final String DIR_PROPERTY_NAME = "discvrJAR.rootDir";
	public static final String currentDir = System.getProperty("user.dir");
	
	TreeMap<String,Integer> allMatchedKmers;
	VirusResultDatabase vrDB;
	SampleClassification sampleClass;
	String resultsText ;
	TextPanel textPanel;
	TextPanel summaryPanel;
	TablePanel tablePanel;
	ProgressPanel progressPanel; 
	JPanel scoring;
	
	private String [] prams = new String [8];//Parameters passed from GUI
		
	public ClassificationWorker(final String prams [], final TextPanel textPanel, final TextPanel summaryPanel, final JPanel scoring, final TablePanel tablePanel, final ProgressPanel progressPanel)  {
		vrDB = new VirusResultDatabase ();
		sampleClass = new SampleClassification();
		resultsText = "";
		this.prams = prams;
		this.textPanel = textPanel;
		this.summaryPanel = summaryPanel;
		this.tablePanel = tablePanel;
		this.progressPanel = progressPanel;	
		this.scoring = scoring;
	}
	
	public ArrayList<VirusResult> getClassificationResult () {		
		return sampleClass.getVirusResultsList () ;		
	}
	
	public int getNumOfVirusResults () {
		return sampleClass.getResultSize();
	}
	
	public String getResultsText() {
		return resultsText;
	}
	
	//upload the classification results from a text into virusResultDatabase
	public void addResults (long num1, long num2){
		String [] line = resultsText.split("\n");
		for (int i =0; i<line.length;i++){
			String [] virusInfo = line[i].split(": ");
			String name =virusInfo[0];
			String taxaID =virusInfo[1];
			int disKmersDB =Integer.parseInt(virusInfo[2]);
			int totKmersDB=Integer.parseInt(virusInfo[3]);
			int disKmers = Integer.parseInt(virusInfo[4]);
			int totKmers = Integer.parseInt(virusInfo[5]);
			String rank = virusInfo[6];
			VirusResult vResult = new VirusResult (name,taxaID,disKmers,totKmers,rank,disKmersDB,totKmersDB);
			vResult.setPercentage(disKmers, num1, 0); //First percentage is % of distinct k-mers
			vResult.setPercentage(totKmers, num2, 1); //Second percentage is % of total k-mers
			vrDB.addVirusResult(vResult);
		}		
	}
	
	public ArrayList<VirusResult> getResultDB () {
		return vrDB.getVirusResults();
	}
	
	//saves results of the classification to a file
	public void saveToFile (File file) throws IOException {
		vrDB.saveToFile(file);
	}
	
	//returns all the matched k-mers as an arrayList of the kmers and their counts 
	public ArrayList<Kmers> getAllMatchedKmers () {
		TreeMap<String,Integer> allMatchedKmers = sampleClass.getAllMatchedKmers();
		ArrayList<Kmers> kmersList = new ArrayList<Kmers>();
		//iterate over the treemap
		Set<Entry<String,Integer>> set = allMatchedKmers.entrySet(); //get a set of the entries
		Iterator<Entry<String,Integer>> i = set.iterator(); //get an iterator
		 
		 //Display elements
		 while (i.hasNext()) {
			 Map.Entry me =(Map.Entry)i.next();
			 
			 String kmerString = (String)me.getKey();
			 int count = (Integer) me.getValue();			 
			 Kmers akmer = new Kmers(kmerString,count);
			 
			 if(!kmersList.contains(akmer)) kmersList.add(akmer);			
		 }		
		return kmersList;
	}		
	
	public void setMatchedKmersMap() {
		allMatchedKmers =  sampleClass.getAllMatchedKmers();
	}
	
	//Executes classification process
	protected  VirusResultDatabase doInBackground() throws Exception {		
		long startTime = System.currentTimeMillis();			
		String savingDir = prams [0]; 
		String sampleFile = prams [1];
		String kSize = prams [2];
		String inputFormat = prams [3];
		String kAnalyzeDir = prams [4];
		String dbLibrary = prams [5];
		String dbOption = prams [6];
		String entropyThrshld = prams [7];
		
		//Extracting the sample file name from the full path
		Path p = Paths.get(sampleFile);
		String file = p.getFileName().toString();
		int extIndex = file.indexOf('.');
		String filename = file.substring(0,extIndex);
				
		progressPanel.setValue(0); //0% on progress bar
		
		
		textPanel.appendText("\nExtracting Information from the Sample...\n");
		
		/*1st step: Counting k-mers from the sample file*/
		String sampleKmersFile = savingDir+"SampleKmers_"+kSize;
		String [] samplekmerCountingParms = {sampleFile, kSize,inputFormat, sampleKmersFile,kAnalyzeDir,entropyThrshld};
		
		KmersCounting kC = new KmersCounting(samplekmerCountingParms);
		sampleClass.setkC(kC);
		 
		String sampleInfo ="======================================================================\n";
		 sampleInfo =sampleInfo+"There are ("+String.format("%,d", kC.getNumOfReads())+") reads in the sample.\n"		 
		 +"There are ("+String.format("%,d", kC.getNumKmers())
		 +") distinct k-mers in the sample. The sum of their counts is ("+String.format("%,d",kC.getTotalKmersCounts())+")\n\n";
		
		 textPanel.appendText(sampleInfo);
		 
		 textPanel.appendText("Processing the sample k-mers...\n");
		             
		 sampleInfo ="======================================================================\n";
		 sampleInfo =sampleInfo+"Number of distinct k-mers removed, because they occur once in the sample, are ("+String.format("%,d", kC.getBadKmers())+")\n"
		 +"Number of distinct k-mers removed, due to their low complexity, are ("+String.format("%,d", kC.getLowEntropyKmers())+")\n\n"
		 +"Number of distinct k-mer in the sample to be classified are ("+String.format("%,d", kC.getGoodKmers())+")\n"
		 +"The sum of their counts is ("+String.format("%,d",kC.getTotalGoodKmers())+")\n\n";
		
		 textPanel.appendText(sampleInfo);
		 
		 /*2nd step: splitting files (sample k-mers and database k-mers) for matching */
		 int permsPower = 5; //default setting for the permutations used to split large files
		 		 
         /* sample file splitting */
		 String statement=sampleClass.sampleFileSplitting (savingDir,sampleKmersFile, "s", kSize, permsPower);
		 		
		 publish("Finished splitting the sample Kmers file\n");//25% on progress bar
		 
		  textPanel.appendText("Extracting information from the database...\n");
		 
		 /* Retrieval of database file */
		 String databaseKmersFile = "";
		 if (dbOption.equals("BuiltInDB")){
			 databaseKmersFile= "/resources/"+dbLibrary+"_"+kSize;
		 }
		 if(dbOption.equals("customisedDB")){
			databaseKmersFile=dbLibrary;
		 }
			 
		 String fileNamePrefix = savingDir+"db"+"Kmers_"; //prefix for all file names
		 statement=sampleClass.dbFileSplitting (databaseKmersFile, fileNamePrefix, dbOption, "db",permsPower);
		 
		 publish("Finished Splitting the database Kmers file\n");//50% on progress bar	 
		 
		 textPanel.appendText(statement);
		
		/* 3rd step: matching sample k-mers to database k-mers*/
		textPanel.appendText("matching Sample k-mers with the Database k-mers...\n");
				
		SampleKmersMatching sKM =new SampleKmersMatching();
		sampleClass.setKM(sKM);
		 
		int[] numOfMatches = sKM.searchForKmersMatches(savingDir, Integer.parseInt(kSize), permsPower,filename);
		 		 
		statement="======================================================================\n";
		statement=statement+"There are ("+String.format("%,d",numOfMatches[0])+") distinct k-mers in the sample matched with the database.\n";
		statement=statement+"The total number of their counts is ("+String.format("%,d",numOfMatches[1])+")\n";
		  	
		 textPanel.appendText(statement);
		 
		 publish("Finished matching Sample K-mers with the Database K-mers.\n");//*75% on progress bar
		
		 /*4th step: printing matching results*/
		 String matchFileName = savingDir+"allMatchedKmers_"+filename+"_"+kSize;
		
		 resultsText = sampleClass.printingMatchingResults(matchFileName,databaseKmersFile,dbOption);
		
		 if(numOfMatches[0]==0){ //zero matches 			
						
			textPanel.appendText("\n\nRESULTS...\n"
			+"======================================================================\n"
			+"There are  "+String.format("%,d",numOfMatches[0])+"  viruses found in the sample.");
			
			long endTime = System.currentTimeMillis();
			long time=endTime-startTime;
			
			/*calculate run classification time*/			
			int seconds = (int)(time / 1000) % 60 ;
			int minutes = (int)((time / (1000*60)) % 60);
			int hours = (int)((time / (1000*60*60)) % 24);
			
			String timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
			
			
			textPanel.appendText("\nTime taken (hh:mm:ss): "+timeText);
			
			publish("Done!\n\n"); //100% on progress bar
		}
		
		else {
			
			String summaryText =sampleClass.getSummaryText();
            summaryPanel.appendText(summaryText); 
        
            String [] highestScoresNames = sampleClass.getHighestScoresVirus();
            int [] highestScoresResults = sampleClass.getHighestScoresSpecific();
            int [] highestScoresShared = sampleClass.getHighestScoresShared();
       
            //add the top scores to the scoring panel
            scoring.setLayout(new BorderLayout());
            scoring.add(new ScoringPanel (highestScoresNames,highestScoresResults,highestScoresShared), BorderLayout.WEST);
        
		    publish("Done!\n\n");
		
		    addResults (kC.getGoodKmers(),kC.getTotalGoodKmers());
		
		   textPanel.appendText("\n\nRESULTS...\n"
		   +"======================================================================\n"
		   +"There are  "+String.format("%,d",vrDB.getDBSize())+"  virus(es) found in the sample.");
		
		   long endTime = System.currentTimeMillis();
		   long time=endTime-startTime;
		
		  /*calculate run classification time*/
		  int seconds = (int)(time / 1000) % 60 ;
		  int minutes = (int)((time / (1000*60)) % 60);
		  int hours = (int)((time / (1000*60*60)) % 24);
		
		  String timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		
		  textPanel.appendText("\nTime taken (hh:mm:ss): "+timeText);
        } 
				 
		deleteTempFolder(savingDir);//delete temp files
		return vrDB;
	}
	
	//to update the progress bar
	protected void process(final List<String> chunks) {
		int value = progressPanel.getValue();		
	    // Updates the messages text area
	    for (final String str : chunks) {	    	
	        value++;
	        progressPanel.setValue(value);
	     }
	 }
	
	//uploads results on the table when classification is done
	protected void done() {		
		tablePanel.setData(getResultDB());
	    tablePanel.refresh();
	}
	
	private  void deleteTempFolder(String dirName)   {
		final File dir = new File(dirName);
	    final String[] allFiles = dir.list();   
        for (int i=0; i<allFiles.length; i++) {
               File aFile = new File(dir,allFiles[i]); 
               aFile.delete();
        }
      dir.delete();
	}
}
