package com.example.mycamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.hardware.Camera.Face;
public class DrawView extends SurfaceView implements SurfaceHolder.Callback,Runnable {

	public SurfaceHolder mHolder;
	
	private boolean mThreadRunning = false;
	private Thread mThread = null;
	private Canvas canvas = null;
	private Paint paint = null;
	//private Bitmap circleBG = null;
	private Face mFace = null;
	private float mWidth = 0;
	private float mHeight = 0;
	private CameraController mCameraController = null;
	private MainController mMainController = null;
	private RectF oval = null;
	//private Bitmap cameraBitmap = null;
	private int secondTime = -1;
	public DrawView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		oval = new RectF();
		mCameraController = CameraController.getInstance();
		mMainController = MainController.getInstance();
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setFormat(PixelFormat.TRANSPARENT);
		setZOrderOnTop(true);
		mThread = new Thread(this);
		mThreadRunning = true;
		//cameraBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cameraicon);
		//circleBG     = BitmapFactory.decodeResource(context.getResources(), R.drawable.circlebg);
		// TODO Auto-generated constructor stub
	}
	public void beginDraw()
	{
		paint = new Paint();
		canvas= mHolder.lockCanvas();
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
	}
	public void endDraw()
	{
		mHolder.unlockCanvasAndPost(canvas);
	}
	public void drawRectangle(){
		paint.setColor(Color.RED);
		canvas.drawRect(50, 50, 500, 500, paint);
	}
	public void drawFace()
	{
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(6);
		paint.setColor(Color.RED);
		canvas.drawRect(mCameraController.faceLeft, mCameraController.faceTop, 
						mCameraController.faceRight,mCameraController.faceBottom,
						paint);
	}
	public void drawFps(){
		paint.setColor(Color.GREEN);
		paint.setTextSize(40);
		canvas.drawText(""+(int)CameraController.getInstance().getCalFps(), 50, 50, paint);
	}
	public void drawSelection(String selection,float x,float y)
	{
		paint.setColor(Color.GREEN);
		paint.setTextSize(100);
		canvas.drawText(selection, x*6, y*6-350, paint);
	}
	public void drawLine(float[]pointa,float[]pointb)
	{
		
		
		paint.setStrokeWidth(6);
		paint.setColor(Color.BLUE);
		for(int i=0;i<pointa.length;i+=2)
		{
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.CYAN);
			canvas.drawCircle(pointa[i]*6, pointa[i+1]*6,5, paint);
			
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.LTGRAY);
			canvas.drawCircle(pointa[i]*6, pointa[i+1]*6,5, paint);
			
			paint.setColor(Color.MAGENTA);
			canvas.drawLine(pointa[i]*6, pointa[i+1]*6, pointb[i]*6, pointb[i+1]*6, paint);
		}
		//Log.i("point center","x:"+pointa[0]*6+"  y:"+pointa[1]);
	}
	public void drawNumber(int[]condition,float x,float y)
	{
		paint.setColor(Color.GREEN);
		paint.setTextSize(40);
		paint.setStrokeWidth(3);
		if(condition[1] == MainController.getInstance().WAVE)
		{
			canvas.drawText(""+condition[2], x*6, y*6-200, paint);
		}
		else if(condition[1] == MainController.getInstance().ROLL)
		{
			canvas.drawText(condition[3]+"º", x*6, y*6-200, paint);
		}
	}
	public void drawCircle(float centerX,float centerY,float radius)
	{
		//canvas.drawBitmap(cameraBitmap, null, new Rect((int)left,(int)top,(int)right,(int)bottom), paint);
		//canvas.drawBitmap(circleBG, new Rect(), paint);
		oval.top = centerY - radius;
		oval.left = centerX - radius;
		oval.right = centerX+radius;
		oval.bottom = centerY + radius;
		paint.setAntiAlias(true); // 设置画笔为抗锯齿
		paint.setStrokeWidth(20);
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.argb(90, 161, 251, 194)); // 设置画笔颜色
		canvas.drawArc(oval, -90,360, false, paint);
		paint.setColor(Color.argb(116, 124, 208, 255));
		canvas.drawArc(oval, -90,mMainController.deltaRotation(), false, paint);
		paint.setStyle(Style.FILL);
		paint.setTextAlign(Align.CENTER);
		paint.setColor(Color.rgb(231, 97, 152));
		paint.setTextSize(80);
		canvas.drawText((int)(mMainController.deltaRotation()/360.0*100)+"%", centerX, centerY, paint);
	}
	public void drawTime(int second)
	{
		paint.setTextSize(400);
		paint.setStrokeWidth(6);
		paint.setColor(Color.argb(255, 224, 125,148));
		canvas.drawText(second+"", this.getWidth()/2, this.getHeight()/2, paint);
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		//drawRectangle();
		mThread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mThread.stop(new Throwable("mThread stop failed"));
		mThreadRunning = false;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(mThreadRunning)
		{
			beginDraw();
			//drawRectangle();
			if(mMainController.condition[0] == mMainController.START)
			{
				if(mMainController.scene == mMainController.TAKEPHOTO)
				{
					if(secondTime == -1)
					{
						secondTime = 5;
					}
					drawTime(secondTime--);
					endDraw();
					if(secondTime == 0)
					{
						secondTime = -1;//搁置
						mMainController.condition[0] = mMainController.NOTSTART;
						mCameraController.takePhoto();
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
			}
			else
			{
				drawFps();
				if(mCameraController.mFace!=null)
				{
					drawFace();
				}
				drawCircle(mCameraController.point[0]*6,mCameraController.point[1]*6,mCameraController.radius*2.5f);
				//drawLine(mCameraController.point,mCameraController.pointc);
				//drawNumber(MainController.getInstance().condition,mCameraController.point[0],mCameraController.point[1]);
				drawSelection(MainController.getInstance().getSelection(),mCameraController.point[0],mCameraController.point[1]);
			}
			endDraw();
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
