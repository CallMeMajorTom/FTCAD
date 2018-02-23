package Launcher;
//import server.ClientConnection;
import java.io.*;  
import java.util.*;
import java.lang.Process;
import java.lang.ProcessBuilder;


public class Launcher{
	private ArrayList<Process> plist = new ArrayList<Process>();
	public static void main(String[] args) throws Exception{
		Launcher instance = new Launcher();
	}
	private Launcher(){
		String file_name = "configuration";
		Properties properties = new Properties();  
/*      The process of establishment of configuration file.
 * 		properties.setProperty("port", "20251"); 
        properties.setProperty("role", "server"); 
        properties.storeToXML(new FileOutputStream("p:\\configuration0.xml"), "server");
        properties = new Properties(); 
        properties.setProperty("username", "David"); 
        properties.setProperty("port", "20251");
        properties.setProperty("ip", "127.0.0.1");
        properties.setProperty("role", "client");  
        properties.storeToXML(new FileOutputStream("p:\\configuration1.xml",true),"client1");
        properties.setProperty("username", "Peter");  
        properties.setProperty("port", "20251");
        properties.setProperty("ip", "127.0.0.1");
        properties.setProperty("role", "client");  
        properties.storeToXML(new FileOutputStream("p:\\configuration2.xml",true),"client2");
        properties.setProperty("username", "xiaotong"); 
        properties.setProperty("port", "20251");
        properties.setProperty("ip", "127.0.0.1");
        properties.setProperty("role", "client");  
        properties.storeToXML(new FileOutputStream("p:\\configuration3.xml",true),"client3"); */
        boolean alive = false;
    	Process p1, p2, p3, p4; 
		for(int i=0;i<4;i++){
            try {
				properties.loadFromXML(new FileInputStream(file_name+".xml"));
			} catch (Exception e) {
				e.printStackTrace();System.exit(-1);
			} 
            if(properties.getProperty("role").equalsIgnoreCase("server")){
            	try {
					plist.add(i,new ProcessBuilder("java","-jar","a17xiaji_tcp_server.jar",properties.getProperty("port")).start());
				} catch (IOException e) {
					e.printStackTrace();System.exit(-1);
				}
            }	
            else if(properties.getProperty("role").equalsIgnoreCase("client")){
            	try {
					plist.add(i,new ProcessBuilder("java","-jar","a17xiaji_tcp_client.jar",properties.getProperty("ip"),properties.getProperty("port"),properties.getProperty("username")).start());
				} catch (IOException e) {
					e.printStackTrace();System.exit(-1);
				}
            }	
            //alive = eof();
        }
		while(true){
			
		}
	}
}