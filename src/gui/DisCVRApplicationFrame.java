package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import controller.AlignmentWorker;
import controller.ClassificationWorker;
import controller.KmersMappingWorker;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/***
 * Formats the main frame for DisCVR's GUI.
 * 
 * @author Maha Maabar
 *
 */
public class DisCVRApplicationFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;	
	private InputPanel inputPanel;	
	private ProgressPanel progressPanel;	
	private TextPanel progressText; //to upload progress information
	private TextPanel summaryText;	//to upload summary results
	private JPanel scoring;         //to show the viruses with the highest scores
    private TablePanel tablePanel; 	//to upload full analysis of hits in the sample
	private JSplitPane splitPane;
	private JSplitPane splitPane2;	
	private JTabbedPane tabPane;	
	private JFileChooser fileChooser;	
	private ClassificationWorker controller;//to run classification	
	private AlignmentWorker aligner;       //to run read assembly
	private KmersMappingWorker kmerAligner;//to run k-mer assembly
	
	public static final String DIR_PROPERTY_NAME = "discvrJAR.rootDir";
	public static final String currentDir = System.getProperty("user.dir");
	
	public DisCVRApplicationFrame () {
		super ("DisCVR"); 
		
        inputPanel = new InputPanel();		
		progressPanel = new ProgressPanel();		
		tablePanel = new TablePanel();		
						
		Color color1 = Color.BLUE;
		progressText = new TextPanel("Progress Information",new Font("Verdana", Font.BOLD, 16),color1);
		
		Color color2 = Color.BLACK;
		//has to be monospaced font for formatting on JTextArea
		summaryText = new TextPanel("Analysis Summary",new Font("monospaced", Font.BOLD, 16),color2);
		
		scoring = new JPanel();
		
		tabPane = new JTabbedPane();
		tabPane.add("Scoring", scoring); 
		tabPane.add("Summary", summaryText); 
		tabPane.add("Full Analysis", tablePanel);		
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, inputPanel, tabPane);
		splitPane.setOneTouchExpandable(true);
	  
	    splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane,progressText);
	    splitPane2.setOneTouchExpandable(true);
	    
	    setJMenuBar(createMenuBar());
		
		fileChooser = new JFileChooser(); 
		fileChooser.addChoosableFileFilter(new ClassificationResultFilter());
		
						
		//listens to the classify button in the inputPanel
		inputPanel.setOptionListener(new OptionListener (){
		    	public void optionEventOccurred(OptionEvent e){
		    		String actualPath = System.getProperty(DIR_PROPERTY_NAME, currentDir );
		    		String kAnalyzeDir = actualPath+"/lib";
		    		
		    		//make a directory to hold temp files
		       	    String savingDir = actualPath+"/TempFiles/";
		       	   
		       		File directory = new File(savingDir);
					if (!directory.exists()) {
				    	if (directory.mkdir()) {
				    		   //System.out.println("Temporary Folder: "+directory+" is created to hold intermediate files.");
						 } else {
						   System.err.println("Failed to create temporary folder to hold intermediate files.");
					    }
				     }
		    		 
					//get the parameters for the sample classification from the GUI
					String sampleFileName = e.getInputFile();
		       	    String inputFormat = e.getFileFormat();
		       		String dbOption = e.getDbOption();
		       	    String dbLibrary = e.getDbLibrary();
		       		String kSize = e.getkSize();
		       		String entropyThrshld = e.getEntropyThrshld();
		       				       		
		            if(dbOption.equals("BuiltInDB")){
		            	kSize = "22";
		            	entropyThrshld= "2.5";
		            }
				
		            //make an array of the parameters to pass for the classification process
		       	      String [] prams = new String [8];
		              prams [0] = savingDir;
		       	      prams [1] = sampleFileName;
		       	      prams [2] = kSize;
		       	      prams [3] = inputFormat;
		       	      prams [4] = kAnalyzeDir;
		       	      prams [5] = dbLibrary;
		       	      prams [6] = dbOption;
		       	      prams [7] = entropyThrshld;
		       	      
		       	      tablePanel.reset(); //reset table from previous run
		          	  summaryText.setText ();
		          	  progressText.setText ();
		              scoring.removeAll();
		            
		              revalidate();
		              repaint();
		       	
		        	  tablePanel.setVirusTableListener(new VirusTableListener () {
		        		public void rowDetected (int row, String virusName, String virusTaxaID, int assemblyOption){
		        			String referenceGenomesFile = "";
		        			if(dbOption.equalsIgnoreCase("BuiltInDB")){
		        				referenceGenomesFile =  "/resources/"+dbLibrary+"_referenceGenomesLibrary";	
		        			}
		        			if(dbOption.equalsIgnoreCase("customisedDB")){
		        				Path p = Paths.get(dbLibrary);
		        				String file = p.getFileName().toString();
		        				int kSizeIndex = file.indexOf('_');
		        				String dbName = file.substring(0,kSizeIndex);
		        				referenceGenomesFile = actualPath+"/customisedDB/"+dbName+"_referenceGenomesLibrary";
			        		}
		        			//run read Assembly 
		       			   if(assemblyOption == 1){
		       				   String [] args = {virusTaxaID, referenceGenomesFile,sampleFileName,dbOption};	
		       				   aligner = new AlignmentWorker(args,progressText, virusName);		                                
		                       aligner.execute();
		       		    	}
		       			  //run k-mer Assembly 
                          if(assemblyOption == 2){
                        	  controller.setMatchedKmersMap();//loads all the matched k-mers 
                        	  String [] args = {virusTaxaID, referenceGenomesFile,virusName, dbOption};	
                        	  kmerAligner = new KmersMappingWorker (args,progressText, controller);    		       			
    		       			  kmerAligner.execute();
		       			  }		       			
		       		}
		       	});
		       	
		         //run classification process 
		         controller = new ClassificationWorker(prams,progressText,summaryText,scoring,tablePanel,progressPanel);	
		         controller.execute();
		  }
		});
		
		add(progressPanel, BorderLayout.NORTH);
		
		add(splitPane2, BorderLayout.CENTER);
		pack();
		
		//add the CVR logo to the frame		
		setIconImage(createIcon("/resources/cvr_logo.gif"));
		
		setSize(800,800);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		splitPane2.setDividerLocation(0.5);
		
	}
	
		
	//set the icon for the frame
	private Image createIcon (String path) {
		URL url = getClass().getResource(path);		
		if(url == null) {
			System.err.println("Unable to load image: "+ path);
		}
				
		Image icon = new ImageIcon (url).getImage();		
		return icon;
	}
	
	//set the Menu bar for the frame
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		//add File Icon and its items		
		JMenu fileMenu = new JMenu("File");
		JMenuItem exportDataItem = new JMenuItem("Save Results...");
		JMenuItem exitItem = new JMenuItem("Exit");
		
		fileMenu.add(exportDataItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
		//add mnemonics to the File Menu and its items
		fileMenu.setMnemonic(KeyEvent.VK_F);
		exportDataItem.setMnemonic(KeyEvent.VK_S);
		exitItem.setMnemonic(KeyEvent.VK_X);		
		//set and accelarator to the exit item
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		
		//add ActionListener to the export item
		exportDataItem.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				if(fileChooser.showSaveDialog(DisCVRApplicationFrame.this)== JFileChooser.APPROVE_OPTION) {
					try {
						controller.saveToFile(fileChooser.getSelectedFile());
						System.out.println(fileChooser.getSelectedFile());
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(DisCVRApplicationFrame.this,"Couldn't save data to file.","Error",JOptionPane.ERROR_MESSAGE);
					}
		     }
		  }
			
		});
		
		//add ActionListener to the exit item
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
			
		});
		
		//add Database Icon and its items
		JMenu dbMenu = new JMenu("Database");		
		JMenuItem dbItem1 = new JMenuItem("Haemorrhagic Viruses");
		JMenuItem dbItem2 = new JMenuItem("Respiratory Viruses");
		JMenuItem dbItem3 = new JMenuItem("Human Pathogenic Viruses");
		
		dbMenu.add(dbItem1);
		dbMenu.addSeparator();
		dbMenu.add(dbItem2);
		dbMenu.addSeparator();
		dbMenu.add(dbItem3);
		
		//add mnemonics to the Database Menu and its items
		dbMenu.setMnemonic(KeyEvent.VK_D);
		dbItem1.setMnemonic(KeyEvent.VK_M);
		dbItem2.setMnemonic(KeyEvent.VK_R);	
		dbItem3.setMnemonic(KeyEvent.VK_P);
		
		
		//add ActionListener to the database items
		dbItem1.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				new MenuBarFrame("List of Haemorrhagic Viruses","HaemorrhagicViruses");  
			}
		});
		dbItem2.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				new MenuBarFrame("List of Respiratory Viruses","RespiratoryViruses");  
			}
		});
		dbItem3.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				new MenuBarFrame("List of Human Pathogenic Viruses","HSEViruses");  
			}
		});
		
		//add Help Icon and its items
		JMenu helpMenu = new JMenu("Help");		
		JMenuItem copyrightItem = new JMenuItem("About");
		
		helpMenu.add(copyrightItem);
		//Add mnemonics to the Help Menu and its item
		helpMenu.setMnemonic(KeyEvent.VK_H);
		copyrightItem.setMnemonic(KeyEvent.VK_A);			
		
		copyrightItem.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				new MenuBarFrame();  
				}
		});
		
		menuBar.add(fileMenu);
		menuBar.add(dbMenu);
		menuBar.add(helpMenu);
		
		return menuBar;
	}
	
 
}
