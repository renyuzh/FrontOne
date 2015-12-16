package com.onebus.view;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.baidu.mapapi.model.LatLng;
import com.onebus.R;

public class MapFragment extends Fragment {

	private Context mContext;
	
	// location
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	
	/**
	 * locate button
	 */
	ImageButton locationImg;
	boolean isFirstLoc = true;
	/**
	 * zoom button
	 */
	private ImageButton zoomInButton;
	private ImageButton zoomOutButton;
	private float zoomLevel;
	
	
	
	
	@Override
	public void onAttach(Activity activity) {
		mContext = activity.getApplicationContext();
		super.onAttach(activity);
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_map_fragment, container,false);
		mMapView = (MapView)view.findViewById(R.id.mapfragment_mapView);
		mBaiduMap = mMapView.getMap();
		initMap();
		initView(view);
		return view;
	}



	public void initMap(){
		
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
		
		mBaiduMap.setMyLocationEnabled(true);

		mLocClient = new LocationClient(mContext);
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
	
	
	public void initView(View view){
		locationImg = (ImageButton) view.findViewById(R.id.mapfragment_location);
		mCurrentMode = LocationMode.NORMAL;
		locationImg.setImageResource(R.drawable.main_icon_location);
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					locationImg.setImageResource(R.drawable.main_icon_follow);
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));
					break;
				case COMPASS:
					locationImg.setImageResource(R.drawable.main_icon_location);
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));
					MapStatus ms = new MapStatus.Builder(mBaiduMap.getMapStatus()).overlook(0).rotate(0).build();
					MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
					mBaiduMap.animateMapStatus(u);
					break;
				case FOLLOWING:
					locationImg.setImageResource(R.drawable.main_icon_compass);
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));
					break;
				}
			}
		};
		locationImg.setOnClickListener(btnClickListener);
		

		zoomInButton = (ImageButton) view.findViewById(R.id.mapfragment_zoom_in);
		zoomOutButton = (ImageButton) view.findViewById(R.id.mapfragment_zoom_out);
		zoomLevel = (float) (mBaiduMap.getMaxZoomLevel()*0.9);
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
	}
	
	
	/**
	 *  zoom 3 - 19
	 */
	@SuppressLint("NewApi")
	private void perfomZoom(boolean isZoomIn) {
		try {
			MapStatusUpdate u;
			zoomLevel = mBaiduMap.getMapStatus().zoom;
			if(isZoomIn){
				zoomOutButton.setImageResource(R.drawable.main_icon_zoomout);
				if(zoomLevel+0.5 < mBaiduMap.getMaxZoomLevel()*0.9){
					zoomLevel +=0.5;
					u = MapStatusUpdateFactory.zoomTo(zoomLevel);
				}else{
					zoomLevel = (float) (mBaiduMap.getMaxZoomLevel()*0.9);
					u = MapStatusUpdateFactory.zoomTo(zoomLevel);
					zoomInButton.setImageResource(R.drawable.main_icon_zoomin_dis);
				}
				zoomInButton.setBackground(getResources().getDrawable(R.drawable.main_widget_bottombutton_selector));
				
			}else{
				zoomInButton.setImageResource(R.drawable.main_icon_zoomin);
				if(zoomLevel-0.5 > mBaiduMap.getMinZoomLevel()+5){
					zoomLevel -=0.5;
					u = MapStatusUpdateFactory.zoomTo(zoomLevel);
				}else{
					zoomLevel = mBaiduMap.getMinZoomLevel()+5;
					u = MapStatusUpdateFactory.zoomTo(zoomLevel);
					zoomOutButton.setImageResource(R.drawable.main_icon_zoomout_dis);
				}
				zoomOutButton.setBackground(getResources().getDrawable(R.drawable.main_widget_bottombutton_selector));
			}
			
			mBaiduMap.animateMapStatus(u);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	

	
	public MapView getMapView() {
		return mMapView;
	}


	public BaiduMap getBaiduMap() {
		return mBaiduMap;
	}
	

	@Override
	public void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		mMapView.onPause();
		super.onPause();
	}


	@Override
	public void onDestroy() {
		// �˳�ʱ��ٶ�λ
		mLocClient.stop();
		// �رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}
	
	/**
	 * ��λSDK������
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view ��ٺ��ڴ����½��յ�λ��
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if(isFirstLoc){
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

}
