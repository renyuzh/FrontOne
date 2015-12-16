package com.onebus.util;

import com.onebus.OneBusApplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetWorkUtil {

	/**
	 * check net work is connected
	 * @return
	 */
	public static boolean isNetWorkConnected(Context context){
		
		if(context!=null){
			ConnectivityManager mConnectivityManager = (ConnectivityManager)  
					context.getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 
			if (mNetworkInfo != null) { 
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
	
	/**
	 * 获取当前网络类型
	 * get current network type
	 * -1：没有网络 1：WIFI网络2：wap网络3：net网络 
	 * @return
	 */
	public static int getNetWorkType(Context context){
		if (context != null) { 
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context 
			.getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 
			if (mNetworkInfo != null && mNetworkInfo.isAvailable()) { 
				return mNetworkInfo.getType(); 
			} 
		} 
		
		return -1; 

	}
}
