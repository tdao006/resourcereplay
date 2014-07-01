package com.example.resrcreplay;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

public class AlarmUtil {
	static Context ctx;
//	public static ArrayList<BroadcastReceiver> receivers=new ArrayList<BroadcastReceiver>();
	public static HashSet<PendingIntent> set=new HashSet<PendingIntent>();
	public static void setAlarm(Context ctx,BroadcastReceiver rcv, String action,int id, long time){
		//Alarm Manager
			IntentFilter intentFilter=new IntentFilter(action);
			
			AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			AlarmUtil.ctx=ctx;
			ctx.registerReceiver(rcv, intentFilter);
			
			Intent alarm=new Intent(action);
			PendingIntent sender=PendingIntent.getBroadcast(ctx,id,alarm, PendingIntent.FLAG_UPDATE_CURRENT);
				
			long current=SystemClock.elapsedRealtime();
			am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, current+time,sender);
			set.add(sender);
	}
	public static void cleanUp(){
		PendingIntent arr[]=new PendingIntent[set.size()];
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		arr=set.toArray(arr);
		for (int i=0;i<arr.length;i++){
			am.cancel(arr[i]);
		}
	}
}
