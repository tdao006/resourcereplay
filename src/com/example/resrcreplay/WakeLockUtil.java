package com.example.resrcreplay;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class WakeLockUtil {
	public static WakeLock holdPartialWL(String tag, Context ctx){
		PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, tag);
		wl.acquire();
		return wl;
	}
	public static void releasePartialWL(PowerManager.WakeLock wl){
		wl.release();
	}
	
}
