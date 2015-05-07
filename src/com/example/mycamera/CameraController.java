package com.example.mycamera;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CameraController implements Runnable{
	private Mat mFrame;
	private Mat mRgba;
	//关于是不是新传进来的frame的变量
	private boolean isNew = false;
	private static CameraController instance = null;
	private Camera mCamera = null;
	List <String> whiteBalanceList = null;
	List<Integer> exposureList = null;
	private Thread mThread;
	private long mPretime = 0; 
	private double calFps = 0;
	private boolean mStatus = true;
	private CameraView mCameraView = null;
	private  String storagePath = "";
	private  final File parentPath = Environment.getExternalStorageDirectory();  
	public Face mFace = null;
	public float[]point = new float[18];
	public float[]pointc = new float[18];
	public float radius =0;
	public float faceLeft = 0;
	public float faceTop = 0;
	public float faceRight = 0;
	public float faceBottom = 0;
	private MainController mainControllerInstance = null;
	private CameraController()
	{
		mainControllerInstance = MainController.getInstance();
		MyFaceDetectionController.GetInstance().setHandler(new Handler(){
        	public void handleMessage(Message msg) {   
	              switch (msg.what) {   
	              		case MyFaceDetectionController.FACEDETECTED:
	              			mFace = (Face)msg.obj;
	              			break;
	              		case MyFaceDetectionController.FACENOTDETECTED:
	              			mFace = null;
	              			break;
	              }   
	              processFace();
	              super.handleMessage(msg);   
	         } 
        });
		mThread = new Thread(this);
		mThread.start();
	}
	public void setCamera(Camera camera)
	{
		mCamera = camera;
		initWhiteBalance();
		initExposure();
	}
	public void setCameraView(CameraView cameraView)
	{
		this.mCameraView = cameraView;
	}
	public void takePhoto()
	{
		//Parameters parameters = mCamera.getParameters();
		//mCamera.autoFocus(null);
		mCamera.takePicture(new ShutterCallback(){

			@Override
			public void onShutter() {
				// TODO Auto-generated method stub
				
			}
			
		}, null, new PictureCallback(){

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// TODO Auto-generated method stub
				Bitmap b = null;  
	            if(null != data){  
	                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图  
	                mCamera.stopPreview();
	            }  
	            //保存图片到sdcard  
	            if(null != b)  
	            {  
	                //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。  
	                //图片竟然不能旋转了，故这里要旋转下  
	                //Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);  
	                saveBitmap(b);  
	            }  
	            //再次进入预览  
	            mCameraView.getReadyForPreview();
	            mCamera.startPreview();
			}
			
		});
		
	}
	private void initWhiteBalance()
	{
		Parameters parameters = mCamera.getParameters();
		whiteBalanceList = parameters.getSupportedWhiteBalance();
		mainControllerInstance.level2WhiteBalance = whiteBalanceList;
		
	}
	public void setWhiteBalance(int id)
	{
		Parameters parameters = mCamera.getParameters();
		parameters.setWhiteBalance(whiteBalanceList.get(id));
		mCamera.setParameters(parameters);
	}
	private void initExposure()
	{
		Parameters parameters = mCamera.getParameters();
		exposureList = new ArrayList<Integer>();
		int maxExposure = parameters.getMaxExposureCompensation();
		int minExposure = parameters.getMinExposureCompensation();
		for(int i=minExposure;i<=maxExposure;i++)
		{
			exposureList.add(i);
		}
		mainControllerInstance.level2Exposure = exposureList;
		//Log.i("zoom",""+exposureList.toString());
		
	}
	public void setExposure(int id)
	{
		Parameters parameters = mCamera.getParameters();
		parameters.setAutoExposureLock(true);
		parameters.setExposureCompensation(exposureList.get(id));
		
		mCamera.setParameters(parameters);
	}
	private  String initPath(){  
        if(storagePath.equals("")){  
            storagePath = parentPath.getAbsolutePath()+"/" + "DCIM/MyCamera";  
            File f = new File(storagePath);  
            if(!f.exists()){  
                f.mkdir();  
            }  
        }  
        return storagePath;  
    } 
	public void saveBitmap(Bitmap bitmap)
	{
		String path = initPath();  
        long dataTake = System.currentTimeMillis();  
        String jpegName = path + "/" + dataTake +".jpg";  
        Log.i("takephoto", "saveBitmap:jpegName = " + jpegName);  
        try {  
            FileOutputStream fout = new FileOutputStream(jpegName);  
            BufferedOutputStream bos = new BufferedOutputStream(fout);  
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);  
            bos.flush();  
            bos.close();  
            Uri filePath = Uri.parse("file://"+jpegName);
            Log.i("takephoto", "saveBitmap成功:");
            mainControllerInstance.mainActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, filePath));  
            mainControllerInstance.Clean();
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            Log.i("takephoto", "saveBitmap:失败");
            mainControllerInstance.Clean();
            e.printStackTrace();  
        }  
        
	}
	public static CameraController getInstance()
	{
		if(instance == null)
		{
			instance = new CameraController();
		}
		return instance;
	}
	public void processFace()
	{
		if(mFace != null&& mFrame!=null)
		{
			faceLeft =(float)((1000+mFace.rect.left)*1920/2000.0);
			faceTop =(float)((1000+mFace.rect.top)*1080/2000.0);
			faceRight=(float)((1000+mFace.rect.right)*1920/2000.0);
			faceBottom=(float)((1000+mFace.rect.bottom)*1080/2000.0);
			//Log.i("face", )
			if(mFace.rect.centerX()<0)
			{
				//When the face is on the left.
				
				//center Point
				float tmp0 = faceRight + 500;
				float tmp1 = faceBottom;				//距离大于300进行像素更新
				if(Math.sqrt((tmp0-point[0])*(tmp0-point[0]) + (tmp1-point[1])*(tmp1-point[1]))>300)
				{
					point[0] = tmp0;
					point[1] = tmp1;
				}			
			}
			else
			{
				//When the face is on the right.
				float tmp0 = faceLeft - 500;
				float tmp1 = faceBottom;
				//距离大于300进行像素更新
				if(Math.sqrt((tmp0-point[0])*(tmp0-point[0]) + (tmp1-point[1])*(tmp1-point[1]))>300)
				{
					point[0] = tmp0;
					point[1] = tmp1;
				}
				
			}
			//radius
			radius = (faceRight-faceLeft)/7.0f;
			//up
			point[2] = point[0];
			point[3] = point[1]-radius;
			
			//right
			point[4] = point[0]+radius;
			point[5] = point[1];
			
			//bottom
			point[6] = point[0];
			point[7] = point[1]+radius;
			
			//left
			point[8] = point[0]-radius;
			point[9] = point[1];
			
			//left up
			point[10] = point[0]-radius;
			point[11] = point[1]-radius;
			
			//right up
			point[12] = point[0]+radius;
			point[13] = point[1]-radius;
			
			//left bottom
			point[14] = point[0]-radius;
			point[15] = point[1]+radius;
			
			//right bottom
			point[16] = point[0]+radius;
			point[17] = point[1]+radius;
			for(int i=0;i<18;i++)
			{
				point[i] = point[i]/6.0f;//变成原生代码中处理的分辨率大小
			}
			//Log.i("face center" , "x:"+point[0]+"   ,y:"+point[1]+"  radius:"+radius);
		}
		else
		{
			faceLeft = 0;
			faceTop = 0;
			faceRight = 0;
			faceBottom = 0;
		}
	}
	public void setFrame(Mat frame)
	{
		mFrame = frame;
		isNew = true;
		//Log.i("cameraview","frame setted.");
		//Log.i("cameraview", frame.size().toString());
	}

	public void calculateFps(){
		long tmp = System.currentTimeMillis();
		calFps = 1/((tmp - mPretime)/1000.0);
		mPretime = tmp;
	}
	public double getCalFps()
	{
		return calFps;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(mStatus)
		{
			if(isNew)
			{
				isNew = false;
				Log.i("image","condition[0] :"+mainControllerInstance.condition[0]);
				//start过程 如果是拍照片可以不进行处理
				if(mainControllerInstance.condition[0] == mainControllerInstance.START)
				{
					if(mainControllerInstance.scene != mainControllerInstance.TAKEPHOTO)
					{
						//返回上一层
						mainControllerInstance.scene = mainControllerInstance.TAKEPHOTO;
						mainControllerInstance.condition[0] = mainControllerInstance.NOTSTART;
						mainControllerInstance.selectedId = 0;
						//重启人脸识别
						mCamera.stopFaceDetection();
						mCamera.startFaceDetection();
						//把wave的次数置为0
						mainControllerInstance.condition[2] = 0;
					}
					continue;
				}
				
				if(mRgba == null)
				{
					mRgba = new Mat();
				}
				Imgproc.cvtColor(mFrame, mRgba, Imgproc.COLOR_YUV2BGR_NV12, 3);
				Computation.Compute(mRgba.getNativeObjAddr(),point,pointc,mainControllerInstance.condition);
				if(mainControllerInstance.condition[1] == mainControllerInstance.ROLL)
				{
					mainControllerInstance.rotation(mainControllerInstance.condition[3]);
				}
				if(mainControllerInstance.condition[1] == mainControllerInstance.NOGESTURE)
				{
					if(mainControllerInstance.scene == mainControllerInstance.TAKEPHOTO)
					{
						mainControllerInstance.scene = mainControllerInstance.selectedId;
					}
				}
				//Log.i("floatarray",pointc[0]+" , "+pointc[1]);
			}	
		}
	}
	
	
}
