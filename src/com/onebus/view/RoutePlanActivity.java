package com.onebus.view;



import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.onebus.OneBusApplication;
import com.onebus.R;
import com.onebus.util.NetWorkUtil;

/**
 * android:theme="@android:style/Theme.Holo.NoActionBar"
 * 
 * @author Think-Usar
 * 
 */
public class RoutePlanActivity extends FragmentActivity implements BaiduMap.OnMapClickListener,
			OnGetRoutePlanResultListener, OnGetGeoCoderResultListener{
	private final static String TAG = "RoutePlanActivity";
	private final static String RightArrow = "→";
	
	private final static int STATUS_LIST = 0x1;
	private final static int STATUS_MAP = 0x2;
	private static int currentStatus = STATUS_LIST;
	private LinearLayout searchLayout;
	private RelativeLayout topLayout;
	
	/**
	 * RadioGroup
	 */
	private RadioGroup mBusRadioGroup;
	private RadioGroup mCoachRadioGroup;
	private RadioGroup mFootRadioGroup;
	private RadioButton mBusRadioButton;
	private RadioButton mCoachRadioButton;
	private RadioButton mFootRadioButton;
	private boolean changedGroup = false;
	private MyOnCheckedChangeListener routePlanChangeListener = new MyOnCheckedChangeListener();
	private int currentVehicle = R.id.routePlan_radioGroup_bus;		//默认查询为公交查询
	
	
	private ImageButton mBackButton;	
	private Button mSearchButton;	
	private ListView mResultListview;	//show result list
	private ListView mHistoryListview;	//show result list
	private ArrayList<HashMap<String, Object>> mResultListData;	//result list data
	private ArrayList<HashMap<String, Object>> mHistoryListData;	//result list data
	private SimpleAdapter mResultListViewAdapter;
	private ProgressDialog mProgressDialog;
	private AutoCompleteTextView startPosView;	//start position
	private AutoCompleteTextView endPosView;	//destination
	private String mStartPosition = "";
	private String mEndPosition = "";
	private LatLng mStartLocation, mEndLocation;	//start and end locatoin (LatLng)
	private ArrayAdapter<String> suggestPosAdapter;	//suggestListAdapter
	private int nodeStartIndex = 0, nodeEndIndex = 0;		//process ambigiuous position
	
	private SharedPreferences preference;	//存储历史记录
	LayoutInflater inflater;
	private View listFooterView;
	
	
    public static ArrayList<TransitRouteLine> mRoutePlans;

    private GeoCoder mGeoSearch = null;
    private LatLng currentGeoResult;		
    private String currentGeoResultAddress;	
    private RoutePlanSearch mSearch = null;    
    private SuggestionSearch mSuggestionSearch = null;	
    private OverlayManager routeOverlay = null;
    boolean useDefaultIcon = false;

    private RelativeLayout mMapLayout;
    private MapFragment mMapFragment;
    private MapView mMapView = null;    
    private BaiduMap mBaiduMap = null;


    private LocationClient mLocClient;
 	public MyLocationListenner myListener = new MyLocationListenner();
 	private boolean isFirstLoc = true;
 	private String currentPosition;
 	private BDLocation myLocation;
 	
 	private InputMethodManager manager;
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_plan);

		searchLayout = (LinearLayout)findViewById(R.id.routePlan_listview);
		topLayout = (RelativeLayout)findViewById(R.id.routePlan_btn_layout);
		
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inflater =  (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		initSearchView();
		initMapView();
	}
	
	/**
	 * 隐藏软键盘
	 */
	public void hideKeyboard() {
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null){
				manager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}
	

	/**
	 * 初始化搜索控件
	 */
	public void initSearchView() {
		mBackButton = (ImageButton)findViewById(R.id.routePlan_btn_back);
		mBackButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		mSearchButton = (Button)findViewById(R.id.routePlan_btn_search);
		mSearchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideKeyboard();
				startSearch();
			}

		});
		
		mBusRadioGroup = (RadioGroup) findViewById(R.id.routePlan_radioGroup_bus);
		mCoachRadioGroup = (RadioGroup) findViewById(R.id.routePlan_radioGroup_car);
		mFootRadioGroup = (RadioGroup) findViewById(R.id.routePlan_radioGroup_foot);
		mBusRadioGroup.setOnCheckedChangeListener(routePlanChangeListener);
		mCoachRadioGroup.setOnCheckedChangeListener(routePlanChangeListener);
		mFootRadioGroup.setOnCheckedChangeListener(routePlanChangeListener);
		
		mBusRadioButton = (RadioButton) findViewById(R.id.routePlan_btn_bus);
		mCoachRadioButton = (RadioButton) findViewById(R.id.routePlan_btn_car);
		mFootRadioButton = (RadioButton) findViewById(R.id.routePlan_btn_foot);
		mBusRadioButton.setChecked(true);
		mBusRadioButton.setBackgroundResource(R.drawable.common_topbar_route_bus_pressed);
		
		//初始化结果展示列表
		initResultListView();

		startPosView = (AutoCompleteTextView)findViewById(R.id.routePlan_et_start);
		endPosView = (AutoCompleteTextView)findViewById(R.id.routePlan_et_end);
		
		//End position has filled by the parameter `DestLocation` from other activity
		Intent destIntent = getIntent();
		String destLocation = destIntent.getStringExtra("DestLocation");		//接受传入参数作为终点
		if(destLocation!=null){
			endPosView.setText(destLocation);
		}
		
		suggestPosAdapter = new ArrayAdapter<String>(this ,
				android.R.layout.simple_dropdown_item_1line );
		startPosView.setAdapter (suggestPosAdapter);
		endPosView.setAdapter (suggestPosAdapter);
		
		TextWatcher watcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() <= 0) {
					return;
				}
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(s.toString()).city(OneBusApplication.CURRENT_CITY));
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		};
		
		startPosView.addTextChangedListener(watcher);
		endPosView.addTextChangedListener(watcher);
	}
	
	
	/**
	 * 初始化结果列表组件
	 */
	public void initResultListView(){
		preference = getSharedPreferences("History",Context.MODE_PRIVATE);
		
		mRoutePlans = new ArrayList<TransitRouteLine>();
		mHistoryListview = (ListView)findViewById(R.id.routePlan_history_listview);
		mResultListview = (ListView)findViewById(R.id.routePlan_result_listview);
		mResultListview.setVisibility(View.GONE);
		
		mResultListData = new ArrayList<HashMap<String,Object>>();
		mHistoryListData = new ArrayList<HashMap<String,Object>>();
		
		//加载历史记录
		try{
			String historyData = preference.getString("RoutePlanHistory", "");
			if(!historyData.trim().equals("")){
				String[] records = historyData.split(";");
				for(int i=0; i<records.length; i++){
					HashMap<String,Object> map = new HashMap<String, Object>();
					map.put("title", records[i]);
					mHistoryListData.add(map);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(mHistoryListData.size()!=0){
			//ListView 底部View
			listFooterView = inflater.inflate(R.layout.search_list_footer, null);
			mHistoryListview.addFooterView(listFooterView);
			listFooterView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mHistoryListData.clear();
					mHistoryListview.removeFooterView(listFooterView);
					//隐藏历史记录列表
					mResultListview.setVisibility(View.VISIBLE);
					mHistoryListview.setVisibility(View.GONE);
					
				}
			});
		}
		
		SimpleAdapter historyListAdapter = new SimpleAdapter(this, mHistoryListData,
							R.layout.routeplan_history_list_item,
							new String[]{"title"},
							new int[]{R.id.routeplan_history_list_item_title});
		mHistoryListview.setAdapter(historyListAdapter);
		mHistoryListview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				startPosView.setText(mHistoryListData.get(position).get("title").toString().split(RightArrow)[0]);
				endPosView.setText(mHistoryListData.get(position).get("title").toString().split(RightArrow)[1]);
				hideKeyboard();
				startSearch();
			}
		});
		
		mResultListViewAdapter = new SimpleAdapter(this,mResultListData,
							R.layout.routeplan_list_item,
							new String[]{"title","time","distance","walk"},
							new int[]{R.id.routeplan_list_item_title,R.id.routeplan_list_item_time
							,R.id.routeplan_list_item_distance,R.id.routeplan_list_item_walk});
		mResultListview.setAdapter(mResultListViewAdapter);
		mResultListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

	            TextView titleView = (TextView)view.findViewById(R.id.routeplan_list_item_title);
				TextView timeView = (TextView)view.findViewById(R.id.routeplan_list_item_time);
				TextView distanceView = (TextView)view.findViewById(R.id.routeplan_list_item_distance);
				TextView walkView = (TextView)view.findViewById(R.id.routeplan_list_item_walk);
				
				Intent detailIntent = new Intent(RoutePlanActivity.this, TransitRouteResultActivity.class);
				detailIntent.putExtra("startPosition", mStartPosition);
				detailIntent.putExtra("endPosition", mEndPosition);
				detailIntent.putExtra("title", titleView.getText().toString());
				detailIntent.putExtra("time", timeView.getText().toString());
				detailIntent.putExtra("distance", distanceView.getText().toString());
				detailIntent.putExtra("walk", walkView.getText().toString());
				detailIntent.putExtra("position", position);
				startActivity(detailIntent);
			
			}
		});
		
	}
	
	
	/**
	 * 初始化地图控件
	 */
	public void initMapView() {
		mMapLayout = (RelativeLayout)findViewById(R.id.routePlan_mapLayout);
		mMapLayout.setVisibility(View.GONE);

		mMapFragment = ((MapFragment) (getSupportFragmentManager()
				.findFragmentById(R.id.routePlan_mapview)));
		mMapView = mMapFragment.getMapView();
		mBaiduMap = mMapFragment.getBaiduMap();
		
		//DeoSearch
		mGeoSearch = GeoCoder.newInstance();
		mGeoSearch.setOnGetGeoCodeResultListener(this);
		
		//Map click handler
		mBaiduMap.setOnMapClickListener(this);
		
		//RoutePlanSearch
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        
        //location
 		mBaiduMap.setMyLocationEnabled(true);
 		mLocClient = new LocationClient(this);
 		mLocClient.registerLocationListener(myListener);
 		LocationClientOption option = new LocationClientOption();
 		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
 		option.setIsNeedAddress(true);
 		option.setOpenGps(true);
 		option.setCoorType("bd09ll"); 
 		option.setScanSpan(1000);
 		option.setAddrType("all"); 
 		mLocClient.setLocOption(option);
 		mLocClient.start();
 		
 		//SuggestSearch
 		mSuggestionSearch = SuggestionSearch.newInstance();
		OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {  
		    public void onGetSuggestionResult(SuggestionResult res) {  
		        if (res == null || res.getAllSuggestions() == null) {  
		            return;  
		        }
		        suggestPosAdapter.clear();
				for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
					if (info.key != null)
						suggestPosAdapter.add(info.district+info.key);
				}
				suggestPosAdapter.notifyDataSetChanged();
			
		    }  
		};
		mSuggestionSearch.setOnGetSuggestionResultListener(listener);

	}
	
	/**
	 * 搜索事件发生
	 */
	public void startSearch(){
		if(currentStatus == STATUS_MAP) return;
		
		if(!NetWorkUtil.isNetWorkConnected(getApplicationContext())){
			Toast.makeText(RoutePlanActivity.this, getResources().getString(R.string.networkdisabled), Toast.LENGTH_LONG).show();
			return;
		}
		
		//隐藏历史记录列表
		mResultListview.setVisibility(View.VISIBLE);
		mHistoryListview.setVisibility(View.GONE);
		
		try {
			nodeStartIndex = nodeEndIndex = 0;
	        
	        Message msg = mHandler.obtainMessage();
	        msg.what = MSG_SEARCH_START;

	        //process the start position
	        if(startPosView.getText().toString().trim().equals("我的位置")){
	        	mStartPosition = "我的位置";
	        	mStartLocation = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
		        msg.arg1 = 1;	
	        }
	        else {
	        	mStartPosition = startPosView.getText().toString().trim();
	        	mGeoSearch.geocode(new GeoCodeOption().city(
	        			OneBusApplication.CURRENT_CITY).address(mStartPosition));
	        	mStartLocation = currentGeoResult;
	        	msg.arg1 = 2;
	        }	        

	        //process the end position
	        mEndPosition = endPosView.getText().toString().trim();
	        mGeoSearch.geocode(new GeoCodeOption().city(
        			OneBusApplication.CURRENT_CITY).address(mEndPosition));
        	mEndLocation = currentGeoResult;
        	
        	//check the position is not empty
        	if(mStartPosition.trim().equals("")){
        		Toast.makeText(getApplicationContext(), "请输入起点", Toast.LENGTH_SHORT).show();
        		return;
        	}
        	if(mEndPosition.trim().equals("")){
        		Toast.makeText(getApplicationContext(), "请输入终点", Toast.LENGTH_SHORT).show();
        		return;
        	}
        	
        	//判断此记录是否存在HistoryListData中，若不存在则加入
			if(!(isAlreadyAdd(mStartPosition+RightArrow+mEndPosition))){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("title", mStartPosition+RightArrow+mEndPosition);
				mHistoryListData.add(map);
			}
        	
        	//send search command
        	mHandler.sendMessage(msg);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private final static int MSG_UPDATE_LIST = 1;
	private final static int MSG_SEARCH_START = 2;
	private final static int MSG_SEARCH_DONE = 3;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_LIST:
				mResultListViewAdapter.notifyDataSetChanged();
				Log.i(TAG,"Update Result List View");
				break;
			case MSG_SEARCH_DONE:
				try {
					showResultList();
					if(mProgressDialog!=null){
						mProgressDialog.dismiss();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case MSG_SEARCH_START:
				try {
					if(mProgressDialog==null || !mProgressDialog.isShowing())
						showProgressDialog();

					if(msg.arg1==1){
						searchRoute(mStartLocation, mEndPosition);
					}else if(msg.arg1==2){
						searchRoute(mStartPosition, mEndPosition);
					}else{
						searchRoute(mStartPosition, mEndPosition);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				super.handleMessage(msg);
			}
			
		}
	};
	
	
	/**
	 * 判断列表是否已经添加过同样的关键词
	 * @param str
	 * @return
	 */
	public boolean isAlreadyAdd(String str){
		for(HashMap<String, Object> map : mHistoryListData){
			if(map.get("title").toString().equals(str))
				return true;
		}
		return false;
	}
	
	
	/**
	 * 显示加载对话框
	 */
	private void showProgressDialog() {
		mProgressDialog = new ProgressDialog(RoutePlanActivity.this);
		mProgressDialog.setIcon(R.drawable.common_topbar_route_foot_pressed);
		mProgressDialog.setMessage("请稍等....");
		mProgressDialog.show();
	}
	
	/**
	 * 定位起点，输入终点
	 * @param startLocation
	 * @param endPosition
	 * @throws Exception
	 */
	private void searchRoute(LatLng startLocation, String endPosition) throws Exception{
		Log.i(TAG+" searchRoute", "(Location, Position)");
		PlanNode stNode = PlanNode.withLocation(startLocation);
		PlanNode enNode = PlanNode.withCityNameAndPlaceName(OneBusApplication.CURRENT_CITY, endPosition);
		searchRoute0(stNode,enNode);
	}
	
	/**
	 * 输入起始点和输入终点
	 * @param startPosition
	 * @param endPosition
	 * @throws Exception
	 */
	private void searchRoute(String startPosition, String endPosition) throws Exception{
		Log.i(TAG+" searchRoute", "(Position, Position)");
		PlanNode stNode = PlanNode.withCityNameAndPlaceName(OneBusApplication.CURRENT_CITY, startPosition);
        PlanNode enNode = PlanNode.withCityNameAndPlaceName(OneBusApplication.CURRENT_CITY, endPosition);
        searchRoute0(stNode,enNode);
	}
	
	/**
	 * 起始点和终点都为定位结果
	 * @param startLocation
	 * @param endLocation
	 * @throws Exception
	 */
	private void searchRoute(LatLng startLocation, LatLng endLocation) throws Exception{
		Log.i(TAG+" searchRoute", "(Location, Location)");
		PlanNode stNode = PlanNode.withLocation(startLocation);
        PlanNode enNode = PlanNode.withLocation(endLocation);
        searchRoute0(stNode,enNode);
	}
	
	/**
	 * 搜索
	 */
	private void searchRoute0(PlanNode stNode, PlanNode enNode) throws Exception{
        mBaiduMap.clear();
        mRoutePlans.clear();
        
        if (currentVehicle == R.id.routePlan_radioGroup_car) {
            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        } else if (currentVehicle == R.id.routePlan_radioGroup_bus) {
            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode)
                    .city(OneBusApplication.CURRENT_CITY)
                    .to(enNode));
        } else if (currentVehicle == R.id.routePlan_radioGroup_foot) {
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        }
	}
	
	/**
     * 展示搜索结果到界面上
     */
	private void showResultList() throws Exception{
		Log.i(TAG, "Search Done, Show Result List");
		if(mRoutePlans==null){
			return;
		}
        mResultListData.clear();
        
        for(int i=0; i<mRoutePlans.size(); i++){
        	TransitRouteLine routeLine = mRoutePlans.get(i);
        	String title = "";
        	int time = routeLine.getDuration();
        	int distance = routeLine.getDistance();
        	int walk = 0;
        	Log.i(TAG+" routeLine title", ""+title);
        	
 
        	for(int j=0; j<routeLine.getAllStep().size(); j++){
            	
                String nodeTitle = null;
                boolean isFirstRouteTitle = true;
                Object step = routeLine.getAllStep().get(j);
                if (step instanceof TransitRouteLine.TransitStep) {
                    nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
                    Log.i(TAG+"结果",""+nodeTitle);
                    int startPos = -1, endPos = -1;
                    if(nodeTitle.indexOf("乘坐")!=-1){
                    	startPos = nodeTitle.indexOf("乘坐");
                    	endPos = nodeTitle.indexOf(",");
                        
                        if(startPos!=-1 && endPos!=-1){
                        	Log.i(TAG+"乘坐", nodeTitle.substring(startPos+2,endPos));
                        	if(!isFirstRouteTitle){
                        		//TODO 这里是什么？？看不懂了-.-
                        		title = title + " " + nodeTitle.substring(startPos+2,endPos);
                        	}else{
                        		title = nodeTitle.substring(startPos+2,endPos);
                        		isFirstRouteTitle = false;
                        	}
                        	
                        }else{
                        	title = "--";
                        }
                    }else if(nodeTitle.indexOf("步行")!=-1){
                    	startPos = nodeTitle.indexOf("步行");
                        endPos = nodeTitle.indexOf("米");
                        if(startPos!=-1 && endPos!=-1){
                        	Log.i(TAG+"步行", nodeTitle.substring(startPos+2,endPos));
                        	walk += Integer.parseInt(
                        			nodeTitle.substring(startPos+2,endPos));
                        }else{
                        	walk = 0;
                        }
                    }
                }
                
            }

        	HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("title", title);
            map.put("time", time/60+"分钟");
            DecimalFormat decimalFormat=new DecimalFormat(".0");
            String dis = decimalFormat.format(distance/1000);
            map.put("distance", ""+dis+"公里");
            map.put("walk", "步行"+walk+"米");
            mResultListData.add(map);
        }
        
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_UPDATE_LIST;
        mHandler.sendMessage(msg);

	}
	
	/**
	 * 改变界面显示 1地图	2列表
	 * @param status
	 */
	private void changeStatus(int status){
		switch(status){
		case STATUS_LIST:{
			searchLayout.setVisibility(View.VISIBLE);
			mMapLayout.setVisibility(View.GONE);
			currentStatus = STATUS_LIST;
			mSearchButton.setBackgroundResource(R.drawable.routeplan_searchbutton_selector);
			break;
		}
		case STATUS_MAP:{
			searchLayout.setVisibility(View.GONE);
			mMapLayout.setVisibility(View.VISIBLE);
			currentStatus = STATUS_MAP;
			mSearchButton.setBackgroundResource(R.drawable.common_btn_normal);
			break;
		}
		}
	}
	
	/**
	 * 步行搜索结果
	 */
	@Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
        	Log.i(TAG+"onGetWalkingRouteResult", ""+result.error.name());
        }
        if(result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND){
        	Toast.makeText(this, getResources().getString(R.string.notFoundResult), Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {

        	PoiInfo startInfo = null;
        	PoiInfo endInfo =  null;
        	String start = "", end = "";
        	boolean notSearch = false;
        	if(result.getSuggestAddrInfo().getSuggestStartNode()!=null){
        		if(nodeStartIndex==result.getSuggestAddrInfo().getSuggestStartNode().size()){
            		notSearch = true;
        		}
        		else{
        			startInfo = result.getSuggestAddrInfo().getSuggestStartNode().get(nodeStartIndex++);
                	start = startInfo.name;
        		}
            }
            if(result.getSuggestAddrInfo().getSuggestEndNode()!=null){
            	if(nodeEndIndex==result.getSuggestAddrInfo().getSuggestEndNode().size()){
            		notSearch = true;
            	}
            	else{
	            	endInfo = result.getSuggestAddrInfo().getSuggestEndNode().get(nodeEndIndex++);
	            	end = endInfo.name;
            	}
            }
            if(start.trim()!="")	mStartPosition = start;
            if(end.trim()!="")	mEndPosition = end;
            Log.i(TAG+" Walking Start End", mStartPosition+"  "+mEndPosition);
            if(!notSearch){
            	Message msg = mHandler.obtainMessage();
            	if(mStartPosition.equals("我的位置")){
            		msg.arg1 = 1;
            	}else{
            		msg.arg1 = 2;
            	}
	            msg.what = MSG_SEARCH_START;
	            mHandler.sendMessage(msg);
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
        	nodeStartIndex = nodeEndIndex = 0;
            changeStatus(STATUS_MAP);
            mBaiduMap.clear();
            WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
	    }
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_SEARCH_DONE;
        mHandler.sendMessage(msg);
      	
    }

	/**
	 * 公交搜索结果
	 */
    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {

    	try{
	        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
	        	Log.i(TAG+"onGetTransitRouteResult", ""+result.error.name());
	        }
	        if(result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND){
	        	Toast.makeText(this, getResources().getString(R.string.notFoundResult), Toast.LENGTH_SHORT).show();
	        }
	        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
	        	PoiInfo startInfo = null;
	        	PoiInfo endInfo =  null;
	        	String start = "", end = "";
	        	boolean notSearch = false;
	        	if(result.getSuggestAddrInfo().getSuggestStartNode()!=null){
	        		//Log.i(TAG+" Transit getSuggestStartNode", ""+nodeStartIndex);
	        		if(nodeStartIndex==result.getSuggestAddrInfo().getSuggestStartNode().size()){
	            		notSearch = true;
	        		}
	        		else{
	        			startInfo = result.getSuggestAddrInfo().getSuggestStartNode().get(nodeStartIndex++);
	                	start = startInfo.name;
	        		}
	            }
	            if(result.getSuggestAddrInfo().getSuggestEndNode()!=null){
	            	//Log.i(TAG+" Transit getSuggestEndNode", ""+nodeEndIndex);
	            	if(nodeEndIndex==result.getSuggestAddrInfo().getSuggestEndNode().size()){
	            		notSearch = true;
	            	}
	            	else{
		            	endInfo = result.getSuggestAddrInfo().getSuggestEndNode().get(nodeEndIndex++);
		            	end = endInfo.name;
	            	}
	            }
	            if(start.trim()!="")	mStartPosition = start;
	            if(end.trim()!="")	mEndPosition = end;
	            Log.i(TAG+"Transit Start End", mStartPosition+"  "+mEndPosition);
	            if(!notSearch){
	            	Message msg = mHandler.obtainMessage();
	            	if(mStartPosition.equals("我的位置")){
	            		msg.arg1 = 1;
	            	}else{
	            		msg.arg1 = 2;
	            	}
		            msg.what = MSG_SEARCH_START;
		            mHandler.sendMessage(msg);
	            }
	            return;
	        }
	        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
	            nodeStartIndex = nodeEndIndex = 0;
	            mRoutePlans = (ArrayList<TransitRouteLine>) result.getRouteLines();
	        }
	        Message msg = mHandler.obtainMessage();
	        msg.what = MSG_SEARCH_DONE;
	        mHandler.sendMessage(msg);
	        
    	}catch(Exception e){
        	Toast.makeText(this, "Exception"+e.getMessage(), Toast.LENGTH_SHORT).show();
        	e.printStackTrace();
        }
    }

    /**
     * 驾车搜索结果
     */
    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
    	if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
    		Log.i(TAG+"onGetDrivingRouteResult", ""+result.error.name());
        }
    	if(result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND){
        	Toast.makeText(this, getResources().getString(R.string.notFoundResult), Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
        	
        	PoiInfo startInfo = null;
        	PoiInfo endInfo =  null;
        	String start = "", end = "";
        	boolean notSearch = false;
        	if(result.getSuggestAddrInfo().getSuggestStartNode()!=null){
        		if(nodeStartIndex==result.getSuggestAddrInfo().getSuggestStartNode().size()){
            		notSearch = true;
        		}
        		else{
        			startInfo = result.getSuggestAddrInfo().getSuggestStartNode().get(nodeStartIndex++);
                	start = startInfo.name;
        		}
            }
            if(result.getSuggestAddrInfo().getSuggestEndNode()!=null){
            	if(nodeEndIndex==result.getSuggestAddrInfo().getSuggestEndNode().size()){
            		notSearch = true;
            	}
            	else{
	            	endInfo = result.getSuggestAddrInfo().getSuggestEndNode().get(nodeEndIndex++);
	            	end = endInfo.name;
            	}
            }
            if(start.trim()!="")	mStartPosition = start;
            if(end.trim()!="")	mEndPosition = end;
            Log.i(TAG+" Driving Start End", mStartPosition+"  "+mEndPosition);
            if(!notSearch){
            	Message msg = mHandler.obtainMessage();
            	if(mStartPosition.equals("我的位置")){
            		msg.arg1 = 1;
            	}else{
            		msg.arg1 = 2;
            	}
	            msg.what = MSG_SEARCH_START;
	            mHandler.sendMessage(msg);
            }
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
        	nodeStartIndex = nodeEndIndex = 0;
            changeStatus(STATUS_MAP);
            mBaiduMap.clear();
            DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
	    }
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_SEARCH_DONE;
        mHandler.sendMessage(msg);
    }
    
    
    /**
     * 地理编码到位置 获得经纬度
     */
    @Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Log.i(TAG+" onGetGeoCodeResult", "Error: "+result.error.toString());
			return;
		}
		if (result.error == GeoCodeResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			Toast.makeText(this, "AMBIGUOUS_ROURE_ADDR", Toast.LENGTH_SHORT).show();
		}
		currentGeoResult = result.getLocation();
	}

    /**
     * 地理位置反编码 获得地址名称
     */
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Log.i(TAG+" onGetReverseGeoCodeResult", "Error: "+result.error.toString());
			return;
		}
		currentGeoResultAddress = result.getAddress();
	}
 
    
	@Override
	public void onMapClick(LatLng arg0) {
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		return false;
	}

    
    /**
	 * 定位
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || mMapView == null)	return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if(isFirstLoc){
				isFirstLoc = false;
				currentPosition = location.getAddrStr();
				myLocation = location;
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
	
	/**
	 * 更换查询方式
	 * @author Think-Usar
	 *
	 */
	class MyOnCheckedChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			if(!changedGroup){
				changedGroup = true;
				switch (group.getId()) {
				case R.id.routePlan_radioGroup_bus:
					currentVehicle = R.id.routePlan_radioGroup_bus;
					mCoachRadioGroup.clearCheck();
					mFootRadioGroup.clearCheck();
					
					mBusRadioButton.setChecked(true);
					mFootRadioButton.setBackgroundResource(R.drawable.common_topbar_route_foot_normal);
					mCoachRadioButton.setBackgroundResource(R.drawable.common_topbar_route_car_normal);
					mBusRadioButton.setBackgroundResource(R.drawable.common_topbar_route_bus_pressed);
					break;
				case R.id.routePlan_radioGroup_car:
					currentVehicle = R.id.routePlan_radioGroup_car;
					mBusRadioGroup.clearCheck();
					mFootRadioGroup.clearCheck();
					
					mCoachRadioButton.setChecked(true);
					mFootRadioButton.setBackgroundResource(R.drawable.common_topbar_route_foot_normal);
					mCoachRadioButton.setBackgroundResource(R.drawable.common_topbar_route_car_pressed);
					mBusRadioButton.setBackgroundResource(R.drawable.common_topbar_route_bus_normal);
					break;
				case R.id.routePlan_radioGroup_foot:
					currentVehicle = R.id.routePlan_radioGroup_foot;
					mCoachRadioGroup.clearCheck();
					mBusRadioGroup.clearCheck();
					
					mFootRadioButton.setChecked(true);
					mFootRadioButton.setBackgroundResource(R.drawable.common_topbar_route_foot_pressed);
					mCoachRadioButton.setBackgroundResource(R.drawable.common_topbar_route_car_normal);
					mBusRadioButton.setBackgroundResource(R.drawable.common_topbar_route_bus_normal);
					break;
				}
				changedGroup = false;
			}
		}

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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
	
	@Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }
	
	@Override
	protected void onStop() {
		try{
			Editor editor = preference.edit();
			StringBuilder records = new StringBuilder();
			for(int i=mHistoryListData.size()-1, number = 1; i>=0&&number<=10; i--, number++){
				String record = mHistoryListData.get(i).get("title").toString();
				records.append(record+";");
			}
			if(records.length()!=0){
				records.replace(records.length()-1,records.length(), "");
			}
			Log.i("records", records.toString());
			editor.putString("RoutePlanHistory", records.toString());
			editor.commit();
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onStop();
	}

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
		mLocClient.stop();
		mBaiduMap.setMyLocationEnabled(false);
        mSearch.destroy();
        mGeoSearch.destroy();
        mSuggestionSearch.destroy();
        super.onDestroy();
    }

}

