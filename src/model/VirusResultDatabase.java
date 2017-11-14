package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;

/***
 * To store the virusResult from the classification process. 
 * It is used by the classification worker.
 * 
 * @author Maha Maabar
 *  
 */
public class VirusResultDatabase {
	
	private ArrayList<VirusResult> virusResults;
	
	public VirusResultDatabase () {
		virusResults = new ArrayList<VirusResult> ();		
	}
	
	public void addVirusResult (VirusResult vResult){
		virusResults.add(vResult);
	}
	
	public ArrayList<VirusResult> getVirusResults () {
		return virusResults;
	}
	
	public int getDBSize () {
		return virusResults.size();
	}
	
	//To save results to a file from the GUI 
	public void saveToFile(File file)  {
		//add the extension .csv if not there
		String filename =file.getAbsolutePath();
		int extIndex = filename.indexOf('.');
		if(extIndex == -1){
			filename = filename+".csv";
			file = new File(filename);			
		}
				
		try(PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)))){
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
	    catch (IOException ex) {
	    	System.out.println("Errors writing to file "+file);	   
        }	
	}
	
	private double roundTwoDecimals2(double t){
		NumberFormat nf= NumberFormat.getInstance();
		nf.setMaximumFractionDigits(6);
		nf.setMinimumFractionDigits(6);
		nf.setRoundingMode(RoundingMode.HALF_UP);

		return Double.valueOf(nf.format(t));
	}

}
