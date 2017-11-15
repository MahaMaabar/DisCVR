package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.jfree.ui.RefineryUtilities;

/***
 * Sets the contents for the Menu bar icons in DisCVR's GUI main frame.
 * The Help and Database icons.
 * 
 * @author Maha Maabar
 *
 */
public class MenuBarFrame extends JFrame{
	
	private static final long serialVersionUID = 1L;

	//constructor for the Help menu
	public MenuBarFrame(){
		super("About DisCVR");
		
		JTextArea textArea = new JTextArea (5,5);
		
		Font font = new Font("Verdana", Font.PLAIN, 14);
		textArea.setFont(font);
		
		
		String text = "DisCVR: A Viral Diagnostic Tool\n\n"
				+ "Copyright (C) 2017  Maha Maabar <Maha.Maabar@glasgow.ac.uk>\n\n"
				+ "This program is free software: you can redistribute it and/or modify it \n"
				+ "under the terms of the GNU General Public License as published by the \n"
				+ "Free Software Foundation, either version 3 of the License, or \n"
				+ "(at your option) any later version.\n\n"
				+ "This program is distributed in the hope that it will be useful, \n"
				+ "but WITHOUT ANY WARRANTY; without even the implied warranty of \n"
				+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the \n"
				+ "GNU General Public License for more details.\n"
				+ "You should have received a copy of the GNU General Public License \n"
				+ "along with this program.  see the file \"GNU License.text\". \n"
				+ "If not, see <http://www.gnu.org/licenses/>";
		
		textArea.append(text);
		
		JPanel panel = new JPanel();		 
		panel.setLayout (new BorderLayout ());
		
		panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
		 
		add(panel,BorderLayout.CENTER);
		getContentPane().add(panel );
				
		//set the logo for the frame		
		setIconImage(createIcon("/resources/cvr_logo.gif"));
			
		//settings for the frame
		pack();
		setSize(500,400);
		RefineryUtilities.centerFrameOnScreen(this);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //so main application is not terminated
		setVisible(true);
		
	}
	
	//constructor for the database menu
	public MenuBarFrame (String title, String virusFile){
		super(title);
		
		JPanel panel = new JPanel();		 
		panel.setLayout (new BorderLayout ());
		
        JTextArea textArea = new JTextArea (2,10);	
		
		Font font = new Font("Verdana", Font.BOLD, 18);
		textArea.setFont(font);
		textArea.setForeground(Color.RED);
		
		//print a message about date of data downloading
		String str = "The information listed here was last updated on: 29/4/2017\n";
		textArea.append(str);
		
		
		String virusFilePath = "/resources/"+virusFile;
				
		//upload the file into a table 
		ArrayList<String> columns = new ArrayList<String>();
		
		columns.add("Family");
		columns.add("Total Number of Sequences");
		columns.add("Virus Name"); 
		columns.add("Virus Rank"); 
		columns.add("Reference Genome Information");	
		
				
		String[] columnNames = columns.toArray(new String[columns.size()]);		
	   
		String [][] vItems = getVirusList(virusFilePath);	
			
		DefaultTableModel model= new DefaultTableModel(vItems, columnNames);
		JTable table = new JTable (model){
	         private static final long serialVersionUID = 1L;
				@Override
	            public Dimension getPreferredScrollableViewportSize() {
	                return new Dimension(350, 150);
	            }
	            @Override
	            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
	            	Component c= super.prepareRenderer(renderer, row, col);
	            	String status = (String)getValueAt(row, 0);
	            	if(status.length()>1){
	            		c.setBackground(Color.LIGHT_GRAY);
	                    c.setForeground(Color.BLACK);
	            	}
	            	else {
	                	c.setBackground(Color.WHITE);
	                    c.setForeground(Color.BLACK);
	             }
	            	
	                return c;
	            }
	          
	        };		
		
		
		//set the layout for the table		 
		table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 16));
		table.setFont(new Font("Arial", Font.PLAIN, 18));
		
		 table.setDefaultRenderer(URL.class, new URLTableCellRenderer());
         table.setDefaultEditor(URL.class, new URLTableCellEditor());
		
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER );
		for(int i=0;i<columnNames.length;i++){
			table.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
			
		}
	
		table.setRowHeight(50);
		TableColumnModel columnModel = table.getColumnModel();
		
		columnModel.getColumn(0).setPreferredWidth(80);
		columnModel.getColumn(1).setPreferredWidth(80); 
		columnModel.getColumn(2).setPreferredWidth(400); //wide column
		columnModel.getColumn(3).setPreferredWidth(80); 
		columnModel.getColumn(4).setPreferredWidth(400);		
	
		//make  all cells unselectable
		table.setFocusable(false);
		table.setRowSelectionAllowed(false);
			
		table.setPreferredScrollableViewportSize(new Dimension(750,200));		
		
		//add actionListener to table to open link to reference genome
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Point pt = e.getPoint();
				int row = table.rowAtPoint(pt);
				int col = table.columnAtPoint(pt);
				
				//allow last col to be selected 
				if (col == 4) {
					 URI uri;
					 String address = "https://www.ncbi.nlm.nih.gov/nuccore/";
			        //create the address from the link value in the table
                    String accLink = (String) table.getValueAt(row, col);
                    accLink = accLink.replaceAll("\\s","");
                    
                    String [] links = accLink.split("\\+");
                    
                    if(links.length == 1){
                    	address = address+links[0];                    	 
                    }
                    else{
                    	for(int i= 0;i<links.length;i++){
                    		
                    		if(i==links.length-1)
                    			address = address+links[i];	
                    		else
                    			address = address+links[i]+",";	
                    	}                    	
                    }
                    try {
                    	uri = new URI(address);
						
				        launchAddress(uri);
					} catch (URISyntaxException e1) {
						
						e1.printStackTrace();
					}
			      }
			}
		});
		
		
		//add table to panel with scrollPane
		panel.add(textArea, BorderLayout.BEFORE_FIRST_LINE);
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		 
		add(panel,BorderLayout.CENTER);
		getContentPane().add(panel );
			
		//set the logo for the frame		
		setIconImage(createIcon("/resources/cvr_logo.gif"));
			
		//settings for the frame
		pack();
		setSize(800,300);
		RefineryUtilities.centerFrameOnScreen(this);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //so main application is not terminated
		setVisible(true);
		
	}
	
	/*sets the icon for the frame to be the CVR logo */
	private Image createIcon (String path) {
		URL url = getClass().getResource(path);
		
		if(url == null) {
			System.err.println("Unable to load image: "+ path);
		}
				
		Image icon = new ImageIcon (url).getImage();		
		return icon;
	}
	
	//reads the file which contains the list of viruses in the specific database
	private String [][] getVirusList(String virusFile){
		
        URL refGenURL = getClass().getResource(virusFile);
		
		if(refGenURL == null) {
			System.err.println("Unable to load the virus file: "+ virusFile);
		}
		
		ArrayList<String> vItems = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(refGenURL.openStream()))) {
			String line = null;
			
			while((line = br.readLine()) != null){
				vItems.add(line);
			}
		br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String [][] vList = new String[vItems.size()][5];
		
		for(int row=0; row<vItems.size();row++){
			String [] vInfo = vItems.get(row).split(":");
			for(int col=0; col<vInfo.length;col++){
				vList[row][col]= vInfo[col];
			}
		}
		return vList;		
	}
	
	private static void launchAddress(URI uri) {
		  if (Desktop.isDesktopSupported()) {			  
		    try {
		       Desktop.getDesktop().browse(uri);
		      } catch (IOException e) { 
		    	  e.printStackTrace();
		      }
		   } else { 
			   System.out.println("Platform is not supported");
			   }
		 }
	
	public class URLTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public URLTableCellRenderer() {
            setForeground(Color.BLUE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); //To change body of generated methods, choose Tools | Templates.
            if (value instanceof URL && column==5) {
                value = "<html><u>" + ((URL)value).toString() + "</u></html>";
                setText(value.toString());
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return this;
        }

    }
	 public class URLTableCellEditor extends AbstractCellEditor implements TableCellEditor {
		 private static final long serialVersionUID = 1L;
			private URL url;

	        @Override
	        public Object getCellEditorValue() {
	            return url;
	        }

	        @Override
	        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
	            JLabel editor = new JLabel("Clicked");
	            String str = (String)table.getValueAt(row, column);
	            editor.setText("<html><ul>" + str + "</ul></html>");
	            SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() {
	                    stopCellEditing();
	                    try {
	                        Desktop.getDesktop().browse(url.toURI());
	                    } catch (IOException | URISyntaxException ex) {
	                        ex.printStackTrace();
	                    }
	                }
	            });
	            return editor;
	        }

	        @Override
	        public boolean isCellEditable(EventObject e) {
	            boolean editable = false;
	            if (e instanceof MouseEvent) {
	                MouseEvent me = (MouseEvent) e;
	                if (me.getClickCount() == 1 && SwingUtilities.isLeftMouseButton(me)) {
	                    editable = true;
	                }
	            }
	            return editable;
	        }
	    }        
}




