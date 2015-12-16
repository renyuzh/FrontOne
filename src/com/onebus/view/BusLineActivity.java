package com.onebus.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineResult.BusStation;
import com.baidu.mapapi.search.busline.BusLineResult.BusStep;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.onebus.OneBusApplication;
import com.onebus.R;
import com.onebus.model.AlarmTask;
import com.onebus.model.Bus;
import com.onebus.service.AlarmService;
import com.onebus.util.BusCheckUtil;
import com.onebus.util.FavoriteUtil;
import com.onebus.util.HttpUtil;
import com.onebus.util.NetWorkUtil;
import com.onebus.widget.BusLineListViewAdapter;
import com.onebus.widget.HorizontalListView;


/**
 * 显示搜索的公交 
 * 传入参数 busLineId
 * 
 * @author John
 *
 */
public class BusLineActivity extends FragmentActivity implements OnGetBusLineSearchResultListener
							,OnGetPoiSearchResultListener{
	
	private ImageButton mImageButtonBack;
	private Button mButtonShowMap;
	private ImageButton mImageButtonAlarm;
	private ProgressDialog mProgressDialog;
	
	private TextView mBusLineNameTextView;
	private TextView mBusLineStartTimeTextView;
	private TextView mBusLineEndTimeTextView;
	private LinearLayout mSameStationBusLineTextView;
	
	private TextView mRoadStatusTextView;
	private TextView mBusStatusTextView;
	private String roadStatus = "";
	private String busStatus = "";
	private int curBusIndex = -1;
	
	private TextView mNearestTimeTextView;
	private TextView mNearestDistanceTextView;
	
	private ImageView mCollectImageView;
    private boolean isCollect;
	
	private PoiSearch mPoiSearch;	//PoiSearch
	private BusLineSearch mBusLineSearch = null;	//公交搜索
	private HorizontalListView mBusStationListView;			//公交站点列表
	private BusLineListViewAdapter mBusLineListViewAdapter;	
	private ArrayList<BusStation> mBusStationList;
	private BusLineResult mBusLineResult;	//当前公交线路搜索结果，用于地图展示
	private List<BusStep> mBusLineStepList;
	
	private List<Bus> mCurrentRunBusList;
	
	private boolean sendData = true;
	
	
	private LinearLayout mBusStationsLayout;
	private RelativeLayout mMapLayout;
	private MapFragment mMapFragment;
	private MapView mMapView;
	private BaiduMap mBaiduMap;

	private LocationClient mLocClient;
	//private MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	private boolean isFirstLoc = true;
	private BusLineOverlay mBusLineOverlay;

	
	private final static int STATUS_LIST = 0x1;
	private final static int STATUS_MAP = 0x2;
	private static int currentStatus = STATUS_LIST;
	
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
	private ArrayList<Bus> runBusList;
	private int currentSelectIndex = 0;
	private static String busLineId;
	
	private AlarmService mService = null;
	
	private String nearestTime = "<font color=\"#DA765B\"><big>0</big></font>"+
			"<font color=\"#AAAAAA\"><small>分</small></font>"+
			"<font color=\"#DA765B\"><big>0</big></font>"+
			"<font color=\"#AAAAAA\"><small>秒</small></font>";
	private String nearestDistance = "<font color=\"#228FBD\"><big>0</big></font>"+
			"<font color=\"#AAAAAA\"><small>站</small></font>"+
			"<font color=\"#228FBD\"><big>0</big></font>"+
			"<font color=\"#AAAAAA\"><small>米</small></font>";
	
	private Date busLineStartTime, busLineEndTime;
	
	private boolean isToasted = false;	//是否已经提示用户网络连接异常
	
	
	private final static int MSG_UPDATE = 0x1;
	private final static int MSG_UPDATE_TIPS = 0x2;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_UPDATE:{
				
				mNearestTimeTextView.setText(Html.fromHtml(nearestTime));
				mNearestDistanceTextView.setText(Html.fromHtml(nearestDistance));
				mBusLineListViewAdapter.notifyDataSetChanged();
				break;
			}
			case MSG_UPDATE_TIPS:{
				if(msg.arg1!=-1){
					
					double rd = Math.random();
					if(rd<0.3){
						roadStatus = "拥堵";
						mRoadStatusTextView.setTextColor(Color.parseColor("#DB4527"));
						mRoadStatusTextView.setBackgroundResource(R.drawable.bus_tip_shape_red);
					}else {
						roadStatus = "畅行";
						mRoadStatusTextView.setTextColor(Color.parseColor("#15AD66"));
						mRoadStatusTextView.setBackgroundResource(R.drawable.bus_tip_shape_green);
					}
					mRoadStatusTextView.setText(roadStatus);
					
					rd = Math.random();
					if(rd<0.3){
						busStatus = "拥挤";
						mBusStatusTextView.setTextColor(Color.parseColor("#DB4527"));
						mBusStatusTextView.setBackgroundResource(R.drawable.bus_tip_shape_red);
					}else {
						busStatus = "有座";
						mBusStatusTextView.setTextColor(Color.parseColor("#15AD66"));
						mBusStatusTextView.setBackgroundResource(R.drawable.bus_tip_shape_green);
					}
					mBusStatusTextView.setText(busStatus);

					
				}else{
					mBusStatusTextView.setTextColor(Color.parseColor("#AAAAAA"));
					mBusStatusTextView.setBackgroundResource(R.drawable.bus_tip_shape_gray);
					mBusStatusTextView.setText("未知");
					mRoadStatusTextView.setTextColor(Color.parseColor("#AAAAAA"));
					mRoadStatusTextView.setBackgroundResource(R.drawable.bus_tip_shape_gray);
					mRoadStatusTextView.setText("未知");
				}
				
				break;
			}
			default:super.handleMessage(msg);
			}
			
		}
		
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_line);
		
		try{
			init();
			initMapView();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void initMapView(){
		mMapLayout = (RelativeLayout)findViewById(R.id.busline_mapLayout);
		mMapLayout.setVisibility(View.GONE);

		mMapFragment = ((MapFragment) (getSupportFragmentManager()
				.findFragmentById(R.id.busline_mapView)));
		mMapView = mMapFragment.getMapView();
		mBaiduMap = mMapFragment.getBaiduMap();

		mMapView.setClickable(false);
	}
	

	private void init() throws Exception{
		mBusStationsLayout = (LinearLayout)findViewById(R.id.busline_listLayout);
		
		mImageButtonBack = (ImageButton)findViewById(R.id.busline_btn_back);
		mImageButtonBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		mButtonShowMap = (Button)findViewById(R.id.busline_btn_showmap);
		mButtonShowMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 根据状态展示地图和列表
				if(currentStatus==STATUS_MAP){
					changeStatus(STATUS_LIST);
				}
				else{
					showMap(currentSelectIndex);
				}
			}

		});
		
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		mSameStationBusLineTextView = (LinearLayout)findViewById(R.id.busline_sameSationBusLine);
		mSameStationBusLineTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPoiSearch.searchNearby((new PoiNearbySearchOption())
						.location(OneBusApplication.CURRENT_LOCATION).radius(2000).keyword(
								mBusStationList.get(currentSelectIndex).getTitle()+" 公交"));
			}
		});
		
		/**车辆运行情况和车内拥挤程度**/
		mRoadStatusTextView = (TextView)findViewById(R.id.busline_road_status);
		mBusStatusTextView = (TextView)findViewById(R.id.busline_bus_status);
		
		mImageButtonAlarm = (ImageButton)findViewById(R.id.busline_btn_alarm);
		mImageButtonAlarm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 开启提醒或者关闭提醒
				try{
					mImageButtonAlarm.setClickable(false);
					
					if(mService!=null){
						if(mService.isSetAlarm(busLineId)){
							mService.removeTask(busLineId);
							mImageButtonAlarm.setImageResource(R.drawable.tixing);
						}else{
							AlarmTask task = new AlarmTask(getApplicationContext(), busLineId, mBusStationList.get(currentSelectIndex),
									mBusLineStepList, mBusStationList);
							mService.addTask(task);
							mImageButtonAlarm.setImageResource(R.drawable.tixingon);
						}
					}else{
						Log.i("AlarmButton", "Service is NULL");
					}
					mImageButtonAlarm.setClickable(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
		//绑定开启AlarmService
		Intent intent=new Intent(this,AlarmService.class);
        bindService(intent, new ServiceConnection(){
           @Override
           public void onServiceConnected(ComponentName componentName, IBinder binder) {
               mService=((AlarmService.AlarmServiceBinder)binder).getService();
               if(mService.isSetAlarm(busLineId)){
            	   mImageButtonAlarm.setImageResource(R.drawable.tixingon);
               }
           }

           @Override
           public void onServiceDisconnected(ComponentName componentName) {
        	   mService=null;
           }     
        }, Context.BIND_AUTO_CREATE);
		
		mBusLineNameTextView = (TextView)findViewById(R.id.busline_name);
		mBusLineStartTimeTextView = (TextView)findViewById(R.id.busline_starttime);
		mBusLineEndTimeTextView = (TextView)findViewById(R.id.busline_endtime);
		mNearestTimeTextView = (TextView)findViewById(R.id.busline_nearest_time);
		mNearestDistanceTextView = (TextView)findViewById(R.id.busline_nearest_distance);
		
		mNearestTimeTextView.setText(Html.fromHtml(nearestTime));
		mNearestDistanceTextView.setText(Html.fromHtml(nearestDistance));
		
		mBusStationListView = (HorizontalListView)findViewById(R.id.busline_stations);
		mBusStationList = new ArrayList<BusLineResult.BusStation>();
		runBusList = new ArrayList<Bus>();
		mBusLineListViewAdapter = new BusLineListViewAdapter(this, mBusStationList, runBusList);
		mBusStationListView.setAdapter(mBusLineListViewAdapter);
		mBusStationListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				currentSelectIndex = position;
				mBusLineListViewAdapter.setSelectIndex(currentSelectIndex);
				mBusLineListViewAdapter.notifyDataSetChanged();
				calculate();
				
				Message msgTip = mHandler.obtainMessage();
				msgTip.what = MSG_UPDATE_TIPS;
				msgTip.arg1 = curBusIndex;
				mHandler.sendMessage(msgTip);
				
			}
		});
		
		mCollectImageView = (ImageView)findViewById(R.id.busline_button_collect_img);
		
		mBusLineSearch = BusLineSearch.newInstance();
		mBusLineSearch.setOnGetBusLineSearchResultListener(this);
		busLineId = getIntent().getExtras().getString("busLineId");
		if(busLineId!=null){
			//TODO 查看此线路是否已经被收藏
			isCollect = FavoriteUtil.favoriteBusLineListContains(getApplicationContext(), busLineId);
			if(isCollect){
				mCollectImageView.setImageResource(R.drawable.icon_collect_select);
			}
			
			if(NetWorkUtil.isNetWorkConnected(getApplicationContext())){
				mProgressDialog = new ProgressDialog(BusLineActivity.this);
				mProgressDialog.setIcon(R.drawable.common_topbar_route_foot_pressed);
				mProgressDialog.setMessage("请稍等....");
				mProgressDialog.show();
				
				//获取线路
				mBusStationList.clear();
				mBusLineSearch.searchBusLine((new BusLineSearchOption()  
			    .city(OneBusApplication.CURRENT_CITY).uid(busLineId)));
			}else{
				Toast.makeText(BusLineActivity.this, getResources().getString(R.string.networkdisabled), Toast.LENGTH_LONG).show();
			}
		}
		else{
			Toast.makeText(BusLineActivity.this, getResources().getString(R.string.notFoundResult), Toast.LENGTH_LONG).show();
		}
		
	}
	
	/**
	 * 显示地图
	 * @param position 点击第几个公交站点
	 */
	private void showMap(int position) {
		calculate();
		changeStatus(STATUS_MAP);
		
		mBaiduMap.clear();
		mBusLineOverlay = new BusLineOverlay(mBaiduMap);
		mBaiduMap.setOnMarkerClickListener(mBusLineOverlay);
		mBusLineOverlay.setData(mBusLineResult);
		mBusLineOverlay.addToMap();
		mBusLineOverlay.zoomToSpan();
		
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(
				mBusLineResult.getStations().get(position).getLocation(), (float) (mBaiduMap.getMaxZoomLevel()*0.75));
		mBaiduMap.animateMapStatus(u);
		//画点
		BitmapDescriptor stationBitmap = BitmapDescriptorFactory  
			    .fromResource(R.drawable.ic_locate);
		OverlayOptions option = new MarkerOptions()  
	    .position(mBusLineResult.getStations().get(position).getLocation()).anchor(0.5f, 1.0f).zIndex(100).icon(stationBitmap);
		mBaiduMap.addOverlay(option);
		
		BitmapDescriptor bitmap = BitmapDescriptorFactory  
			    .fromResource(R.drawable.walk_turn);
		if(runBusList!=null){
			for(Bus bus : runBusList){
				OverlayOptions cOption = new MarkerOptions()  
			    .position(new LatLng(bus.getLatitude(),bus.getLongitude())).anchor(0.5f, 1.0f).zIndex(100).icon(bitmap);
				mBaiduMap.addOverlay(cOption);
			}
		}

	}

	

	/**
	 * 展示搜索结果
	 */
	@Override
	public void onGetBusLineResult(BusLineResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(BusLineActivity.this, getResources().getString(R.string.notFoundResult), Toast.LENGTH_LONG).show();
			mProgressDialog.dismiss();
			return;
	    }
		try{
			mBusLineNameTextView.setText(result.getBusLineName()+"");
			if(result.getStartTime().getMinutes()==0){
				mBusLineStartTimeTextView.setText(""+result.getStartTime().getHours()+":00");
			}else{
				mBusLineStartTimeTextView.setText(""+result.getStartTime().getHours()+":"+result.getStartTime().getMinutes());
			}
			if(result.getEndTime().getMinutes()==0){
				mBusLineEndTimeTextView.setText(""+result.getEndTime().getHours()+":00");
			}else{
				mBusLineEndTimeTextView.setText(""+result.getEndTime().getHours()+":"+result.getEndTime().getMinutes());
			}
			
			busLineStartTime = result.getStartTime();
			busLineEndTime = result.getEndTime();
			
			mBusLineStepList = result.getSteps();
			mBusLineResult= result;
			
			//TODO 检查距离自己最近的公交站点，显示列表在某个位置
			double minDistance = Integer.MAX_VALUE;
			int i = 0;
			currentSelectIndex = 0;
			for(BusStation station : result.getStations()){
				double cDis = DistanceUtil.getDistance(station.getLocation(), OneBusApplication.CURRENT_LOCATION);
				if(cDis < minDistance){
					minDistance = cDis;
					currentSelectIndex = i;
				}
				mBusStationList.add(station);
				i++;
			}
			
			BusCheckUtil.initBusLineData(mBusLineStepList, mBusStationList);
			
			
			scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			 
				@Override
				public void run() {
					Log.i("BusLine ScheduledThreadPool", "running");
					try {
						if(NetWorkUtil.isNetWorkConnected(getApplicationContext())){
							if(!busLineId.trim().equals("")){
								synchronized(runBusList){
									runBusList.clear();
									//模拟，需要发送线路的每个点
									ArrayList<Bus> cBusList = HttpUtil.getRunBusList(busLineId, mBusLineStepList, sendData);
									sendData = false;
									for(Bus cBus : cBusList){
										runBusList.add(cBus);
									}
									cBusList = null;
									runBusList.notifyAll();
								}
								calculate();
							}
						}else{
							if(!isToasted){
								Toast.makeText(BusLineActivity.this, 
										getResources().getString(R.string.networkdisabled), Toast.LENGTH_LONG).show();
							}
						}
						
					} catch (Exception e) {
						Log.i("scheduledThreadPool", ""+e.getMessage());
						e.printStackTrace();
					}
				}
			}, 0, 10, TimeUnit.SECONDS);	//10秒

			
			mBusLineListViewAdapter.setSelectIndex(currentSelectIndex);
			mBusLineListViewAdapter.notifyDataSetChanged();
			mProgressDialog.dismiss();
			
			//第一次刷新
			calculate();
			Message msgTip = mHandler.obtainMessage();
			msgTip.what = MSG_UPDATE_TIPS;
			msgTip.arg1 = curBusIndex;
			mHandler.sendMessage(msgTip);

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void calculate(){
		synchronized(runBusList){
			boolean hasCar = false;
			//遍历前面的站点，查看是否有公交车
			outer:
			for(int i=currentSelectIndex; i>0; i--){
				for(Bus bus : runBusList){
					int code = BusCheckUtil.getBusPosition(i-1, i, new LatLng(bus.getLatitude(),bus.getLongitude()));
					if(code==1){		//in station
						hasCar = true;
						setNearestTimeAndDistance(i, 1, mBusStationList.get(i).getLocation());
						break outer;
					}else if(code==0){	//near
						hasCar = true;
						setNearestTimeAndDistance(i, 2, new LatLng(bus.getLatitude(),bus.getLongitude()));
						break outer;
					}
				}
			}
			if(!hasCar){
				//没有车辆
				setNearestTimeAndDistance(-1,-1, null);
			}
			runBusList.notifyAll();
		}
	}
	
	
	
	/**
	 * 计算车到站的时间和距离
	 */
	public void calculate0(){
		synchronized(runBusList){
			boolean hasCar = false;
			for(int i=0; i<=currentSelectIndex; i++){
				if(isBusInStation(mBusStationList.get(i).getLocation())){
					hasCar = true;
					setNearestTimeAndDistance(i, 1, mBusStationList.get(i).getLocation());
				} else {
					LatLng busPosition = isBusNear(i);
					if(busPosition!=null){
						hasCar = true;
						setNearestTimeAndDistance(i, 2, busPosition);
					}
				}
			}
			if(!hasCar){
				//没有车辆
				setNearestTimeAndDistance(-1,-1, null);
			}
			runBusList.notifyAll();
		}
	}
	
	
	/**
	 * 判断车辆是否进站
	 * @param stationLocation
	 * @return
	 */
	private boolean isBusInStation(LatLng position){
		//TODO 判断公交车是否进站
		for(Bus bus : runBusList){
			double cDis = DistanceUtil.getDistance(new LatLng(bus.getLatitude(),bus.getLongitude()), position);
			if(cDis <= 10){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断车辆是否快抵达此站
	 * @return
	 */
	private LatLng isBusNear(int position){
		//TODO 判断公交车是否在此站之前，上一个站点之后
		if(position==0) return null;
		
		LatLng startPos = mBusStationList.get(position-1).getLocation();
		LatLng endPos = mBusStationList.get(position).getLocation();
		
		double cLineDis = DistanceUtil.getDistance(startPos, endPos);
		LatLng cricleCenter = new LatLng((startPos.latitude+endPos.latitude)/2,
				(startPos.longitude+endPos.longitude)/2);
		for(Bus bus : runBusList){
			double cDis = DistanceUtil.getDistance(new LatLng(bus.getLatitude(),bus.getLongitude()), endPos);
			
			if(SpatialRelationUtil.isCircleContainsPoint(cricleCenter,
					(int) DistanceUtil.getDistance(startPos, endPos)/2, 
					new LatLng(bus.getLatitude(),bus.getLongitude())) 
					&& DistanceUtil.getDistance(new LatLng(bus.getLatitude(),bus.getLongitude()), startPos)>10){
				return new LatLng(bus.getLatitude(),bus.getLongitude());
			}
		}
		
		return null;
	}
	
	
	public void setNearestTimeAndDistance(int busIndex, int type, LatLng busPosition){
		
		nearestTime = "";
		nearestDistance = "";
		curBusIndex = busIndex;
		if(busIndex!=-1){
			int stationNumber = currentSelectIndex-busIndex;
			if(busIndex==currentSelectIndex){
				//即将到站
				if(type==1){
					if(currentSelectIndex==0){
						//已经进站
						nearestTime = "<font color=\"#DA765B\"><big>已发车</big></font>";
					}else{
						//已经进站
						nearestTime = "<font color=\"#DA765B\"><big>车辆进站</big></font>";
					}
					
					nearestDistance = "<font color=\"#228FBD\"><big>0</big></font>"+
							"<font color=\"#AAAAAA\"><small>站</small></font>"+
							"<font color=\"#228FBD\"><big>0</big></font>"+
							"<font color=\"#AAAAAA\"><small>米</small></font>";
				}else{
					//即将进站
					double nearDistance = DistanceUtil.getDistance(busPosition, 
							mBusStationList.get(currentSelectIndex).getLocation());
					String dis = "";
					if(nearDistance>1000){
						nearDistance = nearDistance/1000;
						dis = String.format("%.1f", nearDistance)
								+"</big></font><font color=\"#AAAAAA\"><small>公里</small></font>";
					}else{
						dis = (int)nearDistance + "</big></font><font color=\"#AAAAAA\"><small>米</small></font>";
					}
					double nearTime = nearDistance/8.3333333;
					int nearMinutes = (int) (nearTime/60);
					int nearSeconds = (int) (nearTime-nearMinutes*60);
					if(nearMinutes>0){
						nearestTime = "<font color=\"#DA765B\"><big>"+nearMinutes+"</big></font>"+
										"<font color=\"#AAAAAA\"><small>分</small></font>";
					}
					nearestTime += "<font color=\"#DA765B\"><big>"+nearSeconds+"</big></font>"+
							"<font color=\"#AAAAAA\"><small>秒</small></font>";
					nearestDistance = "<font color=\"#228FBD\"><small>即将到站</small></font>"+
							"<font color=\"#228FBD\"><big>"+(int)nearDistance 
							+ "</big></font><font color=\"#AAAAAA\"><small>米</small></font>";;
				}
				
			}else{
				//计算离几站
				double mutipleDistance = DistanceUtil.getDistance(busPosition, 
						mBusStationList.get(busIndex).getLocation());
				for(int i=busIndex; i<currentSelectIndex; i++){
					mutipleDistance += DistanceUtil.getDistance(mBusStationList.get(busIndex).getLocation(), 
							mBusStationList.get(busIndex+1).getLocation());
				}
				
				double mutipleTime = mutipleDistance/8.3333333;
				int mutipleMinutes = (int) (mutipleTime/60);
				int mutipleSeconds = (int) (mutipleTime-mutipleMinutes*60);
				
				String dis = "";
				if(mutipleDistance>1000){
					mutipleDistance = mutipleDistance/1000;
					dis = String.format("%.1f", mutipleDistance)
							+"</big></font><font color=\"#AAAAAA\"><small>公里</small></font>";
				}else{
					dis = (int)mutipleDistance + "</big></font><font color=\"#AAAAAA\"><small>米</small></font>";
				}

				nearestDistance = "<font color=\"#228FBD\"><big>"+stationNumber+"</big></font>"+
						"<font color=\"#AAAAAA\"><small>站</small></font>"+
						"<font color=\"#228FBD\"><big>"+dis;
				nearestTime = "<font color=\"#DA765B\"><big>"+mutipleMinutes+"</big></font>"+
						"<font color=\"#AAAAAA\"><small>分</small></font>"+
						"<font color=\"#DA765B\"><big>"+mutipleSeconds+"</big></font>"+
						"<font color=\"#AAAAAA\"><small>秒</small></font>";
			}
		}else{
			if(checkTime()==0){	
				nearestTime = "<font color=\"#DA765B\"><big>等待发车</big></font>";
				nearestDistance = "<font color=\"#228FBD\"><big>0</big></font>"+
						"<font color=\"#AAAAAA\"><small>站</small></font>"+
						"<font color=\"#228FBD\"><big>0</big></font>"+
						"<font color=\"#AAAAAA\"><small>米</small></font>";
			}else if(checkTime()<0){
				nearestTime = "<font color=\"#AAAAAA\"><big>首班未发</big></font>";
				nearestDistance = "<font color=\"#228FBD\"><big>0</big></font>"+
						"<font color=\"#AAAAAA\"><small>站</small></font>"+
						"<font color=\"#228FBD\"><big>0</big></font>"+
						"<font color=\"#AAAAAA\"><small>米</small></font>";
			}else if(checkTime()>0){
				nearestTime = "<font color=\"#AAAAAA\"><big>末班已过</big></font>";
				nearestDistance = "<font color=\"#228FBD\"><big>0</big></font>"+
						"<font color=\"#AAAAAA\"><small>站</small></font>"+
						"<font color=\"#228FBD\"><big>0</big></font>"+
						"<font color=\"#AAAAAA\"><small>米</small></font>";
			}
		}

		Message msg = mHandler.obtainMessage();
		msg.what = MSG_UPDATE;
		mHandler.sendMessage(msg);

	}
	
	/**
	 * 判断当前时间是否还在发车
	 * @return <ol><li><0 还未发车-1</li>  <li>=0 等待发车0</li>  <li>>0 不再发车1</li></ol>
	 */
	@SuppressWarnings("deprecation")
	private int checkTime(){
		//TODO 判断时间
		Date curTime = new Date();
		if(curTime.getHours() < busLineStartTime.getHours()){
			return -1;
		}else if(curTime.getHours() > busLineEndTime.getHours()){
			return 1;
		}else {
			if(curTime.getHours() == busLineStartTime.getHours()){
				if(curTime.getMinutes() < busLineStartTime.getMinutes()){
					return -1;
				}else{
					return 0;
				}
			}else if(curTime.getHours() == busLineEndTime.getHours()){
				if(curTime.getMinutes() < busLineEndTime.getMinutes()){
					return 0;
				}else {
					return 1;
				}
			}else {
				return 0;
			}
			
		}
	}
	

	private void changeStatus(int status){
		switch(status){
		case STATUS_LIST:{
			mBusStationsLayout.setVisibility(View.VISIBLE);
			mMapLayout.setVisibility(View.GONE);
			mMapView.setClickable(false);
			currentStatus = STATUS_LIST;
			mButtonShowMap.setText(getResources().getString(R.string.map));
			break;
		}
		case STATUS_MAP:{
			mBusStationsLayout.setVisibility(View.GONE);
			mMapLayout.setVisibility(View.VISIBLE);
			mMapView.setClickable(true);
			currentStatus = STATUS_MAP;
			mButtonShowMap.setText(getResources().getString(R.string.list));
			break;
		}
		}
	}
	
	
	/**
	 * 处理界面底部按钮点击
	 * @param v
	 */
	public void processAction(View v){
		switch(v.getId()){	
		case R.id.busline_button_collect:
		{
			//TODO 收藏未实现
			if(!isCollect){
				FavoriteUtil.collect(getApplicationContext(), FavoriteUtil.BUS_LINE, busLineId+":"
								+mBusLineNameTextView.getText().toString());
				Toast.makeText(BusLineActivity.this, "已添加到收藏夹", Toast.LENGTH_SHORT).show();
				mCollectImageView.setImageResource(R.drawable.icon_collect_select);
				isCollect = true;
			}else{
				FavoriteUtil.cancelFavoriteBusLine(getApplicationContext(), busLineId);
				Toast.makeText(BusLineActivity.this, "已取消收藏", Toast.LENGTH_SHORT).show();
				mCollectImageView.setImageResource(R.drawable.icon_collect);
				isCollect = false;
			}
			break;
		}

		default:return;
		
		}
	}
	
	
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mPoiSearch.destroy();
		mBusLineSearch.destroy();
		if(scheduledThreadPool!=null){
			scheduledThreadPool.shutdown();
		}
		
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if(currentStatus == STATUS_MAP){
			changeStatus(STATUS_LIST);
		}else{
			super.onBackPressed();
		}
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult arg0) {
	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(BusLineActivity.this, getResources().getString(R.string.notFoundResult), Toast.LENGTH_LONG).show();
			return;
		}
		
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			for (PoiInfo poi : result.getAllPoi()) {
		        if (poi.type == PoiInfo.POITYPE.BUS_STATION) {
			    	Intent stationBusLineIntent = new Intent(BusLineActivity.this, StationBusLineActivity.class);
					String[] busLineNameList = (poi.address).split(";");
					stationBusLineIntent.putExtra("busLineNameList", busLineNameList);
					stationBusLineIntent.putExtra("stationName", poi.name + "-公交车站");
					startActivity(stationBusLineIntent);
					return;
		        }
		    }

		}
	}

}
