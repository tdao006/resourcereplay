package com.example.resrcreplay;
import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
public class NetworkControl {
	public static LinkedBlockingQueue<NetworkPacket> queue;
	static String fileName="/mnt/sdcard/network.txt";
	static BufferedReader reader=null;
	static Context ctx;
	static NetworkPacket currentPacket=null;
	static boolean cont=false;
	
	static String serverIP="192.168.43.165";
	static int Port=10000;
	static int MAX_WAIT=8;//8 seconds
	
	public static void setContext(Context ctx){
		NetworkControl.ctx=ctx;
	}
	
	public static void cleanup(){
		try{
			ctx.unregisterReceiver(networkReceiver);
		}
		catch(Exception e){
			
		}
	}
	
	//then connect to the server
	public static void init() throws IOException, InterruptedException{
		reader=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		cont=true;
		queue=new LinkedBlockingQueue<NetworkPacket>();
		int prePut=100;//put 100 packets int the queue first
		
		String line=null;
		while ((line=reader.readLine())!=null && prePut>=0 && cont){
			NetworkPacket packet=new NetworkPacket(line);
			queue.put(packet);
			prePut--;
		}
		
		if (line!=null){
			Thread t=new Thread(new InitThread());
			t.start();
		}
		else{
			reader.close();
			NetworkPacket dummy=new NetworkPacket();
			//signal the last packet from the file
			queue.put(dummy);
		}
		//start the sending/recv thread
		connectServer(serverIP,Port);
		currentPacket=queue.take();
		processNextPacket();
//		Thread t=new Thread(new DoNetwork());
//		t.start();
		
	}
	static class InitThread implements Runnable{
		public void run(){
			String line=null;
			try{
				while ((line=reader.readLine())!=null && cont){
					NetworkPacket packet=new NetworkPacket(line);
					queue.put(packet);
				}
				reader.close();
				if (cont){
					NetworkPacket dummy=new NetworkPacket();
					queue.put(dummy);
				}
			}
			catch (IOException e){
				
			}
			catch (InterruptedException e){
				
			}
		}
	}
	
	static Socket socket;
//	static class DoNetwork implements Runnable{
//		public void run(){
//			try{
//				//get the next packet
//				DataInputStream iStream=new DataInputStream(socket.getInputStream());
//				DataOutputStream oStream=new DataOutputStream(socket.getOutputStream());
//				NetworkPacket packet=queue.take();
//				if (packet.type==NetworkPacket.TYPE_CLIENT_SERVER){
//					
//				}
//			}
//			catch (IOException e){
//				
//			}
//			catch (InterruptedException e){
//				
//			}
//		}
//	}
	
	static void connectServer(String ipAdd,int port) throws IOException{
		socket=new Socket(Inet4Address.getByName(ipAdd), port);
	}
	static void processNextPacket() throws IOException, InterruptedException{
		DataInputStream iStream=new DataInputStream(socket.getInputStream());
		DataOutputStream oStream=new DataOutputStream(socket.getOutputStream());
		NetworkPacket last=null;
		//packet sent from client to server
		if (currentPacket.type==NetworkPacket.TYPE_CLIENT_SERVER && currentPacket.id!=-1){
			currentPacket.send(oStream);
			last=currentPacket;
			currentPacket=queue.take();
		}
		//don't need to hold wake lock here
		//PowerManager.WakeLock wl=WakeLockUtil.holdPartialWL("NetworkCtrlWL", ctx);
		while (currentPacket.type==NetworkPacket.TYPE_SERVER_CLIENT && currentPacket.id!=-1 && cont){
			byte[] temp=new byte[(int) currentPacket.length];
			Log.d("NetworkCtrl","Waiting for server");
			iStream.readFully(temp);
			last=currentPacket;
			currentPacket=queue.take();
		}
		//meet Cl->Server again, wait
		if (currentPacket.id!=-1 && cont){
			double waitTime=(double) (currentPacket.timestamp-last.timestamp);
			if (waitTime>MAX_WAIT)
				waitTime=MAX_WAIT;
			Log.d("NetworkCtrl","Packet ID:"+currentPacket.id+", timestamp="+currentPacket.timestamp+"; last timestamp="+last.timestamp);
			Log.d("NetworkCtrl","Client -> Server, sleep: "+waitTime+" sec");
			setNetworkAlarm(waitTime);
		}
//		wl.release();
	}
	static NetworkRecv networkReceiver=new NetworkRecv();
	static String NETWORK_ACTION="cs.ucr.edu.ntuan.ResrReplay.NetworkAction";
	
	public static void setNetworkAlarm(double sec){
		int NETWORK_ID=1000;
		double millisec=sec*1000;
		AlarmUtil.setAlarm(ctx, networkReceiver, NETWORK_ACTION, NETWORK_ID, (long)millisec);
	}
	
	static class NetworkRecv extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			PowerManager.WakeLock wl=WakeLockUtil.holdPartialWL("Network_Packet_Process", ctx);
			Thread t=new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
						processNextPacket();
					}
					catch (Exception e){
						
					}
				}
			});
			t.start();
			wl.release();
		}
		
	}
}
