package com.example.mycamera;

import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class MyFaceDetectionController implements Camera.FaceDetectionListener {
	public static final int FACEDETECTED = 0;
	public static final int FACENOTDETECTED = 1;
	public static final int CAMERASTARTED = 2;
	public static MyFaceDetectionController instance= null;
	Handler handler;
	private MyFaceDetectionController(){
		Log.i("MyFaceDetectionController", "only one instance...!!!!!");
	}
	@Override
	
	public void onFaceDetection(Face[] faces, Camera camera) 
	{
		// TODO Auto-generated method stub
		Message msg = handler.obtainMessage();
		if(faces.length>0){
			msg.obj=faces[0];
			msg.what = FACEDETECTED;
			msg.sendToTarget();
		}
		else{
			Log.i("face", "facenotdetected");
			msg.what = FACENOTDETECTED;
			msg.sendToTarget();
		}
	}

	public static MyFaceDetectionController GetInstance()
	{
		if(instance == null)
		{
			instance = new MyFaceDetectionController();
		}
		return instance;
	}
	public void setHandler(Handler handler)
	{
		this.handler = handler;
	}
}