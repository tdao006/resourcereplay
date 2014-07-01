package com.example.resrcreplay;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class LoadApp {
	Context ctx;
	String packageName;
	public LoadApp(String pakName,Context ctx){
		this.ctx=ctx;
		packageName=pakName;
	}
	public void loadApp(){
		PackageManager pkMag=ctx.getPackageManager();
		Intent intent=pkMag.getLaunchIntentForPackage(packageName);
		if (intent != null)
		{
		    // start the activity
		    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    ctx.startActivity(intent);
		}
	}
}
