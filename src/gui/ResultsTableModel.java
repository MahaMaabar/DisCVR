package gui;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import model.VirusResult;

public class ResultsTableModel extends AbstractTableModel {
	
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_TAXAID = 1;
	private static final int COLUMN_RANK =2;
	private static final int COLUMN_TOTKMERSDB = 3;
	private static final int COLUMN_DISKMERS = 4;
	private static final int COLUMN_PERCENTAGE1 = 5;// perc of Distinct kmers 
	//private static final int COLUMN_DISKMERS = 5;
	private static final int COLUMN_TOTKMERS = 6;	
	private static final int COLUMN_PERCENTAGE2 = 7; //perc of kmers counts

	private ArrayList<VirusResult> resultDB;
	/*private String [] header  = {"Virus Name", "Taxa ID", "No. Distinct K-mers","Total K-mers Count"};
	*/
	/*private String [] header  = {"Virus Name", "Taxa ID", "Virus Rank","No. of K-mers in DB", "Counts of k-mers in DB","Percentage of Matching Motifs","No. of Matching Motifs","Counts of Matching Motifs" };
	*/
	private String [] header  = {"Virus Name", "Taxa ID", "Virus Rank","Total counts of k-mers in DB","No. of distinct Classified K-mers", 
			                     "(%) of distinct Classified K-mers","Total counts of Classified K-mers","(%) of total Classified K-mers",};
	
    public ResultsTableModel(ArrayList<VirusResult> vr, String [] header)
    {
    	this.header = header;
    	resultDB = new ArrayList<VirusResult>();
    
    }	
	
	public ResultsTableModel (){		
		resultDB = new ArrayList<VirusResult>();		
		
	}
	
	public void setData(ArrayList<VirusResult> resultDB) {
		this.resultDB = resultDB;
		/*System.out.println("There are "+resultDB.size()+" viruses in the sample (ResultTableModel)");
		for(VirusResult v:resultDB)
			System.out.println(v.getName()+" :: "+v.getTaxaID()+" :: "+v.getDisKmers()+" :: "+v.getTotKmers()+"\n");
        */
	} 
	
    

	@Override
	public String getColumnName(int column) {
		
		return header[column];
	}


	@Override
	public int getColumnCount() {
		
		return header.length;
	}

	@Override
	public int getRowCount() {
		return resultDB.size();
       
	}

	@Override
	public Object getValueAt(int row, int col) {
		/*		
		VirusResult virusResult = resultDB.get(row);
		
		switch (col) {
		case 0:
			return virusResult.getName();
		case 1:
			return virusResult.getTaxaID();
		case 2:
			return virusResult.getDisKmers();
		case 3:
			return virusResult.getTotKmers();
		}		
		return null;
		*/
		VirusResult virusResult = resultDB.get(row);
        Object returnValue = null;
        
       
         
        switch (col) {
        case  COLUMN_NAME:
            returnValue = virusResult.getName();
            break;
        case COLUMN_TAXAID:
            returnValue = virusResult.getTaxaID();
            break;
        case COLUMN_RANK:
            returnValue = virusResult.getRank();
            break;
       /* case COLUMN_DISKMERSDB:
            returnValue = virusResult.getDisKmersDB();
            break;*/
        case COLUMN_TOTKMERSDB:
            returnValue = virusResult.getTotKmersDB();
            break;
        case COLUMN_PERCENTAGE1:
        	//returnValue = virusResult.getPercentage();
            returnValue = roundTwoDecimals2(virusResult.getPercentage(0));
            break;
        case COLUMN_TOTKMERS:
            returnValue = virusResult.getTotKmers();
            break;   
        case COLUMN_DISKMERS:
            returnValue = virusResult.getDisKmers();
            break;
        case COLUMN_PERCENTAGE2:
        	//returnValue = virusResult.getPercentage();
            returnValue = roundTwoDecimals2(virusResult.getPercentage(1));
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }
         
        return returnValue;
		
	}
	
	/*private double roundTwoDecimals(double d) {
		DecimalFormat df = new DecimalFormat("#.0000");
		System.out.println(df.format(d));
		return Double.valueOf(df.format(d));
		//DecimalFormat twoDecimals = new DecimalFormat("#.####");
        //return Double.valueOf(twoDecimals.format(d));
}*/
	private static double roundTwoDecimals2(double t){
		NumberFormat nf= NumberFormat.getInstance();
		nf.setMaximumFractionDigits(6);
		nf.setMinimumFractionDigits(6);
		nf.setRoundingMode(RoundingMode.HALF_UP);

		//System.out.print(nf.format(t));
		return Double.valueOf(nf.format(t));
	}
	
	

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(resultDB.isEmpty()){
			return Object.class;
		}
		return getValueAt(0, columnIndex).getClass();
	}
	
	
	public void deleteData() {
        int rows = getRowCount();
        if (rows == 0) {
            return;
        }
        resultDB.clear();
        fireTableRowsDeleted(0, rows - 1);
    }
	/*
	public void add(VirusResult vr){
		resultDB.add(vr);		
		fireTableDataChanged();
	}
	*/
	

}
