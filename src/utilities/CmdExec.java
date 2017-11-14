package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/***
 * Runs Tanoti as subprocess when called from the ReferenceAssembly program in model package 
 * 
 * @author Maha Maabar
 *
 */
public class CmdExec extends Thread
{
	InputStream is;
	String type;
	OutputStream os;
	

public CmdExec() {
}

public CmdExec(InputStream is, String type, OutputStream redirect) {
	this.is =is;
	this.type = type;
	this.os = redirect;
}

public CmdExec(InputStream is, String type) {
	this (is,type,null);
}

public void run(){
	try{
		PrintWriter pw = null;
		if(os != null)
			pw = new PrintWriter(os);
		
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		while ((line=br.readLine()) !=null){
			if (pw != null)
				pw.println(line);			
		}
		if (pw != null)
			pw.flush();
	} catch (IOException ioe)
	{
		ioe.printStackTrace();
	}
		
  }

}

