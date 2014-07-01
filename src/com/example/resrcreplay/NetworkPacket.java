package com.example.resrcreplay;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.io.*;

public class NetworkPacket implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8353044196126647140L;
	public static byte TYPE_SERVER_CLIENT=0;
	public static byte TYPE_CLIENT_SERVER=1;
	//change client ip when changing file
	public static String CLIENT_IP="192.168.1.109";
	public double timestamp;
	public short length;
//	long nextLength=0;
	public byte type;
	public int id;
	public NetworkPacket(){
		//a dummy packet
		id=-1;
	}
	//read network packet from one line
	public NetworkPacket(String str){
		str=str.trim();
		str=str.replaceAll(" {2,}"," ");
		String[] temp=str.split(" ");
		id=Integer.parseInt(temp[0]);
		timestamp=Double.parseDouble(temp[1]);
		String src=temp[2];
		//if (src.equals(CLIENT_IP)){
		if (src.contains("169.235") || src.contains("10.") || src.contains("192.") || src.contains("172.")){
			type=TYPE_CLIENT_SERVER;
		}
		else{
			type=TYPE_SERVER_CLIENT;
		}
		length=(short) (Short.parseShort(temp[6])-40);
		for (String s : temp){
			if (s.startsWith("Len=")){
				try{
					length=Short.parseShort(s.substring(4));
				}
				catch(NumberFormatException e){
					length=0;
				}
			}
		}
	}
	public String toString(){
		String strType;
		if (type==TYPE_SERVER_CLIENT){
			strType="TYPE_SERVER_CLIENT";
		}
		else{
			strType="TYPE_CLIENT_SERVER";
		}
		String str=("id="+id+";timestamp="+timestamp+";Len="+length+
				";type="+strType);
		return str;
	}
	public void send(DataOutputStream str) throws IOException{
		if (length==0)
			return;
		byte[] temp=null;
		temp=new byte[(int)length];
		str.write(temp);
	}
}
