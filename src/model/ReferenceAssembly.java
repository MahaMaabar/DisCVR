package model;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public class ReferenceAssembly {
	
	public ReferenceAssembly(String [] parms){
		/* parms[0] = sample file (.fq or .fastq)
		 * parms[1] = reference file (.fa or .fasta )
		 * parms[2] = output file (.sam)
		 * parms[3] = log file directory (current directory)
		 */
		runsRefAssembly (parms);
	}
	
	public ReferenceAssembly(){
		
	}

	public void runsRefAssembly (String [] parms){
		String sampleFile = parms[0]; //string includes full path
		String refFile = parms[1];
		String outputFile = parms [2];
		String logDir = parms [3];
		
		
		//to get the path of a file 
		
		//String s = ReferenceAssembly.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		//System.out.println("Current dir: "+s);
		URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        File file = new File (location.getPath());
        //System.out.println("Path: "+file.getAbsolutePath());
        //System.out.println("File: "+file.getName());
		
				
		//String [] commands = new String[]{"java ","-cp",s,"tanotipackage.Tanoti","-i",sampleFile,"-r",refFile,"-o",outputFile};
		String [] commands = new String[]{"java ","-cp",file.getAbsolutePath(),"tanotipackage.Tanoti","-i",sampleFile,"-r",refFile,"-o",outputFile};
		
		/*for( String c:commands)
		{
			System.out.println(c+"::");
		}
		System.out.println();*/
		
		try {
			FileOutputStream fos = new FileOutputStream(logDir+"Tanoti_log.txt");
			Runtime rt = Runtime.getRuntime();
			//Process proc = rt.exec(command);
			Process proc = rt.exec(commands);
			
			  
			//any error message?
			CmdExec erroMessage = new CmdExec(proc.getErrorStream(), "ERROR");
			  
			//any output?
			CmdExec outputMessage = new CmdExec(proc.getInputStream(), "OUTPUT",fos);
			  
			//kick them off
			erroMessage.start();
			outputMessage.start();
			  
			//any error?
			int exitVal = proc.waitFor();
			//System.out.println("ExitVlaue: "+ exitVal);
			fos.flush();
			fos.close();
		  }
		
		catch (Throwable t){
			t.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		//new ReferenceAssembly (args);	
		String []parms= new String[4];
		
		parms[0] ="E:/Datasets/Elihu_Data/Dengue_SampleData/130166_S12_R1R2_001.fastq";// parms[0];
		parms[1] = "E:/Eclipse_WorkSpace/DisCVR_AlphaVersion/refGenome_11053.fa";
		parms[2] ="output.sam";
		parms[3] =System.getProperty("user.dir");
		
		new ReferenceAssembly(parms);
		 
	}

}
