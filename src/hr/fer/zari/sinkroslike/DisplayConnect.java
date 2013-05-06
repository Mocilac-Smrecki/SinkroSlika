package hr.fer.zari.sinkroslike;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class DisplayConnect{

	private Handler handler = new Handler();
	TextView textView;
	
	
	protected void connectToInternet(String ip, String port)
	{
	    ClientZaAndroid c = new ClientZaAndroid("dretvaKonekcija", ip, port);
	    new Thread(c).start();
	}

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
				handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(serverMessage);
                    }
                });
				Log.i(serverMessage, serverMessage);
				
				while(ide && socket.isConnected() && !socket.isClosed()){

					
					if((serverMessage = inputMessage.readLine())!= null){
						handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(serverMessage);
                            }
                        });
						Log.i(serverMessage, serverMessage);
					}
				}
				Log.i("kraj", "aaaaaaaaaaaa");
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	

		}

	}


}
