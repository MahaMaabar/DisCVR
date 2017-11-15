package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import controller.VirusResult;

/***
 * A class to set the full analysis panel in DisCVR's GUI.
 * It uses the ResultTableModel to format the table.
 * 
 * @author Maha Maabar
 *
 */
public class TablePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTable table;
	private ResultsTableModel tableModel;	
	private VirusTableListener virusTableListener;	
	private JPopupMenu popup; 
	
	public TablePanel () {
		
		Dimension dim = getPreferredSize();
		dim.width = 400;
		dim.height = 200;
		setPreferredSize(dim);
		setMinimumSize(dim);		
		
		tableModel = new ResultsTableModel();
		table = new JTable(tableModel);
		
		popup = new JPopupMenu();
		//for reference-assembly selection
		JMenuItem  assemblyItem1 = new JMenuItem("Read Assembly");
		JMenuItem  assemblyItem2 = new JMenuItem("K-mer Assembly");
		popup.add(assemblyItem1);
		popup.add(assemblyItem2);		
		
		Border innerBorder = BorderFactory.createTitledBorder(null, "Classification Results",TitledBorder.LEFT, TitledBorder.TOP, new Font("Verdana",Font.BOLD,12), Color.BLACK);
		Border outerBorder = BorderFactory.createEmptyBorder(5,5,5,5);
		setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		
		table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 16));
		table.setFont(new Font("Arial", Font.PLAIN, 16));
		
		table.setRowHeight(20);
		
		table.setPreferredScrollableViewportSize(new Dimension(500,50));
		//enable column sorting
		table.setAutoCreateRowSorter(true);
		
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		table.setRowSorter(sorter);
		ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<>();
		 
		int columnIndexToSort = 4; //sort so that elements on column 5 appear descendingly 
		sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.DESCENDING));
		 
		sorter.setSortKeys(sortKeys);
		sorter.sort();
		
		//when mouse is pressed on a row
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				
				int row = table.rowAtPoint(e.getPoint());				
				
				table.getSelectionModel().setSelectionInterval(row, row);				
				if (e.getButton() == MouseEvent.BUTTON3) {
					popup.show(table,e.getX(), e.getY());
				}
			}
		});
		
		assemblyItem1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				
				String virusName =(String) table.getValueAt(row, 0);
            	String taxaID  = (String)table.getValueAt(row, 1);
				
				if (virusTableListener != null) {
					virusTableListener.rowDetected(row, virusName, taxaID,1);
				}
			}			
		});
		
		assemblyItem2.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				
				String virusName =(String) table.getValueAt(row, 0);
            	String taxaID  = (String)table.getValueAt(row, 1);
				
				if (virusTableListener != null) {
					virusTableListener.rowDetected(row, virusName, taxaID,2);
				}
			}			
		});		
				
		setLayout (new BorderLayout ());
		add(new JScrollPane(table), BorderLayout.CENTER);		
	}
	
		
	public void setData(ArrayList<VirusResult> vDB){
		tableModel.setData(vDB);
	}
	
	public void refresh() {
		tableModel.fireTableDataChanged();
	}
	
	public void reset() {
		tableModel.deleteData();
	}
	
	public void setVirusTableListener (VirusTableListener listener){
		this.virusTableListener = listener;		
	}
	
}
