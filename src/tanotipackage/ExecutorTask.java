package tanotipackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
                   //System.out.println("\nError messages During runs:");
                   //System.out.println(errText);
                   PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file+"/log.txt",true))); 
                   //System.out.println("File: "+file);
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
     
 }//executorTask

