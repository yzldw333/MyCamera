package com.example.mycamera;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;

import org.opencv.core.*;

import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
public class CameraView extends SurfaceView implements Runnable,SurfaceHolder.Callback,PreviewCallback{
	private Camera camera;
	private byte [] mBuffer;
	private Mat mFrame;
	private boolean enabled = false;
	private int screenWidth,screenHeight;
	protected SurfaceHolder mHolder;
	protected Thread _thread;
	protected Context context;
	public CameraView(Context context,AttributeSet attrs) 
	{
		super(context,attrs);
		this.context = context;
		this.screenHeight = this.getHeight();
		this.screenWidth = this.getWidth();
		mHolder = getHolder();
		mHolder.addCallback(this);
		_thread = new Thread(this);
		//_thread.start();
		// TODO Auto-generated constructor stub
	}
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		while(true)
		{
			try 
			{
				
				Thread.sleep(20);
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		// TODO Auto-generated method stub
		initCamera();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		camera.setPreviewCallbackWithBuffer(null);
		camera.stopPreview();
		camera.release();
		// TODO Auto-generated method stub
		
	}
	public Size calPreviewSize(List<?> sizes, int maxWidth,int maxHeight)
	{
		
		int calWidth = 0;
		int calHeight = 0;
		for(Object size:sizes)
		{
			Camera.Size tmpSize = (Camera.Size)size;
			int tmpWidth = tmpSize.width;
			int tmpHeight = tmpSize.height;
			if(tmpWidth>calWidth&&tmpWidth<=maxWidth)
			{
				calWidth = tmpWidth;
			}
			if(tmpHeight>calHeight&&tmpHeight<=maxHeight)
			{
				calHeight = tmpHeight;
			}
		}
		Size resultSize = new Size(calWidth,calHeight);
		return resultSize;
	}
	private void initCamera()
	{
		camera = Camera.open();
		CameraController.getInstance().setCamera(camera);
		if(camera!=null)
		{
			
			try {
				Camera.Parameters parameters = camera.getParameters();
				//Log.d("size", parameters.getPreviewSize().toString());
				List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
				Size calSize = calPreviewSize(sizes, screenWidth, screenHeight);
				parameters.setPreviewFormat(ImageFormat.NV21);
				parameters.setPreviewSize(1920,1080);
				parameters.setPictureFormat(ImageFormat.JPEG);
				parameters.setPictureSize(1920, 1080);
				//parameters.setPictureSize(1440, 1080);
				camera.setParameters(parameters);
				//this.setLayoutParams(new LayoutParams(1440, 1080));
				camera.setPreviewDisplay(mHolder);
				this.getReadyForPreview();
				camera.setFaceDetectionListener(MyFaceDetectionController.instance);
				//camera.setDisplayOrientation(90);
				camera.startPreview();
				startFaceDetection();
				camera.autoFocus(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	public void getReadyForPreview()
	{
		int size = 1920*1080;
        size  = size * ImageFormat.getBitsPerPixel(camera.getParameters().getPreviewFormat()) / 8;
        if(mBuffer == null)
        mBuffer = new byte[size];
        //mFrame = new Mat(1080,1920,CvType.CV_8UC1);
        camera.addCallbackBuffer(mBuffer);
		camera.setPreviewCallbackWithBuffer(this);
	}
    public void startFaceDetection(){

		   // Try starting Face Detection

		   Camera.Parameters params = camera.getParameters();

		   // start face detection only *after* preview has started

		   if (params.getMaxNumDetectedFaces() > 0){

		       // camera supports face detection, so can start it:
			   Log.i("face", "maxNumDetectedFaces:"+params.getMaxNumDetectedFaces());
		       camera.startFaceDetection();
		       
		   }
	}
	public void enableView()
	{
		mFrame = new Mat(1080+1080/2,1920,CvType.CV_8UC1);
		Log.i("cameraview", "true");
		enabled = true;
	}
	//PreviewCallback
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		//Ö¡ÂÊ¼ÆËã¡£
		CameraController.getInstance().calculateFps();
		if(camera != null)
		{
			if(mBuffer==null)
			{
				int bytes = 1920*1080*ImageFormat.getBitsPerPixel(camera.getParameters().getPreviewFormat())/8;
				mBuffer = new byte[bytes];
			}
			camera.addCallbackBuffer(mBuffer);
		}
		if(data.length!=0&&enabled)
		{
			mFrame.put(0, 0, data);
			CameraController.getInstance().setFrame(mFrame);
		}
	}
	
}
