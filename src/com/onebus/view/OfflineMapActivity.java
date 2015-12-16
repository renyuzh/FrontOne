package com.onebus.view;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.onebus.OneBusApplication;
import com.onebus.R;
import com.onebus.util.NetWorkUtil;

public class OfflineMapActivity extends Activity implements MKOfflineMapListener{
	
	
	private ImageButton mImageButtonBack;
	private ImageButton mImageButtonDownLoad;
	private TextView mEmptyTextView;
	private TextView mCurrentGPSCity;
	private TextView mRatioTextView;
	
	private MKOfflineMap mOffline = null;
	private ListView mCityListView;	
	private ArrayList<MKOLUpdateElement> mCityList = null;	//已下载城市列表
	private CityListAdapter mCityListAdapter = null;
	
	private boolean isDownLoad = true;
	private int cityId = -1;
	
	private int STATUS = 0;		//0 START  1 DOWNLOADING  2 PAUSE
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offline_map);
		
		try {
			mOffline = new MKOfflineMap();
			mOffline.init(this);
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private void init()throws Exception{
		mImageButtonBack = (ImageButton)findViewById(R.id.offlinemap_btn_back);
		mImageButtonBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		mImageButtonDownLoad = (ImageButton)findViewById(R.id.offlinemap_download);
		mImageButtonDownLoad.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try{
					//判断网络连接和网络类型
					if(NetWorkUtil.isNetWorkConnected(getApplicationContext())){
						if(NetWorkUtil.getNetWorkType(getApplicationContext())!=1){
							//提示用户网络不是WIFI环境
							AlertDialog.Builder builder = new AlertDialog.Builder(
									OfflineMapActivity.this);
							final AlertDialog dialogBuilder = builder.show();

							builder.setIcon(R.drawable.ic_launcher);
							builder.setTitle(getResources().getString(R.string.prompt));
							builder.setMessage(getResources().getString(R.string.confirmNotWiFiAndDownLoad));
							builder.setPositiveButton(getResources().getString(R.string.confirm)
									, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialogBuilder.dismiss();
								}
							});
							builder.setNegativeButton(getResources().getString(R.string.cancel)
									, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialogBuilder.dismiss();
									isDownLoad = false;
								}
							});
							builder.show();
							if(!isDownLoad){
								return;
							}
						}
					}
					else{
						Toast.makeText(OfflineMapActivity.this, getResources().getString(R.string.networkdisabled), Toast.LENGTH_LONG).show();
						return;
					}

					if(STATUS==0){
						ArrayList<MKOLSearchRecord> records = mOffline.searchCity(OneBusApplication.CURRENT_CITY);
						if (records == null || records.size() != 1){
							Toast.makeText(OfflineMapActivity.this, getResources().getString(R.string.downloadfailed), Toast.LENGTH_SHORT).show();
						}else{
							cityId = records.get(0).cityID;
							mCityListView.setVisibility(View.VISIBLE);
							mEmptyTextView.setVisibility(View.GONE);
							mRatioTextView.setVisibility(View.VISIBLE);
							mCityListView.setAdapter(mCityListAdapter);
							if(cityId!=-1)
								start();
						}
					}else if(STATUS==1){
						stop();
					}else if(STATUS==2){
						start();
					}
					
				}catch(Exception e){
					Toast.makeText(OfflineMapActivity.this, getResources().getString(R.string.downloadfailed), Toast.LENGTH_SHORT).show();
					mImageButtonDownLoad.setVisibility(View.GONE);
					mRatioTextView.setVisibility(View.GONE);
					mOffline.remove(cityId);	//删除下载失败的离线地图
					e.printStackTrace();
				}
				
			}
			
		});
		
		mRatioTextView = (TextView)findViewById(R.id.offlinemap_ratio);
		mEmptyTextView = (TextView)findViewById(R.id.offlinemap_empty);
		
		mCurrentGPSCity = (TextView)findViewById(R.id.offlinemap_currentcity);
		if(!OneBusApplication.CURRENT_CITY.trim().equals("")){
			mCurrentGPSCity.setText(OneBusApplication.CURRENT_CITY);
		}else{
			mImageButtonDownLoad.setVisibility(View.GONE);
		}
		
		
		mCityListView = (ListView)findViewById(R.id.offlinemap_citylist);

		mCityList = mOffline.getAllUpdateInfo();
		mCityListAdapter = new CityListAdapter();
		if (mCityList == null) {
			mCityList = new ArrayList<MKOLUpdateElement>();
			mCityListView.setVisibility(View.GONE);
			mEmptyTextView.setVisibility(View.VISIBLE);
			
		}else{
			mCityListView.setAdapter(mCityListAdapter);
			for(MKOLUpdateElement e : mCityList){
				if(e.cityName.indexOf(OneBusApplication.CURRENT_CITY) != -1){
					if(e.ratio!=100){
						STATUS = 2;
						cityId = e.cityID;
						mRatioTextView.setText(e.ratio+"%");
						mRatioTextView.setVisibility(View.VISIBLE);
						
					}else{
						mImageButtonDownLoad.setVisibility(View.GONE);	//已经下载过了，不再下载
					}
				}
			}
			
		}
	}
	

	/**
	 * 开始下载
	 * 
	 * @param view
	 */
	public void start() throws Exception{
		mImageButtonDownLoad.setImageResource(R.drawable.icon_pause);
		mOffline.start(cityId);
		STATUS = 1;
		updateView();
	}

	/**
	 * 暂停下载
	 * 
	 * @param view
	 */
	public void stop() throws Exception{
		mImageButtonDownLoad.setImageResource(R.drawable.icon_download);
		mOffline.pause(cityId);
		STATUS = 2;
		updateView();
	}

	
	/**
	 * 更新状态显示
	 */
	public void updateView(){
		mCityList = mOffline.getAllUpdateInfo();
		if (mCityList == null) {
			mCityList = new ArrayList<MKOLUpdateElement>();
		}
		mCityListAdapter.notifyDataSetChanged();
	}
	
	
	public String formatDataSize(int size) {
		String ret = "";
		if (size < (1024 * 1024)) {
			ret = String.format("%dK", size / 1024);
		} else {
			ret = String.format("%.1fM", size / (1024 * 1024.0));
		}
		return ret;
	}
	
	
	@Override
	public void onGetOfflineMapState(int type, int state) {
		// TODO Auto-generated method stub
		switch (type) {
		case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
			MKOLUpdateElement update = mOffline.getUpdateInfo(state);
			// 处理下载进度更新提示
			if (update != null) {
				mRatioTextView.setText(String.format("%d%%", update.ratio));
				if(update.ratio==100){
					mImageButtonDownLoad.setVisibility(View.GONE);
					mRatioTextView.setVisibility(View.GONE);
				}
				updateView();
			}
		}
		}
	
	}
	
	
	@Override
	protected void onPause() {
		//TODO pause download
		try {
			stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onPause();
	}
	
	

	
	/**
	 * 离线地图管理列表适配器
	 */
	public class CityListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mCityList.size();
		}

		@Override
		public Object getItem(int index) {
			return mCityList.get(index);
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		@Override
		public View getView(int index, View view, ViewGroup arg2) {
			
			try{
			
				final MKOLUpdateElement e = (MKOLUpdateElement) getItem(index);
				view = View.inflate(OfflineMapActivity.this, R.layout.offlinemap_list_item, null);
				
				ImageButton deleteButton = (ImageButton) view.findViewById(R.id.offlinemap_listitem_btn_delete);
				TextView updateInfo = (TextView) view.findViewById(R.id.offlinemap_listitem_updateinfo);
				TextView cityName = (TextView) view.findViewById(R.id.offlinemap_listitem_city);
				TextView size = (TextView) view.findViewById(R.id.offlinemap_listitem_size);
				TextView ratio = (TextView) view.findViewById(R.id.offlinemap_listitem_ratio);
				
				if(e.ratio!=100){
					ratio.setText(e.ratio+"%");
					ratio.setVisibility(View.VISIBLE);
				}
				
				cityName.setText(e.cityName);
				size.setText(formatDataSize(e.size));
	
				if (e.update) {
					updateInfo.setVisibility(View.VISIBLE);
				}
	
				deleteButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						
						AlertDialog.Builder builder = new AlertDialog.Builder(
								OfflineMapActivity.this);
						final AlertDialog dialogBuilder = builder.show();

						builder.setIcon(R.drawable.ic_launcher);
						builder.setTitle(getResources().getString(R.string.prompt));
						builder.setMessage(getResources().getString(R.string.confirmDelete));
						builder.setPositiveButton(getResources().getString(R.string.confirm)
								, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								dialogBuilder.dismiss();
								mOffline.remove(e.cityID);
								updateView();
							}
						});
						builder.setNegativeButton(getResources().getString(R.string.cancel)
								, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialogBuilder.dismiss();
							}
						});
						builder.show();

					}
				});
			}catch(Exception e){
				e.printStackTrace();
			}
			return view;
		}
	}

}
