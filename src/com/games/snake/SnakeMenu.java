package com.games.snake;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class SnakeMenu extends SnakeActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        
        
    	super.onCreate(savedInstanceState);
        
        	((Button)findViewById(R.id.bstart)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                	startActivity(new Intent(SnakeMenu.this,
                			SnakeGame.class));
                }
        	});
        	
    	}
	}