package com.onebus;

import java.util.Calendar;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.onebus.view.AlarmActivity;
import com.onebus.service.AlarmService;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class OneBusApplication extends Application {
	
	public static String GPS_CITY = "";
	public static String CURRENT_CITY = "西安市";
	public static String CURRENT_POSITION = "";
	public static LatLng CURRENT_LOCATION = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		SDKInitializer.initialize(this);
		
		SharedPreferences preferences = getSharedPreferences("LastLocation",Context.MODE_PRIVATE);
		CURRENT_CITY = preferences.getString("currentCity", "西安市");
		CURRENT_POSITION = preferences.getString("LastAddress", "");
		preferences = null;
		
		//开启后台服务
		Intent intent=new Intent(this,AlarmService.class);
        startService(intent);

	}

	@Override
	public void onTerminate() {
		Log.i("OneBusApplication", "onTerminate");
		super.onTerminate();
	}
	
	

}
