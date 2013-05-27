package com.games.snake;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import com.games.snake.SnakeGame.Panel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;

class GameThread extends Thread {
    private SurfaceHolder _surfaceHolder;
    private Panel _panel;
    private boolean _run = false;
    private DisplayMetrics _dm;
    long sysTime;
    private ArrayList<Point> loc = new ArrayList<Point>();
    private Point PowerOrb = new Point(), moveDir = new Point(1,10);
    Random rand = new Random();
    private int score, speed = 4;
    private Matrix mat = new Matrix();
 
    public GameThread(SurfaceHolder surfaceHolder, Panel panel,DisplayMetrics dm) {
    	_surfaceHolder = surfaceHolder;
        _panel = panel;
        _dm = dm;
        sysTime = System.currentTimeMillis();
        
        loc.add(new Point(dm.widthPixels/2, dm.heightPixels/2));
        loc.add(new Point(dm.widthPixels/2-10, dm.heightPixels/2));
        loc.add(new Point(dm.widthPixels/2-20, dm.heightPixels/2));
        loc.add(new Point(dm.widthPixels/2-30, dm.heightPixels/2));
        loc.add(new Point(dm.widthPixels/2-40, dm.heightPixels/2));
        loc.add(new Point(dm.widthPixels/2-50, dm.heightPixels/2));
        
        PowerOrb.set(10+rand.nextInt(dm.widthPixels-20), 10+rand.nextInt(dm.heightPixels-20));
    }
    
    public void setRunning(boolean run) {
        _run = run;
    }
 
    public boolean isRunning() {
        return _run;
    }
    
    @Override
    public void run() {
        Canvas c;
        while (_run) {
            c = null;
            try {
                runGame();
                c = _surfaceHolder.lockCanvas(null);
                synchronized (_surfaceHolder) {
                    _panel.onDraw(c);
                }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    _surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }

    public void runGame(){
    	checkHit();
    	move();
    	return;
    }    
    
    /**
     * Finds angle between two points in degrees
     * @param orig Point of origin
     * @param dest Destination point
     * @return The angle from the origin to the destination in degrees
     */
    public int findAngle(Point orig, Point dest){
    	double x = orig.x-dest.x, y = orig.y-dest.y;
    	
    	if(x != 0 && y != 0){
    		if(x>0 && y>0)
    			return 180 + (int)Math.toDegrees(Math.atan2(y,x));
    		if(x<0 && y>0)
    			return 180 + (int)Math.toDegrees(Math.atan2(y, x));
    		if(x<0 && y<0)
    			return 180 - Math.abs((int)Math.toDegrees(Math.atan2(y, x)));
    		if(x>0 && y<0)
    			return 180 - Math.abs((int)Math.toDegrees(Math.atan2(y, x)));
    			
    	}
    	
    	if(x == 0)
    		if(y>0)
    			return 270;
    		else
    			return 90;
    	
    	if(y == 0)
    		if(x>0)
    			return 180;
    		else
    			return 0;
    	
    	return (int)Math.atan2(y, x);
    }
    
    /**
     * Update the direction that the snake will be traveling in.
     * 
     * @param moveX The amount that the snake will be moved horizontally
     * @param moveY The amount that the snake will be moved vertically.
     */
	public void updateMoveDir(float moveX, float moveY){
		//distance between the 2 points
    	float c = (float) Math.sqrt(Math.pow(moveX, 2)+Math.pow(moveY, 2));
		
    	int x1 = (int)((moveX*speed)/c);
    	int y1 = (int)((moveY*speed)/c);
    	
    
    	
    	
    	int angle = findAngle(new Point(0,0), new Point(x1,y1)) - findAngle(new Point(0,0), moveDir); 
    	
    	/*while(angle > 90 || angle <-90){
    		angle%=90;
    	}
    	if(angle != 0)
    	android.util.Log.d("SnakeGame", "X: "+moveDir.x +" Y: "+moveDir.y+" Angle:"+angle+ 
    			" orig- "+findAngle(new Point(0,0), moveDir )+ 
    			" new- "+findAngle(new Point(0,0), new Point(x1,y1)));
    			
    	if(angle >= 70 || angle <= -70)
    		moveDir= rotate(new Point(x1,y1), 15*(Math.abs(angle)/angle));
    	else*/
    		moveDir.set(x1, y1);
    		
    	
	}
    
    public ArrayList<Point> getLoc(){
    	return loc;
    }

    public Point getPowerOrb(){
    	return PowerOrb;
    }
    
    public int getScore(){
    	return score;
    }
    
    public Matrix getMatrix(){
    	return mat;
    }
    
    public int getDegrees(){
    	return findAngle(new Point(0,0), moveDir);
    }
    
    private void move(){
		Point last = loc.get(0);
    	loc.set(0, new Point(last.x+moveDir.x, last.y+moveDir.y));
    	
    	for(int i =1; i < loc.size();i++){
    		Point tmp = loc.get(i);
    		
    		loc.set(i, last);
    		last = tmp;
    	}
    	
    	
    	//mat.setTranslate(loc.get(0).x, loc.get(0).y);
    	//mat.setRotate(findAngle(new Point(0,0), moveDir), loc.get(0).x, loc.get(0).y);
    	_panel.postInvalidate();
	}
	
    //rotates the direction vector by a degree
    private Point rotate(Point vector, int degree){
    	int y =vector.y, x = vector.x;
    	return new Point((int)Math.ceil((x*Math.cos(degree)-(y*Math.sin(degree)))),(int)Math.ceil(((x*Math.sin(degree))+(y*Math.cos(degree)))));
    }
    
    //Checks for collision with objects(Pickups, PowerOrbs, Self and Walls)
    private void checkHit() {
    	
    	Point head = loc.get(0);
    	
		//Check for collision with PowerOrb
    	if(head.x > PowerOrb.x-_panel.head.getWidth()/2 && head.x < PowerOrb.x+_panel.head.getWidth()/2 && head.y > PowerOrb.y-_panel.head.getHeight()/2 && head.y < PowerOrb.y+_panel.head.getHeight()/2){
			PowerOrb.set(10+rand.nextInt(_dm.widthPixels-20), 10+rand.nextInt(_dm.heightPixels-20));
			score+=10;
			loc.add(loc.get(loc.size()-1));
			loc.add(loc.get(loc.size()-1));
			loc.add(loc.get(loc.size()-1));
		}
    	
    	//Check for Collision with walls 
    	if(head.x > _dm.widthPixels || head.x < 0 || head.y > _dm.heightPixels || head.y < 0){
			setRunning(false);
		}
    	
    	/*/Check for collision with self
    	for(int i = 1; i < loc.size(); i++){
    		if(head.x > loc.get(i).x-_panel.head.getWidth()/2 && 
    		   head.x < loc.get(i).x+_panel.head.getWidth()/2 && 
    		   head.y > loc.get(i).y-_panel.head.getHeight()/2 && 
    		   head.y < loc.get(i).y+_panel.head.getHeight()/2){
    			setRunning(false);
    		}
    	}*/
	}
}