package gui;
import java.util.EventObject;

/***
 * Creates an option event from DisCVR's GUI input for classification
 * 
 * @author Maha Maabar
 *
 */
public class OptionEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private String kSize;
	private String dbOption;
	private String dbLibrary;
	private String inputFile;
	private String fileFormat;
	private String entropyThrshld;
	
	public OptionEvent (Object source) {
		super (source);
	}
	
	public OptionEvent (Object source, String kSize, String dbOption, String dbLibrary) {
		super (source);
		
		this.kSize = kSize;
		this.dbOption = dbOption;
		this.dbLibrary = dbLibrary;
	}
	
	public OptionEvent (Object source, String inputFile, String fileFormat, String dbOption, String dbLibrary) {
		super (source);
		
		this.inputFile = inputFile;
		this.fileFormat = fileFormat;
		this.dbOption = dbOption;
		this.dbLibrary = dbLibrary;
	}
	
	public OptionEvent (Object source, String inputFile, String fileFormat, String dbOption, String dbLibrary, String kSize, String entropyThrshld) {
		super (source);
		
		this.inputFile = inputFile;
		this.fileFormat = fileFormat;
		this.dbOption = dbOption;
		this.dbLibrary = dbLibrary;
		this.kSize = kSize;
		this.entropyThrshld = entropyThrshld;
	}

	public String getEntropyThrshld() {
		return entropyThrshld;
	}

	public void setEntropyThrshld(String entropyThrshld) {
		this.entropyThrshld = entropyThrshld;
	}

	public void setkSize(String kSize) {
		this.kSize = kSize;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	public String getkSize() {
		return kSize;
	}

	public void setkLabel(String kSize) {
		this.kSize = kSize;
	}

	public String getDbOption() {
		return dbOption;
	}

	public void setDbOption(String dbOption) {
		this.dbOption = dbOption;
	}

	public String getDbLibrary() {
		return dbLibrary;
	}

	public void setDbLibrary(String dbLibrary) {
		this.dbLibrary = dbLibrary;
	}
	
	

}
