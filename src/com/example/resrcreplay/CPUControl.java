package com.example.resrcreplay;


import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;


public class CPUControl{

	static String inputFile=Environment.getExternalStorageDirectory().getPath()+"/cpu.txt";
	static boolean cont=false;
	static LinkedBlockingQueue<Short> queue=new LinkedBlockingQueue<Short>(1000);
	public BufferedReader reader;
	public static int SubIntTime=5000;//5 seconds
	static Context ctx;
	
	public CPUControl(Context ctx){
		this.ctx=ctx;
	}
	
	public static void cleanUp(){
		ctx.unregisterReceiver(cpuRcv);
	}
	
	public static BroadcastReceiver cpuRcv=new CPURecv();
	public static int CPU_ALARM_ID=10002;
	public static String CPU_ALARM_ACTION="edu.ucr.tuandao.CPU_ALARM_ACTION";
	
	
	static class CPURecv extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			short cpujiffie=0;
			try {
//				while ((cpujiffie=queue.take())!=-1 && cont){
				cpujiffie=queue.take();
				if (cont && cpujiffie>-1){
					
					//every 5 seconds
					Log.d("CPUDebug","Next jiffie: "+cpujiffie);
					int ticks=cpujiffie*10;//1 tick is 10ms
					long current=SystemClock.elapsedRealtime();
					while(true){
						if (SystemClock.elapsedRealtime()>ticks+current){
							AlarmUtil.setAlarm(ctx, cpuRcv, CPU_ALARM_ACTION, CPU_ALARM_ID, SubIntTime-ticks);
							break;
						}
					}
				}
				else{
					Log.d("CPUDebug","DONE");
				}
//				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void initCPUTicks(){
		String fileName=inputFile;
		try{
			cont=true;
			reader=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String line=null;
			int preLoad=1000;
			int count=0;
			while (count<preLoad && ((line=reader.readLine())!=null)){
				short cpu=Short.parseShort(line);
				queue.put(cpu);
				count++;
			}
			if (count==preLoad){
				reader.close();
				//add an ending flag
				queue.put((short) -1);
			}
			else{
				Thread t=new Thread(new InitThread());
				t.start();
			}
			//start after 100 ms
			AlarmUtil.setAlarm(ctx, cpuRcv, CPU_ALARM_ACTION, CPU_ALARM_ID, 100);
		}
		catch (IOException e){
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	class InitThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String line;
			try {
				while ((line=reader.readLine())!=null){
					short cpu=Short.parseShort(line);
					queue.put(cpu);
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//add an ending flag
			try {
				queue.put((short) -1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
