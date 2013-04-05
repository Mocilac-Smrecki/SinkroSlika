package hr.fer.zari.sinkroslike;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class NetworkTask extends AsyncTask<Void, byte[], Boolean>{
	protected static final String TAG = null;
	Socket client;
	InputStream input;
	OutputStream output;
	@Override
	protected void onPreExecute() {
		Log.i("AsyncTask", "onPreExecute");
	}
	
	@Override
    protected Boolean doInBackground(Void... params) { //This runs on a different thread
        boolean result = false;
        try {
            Log.i("AsyncTask", "doInBackground: Creating socket");
            SocketAddress sockaddr = new InetSocketAddress("10.0.2.2", 8045);
            client = new Socket();
            client.connect(sockaddr, 10000); //10 second connection timeout
            if (client.isConnected()) { 
                input = client.getInputStream();
                output = client.getOutputStream();
                Log.i("AsyncTask", "doInBackground: Socket created, streams assigned");
                Log.i("AsyncTask", "doInBackground: Waiting for inital data...");
                byte[] buffer = new byte[4096];
                int read = input.read(buffer, 0, 4096); //This is blocking
                while(read != -1){
                    byte[] tempdata = new byte[read];
                    System.arraycopy(buffer, 0, tempdata, 0, read);
                    publishProgress(tempdata);
                    Log.i("AsyncTask", "doInBackground: Got some data");
                    read = input.read(buffer, 0, 4096); //This is blocking
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("AsyncTask", "doInBackground: IOException");
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("AsyncTask", "doInBackground: Exception");
            result = true;
        } finally {
            try {
                input.close();
                output.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("AsyncTask", "doInBackground: Finished");
        }
        return result;
    }
	
	public void SendDataToNetwork(String cmd) { //You run this from the main thread.
        try {
            if (client.isConnected()) {
                Log.i("AsyncTask", "SendDataToNetwork: Writing received message to socket");
                output.write(cmd.getBytes());
            } else {
                Log.i("AsyncTask", "SendDataToNetwork: Cannot send message. Socket is closed");
            }
        } catch (Exception e) {
            Log.i("AsyncTask", "SendDataToNetwork: Message send failed. Caught an exception");
        }
    }
	
	/*
	public boolean SendDataToNetwork(final String cmd) { //You run this from the main thread.
            waitForSocketToConnect();  	
            if (client.isConnected()) 
            {
                Log.i("AsyncTask", "SendDataToNetwork: Writing received message to socket");
                new Thread(new Runnable()
                {
                	public void run()
                	{
                		try
                		{
                			output.write(cmd.getBytes());
                		}
                		catch (Exception e)
                		{
                			e.printStackTrace();
                			Log.i(TAG, "SendDataToNetwork: Cannot send message. Caught an exception.");
                		}
                	}
                }).start();
                return true;
            }
            else
            {
            	Log.i(TAG, "SendDataToNetwork: Cannot send message. Socket is closed");
            } 
        	return false; 
	}

	
	public boolean waitForSocketToConnect()
    {
        // immediately return if socket is already open
        if (_bSocketStarted)
        {
            return true;
        }

        // Wait until socket is open and ready to use
        int count = 0;
        while (!_bSocketStarted && count < TOO_MUCH_TIME)
        {
            try
            {
                Thread.sleep(HALF_SECOND);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            count += HALF_SECOND;
        }

        return _bSocketStarted;
    }
	*/
    protected void onProgressUpdate(byte[]... values) {
        if (values.length > 0) {
            Log.i("AsyncTask", "onProgressUpdate: " + values[0].length + " bytes received.");
            //textStatus.setText(new String(values[0]));
        }
    }
	
    @Override
    protected void onCancelled() {
        Log.i("AsyncTask", "Cancelled.");
        //btnStart.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.i("AsyncTask", "onPostExecute: Completed with an Error.");
            //textStatus.setText("There was a connection error.");
        } else {
            Log.i("AsyncTask", "onPostExecute: Completed.");
        }
        //btnStart.setVisibility(View.VISIBLE);
    }

}
