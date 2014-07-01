package com.example.resrcreplay;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

public class ScreenControl {
	static String screenFile=Environment.getExternalStorageDirectory().getPath()+"/lcd.txt";
	public static Context ctx;
	public ScreenControl(Context ctx){
		ScreenControl.ctx=ctx;
	}
	
	public static void cleanUp(){
		ctx.unregisterReceiver(screenRcv);
	}
	
	public void init(){
		try {
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(screenFile)));
			String line=null;
			line=reader.readLine();
			int millisec=Integer.parseInt(line);
			AlarmUtil.setAlarm(ctx, screenRcv, SCREEN_ALARM, SCREEN_ID, millisec);
			reader.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static BroadcastReceiver screenRcv=new ScreenReceiver();
	public static String SCREEN_ALARM="edu.ucr.tuandao.SCREEN_ALARM";
	public static int SCREEN_ID=10010;
	static class ScreenReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			ctx.sendBroadcast(new Intent(MainActivity.SCREEN_OFF));
		}
	}
}
