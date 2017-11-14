package model;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import utilities.CmdExec;

/***
 * Uses reference assembly to map sample reads to a reference genome using Tanoti.
 * 
 * @author Maha Maabar
 *
 */
public class ReferenceAssembly {
	
	public ReferenceAssembly(String [] parms){
		/* parms[0] = sample file (.fq or .fastq)
		 * parms[1] = reference file (.fa or .fasta )
		 * parms[2] = output file (.sam)
		 * parms[3] = log file directory (current directory)
		 */
		runsRefAssembly (parms);
	}
	
	public void runsRefAssembly (String [] parms){
		String sampleFile = parms[0]; //string includes full path
		String refFile = parms[1];
		String outputFile = parms [2];
		String logDir = parms [3];
				
		//get the path of a file 		
		URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        File file = new File (location.getPath());
        String [] commands = new String[]{"java ","-cp",file.getAbsolutePath(),"tanotipackage.Tanoti","-i",sampleFile,"-r",refFile,"-o",outputFile};
		
				
		try {
			FileOutputStream fos = new FileOutputStream(logDir+"Tanoti_log.txt");
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(commands);
			
			  
			CmdExec erroMessage = new CmdExec(proc.getErrorStream(), "ERROR");
			  
			CmdExec outputMessage = new CmdExec(proc.getInputStream(), "OUTPUT",fos);
			  
			erroMessage.start();
			outputMessage.start();
			  
			int exitVal = proc.waitFor();
			//System.out.println("ExitVlaue: "+ exitVal);
			fos.flush();
			fos.close();
		  }
		
		catch (Throwable t){
			t.printStackTrace();
		}
	}

}
