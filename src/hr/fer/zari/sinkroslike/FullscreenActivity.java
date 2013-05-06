package hr.fer.zari.sinkroslike;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import hr.fer.zari.sinkroslike.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.AsyncTask;
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

	private TextView text;
	private Button send;
	private Button act;
	private Socket client;
	private PrintWriter output;
	private NetworkTask networktask;
	private ClientZaAndroid clientThread;
	private String ipAdress = "161.53.67.97";
	private String port = "8080";
	
	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		//final View contentView = findViewById(R.id.fullscreen_content);
		LinearLayout contentView = (LinearLayout) findViewById(R.id.fullscreen_content);
		text = (TextView) findViewById(R.id.textView1);
		send = (Button) findViewById(R.id.send);
		act = (Button) findViewById(R.id.activ);
		BallBounce b = new BallBounce (this);
		contentView.addView(b);
		
		/*
		final ClientZaAndroid clientThread = new ClientZaAndroid("connection", ipAdress, port);
		new Thread(clientThread).start();
		*/
		
		networktask = new NetworkTask();
			
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
		
		act.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				networktask = new NetworkTask();
				networktask.execute();
				
				text.setText("activ");
				
			}
		});
		send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				clientThread.sendMessage("poruka");
				*/
				
				text.setText("Send");
				networktask.SendDataToNetwork("bok!");
				/*
				String message;
				message = "Bok! Ja sam klijent.";
				
				try {
					client = new Socket("192.168.1.1", 8080);
					output = new PrintWriter(client.getOutputStream(), true);
					output.write(message);
					
					output.flush();
					output.close();
					client.close();
				} catch (UnknownHostException e){
					e.printStackTrace();
				} catch (IOException e){
					e.printStackTrace();
				}
				*/	
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
        networktask.cancel(true); //In case the task is currently running
    }
	
	
	class BallBounce extends View {
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
		boolean maxAngle = false;
		boolean scaleDuck = false;
		
		public BallBounce(Context context) {
	        super(context);
	        duck = BitmapFactory.decodeResource(getResources(),R.drawable.duck); //load a duck image
	        bgr = BitmapFactory.decodeResource(getResources(),R.drawable.river); //load a background
	        duckW = duck.getWidth();
	        duckH = duck.getHeight();
	        acc = 0.2f; //acceleration
	        dY = 0; //vertical speed
	        initialY = screenH; //Initial vertical position.
	        initialX = 0;
	        angle = 0; //Start value for rotation angle.
	        dX = 1;
	    }

	    @Override
	    public void onSizeChanged (int w, int h, int oldw, int oldh) {
	        super.onSizeChanged(w, h, oldw, oldh);
	        screenW = w;
	        screenH = h;
	        bgr = Bitmap.createScaledBitmap(bgr, w, h, true); //Resize background to fit the screen.
	        X = initialX; //Centre duck into the centre of the screen.
	        Y = screenH - duckH - duckH/2;
	    }

	    @Override
	    public void onDraw(Canvas canvas) {
	        super.onDraw(canvas);

	        //Draw background.
	        canvas.drawBitmap(bgr, 0, 0, null);
	        
	        /*
	        //Compute roughly duck speed and location.
	        Y+= (int) dY; //Increase or decrease vertical position.
	        if (Y > (screenH - duckH)) {
	            dY=(-1)*dY; //Reverse speed when bottom hit.
	        }
	        dY+= acc; //Increase or decrease speed.
			*/
	        X += (int) dX;
	        if (X > (screenW - duckW) || X < 0) {
	        	dX = (-1)*dX;
	        	scaleDuck = !scaleDuck;
	        	
	        }
	        
	        //Increase rotating angle.
	        if (angle > 15) maxAngle = true;
	        if (maxAngle == false) angle += 0.5;
	        else angle -= 0.5;
	        if (angle < -10) maxAngle = false;
	        
	        canvas.save(); //Save the position of the canvas
	        //Draw duck
	        if (scaleDuck == true)
	        	{
	        	canvas.scale(-1.0f, 1.0f, X + (duckW /2), Y + (duckH /2));	 
	        	}
	        canvas.rotate(angle, X + (duckW / 2), Y + (duckH / 2)); //Rotate the canvas.
	        canvas.drawBitmap(duck, X, Y, null); //Draw the duck on the rotated canvas.
	        canvas.restore(); //Rotate the canvas back so that it looks like duck has rotated.

	        //Call the next frame.
	        invalidate();
	    }
		
	}
	
}
