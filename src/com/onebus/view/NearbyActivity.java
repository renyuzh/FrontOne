package com.onebus.view;


import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.onebus.OneBusApplication;
import com.onebus.R;
import com.onebus.util.NetWorkUtil;

public class NearbyActivity extends Activity implements OnGetPoiSearchResultListener{

	private PoiSearch mPoiSearch;	//PoiSearch
	
	private ListView mStationListView;	//nearby bus station list view
	private StationListAdapter mStationListAdapter;
	private ArrayList<HashMap<String, Object>> mStationList;	//nearby bus station list
	
	private ProgressDialog mProgressDialog;
	private ImageButton mImageButtonBack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby);
		
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() throws Exception{
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		
		mImageButtonBack = (ImageButton)findViewById(R.id.nearby_btn_back);
		mImageButtonBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mStationList = new ArrayList<HashMap<String,Object>>();
		mStationListView = (ListView)findViewById(R.id.nearby_stations);
		mStationListAdapter = new StationListAdapter();
		mStationListView.setAdapter(mStationListAdapter);
		mStationListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent stationBusLineIntent = new Intent(NearbyActivity.this, StationBusLineActivity.class);
				String[] busLineNameList = ((String)(mStationList.get(position).get("address"))).split(";");
				stationBusLineIntent.putExtra("busLineNameList", busLineNameList);
				stationBusLineIntent.putExtra("stationName", 
						(String)(mStationList.get(position).get("name"))+"-公交车站");
				startActivity(stationBusLineIntent);
			}
		});

		if(NetWorkUtil.isNetWorkConnected(getApplicationContext())){
			mProgressDialog = new ProgressDialog(NearbyActivity.this);
			mProgressDialog.setIcon(R.drawable.common_topbar_route_foot_pressed);
			mProgressDialog.setMessage("请稍等....");
			mProgressDialog.show();
			
			mPoiSearch.searchNearby((new PoiNearbySearchOption())
					.location(OneBusApplication.CURRENT_LOCATION).radius(2000).keyword("公交"));
			
		}else{
			Toast.makeText(NearbyActivity.this, getResources().getString(R.string.networkdisabled), Toast.LENGTH_LONG).show();
		}
	}
	
	
	/**
	 * put PoiuDetailInfo into list item
	 */
	@Override
	public void onGetPoiDetailResult(PoiDetailResult result) {
		Log.i("NearbyActivity", "onGetPoiDetailResult");
	}

	/**
	 * This can search which BusLine through that station
	 */
	@Override
	public void onGetPoiResult(PoiResult result) {

		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			
			mProgressDialog.dismiss();
			Toast.makeText(NearbyActivity.this, getResources().getString(R.string.notFoundResult), Toast.LENGTH_LONG).show();
			return;
		}
		
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			for (PoiInfo poi : result.getAllPoi()) {
		        if (poi.type == PoiInfo.POITYPE.BUS_STATION) {
		        	//Log.i("Nearby poiresult",""+poi.name+"-"+poi.address);
		        	
		        	HashMap<String, Object> map = new HashMap<String, Object>();
			    	map.put("name", poi.name);
			    	map.put("address", poi.address);
			    	//计算距离当前位置的距离
			    	double distance = DistanceUtil.getDistance(OneBusApplication.CURRENT_LOCATION, poi.location);
			    	map.put("distance", (int)distance+"米");
			    	mStationList.add(map);
		        }
		    }

			mProgressDialog.dismiss();
			mStationListAdapter.notifyDataSetChanged();
		}
	}
	
	
	
	@Override
	protected void onDestroy() {
		mPoiSearch.destroy();
		super.onDestroy();
	}
	
	
	class StationListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if(mStationList!=null)
				return mStationList.size();
			else 
				return 0;
		}

		@Override
		public Object getItem(int position) {
			if(mStationList!=null)
				return mStationList.get(position);
			else 
				return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			try{
				
				convertView = LayoutInflater.from(NearbyActivity.this).inflate(R.layout.nearby_list_item, null);
				TextView nameTextView = (TextView)convertView.findViewById(R.id.nearby_listitem_name);
				nameTextView.setText(""+mStationList.get(position).get("name"));
				TextView distanceTextView = (TextView)convertView.findViewById(R.id.nearby_listitem_distance);
				distanceTextView.setText(""+mStationList.get(position).get("distance"));
				TextView infoTextView = (TextView)convertView.findViewById(R.id.nearby_listitem_info);
				infoTextView.setText(""+mStationList.get(position).get("address"));

			}catch(Exception e){
				e.printStackTrace();
			}
			
			return convertView;
		}
		
	}

	
}
