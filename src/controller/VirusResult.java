package controller;

/***
 * A class represents a virus k-mer in the result of the sample classification.
 * 
 * @author Maha Maabar
 *
 */
public class VirusResult {
	
	private String name; //name of the virus
	private String taxaID; //taxID assigned to the virus
	private long disKmers ; //number of distinct k-mers represent the virus in the sample
	private long totKmers; //total counts of distinct k-mers represent the virus in the sample;
	private String rank; //rank of the virus 
	
	private long disKmersDB; //number of distinct k-mers represent the virus in DisCVR's db
	private long totKmersDB; //total counts of distinct k-mers represent the virus in DisCVR's db.
	private double [] percentage; //percentage of distinct and total counts of k-mers
	
	public void setPercentage(long virAttr,long num, int index){
		this.percentage [index]=((double)virAttr/(double)num)*100;
		
	}
	public double getPercentage(int index){
		return percentage[index];
	}
	
	public double [] getPercentages(){
		return percentage;
	}
	public VirusResult (String name, String taxaID, long disKmers, long totKmers, String rank,long disKmersDB,long totKmersDB ){
		this.name = name;
		this.taxaID = taxaID;
		this.disKmers = disKmers;
		this.totKmers = totKmers;
		this.rank= rank;
		this.disKmersDB = disKmersDB;
		this.totKmersDB= totKmersDB;
		this.percentage= new double [2]; // only two elements in percentages and their default settings is 0 
		this.percentage[0]=0; this.percentage[1]=0; 
	}
	
	
	public VirusResult (String name, String taxaID, long disKmers, long totKmers){
		this.name = name;
		this.taxaID = taxaID;
		this.disKmers = disKmers;
		this.totKmers = totKmers;
	}
	
	public VirusResult (String name, String taxaID, long disKmers, long totKmers, String rank){
		this.name = name;
		this.taxaID = taxaID;
		this.disKmers = disKmers;
		this.totKmers = totKmers;
		this.rank = rank;
	}
	
	
	public long getDisKmersDB() {
		return disKmersDB;
	}

	public void setDisKmersDB(int disKmersDB) {
		this.disKmersDB = disKmersDB;
	}

	public long getTotKmersDB() {
		return totKmersDB;
	}

	public void setTotKmersDB(long totKmersDB) {
		this.totKmersDB = totKmersDB;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTaxaID() {
		return taxaID;
	}
	public void setTaxaID(String taxaID) {
		this.taxaID = taxaID;
	}
	public long getDisKmers() {
		return disKmers;
	}
	public void setDisKmers(int disKmers) {
		this.disKmers = disKmers;
	}
	public long getTotKmers() {
		return totKmers;
	}
	public void setTotKmers(int totKmers) {
		this.totKmers = totKmers;
	}
	
	public void setRank (String rank) {
		this.rank = rank;
	}
	
	public String getRank () {
		return rank;
	}

	public String toString() {
        return name + "::" + taxaID + "::" + disKmers + "::" + totKmers + "::" + rank + "::" +disKmersDB + "::" + totKmersDB;
    }

}
