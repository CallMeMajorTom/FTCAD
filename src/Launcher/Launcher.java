package Launcher;
//import server.ClientConnection;
import java.io.*;  
import java.util.*;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.*;
import org.apache.commons.collections.*;
import org.apache.commons.lang.*;

import java.lang.Process;
import java.lang.ProcessBuilder;


public class Launcher{
	public static void main(String[] args) throws ConfigurationException, IOException{
	        XMLConfiguration conf = new XMLConfiguration("configuration.xml");
	        Process P = null;
	        for(int i = 0;i < 4; i++){
		        if(conf.getString("databases.database("+i+").role").equals("server"))
		        	P = new ProcessBuilder("java","-jar","server.jar",conf.getString("databases.database("+i+").port")).start();
		        else if(conf.getString("databases.database("+i+").role").equals("client"))
		        	P = new ProcessBuilder("java","-jar","client.jar",conf.getString("databases.database("+i+").ip"),conf.getString("databases.database("+i+").port"),conf.getString("databases.database("+i+").username")).start();
		        else if(conf.getString("databases.database("+i+").role").equals("frontend"))
		        	P = new ProcessBuilder("java","-jar","frontend.jar",conf.getString("databases.database("+i+").port")).start();
	        }
	}
}