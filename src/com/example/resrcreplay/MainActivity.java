package com.example.resrcreplay;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
	Button btnStart;
	Intent serviceIntent;
	
	BroadcastReceiver screenOff=new TurnScreenOff();
	public static String SCREEN_OFF = "tuandao.ucr.edu.SCREEN_OFF";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		serviceIntent=new Intent(this,CtrlService.class);
		mConnection=new MyServiceConnection();
		btnStart=(Button)findViewById(R.id.button1);
		btnStart.setOnClickListener(this);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		IntentFilter iFilter=new IntentFilter(SCREEN_OFF);
		registerReceiver(screenOff, iFilter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (mService==null){
			startService(serviceIntent);
			bindService(serviceIntent, mConnection,0);
			btnStart.setText("Stop");
		}
		else{
			stopService(serviceIntent);
			btnStart.setText("Start");
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		bindService(serviceIntent, mConnection,0);
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unbindService(mConnection);
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(screenOff);
	}
	private CtrlService mService=null;
	private MyServiceConnection mConnection;
	class MyServiceConnection implements ServiceConnection{

		public void onServiceConnected(ComponentName className, IBinder service) {
			// TODO Auto-generated method stub
			mService=((CtrlService.LocalBinder)service).getService();
			btnStart.setText("Stop");
		}

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			mService=null;
			btnStart.setText("Start");
		}
    	
    };
    class TurnScreenOff extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}    	
    }
}
