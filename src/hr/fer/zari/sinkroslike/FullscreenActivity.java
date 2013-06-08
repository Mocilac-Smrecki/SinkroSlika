package hr.fer.zari.sinkroslike;

import hr.fer.zari.sinkroslike.util.SystemUiHider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 10000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
	
	private TextView text;					    //prikazuje poruke sa servera
	private Button send;						//gumb za spajanje na server
	private Button settings;					//gumb za postavljanje IP adrese servera
	private int numOfDevices = 0;
	private int DeviceNumber = 0;

	private boolean returnFromRight = false;	//oznaèava da je došlo do promjene smjera s desna na lijevo
	private boolean returnFromLeft = false;		//oznaèava da je došlo do promjene smjera s lijeva na desno
	private boolean sentNext = false;			//oznaèava da je poslana poruka iduæem mobitelu da pokrene patkicu (kada je smjer kretanja s lijeva na desno)
	private boolean sentPrevious = false;		//oznaèava da je poslana poruka prethodnom mobitelu da pokrene patkicu (kada je smjer kretanja s desna na lijevo)
	private String smjer = "right";
	private boolean zoomDuck = false;			//oznaèava da patkicu treba uveæati
	private boolean initX = false;				//oznaèava da je poèetna koordinata patkice inicijalizirana
	
	private ClientZaAndroid c;
	private String ipAdress = "89.201.215.48";
	private String port = "9090";
	
	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	private Handler handler = new Handler();		//razred Handler služi za slanje poruka izmeðu dretvi
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		LinearLayout contentView = (LinearLayout) findViewById(R.id.fullscreen_content);
		text = (TextView) findViewById(R.id.textView1);
		send = (Button) findViewById(R.id.send); 
		settings = (Button) findViewById (R.id.settings);	
		DuckFloating duck = new DuckFloating (this);
		contentView.addView(duck);
		
		
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});
		
		
		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});
		
	

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		
		//klikom na gumb send stvori novu dretvu klijenta koja æe komunicirati sa serverom
		send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				c = new ClientZaAndroid("dretvaKonekcija", ipAdress, port);
			    new Thread(c).start();
			    send.setEnabled(false);
			}
		});
		
		//gumb za unos IP adrese servera
		settings.setOnClickListener(new View.OnClickListener() {
			
			
			@Override
			public void onClick(View v) {
				
				AlertDialog.Builder alert = new AlertDialog.Builder(FullscreenActivity.this);
				alert.setTitle("Server IP adresa");
				
				final EditText input = new EditText(FullscreenActivity.this);
				alert.setView(input);
				input.setText(ipAdress);
				alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						ipAdress = input.getText().toString();
						
					}
				});
				
				alert.setCancelable(false);
				
				alert.show();
				
			}
		});
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
				
			return false;
		}
		
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
    }
	
	
	class DuckFloating extends View {
		int screenW;
		int screenH;
		int X;
		int Y;
		int initialY;
		int initialX;
		int duckW;
		int duckH;
		float angle;
		double dY;
		double dX;
		float acc;
		Bitmap duck, bgr;
		boolean maxAngle = false;			//oznaèava da je dosegnut maksimalni kut do kojega se patkica može rotirati
		boolean scaleDuck = false;			//oznaèava da patkica treba promjeniti smjer kretanja (tj. da treba primjeniti matricu skaliranja)
		
		public DuckFloating(Context context) {
	        super(context);
	        duck = BitmapFactory.decodeResource(getResources(),R.drawable.duck); //uèitaj sliku patkice
	        bgr = BitmapFactory.decodeResource(getResources(),R.drawable.river); //uèitaj pozadinsku sliku
	        duckW = duck.getWidth();
	        duckH = duck.getHeight();
	        initialX = - (duckW + 4);
	        angle = 0; //poèetni kut rotacije.
	        dX = 1;	   //pomak po osi X
	    }

	    @Override
	    public void onSizeChanged (int w, int h, int oldw, int oldh) {
	        super.onSizeChanged(w, h, oldw, oldh);
	        screenW = w;
	        screenH = h;
	        bgr = Bitmap.createScaledBitmap(bgr, w, h, true); //prikaži pozadinu preko cijelog zaslona
	        X = initialX;
	        Y = screenH - duckH - duckH/2;		//Y koordinata patkice; ne mijenja se tijekom izvoðenja programa
	    }

	    @Override
	    public void onDraw(Canvas canvas) {
	        super.onDraw(canvas);

	        //Nacrtaj pozadinu.
	        canvas.drawBitmap(bgr, 0, 0, null);
	        
	        if (DeviceNumber == 0)
	        {
	        	X = (- duckW) - 2;
	        }
	        
	        else if (DeviceNumber == 1 && initX == false)
	        {
	        	X = 0;
	        	initX = true;
	        	c.sendMessage("right");
	        }
	        else if (DeviceNumber > 1 && initX == false)
	        {
	        	X = - duckW - 2;
	        	dX = (-1)*dX;
	        	smjer = "left";
	        	scaleDuck = !scaleDuck;
	        	initX = true;
	        }
	        
	        X += (int) dX;
   
	        // ako je patkica doplivala do desnog ruba zaslona, a mobitel nije krajnje desni
	        if (X > (screenW - duckW) && DeviceNumber < numOfDevices && sentNext == false && smjer.equals("right"))
	        {
	        	Integer nextDev = DeviceNumber + 1;
        		String nextDevice = nextDev.toString();
        		c.sendMessage(nextDevice);				//pošalji poruku s rednim brojem iduæeg mobitela
        		sentNext = true;
	        }
	        //ako je patkica otplivala izvan desnog ruba zaslona, a mobitel nije krajnji desni
	        else if (X > screenW + 4 && DeviceNumber < numOfDevices && smjer.equals("right"))
	        {
	        	X -= (int) dX; /*zadrži patkicu na istoj poziciji tako da u svakoj iteraciji umanjiš X koordinatu za pomak dX. (buduæi da se u svakoj iteraciji X
	        	 				koordinata najprije uveæava za dX, a na ovom mjestu se smanjuje za isti iznos, to æe za posljedicu imati da se patkica neæe kretati*/
	        	if (returnFromRight == true)
	        	{
	        		dX = (-1)*dX;
	        		scaleDuck = !scaleDuck;
	        		sentNext = false;
	        		smjer = "left";
	        		returnFromRight = false;
	        		
	        	}
	        }
	        //ako je mobitel krajnje desni i patkica je doplivala do desnog ruba zaslona, okreni ju na drugu stranu
	        else if (DeviceNumber == numOfDevices && X > (screenW - duckW))
	        {
	        	dX = (-1)*dX;
	        	scaleDuck = !scaleDuck;
	        	smjer = "left";
	        	c.sendMessage("left");
	        }
	        
	        
	        // kretanje u lijevo
	        
	        //ako je patkica otplivala izvan lijevog ruba zaslona, a mobitel nije krajnje lijevi
	        if (X < -duckW - 4 && DeviceNumber > 1 && smjer.equals("left"))
	        {
	        	X -= (int) dX;	//zadrži patkicu na istom mjestu po istoj logici kao i nekoliko redaka iznad
	        	if (returnFromLeft == true)
	        	{
	        		dX = (-1)*dX;
	        		scaleDuck = !scaleDuck;
	        		sentPrevious = false;
	        		smjer = "right";
	        		returnFromLeft = false;
	        		
	        	}
	        }
	        //ako je patkica doplivala do lijevog ruba zaslona, a mobitel nije krajnje lijevi
	        else if (X < 1 && DeviceNumber > 1 && sentPrevious == false && smjer.equals("left"))
	        {
	        	Integer previousDev = DeviceNumber - 1;
        		String prevDevice = previousDev.toString();
        		c.sendMessage(prevDevice);			//pošalji poruku s rednim brojem iduæeg mobitela na kojemu se patkica treba kretati
        		sentPrevious = true;
	        }
	        
	        //ako je patkica doplivala do lijevog ruba zaslona i mobitel je krajnje lijevi, okreni patkicu
	        else if (DeviceNumber == 1 && X < 0)
	        {
	        	dX = (-1)*dX;
	        	scaleDuck = !scaleDuck;
	        	smjer = "right";
	        	c.sendMessage("right");
	        }
	        
     
	        //Mijenjaj kut rotacije
	        if (angle > 15) maxAngle = true;
	        if (maxAngle == false) angle += 0.5;
	        else angle -= 0.5;
	        if (angle < -10) maxAngle = false;
	        
	        
	        canvas.save(); //Spremi trenutnu matricu na stog
	        if (scaleDuck == true)
	        	{
	        	canvas.scale(-1.0f, 1.0f, X + (duckW /2), Y + (duckH /2));	 
	        	}
	        if (zoomDuck == true)
	        {
	        	canvas.scale(1.5f, 1.5f, X + (duckW /2), Y + (duckH /2));
	        	zoomDuck = false;
	        }
	        canvas.rotate(angle, X + (duckW / 2), Y + (duckH / 2)); //Rotiraj patkicu
	        canvas.drawBitmap(duck, X, Y, null); //Nacrtaj patkicu na koordinatama X, Y
	        canvas.restore(); //Poništi sve transformacije nad trenutnom matricom

	        //Nacrtaj iduæu iteraciju
	        invalidate();
	    }
		
	}
	
	// ******************* spajanje na server ************************ 

	public class ClientZaAndroid extends Thread {

	    Socket socket;
	    private String serverIP; 
	    private Integer serverPort;
	    private PrintWriter outputMessage = null;
	    private BufferedReader inputMessage = null;
	    private String serverMessage;
	    private Boolean ide = true;				//oznaèava da je dretva klijenta pokrenuta

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
				outputMessage = new PrintWriter(socket.getOutputStream(), true);					//za slanje poruka na server
				inputMessage = new BufferedReader(new InputStreamReader(socket.getInputStream()));	//za primanje poruka sa servera
				
				serverMessage = inputMessage.readLine();
				
				handler.post(new Runnable() {
                    @Override
                    public void run() {
                    	String DeviceNum = serverMessage.split(",")[0];
                    	String numDevice = serverMessage.split(",")[1];
                    	DeviceNumber = Integer.parseInt(DeviceNum);
                    	numOfDevices = Integer.parseInt(numDevice);
                    	text.setText(serverMessage);
                    	
                    }
                });
				Log.i(serverMessage, serverMessage);
				
				while(ide && socket.isConnected() && !socket.isClosed()){

					
					if((serverMessage = inputMessage.readLine())!= null){
						
						handler.post(new Runnable() {
                            @Override
                            public void run() {
                            	
                            	if (serverMessage.equals("left")) returnFromRight = true;
                            	else if (serverMessage.equals("right")) returnFromLeft = true;
                            	else if (serverMessage.equals("signal")) zoomDuck = true;
                                text.setText(serverMessage);
                            }
                        });
						Log.i(serverMessage, serverMessage);
					}
				}
				Log.i("kraj", "end");
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	

		}

	}
	
}
