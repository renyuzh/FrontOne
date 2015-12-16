package com.onebus.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.onebus.OneBusApplication;
import com.onebus.R;

public class StationBusLineActivity extends Activity implements OnGetPoiSearchResultListener{
	
	private ImageButton mImageButtonBack;
	
	private TextView mStationNameTextView;
	private ListView mBusLineListView;	//bus line list view
	private SimpleAdapter mBusLineListAdapter;
	private ArrayList<HashMap<String, Object>> mBusLineListList;	//bus line list
	private String[] mBusLineNameList = null;
	
	private PoiSearch mPoiSearch = null;
	private ProgressDialog mProgressDialog;
	
	private Handler mHandler;
	private final static int SEARCH = 0x1;
	private final static int SEARCH_DONE = 0x2;
	
	private int curIndex = 0;	//标记查询的是第几个公交线路
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_station_bus_line);
		
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init()throws Exception{
		mBusLineNameList = getIntent().getStringArrayExtra("busLineNameList");
		
		mStationNameTextView = (TextView)findViewById(R.id.stationbusline_stationName);
		mStationNameTextView.setText(getIntent().getStringExtra("stationName"));
	
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		
		
		mImageButtonBack = (ImageButton)findViewById(R.id.stationbusline_btn_back);
		mImageButtonBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mBusLineListList = new ArrayList<HashMap<String,Object>>();
		mBusLineListView = (ListView)findViewById(R.id.stationbusline_buslines);
		mBusLineListAdapter = new SimpleAdapter(this, mBusLineListList, R.layout.station_busline_list_item,
				new String[]{"busLineName","startAndEnd"},
				new int[]{R.id.stationbusline_listitem_buslineName,R.id.stationbusline_listitem_startandend});
		mBusLineListView.setAdapter(mBusLineListAdapter);
		mBusLineListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent  busLineIntent = new Intent(StationBusLineActivity.this, BusLineActivity.class);
				busLineIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				busLineIntent.putExtra("busLineId", mBusLineListList.get(position).get("buslineId").toString());
				startActivity(busLineIntent);
			}
		});
		
		
		mHandler = new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case SEARCH:{
					mPoiSearch.searchInCity(new PoiCitySearchOption().city(OneBusApplication.CURRENT_CITY)
							.keyword(msg.obj.toString()));
					break;
				}
				case SEARCH_DONE:{
					mProgressDialog.dismiss();
					curIndex = 0;
					break;
				}
				default: 
					super.handleMessage(msg);
				}

			}
		};
		
		
		if(mBusLineNameList!=null){
			mProgressDialog = new ProgressDialog(StationBusLineActivity.this);
			mProgressDialog.setIcon(R.drawable.common_topbar_route_foot_pressed);
			mProgressDialog.setMessage("请稍等....");
			mProgressDialog.show();
			
			Message msg  = mHandler.obtainMessage();
			msg.what = SEARCH;
			msg.obj = mBusLineNameList[curIndex++];
			mHandler.sendMessage(msg);
		}

	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult arg0) {
	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			//Toast.makeText(StationBusLineActivity.this, getResources().getString(R.string.notFoundResult), Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			for (PoiInfo poi : result.getAllPoi()) {
		        if (poi.type == PoiInfo.POITYPE.BUS_LINE || poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {  

		        	HashMap<String, Object> map = new HashMap<String, Object>();
		        	map.put("buslineId", poi.uid);
		        	int startpos = poi.name.indexOf("(")+1;
		        	int endpos = poi.name.lastIndexOf(")");
		        	String startAndEnd = poi.name.substring(startpos,endpos);
					map.put("busLineName", poi.name.substring(0,startpos-1));
					map.put("startAndEnd", startAndEnd);
					mBusLineListList.add(map);
	
		            continue;
		        }
		    }
			mBusLineListAdapter.notifyDataSetChanged();
			
			Message msg  = mHandler.obtainMessage();
			if(curIndex < mBusLineNameList.length){
				msg.what = SEARCH;
				msg.obj = mBusLineNameList[curIndex++];
			}else{
				msg.what = SEARCH_DONE;
			}
			mHandler.sendMessage(msg);
			return;
		}
	}
	
	@Override
	protected void onDestroy() {
		mPoiSearch.destroy();
		super.onDestroy();
	}
	
}
