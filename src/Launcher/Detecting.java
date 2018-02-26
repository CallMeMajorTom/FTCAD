/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Launcher;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author brom
 */
public class Detecting extends Thread{
	private Process P;
	private int position;
	public Detecting (Process p,int i){ P = p; position = i;}
	public void restart(int position) throws ConfigurationException, IOException{
		XMLConfiguration conf = new XMLConfiguration("configuration.xml");
        if(conf.getString("databases.database("+position+").role").equals("server")){
        	this.P = new ProcessBuilder("java","-jar","server.jar",conf.getString("databases.database("+position+").port")).start();
        }
        else if(conf.getString("databases.database("+position+").role").equals("frontend")){
        	this.P = new ProcessBuilder("java","-jar","frontend.jar",conf.getString("databases.database("+position+").port")).start();
        }
	}
    public void run(){
    	if(!P.isAlive()){
    		System.out.println("Process"+P.toString()+" Crashed");
    		try {
				this.restart(position);
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    };
}
