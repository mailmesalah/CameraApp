package com.example.cameraapp;

import com.example.cameraapp.Preview;









import android.app.ActionBar.LayoutParams;
//import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CameraActivity extends Activity {

	private Camera mCamera;
	private Preview mPreview;
	TextView textView;
	Button bMinus;
	Button bPlus;
	Button bConf;
	Button bSound;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		textView = new TextView(this);
		textView.append("Version 15\n");
		
		RelativeLayout rLayout = new RelativeLayout(this);
		// Defining the RelativeLayout layout parameters.
        // In this case I want to fill its parent
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
				
		// Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lpText = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpText.addRule(RelativeLayout.ALIGN_TOP);

        // Setting the parameters on the TextView
        textView.setLayoutParams(lpText);

        // Adding the TextView to the RelativeLayout as a child
        rLayout.addView(textView);
					    
		bMinus= new Button(this);
		//bMinus.setTextColor(Color.WHITE);
		//bMinus.setBackgroundColor(Color.BLACK);		
		//bMinus.getBackground().setAlpha(255);
		bMinus.setId(1);
		bMinus.setText("-");
		
		bPlus= new Button(this);
		//bPlus.setTextColor(Color.WHITE);
		//bPlus.setBackgroundColor(Color.BLACK);
		//bPlus.setAlpha(1);			
		bPlus.setId(2);
		bPlus.setText("+");
		
		bConf= new Button(this);
		//bConf.setTextColor(Color.WHITE);
		//bConf.setBackgroundColor(Color.BLACK);
		//bConf.setAlpha(1);		
		bConf.setId(3);
		bConf.setText("T0");
		
		bSound= new Button(this);
		//bSound.setTextColor(Color.WHITE);
		//bSound.setBackgroundColor(Color.BLACK);
		//bSound.setAlpha(1);		
		bSound.setId(4);
		bSound.setText("S");
		
		mPreview = new Preview(this, textView,bMinus,bPlus,bConf,bSound);		
		setContentView(mPreview);
		
		// Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lpMinus = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpMinus.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		bMinus.setLayoutParams(lpMinus);
		
		RelativeLayout.LayoutParams lpPlus = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
		lpPlus.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lpPlus.addRule(RelativeLayout.RIGHT_OF,bMinus.getId());
		bPlus.setLayoutParams(lpPlus);
		
		RelativeLayout.LayoutParams lpConf = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
		lpConf.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lpConf.addRule(RelativeLayout.RIGHT_OF,bPlus.getId());
		bConf.setLayoutParams(lpConf);
		
		RelativeLayout.LayoutParams lpSound = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
		lpSound.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lpSound.addRule(RelativeLayout.RIGHT_OF,bConf.getId());
		bSound.setLayoutParams(lpSound);
		
		rLayout.addView(bMinus);
		rLayout.addView(bPlus);
		rLayout.addView(bConf);
		rLayout.addView(bSound);
		
		addContentView(rLayout, rlp);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			//textView.append("Opening Camera on onResume()\n");
			mCamera = Camera.open();
			if(mCamera==null){
				//If no back camera, try front camera
				mCamera = Camera.open(0);
			}
			textView.append("InsideonResume() Camera.open() returns!! " + mCamera + "\n");

			mPreview.setCamera(mCamera);
		} catch (Exception e) {
			textView.append(e + " onResume()\n");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		/*try {
			textView.append("Closing Camera on onPause()\n");
			
			if (mCamera != null) {
				mPreview.setCamera(null);
				mCamera.release();
				mCamera = null;
			}
		} catch (Exception e) {
			textView.append(e + " onPause()\n");
		}*/
	}
	
}
