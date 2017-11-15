package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/***
 * Handles the execution of multi sub-processor; used by Tanoti program to execut blast and other C executables 
 * @author Maha Maabar
 *
 */
public class ExecutorTask implements Runnable {
		      
     String command=null;
     String file = null;
     
    public ExecutorTask(String cmd, String f)
     {
    	 command = cmd;
    	 file = f;
     }
     
     @Override
     public void run() {    	 
        
         Process process = null;

         try
            {
            	process = Runtime.getRuntime().exec(command, null, new File(file));
                
                String line;
               
                int count=0;
                String errText="";
              
                BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = stdErr.readLine()) != null) {
                    count++;
                    errText+=line+"\n";
                }
                stdErr.close();
                
                if(count>0) {
                   
                   PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file+"\\log.txt",true))); 
                   pw.println("\nError messages During runs:");
                   pw.println(errText);
                   
       			   pw.close();
                }
                
            }       
            catch (IOException ioe)
                {
            	System.out.println("\nRUN ERROR\n"+ioe);
            }            
     }     
 }

