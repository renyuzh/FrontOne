package com.onebus.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.speech.VoiceRecognitionService;
import com.onebus.OneBusApplication;
import com.onebus.R;
import com.onebus.service.AlarmService;
import com.onebus.util.Constant;
import com.onebus.util.NetWorkUtil;
import com.onebus.widget.MainWidgetImageButton;
import com.onebus.widget.MyOrientationListener;
import com.onebus.widget.MyOrientationListener.OnOrientationListener;

public class MainActivity extends Activity implements RecognitionListener,
		OnGetPoiSearchResultListener {

	private static final int REQUEST_UI = 1;

	// location
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;

	MapView mMapView;
	BaiduMap mBaiduMap;

	private ImageButton mSearchImageButton;

	/**
	 * location button
	 */
	ImageButton locationImg;
	boolean isFirstLoc = true;

	/**
	 * zoom in . zoom out
	 */
	private ImageButton zoomInButton;
	private ImageButton zoomOutButton;
	private float zoomLevel;

	private MainWidgetImageButton mButtonNearBy;
	private MainWidgetImageButton mButtonRoute;
	private MainWidgetImageButton mButtonNav;
	private MainWidgetImageButton mButtonMine;

	private AlarmService mService = null;

	public static final int STATUS_None = 0;
	public static final int STATUS_WaitingReady = 2;
	public static final int STATUS_Ready = 3;
	public static final int STATUS_Speaking = 4;
	public static final int STATUS_Recognition = 5;
	private SpeechRecognizer speechRecognizer;
	private int status = STATUS_None;
	private long speechEndTime = -1;
	private static final int EVENT_ERROR = 11;

	// 周边站点定位
	private PoiSearch mPoiSearch; // PoiSearch
	private PoiOverlay overlay;
	private ArrayList<HashMap<String, Object>> mStationList; // nearby bus
																// station list
	/**
	 * 最新一次的经纬度
	 */
	private double mCurrentLantitude;
	private double mCurrentLongitude;
	/**
	 * 当前的精度
	 */
	private float mCurrentAccracy;
	/**
	 * 方向传感器的监听器
	 */
	private MyOrientationListener myOrientationListener;
	/**
	 * 方向传感器X方向的值
	 */
	private int mXDirection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.i("MainActivity", "OnCreate");

		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this,
				new ComponentName(this, VoiceRecognitionService.class));

		speechRecognizer.setRecognitionListener(this);

		mStationList = new ArrayList<HashMap<String, Object>>();

		try {
			initMap();
			initWidget();

			// 初始化传感器
			initOritationListener();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 初始化方向传感器
	 */
	private void initOritationListener() {
		myOrientationListener = new MyOrientationListener(
				getApplicationContext());
		myOrientationListener
				.setOnOrientationListener(new OnOrientationListener() {
					@Override
					public void onOrientationChanged(float x) {
						mXDirection = (int) x;

						// 构造定位数据
						MyLocationData locData = new MyLocationData.Builder()
								.accuracy(mCurrentAccracy)
								// 此处设置开发者获取到的方向信息，顺时针0-360
								.direction(mXDirection)
								.latitude(mCurrentLantitude)
								.longitude(mCurrentLongitude).build();
						// 设置定位数据
						mBaiduMap.setMyLocationData(locData);

						mBaiduMap
								.setMyLocationConfigeration(new MyLocationConfiguration(
										mCurrentMode, true, null));

						// 设置自定义图标
						// BitmapDescriptor mCurrentMarker =
						// BitmapDescriptorFactory
						// .fromResource(R.drawable.navi_map_gps_locked);
						//
						// MyLocationConfiguration config = new
						// MyLocationConfiguration(
						// mCurrentMode, true, mCurrentMarker);
						// mBaiduMap.setMyLocationConfigeration(config);

					}
				});
	}

	/**
	 * 初始化周边站点信息
	 */
	private void initNearby() {

		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);

		if (NetWorkUtil.isNetWorkConnected(getApplicationContext())) {

			mPoiSearch.searchNearby((new PoiNearbySearchOption())
					.location(OneBusApplication.CURRENT_LOCATION).radius(2000)
					.keyword("公交"));

		} else {
			Toast.makeText(MainActivity.this,
					getResources().getString(R.string.networkdisabled),
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult arg0) {
		// TODO Auto-generated method stub

	}

	private class MyPoiOverlay extends PoiOverlay {
		public MyPoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onPoiClick(int index) {
			super.onPoiClick(index);

			Toast.makeText(
					getApplicationContext(),
					mStationList.get(index).get("name") + "(" + "距离"
							+ mStationList.get(index).get("distance") + ")",
					Toast.LENGTH_SHORT).show();

			return true;
		}
	}

	public void onGetPoiResult(PoiResult result) {

		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {

			Toast.makeText(MainActivity.this,
					getResources().getString(R.string.notFoundResult),
					Toast.LENGTH_LONG).show();
			return;
		}

		int first = 0;

		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			for (PoiInfo poi : result.getAllPoi()) {
				if (poi.type == PoiInfo.POITYPE.BUS_STATION) {
					// Log.i("Nearby poiresult",""+poi.name+"-"+poi.address);

					if (first == 0) {

						// 创建PoiOverlay
						overlay = new MyPoiOverlay(mBaiduMap);
						// 设置overlay可以处理标注点击事件
						mBaiduMap.setOnMarkerClickListener(overlay);
						// 设置PoiOverlay数据
						overlay.setData(result);
						// 添加PoiOverlay到地图中
						overlay.addToMap();
						overlay.zoomToSpan();

						first = 1;

					}

					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("name", poi.name);
					map.put("address", poi.address);
					// 计算距离当前位置的距离
					double distance = DistanceUtil.getDistance(
							OneBusApplication.CURRENT_LOCATION, poi.location);
					map.put("distance", (int) distance + "米");
					mStationList.add(map);

					// return;
				}
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		super.onNewIntent(intent);
	}

	/**
	 * init mapview
	 */
	public void initMap() {

		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		// remove map logo
		mMapView.removeViewAt(1);

		int childCount = mMapView.getChildCount();
		View zoom = null;
		for (int i = 0; i < childCount; i++) {
			View child = mMapView.getChildAt(i);
			if (child instanceof ZoomControls) {
				zoom = child;
				break;
			}
		}
		zoom.setVisibility(View.GONE);

		mBaiduMap = mMapView.getMap();
		UiSettings mUiSettings = mBaiduMap.getUiSettings();
		mUiSettings.setCompassEnabled(false);

		mBaiduMap.setMyLocationEnabled(true);
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(1000);
		option.setIsNeedAddress(true);
		mLocClient.setLocOption(option);
		mLocClient.start();
	}

	/**
	 * 初始化界面部件
	 */
	public void initWidget() {
		locationImg = (ImageButton) findViewById(R.id.main_location);
		mCurrentMode = LocationMode.NORMAL;
		locationImg.setImageResource(R.drawable.main_icon_location);
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:

					// 开启方向传感器
					myOrientationListener.start();

					locationImg.setImageResource(R.drawable.main_icon_follow);
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));

					// BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
					// .fromResource(R.drawable.navi_map_gps_locked);
					// MyLocationConfiguration config = new
					// MyLocationConfiguration(
					// mCurrentMode, true, mCurrentMarker);
					// mBaiduMap.setMyLocationConfigeration(config);

					break;
				case COMPASS:

					// 关闭方向传感器
					myOrientationListener.stop();

					locationImg.setImageResource(R.drawable.main_icon_location);
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));

					MapStatus ms = new MapStatus.Builder(
							mBaiduMap.getMapStatus()).overlook(0).rotate(0)
							.build();
					MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
					mBaiduMap.animateMapStatus(u);

					break;
				case FOLLOWING:
					locationImg.setImageResource(R.drawable.main_icon_compass);
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));

					// BitmapDescriptor mCurrentMarker0 =
					// BitmapDescriptorFactory
					// .fromResource(R.drawable.navi_map_gps_locked);
					// MyLocationConfiguration config0 = new
					// MyLocationConfiguration(
					// mCurrentMode, true, mCurrentMarker0);
					// mBaiduMap.setMyLocationConfigeration(config0);

					break;
				}
			}
		};
		locationImg.setOnClickListener(btnClickListener);

		/**
		 * zoom init a origin zoom level maxLevel*0.85 . But it doesn't work
		 */
		zoomInButton = (ImageButton) findViewById(R.id.main_zoom_in);
		zoomOutButton = (ImageButton) findViewById(R.id.main_zoom_out);
		zoomLevel = (float) (mBaiduMap.getMaxZoomLevel() * 0.85);
		MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(zoomLevel);
		mBaiduMap.animateMapStatus(u);

		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (view.equals(zoomInButton)) {
					perfomZoom(true);
				} else if (view.equals(zoomOutButton)) {
					perfomZoom(false);
				}
			}

		};
		zoomInButton.setOnClickListener(onClickListener);
		zoomOutButton.setOnClickListener(onClickListener);

		mButtonNearBy = (MainWidgetImageButton) findViewById(R.id.main_bottombutton_nearby);
		mButtonRoute = (MainWidgetImageButton) findViewById(R.id.main_bottombutton_route);
		mButtonNav = (MainWidgetImageButton) findViewById(R.id.main_bottombutton_nav);
		mButtonMine = (MainWidgetImageButton) findViewById(R.id.main_bottombutton_mine);
		OnClickListener onBottomButtonClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					switch (view.getId()) {
					case R.id.main_bottombutton_nearby:
						Intent nearbyIntent = new Intent(MainActivity.this,
								NearbyActivity.class);
						startActivity(nearbyIntent);
						break;
					case R.id.main_bottombutton_route:
						Log.i("MainActivity", "Click _route");
						Intent searchIntent = new Intent(MainActivity.this,
								RoutePlanActivity.class);
						startActivity(searchIntent);
						break;
					case R.id.main_bottombutton_nav:
						Intent mineIntent = new Intent(MainActivity.this,
								UserActivity.class);
						startActivity(mineIntent);
						break;
					case R.id.main_bottombutton_mine:
						Intent settingIntent = new Intent(MainActivity.this,
								SettingActivity.class);
						startActivity(settingIntent);
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		mButtonNearBy.setOnClickListener(onBottomButtonClickListener);
		mButtonRoute.setOnClickListener(onBottomButtonClickListener);
		mButtonNav.setOnClickListener(onBottomButtonClickListener);
		mButtonMine.setOnClickListener(onBottomButtonClickListener);

		mSearchImageButton = (ImageButton) findViewById(R.id.main_topView_voice);
		mSearchImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 语音输入 并跳转到SearchActivity 传入 searchKey

				if (!NetWorkUtil.isNetWorkConnected(getApplicationContext())) {

					Toast.makeText(getApplicationContext(), "网络未连接",
							Toast.LENGTH_SHORT).show();
					return;

				}

				cancel();

				switch (status) {
				case STATUS_None:
					start();
					status = STATUS_WaitingReady;
					break;
				case STATUS_WaitingReady:
					cancel();
					status = STATUS_None;
					break;
				case STATUS_Ready:
					cancel();
					status = STATUS_None;
					break;
				case STATUS_Speaking:
					stop();
					status = STATUS_Recognition;
					break;
				case STATUS_Recognition:
					cancel();
					status = STATUS_None;
					break;
				}
			}

		});

	}

	/**
	 * 放大级别 3 - 19
	 */
	@SuppressLint("NewApi")
	private void perfomZoom(boolean isZoomIn) {
		try {
			MapStatusUpdate u;
			zoomLevel = mBaiduMap.getMapStatus().zoom;
			if (isZoomIn) {
				zoomOutButton.setImageResource(R.drawable.main_icon_zoomout);
				if (zoomLevel + 0.5 < mBaiduMap.getMaxZoomLevel() * 0.9) {
					zoomLevel += 0.5;
					u = MapStatusUpdateFactory.zoomTo(zoomLevel);
				} else {
					zoomLevel = (float) (mBaiduMap.getMaxZoomLevel() * 0.9);
					u = MapStatusUpdateFactory.zoomTo(zoomLevel);
					zoomInButton
							.setImageResource(R.drawable.main_icon_zoomin_dis);
				}
				zoomInButton.setBackground(getResources().getDrawable(
						R.drawable.main_widget_bottombutton_selector));

			} else {
				zoomInButton.setImageResource(R.drawable.main_icon_zoomin);
				if (zoomLevel - 0.5 > mBaiduMap.getMinZoomLevel() + 5) {
					zoomLevel -= 0.5;
					u = MapStatusUpdateFactory.zoomTo(zoomLevel);
				} else {
					zoomLevel = mBaiduMap.getMinZoomLevel() + 5;
					u = MapStatusUpdateFactory.zoomTo(zoomLevel);
					zoomOutButton
							.setImageResource(R.drawable.main_icon_zoomout_dis);
				}
				zoomOutButton.setBackground(getResources().getDrawable(
						R.drawable.main_widget_bottombutton_selector));
			}

			mBaiduMap.animateMapStatus(u);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 点击搜索框
	 * 
	 * @param v
	 */
	public void showSearchActivity(View v) {
		switch (v.getId()) {
		case R.id.main_topView:
			// TODO
			Intent searchIntent = new Intent(this, SearchActivity.class);
			searchIntent.putExtra("tag", 0);
			startActivity(searchIntent);
			break;

		default:
			break;
		}

	}

	@Override
	protected void onDestroy() {
		mLocClient.stop();
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		speechRecognizer.destroy();
		mPoiSearch.destroy();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	/**
	 * 定位
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {

			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius()).direction(mXDirection)
					.latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();

			mCurrentAccracy = location.getRadius();

			mBaiduMap.setMyLocationData(locData);

			mCurrentLantitude = location.getLatitude();
			mCurrentLongitude = location.getLongitude();

			OneBusApplication.CURRENT_POSITION = location.getAddrStr();
			LatLng ll = new LatLng(location.getLatitude(),
					location.getLongitude());

			OneBusApplication.CURRENT_LOCATION = ll;

			if (location.getCity() != null) {
				OneBusApplication.GPS_CITY = location.getCity();
			}

			// Log.i("Coortype ",
			// ""+location.getNetworkLocationType()+" : "+location.getCoorType()+" City:"+location.getCity());

			if (isFirstLoc) {
				isFirstLoc = false;
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);

				if (location.getCity() != null) {
					Log.i("MainActivity",
							"SharedPreferences Save Location And City");
					SharedPreferences preferences = getSharedPreferences(
							"LastLocation", Context.MODE_PRIVATE);
					Editor editor = preferences.edit();
					if (preferences.getBoolean("isFirstLocate", true)) {
						OneBusApplication.CURRENT_CITY = location.getCity(); // If
																				// is
																				// the
																				// first
																				// locate,
						editor.putBoolean("isFirstLocate", false); // set the
																	// current
																	// city as
																	// GpsCity
					}

					editor.putString("currentCity", location.getCity());
					editor.putString("LastAddress", location.getAddrStr());
					editor.commit();
					preferences = null;
					editor = null;
				}

				initNearby();
			}

		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	/**
	 * 语音识别相关内容
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			onResults(data.getExtras());
		}
	}

	private void start() {

		Intent intent = new Intent();
		bindParams(intent);
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		{

			String args = sp.getString("args", "");
			if (null != args) {
				// print("参数集：" + args);
				// intent.putExtra("args", args);
			}
		}
		boolean api = sp.getBoolean("api", false);
		if (api) {
			speechEndTime = -1;
			speechRecognizer.startListening(intent);
		} else {
			intent.setAction("com.baidu.action.RECOGNIZE_SPEECH");
			startActivityForResult(intent, REQUEST_UI);
		}

	}

	private void stop() {
		speechRecognizer.stopListening();
	}

	private void cancel() {
		speechRecognizer.cancel();
		status = STATUS_None;
	}

	public void onReadyForSpeech(Bundle params) {
		status = STATUS_Ready;
	}

	public void onBeginningOfSpeech() {
		status = STATUS_Speaking;
	}

	public void onRmsChanged(float rmsdB) {

	}

	public void onBufferReceived(byte[] buffer) {

	}

	public void onEndOfSpeech() {
		speechEndTime = System.currentTimeMillis();
		status = STATUS_Recognition;
	}

	public void onError(int error) {
		status = STATUS_None;
		StringBuilder sb = new StringBuilder();
		switch (error) {
		case SpeechRecognizer.ERROR_AUDIO:
			Toast.makeText(getApplicationContext(), "音频问题", Toast.LENGTH_SHORT)
					.show();
			break;
		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
			Toast.makeText(getApplicationContext(), "没有语音输入",
					Toast.LENGTH_SHORT).show();
			break;
		case SpeechRecognizer.ERROR_CLIENT:
			Toast.makeText(getApplicationContext(), "其它客户端错误",
					Toast.LENGTH_SHORT).show();
			break;
		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
			Toast.makeText(getApplicationContext(), "权限不足", Toast.LENGTH_SHORT)
					.show();
			break;
		case SpeechRecognizer.ERROR_NETWORK:
			Toast.makeText(getApplicationContext(), "网络问题", Toast.LENGTH_SHORT)
					.show();
			break;
		case SpeechRecognizer.ERROR_NO_MATCH:
			Toast.makeText(getApplicationContext(), "没有匹配的识别结果",
					Toast.LENGTH_SHORT).show();
			break;
		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
			Toast.makeText(getApplicationContext(), "引擎忙", Toast.LENGTH_SHORT)
					.show();
			break;
		case SpeechRecognizer.ERROR_SERVER:
			Toast.makeText(getApplicationContext(), "服务端错误", Toast.LENGTH_SHORT)
					.show();
			break;
		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
			Toast.makeText(getApplicationContext(), "连接超时", Toast.LENGTH_SHORT)
					.show();
			break;
		}
	}

	public void onResults(Bundle results) {
		long end2finish = System.currentTimeMillis() - speechEndTime;
		status = STATUS_None;
		ArrayList<String> nbest = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		/*
		 * print("识别成功：" + Arrays.toString(nbest.toArray(new
		 * String[nbest.size()])));
		 */

		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), SearchActivity.class);
		intent.putExtra("tag", 1);
		intent.putExtra("strMessage", nbest.get(0).toString());
		startActivity(intent);

		String json_res = results.getString("origin_result");

		String strEnd2Finish = "";
		if (end2finish < 60 * 1000) {
			strEnd2Finish = "(waited " + end2finish + "ms)";
		}

		cancel();
	}

	public void onPartialResults(Bundle partialResults) {
		ArrayList<String> nbest = partialResults
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		if (nbest.size() > 0) {
			// print("~临时识别结果：" + Arrays.toString(nbest.toArray(new
			// String[0])));

		}
	}

	public void onEvent(int eventType, Bundle params) {
		switch (eventType) {
		case EVENT_ERROR:
			String reason = params.get("reason") + "";
			Toast.makeText(getApplicationContext(), "EVENT_ERROR, " + reason,
					Toast.LENGTH_SHORT).show();
			break;
		case VoiceRecognitionService.EVENT_ENGINE_SWITCH:
			int type = params.getInt("engine_type");
			Toast.makeText(getApplicationContext(),
					"*引擎切换至" + (type == 0 ? "在线" : "离线"), Toast.LENGTH_SHORT)
					.show();
			break;
		}
	}

	public void bindParams(Intent intent) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (sp.getBoolean("tips_sound", true)) {
			intent.putExtra(Constant.EXTRA_SOUND_START,
					R.raw.bdspeech_recognition_start);
			intent.putExtra(Constant.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
			intent.putExtra(Constant.EXTRA_SOUND_SUCCESS,
					R.raw.bdspeech_recognition_success);
			intent.putExtra(Constant.EXTRA_SOUND_ERROR,
					R.raw.bdspeech_recognition_error);
			intent.putExtra(Constant.EXTRA_SOUND_CANCEL,
					R.raw.bdspeech_recognition_cancel);
		}
		if (sp.contains(Constant.EXTRA_INFILE)) {
			String tmp = sp.getString(Constant.EXTRA_INFILE, "")
					.replaceAll(",.*", "").trim();
			intent.putExtra(Constant.EXTRA_INFILE, tmp);
		}
		if (sp.getBoolean(Constant.EXTRA_OUTFILE, false)) {
			intent.putExtra(Constant.EXTRA_OUTFILE, "sdcard/outfile.pcm");
		}
		if (sp.contains(Constant.EXTRA_SAMPLE)) {
			String tmp = sp.getString(Constant.EXTRA_SAMPLE, "")
					.replaceAll(",.*", "").trim();
			if (null != tmp && !"".equals(tmp)) {
				intent.putExtra(Constant.EXTRA_SAMPLE, Integer.parseInt(tmp));
			}
		}
		if (sp.contains(Constant.EXTRA_LANGUAGE)) {
			String tmp = sp.getString(Constant.EXTRA_LANGUAGE, "")
					.replaceAll(",.*", "").trim();
			if (null != tmp && !"".equals(tmp)) {
				intent.putExtra(Constant.EXTRA_LANGUAGE, tmp);
			}
		}
		if (sp.contains(Constant.EXTRA_NLU)) {
			String tmp = sp.getString(Constant.EXTRA_NLU, "")
					.replaceAll(",.*", "").trim();
			if (null != tmp && !"".equals(tmp)) {
				intent.putExtra(Constant.EXTRA_NLU, tmp);
			}
		}

		if (sp.contains(Constant.EXTRA_VAD)) {
			String tmp = sp.getString(Constant.EXTRA_VAD, "")
					.replaceAll(",.*", "").trim();
			if (null != tmp && !"".equals(tmp)) {
				intent.putExtra(Constant.EXTRA_VAD, tmp);
			}
		}
		String prop = null;
		if (sp.contains(Constant.EXTRA_PROP)) {
			String tmp = sp.getString(Constant.EXTRA_PROP, "")
					.replaceAll(",.*", "").trim();
			if (null != tmp && !"".equals(tmp)) {
				intent.putExtra(Constant.EXTRA_PROP, Integer.parseInt(tmp));
				prop = tmp;
			}
		}

		// offline asr
		{
			intent.putExtra(Constant.EXTRA_OFFLINE_ASR_BASE_FILE_PATH,
					"/sdcard/easr/s_1");
			intent.putExtra(Constant.EXTRA_LICENSE_FILE_PATH,
					"/sdcard/easr/license-tmp-20150530.txt");
			if (null != prop) {
				int propInt = Integer.parseInt(prop);
				if (propInt == 10060) {
					intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH,
							"/sdcard/easr/s_2_Navi");
				} else if (propInt == 20000) {
					intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH,
							"/sdcard/easr/s_2_InputMethod");
				}
			}
			intent.putExtra(Constant.EXTRA_OFFLINE_SLOT_DATA,
					buildTestSlotData());
		}
	}

	private String buildTestSlotData() {
		JSONObject slotData = new JSONObject();
		JSONArray name = new JSONArray().put("李涌泉").put("郭下纶");
		JSONArray song = new JSONArray().put("七里香").put("发如雪");
		JSONArray artist = new JSONArray().put("周杰伦").put("李世龙");
		JSONArray app = new JSONArray().put("手机百度").put("百度地图");
		JSONArray usercommand = new JSONArray().put("关灯").put("开门");
		try {
			slotData.put(Constant.EXTRA_OFFLINE_SLOT_NAME, name);
			slotData.put(Constant.EXTRA_OFFLINE_SLOT_SONG, song);
			slotData.put(Constant.EXTRA_OFFLINE_SLOT_ARTIST, artist);
			slotData.put(Constant.EXTRA_OFFLINE_SLOT_APP, app);
			slotData.put(Constant.EXTRA_OFFLINE_SLOT_USERCOMMAND, usercommand);
		} catch (JSONException e) {

		}
		return slotData.toString();
	}

	/**
	 *  * 菜单、返回键响应  
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click(); // 调用双击退出函数
		}
		return false;
	}

	/**
	 *  * 双击退出函数  
	 */
	private static Boolean isExit = false;

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // 准备退出
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {

				public void run() {
					isExit = false; // 取消退出
				}
			}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
		} else {
			finish();
			System.exit(0);
		}
	}
}
