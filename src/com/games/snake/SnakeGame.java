package com.games.snake;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
 
public class SnakeGame extends Activity implements SensorEventListener{
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //sensor setup
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER );
        
        super.onCreate(savedInstanceState);
        vp = new Panel(this);
        setContentView(vp);
        
        
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	
    }

    public void onSensorChanged(SensorEvent event) {
    	float x = event.values[1], y = event.values[0];
    	y-=3;
    	//android.util.Log.d("SnakeGame","X:"+x+" Y: "+y);
    	if(Math.abs(x) < 2 || Math.abs(y) < 2)
    		_thread.updateMoveDir(x,y);
    }
    
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    //super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.gameoptions, menu);
    menu.findItem(R.id.help_menu_item).setIntent(
    new Intent(this, ChallengeAcceptedHelp.class));
    menu.findItem(R.id.settings_menu_item).setIntent(
    new Intent(this, ChallengeAcceptedOptions.class));
    return true;
    }*/

    
    private final Paint mPaint = new    Paint(Paint.ANTI_ALIAS_FLAG);
    GameThread _thread;
    Thread thread = null;
    SurfaceHolder surfaceHolder;
    volatile boolean running = false;
    Panel vp;
    
    Random random;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    
    
    
    
    
    
    //Custom Panel SAWUCE!
    class Panel extends SurfaceView implements SurfaceHolder.Callback{

        DisplayMetrics dm = new DisplayMetrics();
        Bitmap head, body;
        
    	public Panel(Context context) {
            super(context);
            getHolder().addCallback(this);
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            _thread = new GameThread(getHolder(), this, dm);
            
            setFocusable(true);
            loadImages();
            //CONTEXT = this;
            
        }
        
    	/**
    	 Load Images used by games
    	 */
        public void loadImages(){
        	head = BitmapFactory.decodeResource(getResources(), R.drawable.snakehead);
        	body = BitmapFactory.decodeResource(getResources(), R.drawable.bodynrg1);
        }
        
        /**
         Method for surface changed
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // TODO Auto-generated method stub
        }
     
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        	_thread.setRunning(true);
            _thread.start();
        }
     
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        	// simply copied from sample application LunarLander:
            // we have to tell thread to shut down & wait for it to finish, or else
            // it might touch the Surface after we return and explode
            boolean retry = true;
            _thread.setRunning(false);
            while (retry) {
                try {
                    _thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // we will try it again and again...
                }
            }
        }
        
        /**
         * Draws game animation 
         * @param canvas Canvas for animation to be drawn on
         */
        public void onDraw(Canvas canvas) {
            canvas.drawColor(Color.RED);
			mPaint.setColor(Color.GREEN);
			mPaint.setTextSize(18);
			
			//canvas.drawRect(_thread.getLoc().get(0).x, _thread.getLoc().get(0).y, _thread.getLoc().get(0).x+10, _thread.getLoc().get(0).y+10, mPaint);
			
			//Draw Snake Body
			mPaint.setColor(Color.BLUE);
			for(int i = _thread.getLoc().size()-1; i > 0; i--){
				canvas.save();
				canvas.rotate(_thread.findAngle(_thread.getLoc().get(i-1), _thread.getLoc().get(i)), _thread.getLoc().get(i).x, _thread.getLoc().get(i).y);
				canvas.drawBitmap(body, _thread.getLoc().get(i).x-body.getWidth()/2, _thread.getLoc().get(i).y-body.getHeight()/2, mPaint);
				canvas.restore();
			}
			
			//Draw Snake Head
			canvas.save();
			canvas.rotate(_thread.getDegrees(), _thread.getLoc().get(0).x, _thread.getLoc().get(0).y);
			canvas.drawBitmap(head, _thread.getLoc().get(0).x-head.getWidth()/2, _thread.getLoc().get(0).y-+head.getHeight()/2, mPaint);
			canvas.restore();
			
			mPaint.setColor(Color.CYAN);
			canvas.drawRect(_thread.getPowerOrb().x, _thread.getPowerOrb().y, _thread.getPowerOrb().x+10, _thread.getPowerOrb().y+10, mPaint);
			
			canvas.drawText("Score: "+_thread.getScore(), 10, 10, mPaint);
			
			if(!_thread.isRunning()){
				canvas.drawColor(Color.BLACK);
				mPaint.setTextSize(30);
				canvas.drawText("Game Over! Score: "+_thread.getScore(), 10, 100, mPaint);
			}
        }
        
        protected void onMeasure(){
        	 setMeasuredDimension(dm.widthPixels, dm.heightPixels);
        }

        
        /*public boolean onTouchEvent(MotionEvent event) {
        	if(!_thread.isRunning()){
        		startActivity(new Intent(ChallengeAcceptedGame.this,
                        ChallengeAcceptedScores.class));
                        ChallengeAcceptedGame.this.finish();
            }
        	else{

        	}
            
            return true;
        }*/    		
    }
}

 
