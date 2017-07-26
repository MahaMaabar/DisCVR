package tanotipackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;



public class Tanoti {
	
	private final int IDENTITY = 80;
	private long PID ;
    private int splits;
    private int numOfSplit;
    private int proc;
    private int unmapped;
    private int matchValue;
    private int tempFile;
    private int pairedEnd;
    private String [] inputFile;
    private String referenceFile;
    private String outputFile;
    
    
    public static void main(String[] args) {
    	
    	long startTime = System.currentTimeMillis();
		
		new Tanoti(args);	
		
		
			    
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    //System.out.println("Time taken: "+(0.001*elapsedTime)+" seconds.");
		
	}
	
	public Tanoti(String [] args)
    {
    	runTanoti(args);
		 	
    }
	
	private void runTanoti(String [] args){
	    this.getTanotiParameters(args);
		
		this.printTanotiParameters();
		
		String workingdirectory = System.getProperty("user.dir");
		
		//set the current PID
		this.setPID();
				
		//Create directory in the current directory with the PID name to hold results	
		//String file =workingdirectory+"\\"+this.getPID();	
		String file1 =workingdirectory+"/"+this.getPID();	
		//String file2 =workingdirectory+"\\resources";
		String file2 =workingdirectory+"/lib";
		
			
		System.out.println("The file directory for temp folder and output file is: "+file1);
		System.out.println("The file directory for resources is: "+file2);
				
		//File directory = new File(file);
		File directory = new File(file1);
				
		if (!directory.exists()) {
			if (directory.mkdir()) {
				System.out.println("Temporary Folder: "+directory+" is created to hold intermediate files.");
				//hide(directory);
			 } else {
				System.err.println("Failed to create temporary folder to hold intermediate files.");
			 }
		}
		
		//rename read files and put them in directory
		String [] inputFile;
		if (this.getPairedEnd() ==0 )
			{
			//String readFile = file+"\\tnt_1_in";
			  String readFile = file1+"/tnt_1_in";
			  inputFile = this.getInputFile();
			 //this.copyFile (workingdirectory+"\\"+inputFile[0],readFile);
			 this.copyFile (inputFile[0],readFile);
			}
		else {
			  //String readFile1 = file+"\\tnt_1_in";
			  //String readFile2 = file+"\\tnt_2_in";
			  String readFile1 = file1+"/tnt_1_in";
			  String readFile2 = file1+"/tnt_2_in";
			  inputFile = this.getInputFile();
			  //this.copyFile (workingdirectory+"\\"+inputFile[0],readFile1);
			  //this.copyFile (workingdirectory+"\\"+inputFile[1],readFile2);
			  this.copyFile (inputFile[0],readFile1);
			  this.copyFile (inputFile[1],readFile2);
		 }		

		//String refGenFile =  file+"\\tnt_5_"+this.getPID();
		String refGenFile =  file1+"/tnt_5_"+this.getPID();
		//this.copyFile (workingdirectory+"\\"+this.getReferenceFile(),refGenFile);
		this.copyFile (this.getReferenceFile(),refGenFile);
		
		//prepare reference genome for search
		//this.formatDB (file, refGenFile,workingdirectory);
		this.formatDB (file1, refGenFile,file2);
		
		// Run preProcessor on read file(s)
		System.out.println("PreProcessing ...");
				
		//int preProcessorValue=this.runPreProcessor (workingdirectory, file);
		int preProcessorValue=this.runPreProcessor (file2, file1);
		if (preProcessorValue <0)
		{
			System.err.println("Pre-processing files is not completed. ExitValue: "+preProcessorValue);
		}
		
		//get number of split files
		//String splitFile =file+"\\tnt_0_"+this.getPID();
		String splitFile =file1+"/tnt_0_"+this.getPID();
		this.setSplits (splitFile);
		
		//System.out.println("There are "+tanoti.getSplits()+" splits in the file: "+splitFile);
				
		this.setNumOfSplit(1);
		
		//Run Tanoti (Blast, PostProcessor, Assembler)
		//int tanotiValue = this.runTanotiProcess (workingdirectory);
		int tanotiValue = this.runTanotiProcess (workingdirectory,file2);
		if (tanotiValue <0)
		{
			System.err.println("Running Tanoti is not completed. ExitValue: "+tanotiValue);
		}
		        
		//sum results in one file
		this.printallToFile(workingdirectory);
		//this.printallToFile(file);
		
		//add comments to output file
		System.out.println("Generating SAM file.");
		
		//this.getOutput(workingdirectory, this.getOutputFile(), args);
		this.getOutput(workingdirectory, args);
		
		//delete temp folder upon user's request
		if(this.getTempFile()==0)
		{
			this.deleteTempFolder(directory);
		 }		
	}
	
	public int getPairedEnd() {
		return this.pairedEnd;
	}

	public int getSplits(){
		return this.splits;
	}
	
	public String[] getInputFile() {
		return this.inputFile;
	}
	
	public String getReferenceFile() {
		return this.referenceFile;
	}
	
	public int getProc(){
		return this.proc;
	}
	
	public int getMatchValue() {
		return this.matchValue;
	}
	
	public int getIdentity() {
		return this.IDENTITY;
	}
	
	public int getUnmapped() {
		return this.unmapped;
	}
	public long getPID() {
	    return this.PID;
	}
	
	public int getNumOfSplit() {
		return this.numOfSplit;
	}
	
	public String getOutputFile(){
		return this.outputFile;
	}
	
	public int getTempFile(){
		return this.tempFile;
	}
	public void setPID(){
		String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
	    this.PID= Long.parseLong(processName.split("@")[0]);
	}
	
	public  void setSplits (String splitFile) {
		try {
	 		  BufferedReader   bf = new BufferedReader(new FileReader(splitFile));
		      String line = bf.readLine();
		      bf.close();
			  this.splits = Integer.parseInt(line);			  
			}catch (Exception ex)  {
	        	ex.printStackTrace();
	        } 
   }
	
public void setNumOfSplit(int num){
		this.numOfSplit = num;
}

/*private void hide(File src)  {
    // win32 command line variant
    Process p;
	try {
		p = Runtime.getRuntime().exec("attrib +h " + src.getPath());
		 p.waitFor(); // p.waitFor() important, so that the file really appears as hidden immediately after function exit.
	} catch (IOException e) {
		System.err.println("Cannot hide the folder "+src.getPath());
		e.printStackTrace();
	} catch (InterruptedException e) {
		System.err.println("Creating a hidden directory is interrrupted");
        e.printStackTrace();
		
	}
   
}*/
    
 private  int runTanotiProcess (String wkDir, String libDir) {			
    	int exitVal = -1;
    	
    	/*This code is to take care of the case in which the num of splits 
    	 * is less than the number of processes
    	 */
    	if (this.getSplits() < this.getProc())
    		this.proc = this.numOfSplit;
    	
    	int batch = this.getSplits()/this.getProc();
 		int lastbatch = this.getSplits()%this.getProc();
 		
 		//System.out.println ("There are "+batch+" batches of 8 threads and 1 batch of "+lastbatch+" threads to run.");
 		
 		int flag=1;
 		int index=0;
 		int i = this.getNumOfSplit();
 		while(i <= getSplits())	{
 			if (flag == batch && lastbatch != 0)
 			{
 				index=lastbatch;
 			}
 			else
 			{
 				index=getProc();
 			}
 			
 			//blasting		
 			//int blastVal = blastRun (wkDir,index);
 			int blastVal = blastRun (wkDir,index,libDir);
 			if (blastVal >= 0){
 				exitVal++;
 			}
 			else	
 			System.err.println("****** Blasting is not Finished at batch: "+i+" exitValue:"+blastVal);
 		  
 			//Post-Processor             
            int postProcVal = runPostProcessor(wkDir,index,libDir);
            if (postProcVal >= 0){
 				exitVal++;
 			}
 			else	
 			System.err.println("****** Post-processing is not Finished at batch: "+i+" exitValue:"+postProcVal);
 		  
            //Assembler
            int assmValue =runAssembler (wkDir, index,libDir);
            if (assmValue >= 0){
 				exitVal++;
 			}
 			else	
 			System.err.println("****** Post-processing is not Finished at batch: "+i+" exitValue:"+assmValue);
                       
            i = this.getNumOfSplit();
                  		
      		flag++;
      		
 		}//end-while 
 		
		return exitVal;
   }
	
	    
  private  int runPreProcessor (String wkDir, String file) {
    	int exitValue=-1;
   	    try{
			if(this.getPairedEnd() ==0){
				String readFile=file+"/tnt_1_in";
				 				
     			String preProcCmd =wkDir+"/tanoti_preprocessor.exe "+readFile+" 1";
         		
         		Thread task= new Thread(new ExecutorTask(preProcCmd,file));
         		task.start();
         		task.join();
         		exitValue++;
			}
			else{
				String [] readFile={file+"/tnt_1_in",file+"/tnt_2_in"};
				String preProcCmd [] = new String [2];
     			Thread task [] = new Thread [2];
     			for (int j=0; j<2; j++)	{
     				preProcCmd [j]=wkDir+"/tanoti_preprocessor.exe "+readFile[j]+" "+(j+1);
         			
         			task [j]= new Thread(new ExecutorTask(preProcCmd [j],file));
         			task[j].start();
     			}
     			
     			for(Thread t : task){
     				 t.join();
     				 exitValue++;
     			}
			}		 	   
	    }catch (InterruptedException e) {
	        System.err.println("Pre-processing is interrrupted");
	        Thread.currentThread().interrupt();
	        e.printStackTrace();
	    }	 
   	 return exitValue;
    }
    
 private  void formatDB (String file, String rfFile, String libDir)
     {
    	 try {
    		    String command= libDir+"/tan_formatdb -p F -i "+rfFile;
				Thread task = new Thread(new ExecutorTask(command,file));
				task.start();
						
				task.join();			    
				
				
				File logFile= new File(file,"formatdb.log");    
			    logFile.delete();
			} catch (InterruptedException e) {
				 System.err.println("database formatting is interrupted!");
		         e.printStackTrace();
	        }		
     }
	
private  void copyFile (String srcFile, String desFile)
    {
   	 	try {
   	 		  PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(desFile))); 
				
			  BufferedReader   bf = new BufferedReader(new FileReader(srcFile));
		      String line;
		     		    
		     	while ((line = bf.readLine()) != null) 
		     		{
		     			pw.println(line);
		     		}
		     		 bf.close();
				pw.flush();
				pw.close();				
			}catch (Exception ex) {
	        	ex.printStackTrace();
	        } 
    }

private void getTanotiParameters (String [] args)
	{
		if (args.length == 0)
		{
			usage();
			System.exit(0);
			//return;
		}
		
		ArrayList<String> parms = new ArrayList<String>(Arrays.asList(args));
		
		if(parms.contains("-h"))
		{
			usage();
			System.exit(0);		
			//return;
		}
		
		if (parms.contains("-t")){
			tempFile = Integer.parseInt(parms.get(parms.indexOf("-t")+1));	
			if (tempFile != 0 && tempFile != 1)
			{
				System.out.println("Wrong -t value "+ tempFile);
				System.exit(0);	
				//return;
			}
		} 
		else {
			tempFile = 0; //default value 
		}
		
		if (parms.contains("-p")){
			pairedEnd = Integer.parseInt(parms.get(parms.indexOf("-p")+1));		
			if (pairedEnd != 0 && pairedEnd != 1)
			{
				System.out.println("Wrong -p value");
				System.exit(0);	
				//return;
			}
		}
		else {
			pairedEnd = 0; //default value
		}
		
		if (parms.contains("-m")) {
			matchValue = Integer.parseInt(parms.get(parms.indexOf("-m")+1));
		}
		else {
			matchValue =50; //default value 
		}
		
		if (parms.contains("-u")) {
			unmapped = Integer.parseInt(parms.get(parms.indexOf("-u")+1));
		}
		else {
			unmapped =1; //default value 
		}
		
		if (parms.contains("-r")){
			referenceFile= parms.get(parms.indexOf("-r")+1);
		}
		else {
			System.out.println(" (-r) not found");
			System.exit(0);	
			//return;
		}
		
		if (parms.contains("-o")){
			outputFile= parms.get(parms.indexOf("-o")+1);
		}
		else {
			outputFile ="FinalAssembly.sam"; //default value 
		}
		
		if (parms.contains("-P")){
			proc= Integer.parseInt(parms.get(parms.indexOf("-P")+1));			
		}
		else if(!parms.contains("-P")&&pairedEnd == 0) {
			proc = 8; //default value for single-end read			
		}
		else{
			proc = 4; //default value for paired-end read				
		}
		
		if (parms.contains("-i")){
			if(pairedEnd == 0){
				inputFile = new String[1];
				inputFile [0] = parms.get(parms.indexOf("-i")+1);				
			}
			else{
				inputFile = new String[2];
				inputFile [0] = parms.get(parms.indexOf("-i")+1);
				inputFile [1] = parms.get(parms.indexOf("-i")+2);
				
				if(inputFile[1].isEmpty() || inputFile[1].contains("-"))
				{
					System.out.println("Wrong number of input files for paired-end");
					System.exit(0);	
					//return;
				}
			}
			
		}
		else {
			System.out.println(" (-i) not found");
			System.exit(0);	
			//return;			
		}
	}
	
private  void printTanotiParameters()
	{
		System.out.println("=========================");
		if(pairedEnd ==0){
			System.out.println("Pair-end: NO");
		}
		else {
			System.out.println("Pair-end: YES");
		}
		
		System.out.println("Minimum percentage of a match: "+matchValue);
		
		if(unmapped == 0){
			System.out.println("Include unaligned reads in the SAM file: NO");
		}
		else {
			System.out.println("Include unaligned reads in the SAM file: YES");
		}
		
		System.out.println("Reference: "+referenceFile);
		
		System.out.print("Input: ");
		for(int i=0; i<inputFile.length;i++)
			System.out.print(inputFile[i]+" ");
		System.out.println();
		
		System.out.println("Output: "+ outputFile);		
		System.out.println("=========================");
	}
	
	/*helper Method*/
 private  void usage ()
	{
		System.out.println("Usage (Single-end): java Tanoti -r reference -i input1.fq -o output.sam");
		System.out.println("Usage (Paired-end): java Tanoti -r reference -i input1.fq input2.fq -p 1 -o output.sam");
		System.out.println();
		
		System.out.println("Optional parameters:");
		System.out.println("-m : Minimum match percentage of a read (default 5)");
		System.out.println("-u : Include unmapped reads in the output 1/0 (default 0)");
		System.out.println("-t : Keep temporary files");
		System.out.println("-P : Number of parallel BLAST searches (default 4/single-end and 8/paired-end. Don't change this value if you are unsure of it");
		System.out.println("-h : Help");		
	}
    
 private int blastRun (String wkDir, int index, String libDir) {
    	int exitVal = -1;
    	int jobs=0;
    	try {
    		    String file = wkDir+"/"+this.getPID();
    		    String blastCmd1 [] = new String [index];
    		    Thread task1 [] = new Thread [index];
    		    
				String blastCmd2 [] = new String [index];
				Thread task2 [] = new Thread [index];
    		   
    		    int i =this.getNumOfSplit();
    		    for (int j=0; j<index; j++)
				{
					if(i<= this.getSplits())
					{
						/*blastCmd1[j] = wkDir+"\\tan_blast -p blastn -e 0.001 -F F -d "+
					                   file+"\\tnt_5_"+this.getPID()+" -i "+file+"\\tnt_1_3_"+
								       this.getPID()+"_"+i+" -o "+file+"\\tnt_1_6_"+this.getPID()+
								       "_"+i+" -m 7 ";*/
						blastCmd1[j] = libDir+"/tan_blast -p blastn -e 0.001 -F F -d "+
				                   file+"/tnt_5_"+this.getPID()+" -i "+file+"/tnt_1_3_"+
							       this.getPID()+"_"+i+" -o "+file+"/tnt_1_6_"+this.getPID()+
							       "_"+i+" -m 7 ";
						
						System.out.println("Mapping...("+i+" of "+this.getSplits()+")");
						
					    task1[j] = new Thread(new ExecutorTask(blastCmd1[j] ,file));
						task1[j].start();
						
						if(this.getPairedEnd()==1){
							/*blastCmd2 [j]= wkDir+"\\tan_blast -p blastn -e 0.001 -F F -d "+
					                   file+"\\tnt_5_"+this.getPID()+" -i "+file+"\\tnt_2_3_"+PID+"_"+
									   i+" -o "+file+"\\tnt_2_6_"+this.getPID()+"_"+i+" -m 7 ";*/
							blastCmd2 [j]= libDir+"/tan_blast -p blastn -e 0.001 -F F -d "+
					                   file+"/tnt_5_"+this.getPID()+" -i "+file+"/tnt_2_3_"+PID+"_"+
									   i+" -o "+file+"/tnt_2_6_"+this.getPID()+"_"+i+" -m 7 ";
							
						    task2[j]= new Thread(new ExecutorTask(blastCmd2[j] ,file));
						    task2[j].start();
						}
											
					if(j!=index)
 					 {
 						i++; 						
 						jobs++;
 					 }   
					}								    
				}
				this.setNumOfSplit(i-jobs);
				for(Thread t:task1){
					t.join(); exitVal++;
				}
				
				if(this.getPairedEnd()==1){
					for(Thread t:task2){
						t.join(); exitVal++;
					}
				}
				
			}catch (InterruptedException e) {
				 System.err.println("blasting is interrupted!");
		         e.printStackTrace();
	        }    	
    	return exitVal;			
    }
    
 private int runPostProcessor (String wkDir, int index, String libDir )
    {
    	int jobs =0;
    	int exitVal = -1;
    	try {
    		String file = wkDir+"/"+PID;
  			
    		String postCmd1 [] = new String [index];
		    Thread task1 [] = new Thread [index];
		    
			String postCmd2 [] = new String [index];
			Thread task2 [] = new Thread [index];
    		
  			int i=this.getNumOfSplit();
  			
  			for (int j=0; j<index; j++)
  			{
  				if(i<=this.getSplits())
  				{
  					/*postCmd1[j]=wkDir+"\\tanoti_postprocessor.exe "+file+"\\tnt_1_6_"+
  				                    this.getPID()+"_"+i+" "+this.getMatchValue()+" "+1+" "+
  							        i+"  "+this.getIdentity();*/
  					
  					postCmd1[j]=libDir+"/tanoti_postprocessor.exe "+file+"/tnt_1_6_"+
			                    this.getPID()+"_"+i+" "+this.getMatchValue()+" "+1+" "+
						        i+"  "+this.getIdentity();
  					
  					System.out.println("Post-Processing...("+i+" of "+this.getSplits()+")");					
  					
      				task1[j] = new Thread(new ExecutorTask(postCmd1[j] ,file));
      				task1[j].start();
      				
      				if(this.getPairedEnd() == 1) {
      				     /*postCmd2 [j] =wkDir+"\\tanoti_postprocessor.exe "+file+"\\tnt_2_6_"+
      				                 this.getPID()+"_"+i+" "+this.getMatchValue()+" "+2+" "+
      						         i+"  "+this.getIdentity();*/
      					
      					postCmd2 [j] =libDir+"/tanoti_postprocessor.exe "+file+"/tnt_2_6_"+
 				                 this.getPID()+"_"+i+" "+this.getMatchValue()+" "+2+" "+
 						         i+"  "+this.getIdentity();
      				     
      				     task2 [j]= new Thread(new ExecutorTask(postCmd2[j] ,file));
  				         task2[j].start();
      				}
      				      				
      				if(j!=index)
      				  {
      					i++;
      					jobs++;
					  }   
      			}
  			}
  			
  			this.setNumOfSplit(i-jobs);
  			  			
  			for(Thread t:task1){
				t.join(); exitVal++;
			}
			
			if(this.getPairedEnd()==1){
				for(Thread t:task2){
					t.join(); exitVal++;
				}
			}
 			
  			//System.out.println("Finished post Processing");
  		} catch (InterruptedException e) {
  			 System.err.println("Post_processing is interrupted!");
              e.printStackTrace();
        }    	
    	return exitVal;  		
    }
    
 private int runAssembler (String wkDir, int index, String libDir) {
    	
    	int exitVal = -1;
    	try {
  			
    		String file = wkDir+"/"+PID;
  			
    		String assmPro [] = new String [index];
		    Thread task [] = new Thread [index];
    		  			
            int i=this.getNumOfSplit();  			
  			for (int j=0; j<index; j++)
  			{
  				if(i<=this.getSplits())
  				{
  					if(this.getPairedEnd()==1){
  						String arg1=file+"/tnt_1_1_"+this.getPID()+"_"+i;
  				        String arg2=file+"/tnt_1_2_"+this.getPID()+"_"+i;
  				        String arg3=file+"/tnt_1_4_"+this.getPID()+"_"+i;
  				        String arg4=file+"/tnt_2_1_"+this.getPID()+"_"+i;
  				        String arg5=file+"/tnt_2_2_"+this.getPID()+"_"+i;
  				        String arg6=file+"/tnt_2_4_"+this.getPID()+"_"+i;
  				        String arg7=file+"/tnt_1_7_"+this.getPID()+"_"+i;
  				        String arg8=file+"/tnt_2_7_"+this.getPID()+"_"+i;
  				   		/*assmPro [j] =wkDir+"\\TanotiAssembler2.exe  "+
  		   		                     arg1+"  "+arg2+"  "+arg3+"  "+arg4+"  "+
  		   		                     arg5+"  "+arg6+"  "+arg7+"  "+arg8+"  "+
  				   				     this.getUnmapped()+"  "+this.getPID()+"  "+i; */
  				        
  				      assmPro [j] =libDir+"/TanotiAssembler2.exe  "+
	   		                     arg1+"  "+arg2+"  "+arg3+"  "+arg4+"  "+
	   		                     arg5+"  "+arg6+"  "+arg7+"  "+arg8+"  "+
			   				     this.getUnmapped()+"  "+this.getPID()+"  "+i; 
  					}
  					else{
  						String arg1=file+"/tnt_1_1_"+this.getPID()+"_"+i;
  				        String arg2=file+"/tnt_1_2_"+this.getPID()+"_"+i;
  				        String arg3=file+"/tnt_1_4_"+this.getPID()+"_"+i;
  				        String arg4=file+"/tnt_1_7_"+this.getPID()+"_"+i;
  				        /*assmPro [j] =wkDir+"\\Assembler1.exe  "+
  		   		                     arg1+"  "+arg2+"  "+arg3+"  "+arg4+"  "+
  		   		                     this.getUnmapped()+"  "+this.getPID()+"  "+i;*/
  				      assmPro [j] =libDir+"/Assembler1.exe  "+
	   		                     arg1+"  "+arg2+"  "+arg3+"  "+arg4+"  "+
	   		                     this.getUnmapped()+"  "+this.getPID()+"  "+i;
  					}
  				
		   		    System.out.println("Assembling...("+i+" of "+splits+")");
		   		    
				    task[j] = new Thread(new ExecutorTask(assmPro[j] ,file));
				    task[j].start();
				
				if(j!=index)
				{			
					i++;
				}   
			}		
		}		
		this.setNumOfSplit(i);
		  			
		for(Thread t:task){
			t.join(); exitVal++;
		}
		
  	}catch (InterruptedException e) {
  			 System.err.println("Assembling is interrupted!");
              e.printStackTrace();
     }
    	return exitVal;
    }
    
 private  void deleteTempFolder(File dir)
    {
	 
	   System.out.println("Removing temporary files...."); 
   	     
        String[] files;    
        if(dir.isDirectory()){
            files = dir.list();
            for (int i=0; i<files.length; i++) {
               File aFile = new File(dir,files[i]); 
               aFile.delete();
             }
        }
        dir.delete();
	  
        //For testing purposes
	   /*String[] files;    
       if(dir.isDirectory()){
           files = dir.list();
           for (int i=0; i<files.length; i++) {
              File aFile = new File(dir,files[i]); 
              
              if(aFile.delete()) {
            	 System.out.println(aFile.getName() + " is deleted!");
              }else{
            	  System.out.println("Delete operation is failed for "+ aFile.getName());
              }
             
            }
       }
       dir.delete();*/
	 
    }
    
    private  void printallToFile (String dir)
		{
    	  String input =dir+"/"+this.getPID()+"/tnt_9_"+this.getPID()+"_";
		  String output = dir+"/"+this.getPID()+"/tnt_9_"+this.getPID();
			
		   try {
				
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(output))); 
								
				//print results from blast to the file
				for (int i=1; i<=this.getSplits();i++)
				{
					String fileName = input+i;
					BufferedReader   bf = new BufferedReader(new FileReader(fileName));
		     		String line;
		     		    
		     		 while ((line = bf.readLine()) != null) 
		     		 {
		     			pw.println(line);
		     		 }
		     		 bf.close();				}
				pw.flush();
				pw.close();				
			}catch (Exception ex){
	        	ex.printStackTrace();
	        } 
	}
   
   /* private  void getOutput (String dir, String outputFile, String [] cl){
    	
	    String comment = getComment(cl); 
		String input =dir+"\\"+this.getPID()+"\\tnt_9_"+this.getPID();
		String output = dir+"\\"+outputFile;
			
		try {
			 PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(output))); 
				
			 
			 String firstComment =getFirstComment(dir);
			 if(firstComment!= null){ //only print this comment to the file if it is not null
				 //System.out.println("First Comment: "+firstComment);
				 pw.println(firstComment); 
			 }
			 				
			 //Second line in the file is the comment
			 pw.println(comment);
			
			 BufferedReader   bf = new BufferedReader(new FileReader(input));
		     String line;
		     		    
		     while ((line = bf.readLine()) != null) 
		     	{
		    	 pw.println(line);
		     	}
		     	
		      bf.close();				
			  pw.flush();
			  pw.close();				
		}catch (Exception ex) {
	        	ex.printStackTrace();
	    } 
	}
    */
    
private void getOutput (String dir, String [] cl){
    	
	    String comment = getComment(cl); 
		String input =dir+"/"+this.getPID()+"/tnt_9_"+this.getPID();
		String output = this.outputFile;
			
		try {
			 PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(output))); 
				
			 
			 String firstComment =getFirstComment(dir);
			 if(firstComment!= null){ //only print this comment to the file if it is not null
				 //System.out.println("First Comment: "+firstComment);
				 pw.println(firstComment); 
			 }
			 				
			 //Second line in the file is the comment
			 pw.println(comment);
			
			 BufferedReader   bf = new BufferedReader(new FileReader(input));
		     String line;
		     		    
		     while ((line = bf.readLine()) != null) 
		     	{
		    	 pw.println(line);
		     	}
		     	
		      bf.close();				
			  pw.flush();
			  pw.close();				
		}catch (Exception ex) {
	        	ex.printStackTrace();
	    } 
	}
   private String getFirstComment(String dir) {
    	String line = null;
    	BufferedReader   br = null;
    	
    	try {
			 
			 int i = 1;
			 while(i<this.splits){
			 
				/*First line in the file is the same line from the tnt_8_1<PID>
				 *Keep looking for the line in any of these files. Once found exit.
				 *If not found, line is going to be null hence no comment should be printed 
				 *in the SAM file*/
				 
				   br = new BufferedReader(new FileReader(dir+"/"+this.getPID()+
						               "/tnt_8_1"+i+this.getPID()));
				 line = br.readLine();
				 //System.out.println("Line["+i+"]: "+line);
				 if (line != null)
				    break;
				  i++;
				  br.close();				  
			 }
						
		}catch (Exception ex) {
	        	ex.printStackTrace();
	    } 
    	
    	//to ensure closing opened files
    	finally{
    		try {
    			if(br != null)
    				br.close();
    		} catch (IOException ex) {
    			System.out.println("IO error closing file: "+ ex.getMessage());
    		}
    		//return; this line might be needed 
    	}
    	
    	//System.out.println("Returing Comment line: "+line);
    	return line;
    }
		
 private  String getComment(String [] cl) {
    	//print current time stamp
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd- HH:mm:ss").format(new Date());
		//print current directory
		String workingdirectory = System.getProperty("user.dir");
		//print command line 
		String comment="@CO\tID:tanoti\tTM:"+timeStamp+"\tWD:"+workingdirectory+"\tCL:";
		for (String s: cl)
			comment =comment+s+"\t";
		//print host name
		String host = System.getenv("COMPUTERNAME");
		comment= comment+"HT:"+host;
		
		//get the user name
		Process process;		
		try {
			process = new ProcessBuilder("whoami").start();
			InputStream is = process.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);
		    String line ;	 
		    while ((line = br.readLine()) != null) {
		       comment= comment+"\tUN:"+line;
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}			
		return ( comment);
    }
}
