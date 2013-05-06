package hr.fer.zari.sinkroslike;
import java.util.ArrayList;


public class Sync extends Thread{
	ArrayList<ClientThread> threadList;
	private Boolean ide = true;
	private Integer i = 1;
	
	Sync(String threadName, ArrayList<ClientThread> threadList){
		super(threadName);
		this.threadList = threadList;
	}
	
	@SuppressWarnings("unused")
	private void stopSync(){
		ide = false;
	}
	
	public void run(){
		Integer cn = 1;
		try {
			sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(ClientThread t :threadList){
			t.sendMessage("connection: " + Integer.toString(cn++));
		}
		
		try {
			sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while(ide){
			for(ClientThread t :threadList){
				t.sendMessage(Integer.toString(i));
			}
			i++;
			try {
				sleep(100, 0); //sleep(long millis, int nanos)
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
