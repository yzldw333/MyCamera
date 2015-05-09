package com.example.mycamera;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;




public class MainController {
	
	//scene
	final int TAKEPHOTO = 0;
	final int MODIFYWHITEBALANCE = 1;
	final int MODIFYEXPOSURE = 2;
	final int menuNum = 3;
	
	//menu
	final String []level1Menu = new String[]{"拍照","白平衡","曝光度"};
	 List <String> level2WhiteBalance = null;
	 Map <String,String> level2WhiteBalanceChinese = new HashMap<String,String>();
	 List <Integer>level2Exposure = null;
	//condition
	final int NOTSTART = 10;
	final int START = 11;
	
	//gesture
	final int NOGESTURE= 100;
	final int WAVE = 101;
	final int ROLL = 102;
	//100、没有手势  101、挥手 102、转圈
	public int waveCount = 0;
	public int rollDegree = 0;
	public int[]condition = new int[]{NOTSTART,NOGESTURE,0,0};
	public int scene = 0;
	private int presentRotateAngle = 0;
	public int selectedId = 0;
	private static MainController instance;
	public Context mainActivity;
	private MainController()
	{
		level2WhiteBalanceChinese.put("incandescent", "白炽灯");
		level2WhiteBalanceChinese.put("auto", "自动");
		level2WhiteBalanceChinese.put("fluorescent", "荧光灯");
		level2WhiteBalanceChinese.put("daylight", "日光");
		level2WhiteBalanceChinese.put("cloudy-daylight", "多云");
	}
	public int deltaRotation()
	{
		return condition[3] - presentRotateAngle;
	}
	public void rotation(int rotateAngle)
	{
		if(rotateAngle - presentRotateAngle>=360)
		{
			selectedId +=1;
			presentRotateAngle = rotateAngle;
		}
		if(presentRotateAngle - rotateAngle >=360)
		{
			selectedId -=1;
			presentRotateAngle = rotateAngle;
		}
		processSelection();	
	}
	
	public void processSelection()
	{
		switch(scene)
		{
			case TAKEPHOTO:
				if(selectedId>=level1Menu.length)
				{
					selectedId = 0;
				}
				if(selectedId<0)
				{
					selectedId = level1Menu.length-1;
				}
				break;
			case MODIFYWHITEBALANCE:
				if(selectedId>=level2WhiteBalance.size())
				{
					selectedId = 0;
				}
				if(selectedId<0)
				{
					selectedId = level2WhiteBalance.size()-1;
				}
				CameraController.getInstance().setWhiteBalance(selectedId);
				break;
			case MODIFYEXPOSURE:
				if(selectedId>=level2Exposure.size())
				{
					selectedId = 0;
				}
				if(selectedId<0)
				{
					selectedId = level2Exposure.size()-1;
				}
				CameraController.getInstance().setExposure(selectedId);
				break;
		}
	}
	public String getSelection()
	{
		String res = null;
		if(scene == TAKEPHOTO)
		{
			res = level1Menu[selectedId];
		}
		if(scene == MODIFYWHITEBALANCE)
		{
			res = level2WhiteBalance.get(selectedId);
			if(level2WhiteBalanceChinese.get(res)!=null)
			{
				res = level2WhiteBalanceChinese.get(res);
			}
		}
		if(scene == MODIFYEXPOSURE)
		{
			res = ""+level2Exposure.get(selectedId);
		}
		return res;
	}
	public void setActivity(Context context)
	{
		mainActivity = context;
	}
	public static MainController getInstance()
	{
		if(instance == null)
		{
			instance = new MainController();
		}
		return instance;
	}
	public void Clean()
	{
		condition[0] = NOTSTART;
		condition[1] = NOGESTURE;
		condition[2] = 0;
		condition[3] = 0;
		selectedId = 0;
	}
}
