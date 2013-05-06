package hr.fer.zari.sinkroslike;

import java.io.*;
import java.net.*;


public class ClientZaAndroid extends Thread {

    Socket socket;
    private String serverIP; 
    private Integer serverPort;
    private PrintWriter outputMessage = null;
    private BufferedReader inputMessage = null;
    private String serverMessage;
    private Boolean ide = true;

	public ClientZaAndroid(String threadName, String ip, String port){
		super(threadName);
		serverIP = ip;
		serverPort = Integer.parseInt(port);
	}
	
    public void sendMessage(String message){
        if (outputMessage != null && !outputMessage.checkError()) {
        	outputMessage.println(message);
        	outputMessage.flush();
        }
    }
 
    public void stopClient(){
        ide = false;
    }
	
	
	public void run(){
		
		try {
			socket = new Socket(serverIP, serverPort);
			outputMessage = new PrintWriter(socket.getOutputStream(), true);
			inputMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			serverMessage = inputMessage.readLine();

			
			while(ide){
				serverMessage = inputMessage.readLine();//napravi nesto s tim
				System.out.println(serverMessage);
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
