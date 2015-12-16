package com.onebus.service;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.onebus.R;
import com.onebus.model.AlarmTask;
import com.onebus.view.MainActivity;

public class AlarmService extends Service {
	
	
	ScheduledThreadPoolExecutor execThread = new ScheduledThreadPoolExecutor(1);
   
	private ArrayList<AlarmTask> taskQueue;
	
	private IBinder binder=new AlarmService.AlarmServiceBinder();
	
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i("AlarmService", "onBind");
		return binder;
	}
	
	public class AlarmServiceBinder extends Binder{
		public AlarmService getService(){
            return AlarmService.this;
        }
    }
	

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i("AlarmService", "onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent) {
		Log.i("AlarmService", "onRebind");
		super.onRebind(intent);
	}
	

	@Override
	public void onCreate() {
		Log.i("AlarmService", "onCreate");
		
		taskQueue = new ArrayList<AlarmTask>();
		execThread.scheduleAtFixedRate(new Runnable() {//每隔一段时间就触发异常
             @Override
             public void run() {
                 //TODO check task list is empty, if not,start the task
            	 Log.i("AlarmService", "running");
                 if(!taskQueue.isEmpty()){
                	 acquireWakeLock();
                	 
                	 for(AlarmTask task : taskQueue){
                		 if(!task.isStarted){
                			 task.startTask();
                		 }
                		 if(task.isFinish){
                			 removeTask(task.getBusLineId());	//删除任务
                		 }
                	 }
                 }else{
                	 releaseWakeLock();
                 }
             }
         }, 0, 10, TimeUnit.SECONDS);		//check task list every 10 seconds
		
		super.onCreate();
	}

	
	/**
	 * 添加任务，判断是否已经添加闹钟，已经添加闹钟的话先删除旧的
	 */
	public void addTask(AlarmTask task){
		try{
			for(int i=0; i<taskQueue.size(); i++){
				if(taskQueue.get(i).getBusLineId().equals(task.getBusLineId())){
					taskQueue.get(i).destroyTask();
					taskQueue.remove(i);
				}
			}
			
			//添加到队列
			taskQueue.add(task);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 取消任务
	 * @param busLineId
	 */
	public void removeTask(String busLineId){
		try{
			for(int i=0; i<taskQueue.size(); i++){
				if(taskQueue.get(i).getBusLineId().equals(busLineId)){
					taskQueue.get(i).destroyTask();
					taskQueue.remove(i);
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public boolean isTaskQueueEmpty(){
		return taskQueue.isEmpty();
	}
	

	/**
	 * 判断某条线路是否设定闹钟
	 * @param busLineId
	 * @return
	 */
	public boolean isSetAlarm(String busLineId){
		try{
			for(int i=0; i<taskQueue.size(); i++){
				if(taskQueue.get(i).getBusLineId().equals(busLineId)){
					return true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	

	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("AlarmService", "onStartCommand");
		
		/**
		 * 将服务展示到前台通知，防止被杀掉
		 
		Notification notification = new Notification(R.drawable.ic_launcher,  
		getString(R.string.app_name), System.currentTimeMillis());  
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.largeIcon =  BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		Intent serviceIntent = new Intent(this, MainActivity.class);
		serviceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);  
		PendingIntent pendingintent = PendingIntent.getActivity(this, 0,  
				serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
		notification.setLatestEventInfo(this, "One Bus", "请保持程序在后台运行",  
		pendingintent);  
		startForeground(0x111, notification);
		 */
		
		return START_STICKY;
		//return super.onStartCommand(intent, START_STICKY, startId);
	}

	public void removeAllTask(){
		for(AlarmTask task : taskQueue){
			task.destroyTask();
		}
		taskQueue.clear();
	}


	@Override
	public void onDestroy() {
		Log.i("AlarmService", "onDestroy");
		if(taskQueue.isEmpty()){
			removeAllTask();
			execThread.shutdown();
			super.onDestroy();
		}
	}
	
	
	
	WakeLock wakeLock = null;
	//获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	private void acquireWakeLock()
	{
		if (null == wakeLock)
		{
			PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BusLineAlarmService");
			if (null != wakeLock)
			{
				wakeLock.acquire();
				Log.i("AlarmService", "get Wake Lock");
			}
		}
	}
	
	//释放设备电源锁
	private void releaseWakeLock()
	{
		if (null != wakeLock)
		{
			wakeLock.release();
			wakeLock = null;
			Log.i("AlarmService", "release Wake Lock");
		}
	}

}
