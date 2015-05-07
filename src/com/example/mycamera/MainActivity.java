package com.example.mycamera;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera.Face;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {

	private int screenWidth,screenHeight;
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("MAIN", "OpenCV loaded successfully");
                    System.loadLibrary("MyCamera");
                    /* Now enable camera view to start receiving frames */
                    mcameraview.enableView();
                    
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    private CameraView mcameraview = null;
    private DrawView mdrawview = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		//CameraView mcameraview = new CameraView(this,screenWidth,screenHeight);
		//mcameraview.setLayoutParams(new LayoutParams(1440,1080));
		setContentView(R.layout.activity_main);
		mdrawview = (DrawView)findViewById(R.id.myDrawView);
		
        mcameraview = (CameraView)findViewById(R.id.myCameraView);
        CameraController.getInstance().setCameraView(mcameraview);
		MainController.getInstance().setActivity(this);
		//this.addContentView(mcameraview, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		Toast.makeText(this,"aaaaa", Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onResume()
    {
		
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
                
    }
}
