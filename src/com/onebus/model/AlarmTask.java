package com.onebus.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult.BusStation;
import com.baidu.mapapi.search.busline.BusLineResult.BusStep;
import com.baidu.mapapi.utils.DistanceUtil;
import com.onebus.IPAdress;
import com.onebus.util.BusCheckUtil;
import com.onebus.util.HttpUtil;
import com.onebus.util.NetWorkUtil;
import com.onebus.view.AlarmActivity;
import com.onebus.view.MainActivity;

public class AlarmTask {
	
	private String URLGETCODE = "/onebus/android/registerGetCode";
	private IPAdress IPadress;
	
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
	public boolean isFinish = false;
	public boolean isStarted = false;
	
	private String busLineId;
	private BusStation destStation;
	private int destStationIndex;
	
	private ArrayList<Bus> runBusList;
	private List<BusStep> busLineStepList;
	private ArrayList<BusStation> busLineStationList;
	
	public static int AHEAD_STATOIN = 3;	//提前几站
	
	private SharedPreferences preferences;
	
	private Context mContext;
	
	
	public AlarmTask(Context context, String busLineId, BusStation destStation,
			List<BusStep> busLineStepList,
			ArrayList<BusStation> busLineStationList) {
		mContext = context;
		this.busLineId = busLineId;
		this.destStation = destStation;
		this.busLineStepList = busLineStepList;
		this.busLineStationList = busLineStationList;
	}
	

	public AlarmTask(Context context) {
		mContext = context;
		busLineStepList = new ArrayList<BusStep>();
		busLineStationList = new ArrayList<BusStation>();
	}
	
	/**
	 * 检查是否符合条件
	 * @return
	 */
	public boolean check(){
		
		for(int i=destStationIndex-AHEAD_STATOIN; i<=destStationIndex; i++){
			if(i<0){
				continue;
			}
			for(Bus bus : runBusList){
				int code = BusCheckUtil.getBusPosition(i-1, i, new LatLng(bus.getLatitude(),bus.getLongitude()));
				if( (i==destStationIndex-AHEAD_STATOIN && code==1)
						|| (i>destStationIndex-AHEAD_STATOIN && (code==1 || code==0))){
					return true;
				}
			}

/*			if(isBusInStation(destStation.getLocation())){
				return true;
			}*/
		}
		Log.i("AlarmTask Check", "false");
		return false;
	}
	
	
	/**
	 * 判断车辆是否进站
	 * @param stationLocation
	 * @return
	 */
	private boolean isBusInStation(LatLng position){
		for(Bus bus : runBusList){
			double cDis = DistanceUtil.getDistance(new LatLng(bus.getLatitude(),bus.getLongitude()), position);
			if(cDis <= 20){
				return true;
			}
		}
		return false;
	}
	

	/**
	 * 提醒
	 */
	public void setClock(){
		
		IPadress = new IPAdress();
		URLGETCODE = "http://" + IPadress.getIP() + ":" + IPadress.getPORT()
				+ URLGETCODE;
		
		Log.i("AlarmTask setClock", "BusLineId:"+busLineId);
		SharedPreferences preferences = mContext.getSharedPreferences("LastLocation",Context.MODE_PRIVATE);
		String remindType = preferences.getString("remindType", "闹铃");
		
		SharedPreferences preferences_login = mContext.getSharedPreferences("Login", Context.MODE_PRIVATE);
		boolean login = preferences_login.getBoolean("isLogin", false);
		
		SharedPreferences preferences_user = mContext.getSharedPreferences("USERMESSAGE",
				Context.MODE_PRIVATE);
		
		if (login){
			
			if(remindType.equals("闹铃")){
				AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
				Intent intent = new Intent(mContext, AlarmActivity.class);
				
				PendingIntent.getBroadcast(mContext, 0, intent, 0);
				PendingIntent sender = PendingIntent.getActivity(mContext, 0, intent, 0);
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.add(Calendar.SECOND, 6);
				alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
			}
			
			if(remindType.equals("短信")){
				if (!NetWorkUtil
						.isNetWorkConnected(mContext)) {
					Toast.makeText(mContext,
							"网络连接失败，请检查网络是否可用!", Toast.LENGTH_SHORT).show();
					return;
				}
				getCodeFromServer(preferences_user.getString("phone", "18392120258"));
				Log.i("send Msg", "remindType 短信");
			}
			
		}else{
			
			AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(mContext, AlarmActivity.class);
			PendingIntent.getBroadcast(mContext, 0, intent, 0);
			PendingIntent sender = PendingIntent.getActivity(mContext, 0, intent, 0);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.SECOND, 3);
			alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
			
		}
		
		
	}
	
	
	public void startTask(){
		try{
			isStarted = true;
			runBusList = new ArrayList<Bus>();
			initAheadStation();	//获取提前量
			//获取预约站点的下标位置
			for(int i=0; i<busLineStationList.size(); i++){
				if(busLineStationList.get(i).getUid().equals(destStation.getUid())){
					destStationIndex = i;
				}
			}
			exec();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	private void initAheadStation(){
		if(mContext==null){
			Log.i("Alarm Task", "mContext is NULL");
		}
		preferences = mContext.getSharedPreferences("LastLocation",Context.MODE_PRIVATE);
		if(preferences==null){
			Log.i("Alarm Task", "preferences is NULL");
		}
		String aheadType = preferences.getString("remindAhead", "到站提醒");
		if (aheadType.equals("到站提醒")) {
			AHEAD_STATOIN = 0;
		} else if (aheadType.equals("提前一站")) {
			AHEAD_STATOIN = 1;
		}else if (aheadType.equals("提前二站")) {
			AHEAD_STATOIN = 2;
		}else if (aheadType.equals("提前三站")) {
			AHEAD_STATOIN = 3;
		}else if (aheadType.equals("提前四站")) {
			AHEAD_STATOIN = 4;
		} else {
			AHEAD_STATOIN = 0;
		}
		Log.i("Alarm Task", "AHEAD_STATOIN ：" + AHEAD_STATOIN);
	}
	
	
	/**
	 * 执行
	 */
	public void exec(){
		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			 
			@Override
			public void run() {
				Log.i("AlarmTask", "BusLineId:"+busLineId);
				try {
					if(!isFinish && !busLineId.trim().equals("")){
						runBusList.clear();
						//模拟，需要发送线路的每个点
						ArrayList<Bus> cBusList = HttpUtil.getRunBusList(busLineId, busLineStepList, false);
						for(Bus cBus : cBusList){
							runBusList.add(cBus);
						}
						cBusList = null;
						if(runBusList.size()!=0 && check()){
							setClock();
							isFinish = true;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 6, TimeUnit.SECONDS);	//6秒
	}
	
	/**
	 * 关闭定时操作
	 */
	public void destroyTask(){
		try{
			isFinish = true;
			runBusList.clear();
			scheduledThreadPool.shutdown();
			Log.i("AlarmTask Destroy", "BusLineId:"+busLineId);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	

	public String getBusLineId() {
		return busLineId;
	}
	public void setBusLineId(String busLineId) {
		this.busLineId = busLineId;
	}
	public BusStation getDestStation() {
		return destStation;
	}
	public void setDestStation(BusStation destStation) {
		this.destStation = destStation;
	}
	
	public ArrayList<Bus> getRunBusList() {
		return runBusList;
	}
	
	public void setRunBusList(ArrayList<Bus> runBusList) {
		this.runBusList.clear();
		this.runBusList = runBusList;
	}
	public List<BusStep> getBusLineStepList() {
		return busLineStepList;
	}
	
	public void setBusLineStepList(List<BusStep> busLineStepList) {
		this.busLineStepList.clear();
		this.busLineStepList = busLineStepList;
	}
	
	public ArrayList<BusStation> getBusLineStationList() {
		return busLineStationList;
	}
	
	public void setBusLineStationList(ArrayList<BusStation> busLineStationList) {
		this.busLineStationList.clear();
		this.busLineStationList = busLineStationList;
	}
	
	
	/**
	 * 向服务器请求验证码
	 */
	private void getCodeFromServer(final String phoneStr) {

		Log.i("send Msg", "getCodeFromServer");
		
		new Thread() {

			public void run() {

				try {

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("phone", phoneStr);
					jsonObject.put("type", 2);

					jsonArray.put(jsonObject);

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URLGETCODE);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair(
							"registerGetCode", jsonArray + "");
					paramList.add(param);

					httpPost.setEntity(new UrlEncodedFormEntity(paramList,
							"UTF_8"));

					HttpResponse httpResponse = client.execute(httpPost);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						String result = EntityUtils.toString(
								httpResponse.getEntity(), HTTP.UTF_8);
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}.start();

	}
	
	

}
