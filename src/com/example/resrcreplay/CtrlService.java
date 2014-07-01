package com.example.resrcreplay;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;

public class CtrlService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	public class LocalBinder extends Binder {
        CtrlService getService() {
            return CtrlService.this;
        }
    }
	static boolean isRunning=false; 
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		notificationManager = (NotificationManager)getSystemService(
                NOTIFICATION_SERVICE);
	}
	
//	final String ALARM_ACTION = "edu.ucr.tuandao.android.Screen_On";
//	IntentFilter intentFilter;
//	PendingIntent sender;//pending Intent for subInt
//	static AlarmManager am;
//	static WakeLock wl=null;
//	PowerManager pm;
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		if (!isRunning){
			showNotification();
			isRunning=true;
			
			//control network service
			Thread t=new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
						NetworkControl.ctx=(Context)CtrlService.this;
						NetworkControl.init();
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			});
			t.start();
			
			//replay cpu ticks
			Thread tCPU=new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
						CPUControl cpu=new CPUControl(CtrlService.this);
						cpu.initCPUTicks();
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			});
			tCPU.start();
			//replay screen
			Thread tScreen=new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
						ScreenControl scrCtr=new ScreenControl(CtrlService.this);
						scrCtr.init();
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			});
			tScreen.start();
		}
	}
	private final IBinder mBinder = new LocalBinder();
	private Notification notification;
	private NotificationManager notificationManager;
	private static final int NOTIFICATION_ID = 1;
	@SuppressWarnings("deprecation")
	public void showNotification(){
	    int icon = R.drawable.ic_launcher; 
	        
	    // icon from resources
	    CharSequence tickerText = "CtrlApp";              // ticker-text
	    long when = System.currentTimeMillis();         // notification time
	    Context context = getApplicationContext();      // application Context
	    CharSequence contentTitle = "CtrlApp";  // expanded message title
	    CharSequence contentText = "";      // expanded message text

	    Intent notificationIntent = new Intent(this, MainActivity.class);
	    //notificationIntent.putExtra("isFromIcon", true);
	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                                                notificationIntent,
	                                            PendingIntent.FLAG_UPDATE_CURRENT);
	    /* the next two lines initialize the Notification, using the
	     * configurations above.
	     */
	    notification = new Notification(icon, tickerText, when);
	    //notification.iconLevel = 2;
	    notification.setLatestEventInfo(context, contentTitle,
	                                    contentText, contentIntent);

	    /* We need to set the service to run in the foreground so that system
	     * won't try to destroy the power logging service except in the most
	     * critical situations (which should be fairly rare).  Due to differences
	     * in apis across versions of android we have to use reflection.  The newer
	     * api simultaneously sets an app to be in the foreground while adding a
	     * notification icon so services can't 'hide' in the foreground.
	     * In the new api the old call, setForeground, does nothing.
	     * See: http://developer.android.com/reference/android/app/Service.html#startForeground%28int,%20android.app.Notification%29
	     */
	    boolean foregroundSet = false;
	    try {
	      Method startForeground = getClass().getMethod("startForeground",
	                                   int.class, Notification.class);
	      startForeground.invoke(this, NOTIFICATION_ID, notification);
	      foregroundSet = true;
	    } catch (InvocationTargetException e) {
	    } catch (IllegalAccessException e) {
	    } catch(NoSuchMethodException e) {
	    }
	    if(!foregroundSet) {
	      setForeground(true);
	      notificationManager.notify(NOTIFICATION_ID, notification);
	    }
	  }
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isRunning=false;
//		am.cancel(sender);
		NetworkControl.cont=false;
		NetworkControl.cleanup();
		CPUControl.cont=false;
		CPUControl.cleanUp();
		ScreenControl.cleanUp();
		AlarmUtil.cleanUp();
	}
}
