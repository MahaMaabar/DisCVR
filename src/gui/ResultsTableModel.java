package gui;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

import controller.VirusResult;
/***
 * Sets up the table model to hold results for the full analysis panel.
 * 
 * @author Maha Maabar
 *
 */
public class ResultsTableModel extends AbstractTableModel {
	
	private static final long serialVersionUID = 1L;
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_TAXAID = 1;
	private static final int COLUMN_RANK =2;
	private static final int COLUMN_TOTKMERSDB = 3;
	private static final int COLUMN_DISKMERS = 4;
	private static final int COLUMN_PERCENTAGE1 = 5;// perc of Distinct kmers 
	private static final int COLUMN_TOTKMERS = 6;	
	private static final int COLUMN_PERCENTAGE2 = 7; //perc of kmers counts

	private ArrayList<VirusResult> resultDB;
	private String [] header  = {"Virus Name", "Taxa ID", "Virus Rank","Total counts of k-mers in DB","No. of distinct Classified K-mers", 
			                     "(%) of distinct Classified K-mers","Total counts of Classified K-mers","(%) of total Classified K-mers",};
	
    public ResultsTableModel(ArrayList<VirusResult> vr, String [] header) {
    	this.header = header;
    	resultDB = new ArrayList<VirusResult>();    
    }	
	
	public ResultsTableModel (){		
		resultDB = new ArrayList<VirusResult>();	
	}
	
	public void setData(ArrayList<VirusResult> resultDB) {
		this.resultDB = resultDB;		
	} 
	   
	public String getColumnName(int column) {		
		return header[column];
	}

	public int getColumnCount() {		
		return header.length;
	}

	public int getRowCount() {
		return resultDB.size();       
	}

	public Object getValueAt(int row, int col) {		
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
       case COLUMN_TOTKMERSDB:
            returnValue = virusResult.getTotKmersDB();
            break;
        case COLUMN_PERCENTAGE1:
        	returnValue = roundTwoDecimals2(virusResult.getPercentage(0));
            break;
        case COLUMN_TOTKMERS:
            returnValue = virusResult.getTotKmers();
            break;   
        case COLUMN_DISKMERS:
            returnValue = virusResult.getDisKmers();
            break;
        case COLUMN_PERCENTAGE2:
        	returnValue = roundTwoDecimals2(virusResult.getPercentage(1));
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }         
        return returnValue;		
	}
	
	private static double roundTwoDecimals2(double t){
		NumberFormat nf= NumberFormat.getInstance();
		nf.setMaximumFractionDigits(6);
		nf.setMinimumFractionDigits(6);
		nf.setRoundingMode(RoundingMode.HALF_UP);

		return Double.valueOf(nf.format(t));
	}
	
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

}
