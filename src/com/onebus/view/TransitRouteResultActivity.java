package com.onebus.view;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteLine.TransitStep;
import com.onebus.R;
import com.onebus.util.FavoriteUtil;

public class TransitRouteResultActivity extends FragmentActivity implements BaiduMap.OnMapClickListener{
	
	private final static int STATUS_LIST = 0x1;
	private final static int STATUS_MAP = 0x2;
	private static int currentStatus = STATUS_LIST;
	
	private ImageButton mImageButtonBack;
	private Button showMapButton;
	
	private LinearLayout mListLayout;
	private RelativeLayout mMapLayout;
    private MapFragment mMapFragment;
    private MapView mMapView = null;    
    private BaiduMap mBaiduMap = null;
    
    private String startPosition;
    private String endPosition;
    
    private ImageView mCollectImageView;
    private boolean isCollect;
    
    private TextView mTransitTitleTextView;
    private TextView mTransitTimeTextView;
    private TextView mTransitDistanceTextView;
    private TextView mTransitWalkTextView;
    
    private ListView mTransitRouteListView;
    private RouteListAdapter mTransitRouteListAdpater;
    private TransitRouteLine mTransitRouteLine;
    private List<TransitStep> mTransitStepListData;
    
    private StringBuilder dataToCollect = new StringBuilder();
    private String dataToCollectTitle = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transit_route_result);
		
		
		try {
			initView();
			initMapView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void initView() throws Exception {
		mImageButtonBack = (ImageButton)findViewById(R.id.transit_btn_back);
		mImageButtonBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		showMapButton = (Button)findViewById(R.id.transit_btn_showmap);
		showMapButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(currentStatus==STATUS_MAP){
					changeStatus(STATUS_LIST);
				}
				else{
					changeStatus(STATUS_MAP);
					try{
						mBaiduMap.clear();
						TransitRouteOverlay overlay = new TransitRouteOverlay(mBaiduMap);
						mBaiduMap.setOnMarkerClickListener(overlay);
			            overlay.setData(mTransitRouteLine);
			            overlay.addToMap();
			            overlay.zoomToSpan();
			            MapStatusUpdate u = MapStatusUpdateFactory.zoomTo((float) (mBaiduMap.getMaxZoomLevel()*0.7));
			    		mBaiduMap.animateMapStatus(u);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		});
		
		mCollectImageView = (ImageView)findViewById(R.id.transit_button_collect_img);
		
		mTransitTitleTextView = (TextView)findViewById(R.id.transit_title);
		mTransitTimeTextView = (TextView)findViewById(R.id.transit_time);
		mTransitDistanceTextView = (TextView)findViewById(R.id.transit_distance);
		mTransitWalkTextView = (TextView)findViewById(R.id.transit_walk);
		
		fillData();
		mTransitRouteListView = (ListView)findViewById(R.id.transit_result_listview);
		mTransitRouteListAdpater = new RouteListAdapter(getApplicationContext());
		mTransitRouteListView.setAdapter(mTransitRouteListAdpater);
	}

	/**
	 * 获取并填充数据
	 */
	private void fillData(){
		mTransitRouteLine = RoutePlanActivity.mRoutePlans.get(getIntent().getIntExtra("position", 0));
		mTransitStepListData = mTransitRouteLine.getAllStep();
		
		startPosition = getIntent().getStringExtra("startPosition")+"";
		endPosition = getIntent().getStringExtra("endPosition")+"";
		mTransitTitleTextView.setText(getIntent().getStringExtra("title")+"");
		mTransitTimeTextView.setText(getIntent().getStringExtra("time")+"");
		mTransitDistanceTextView.setText(getIntent().getStringExtra("distance")+"");
		mTransitWalkTextView.setText(getIntent().getStringExtra("walk")+"");
		
		//判断此线路是否被收藏
		dataToCollectTitle = startPosition+"→"+endPosition+" "+mTransitTitleTextView.getText().toString();
		isCollect = FavoriteUtil.favoriteTransitRouteListContains(getApplicationContext(), dataToCollectTitle);
		if(isCollect){
			mCollectImageView.setImageResource(R.drawable.icon_collect_select);
		}
	}
	
	
	public void initMapView() throws Exception {
		mListLayout = (LinearLayout)findViewById(R.id.transit_listLayout);
		mMapLayout = (RelativeLayout)findViewById(R.id.transit_mapLayout);
		mMapLayout.setVisibility(View.GONE);

		mMapFragment = ((MapFragment) (getSupportFragmentManager()
				.findFragmentById(R.id.transit_mapview)));
		mMapView = mMapFragment.getMapView();
		mBaiduMap = mMapFragment.getBaiduMap();
	}
	
	
	
	/**
	 * 改变界面显示 1地图	2列表
	 * @param status
	 */
	private void changeStatus(int status){
		switch(status){
		case STATUS_LIST:{
			mListLayout.setVisibility(View.VISIBLE);
			mMapLayout.setVisibility(View.GONE);
			currentStatus = STATUS_LIST;
			showMapButton.setText("地图");
			break;
		}
		case STATUS_MAP:{
			mListLayout.setVisibility(View.GONE);
			mMapLayout.setVisibility(View.VISIBLE);
			currentStatus = STATUS_MAP;
			showMapButton.setText("列表");
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
		case R.id.transit_button_share:
		{
			StringBuilder dataToShare = new StringBuilder();
			dataToShare.append(startPosition+" → "+endPosition);
			dataToShare.append("  [起点："+startPosition+"；");
			for(TransitStep step : mTransitStepListData){
				dataToShare.append(step.getInstructions()+"；");
			}
			dataToShare.append("终点："+endPosition+"]");
			
			Intent it = new Intent(Intent.ACTION_SEND);
			it.putExtra(Intent.EXTRA_TEXT, "换乘: "+dataToShare.toString());
			it.setType("text/plain");
			startActivity(Intent.createChooser(it, "将此线路分享到"));

			break;
		}	
		case R.id.transit_button_collect:
		{
			if(!isCollect){
				if(dataToCollect.toString().equals("")){
					dataToCollect.append(dataToCollectTitle+"#");
					dataToCollect.append(mTransitTitleTextView.getText().toString()+"#");
					dataToCollect.append(mTransitTimeTextView.getText().toString()+"#");
					dataToCollect.append(mTransitDistanceTextView.getText().toString()+"#");
					dataToCollect.append(mTransitWalkTextView.getText().toString()+"#");
					dataToCollect.append("["+startPosition+"#");
					for(TransitStep step : mTransitStepListData){
						dataToCollect.append(step.getInstructions()+"#");
					}
					dataToCollect.append(endPosition+"]");
				}
				FavoriteUtil.collect(getApplicationContext(), FavoriteUtil.TRANSIT_ROUTE, dataToCollect.toString());
				
				Toast.makeText(TransitRouteResultActivity.this, "已添加到收藏夹", Toast.LENGTH_SHORT).show();
				mCollectImageView.setImageResource(R.drawable.icon_collect_select);
				isCollect = true;
			}else{
				FavoriteUtil.cancelFavoriteTransitRoute(getApplicationContext(), dataToCollectTitle);
				
				Toast.makeText(TransitRouteResultActivity.this, "已取消收藏", Toast.LENGTH_SHORT).show();
				mCollectImageView.setImageResource(R.drawable.icon_collect);
				isCollect = false;
			}
			break;
		}
		default:return;
		
		}
	}
	
	

	@Override
	public void onMapClick(LatLng arg0) {
	}
	
	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		return false;
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
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }
    
    
    
    public class RouteListAdapter extends BaseAdapter {

    	private LayoutInflater mInflater;  
    	private Context mContext;
    	
    	public RouteListAdapter(Context context){
    		mContext = context;
    		mInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
    	}
    	
		@Override
		public int getCount() {
			return mTransitStepListData.size()+3;
		}

		@Override
		public Object getItem(int position) {
			return mTransitStepListData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			try{
				convertView = mInflater.inflate(R.layout.transit_route_list_item, null);
	            ImageView imageView = (ImageView)convertView.findViewById(R.id.transit_route_list_item_img);
	            TextView textView = (TextView)convertView.findViewById(R.id.transit_route_list_item_title);
	            
	            if(position == 0){
	            	imageView.setImageResource(R.drawable.route_crosscity_detail_startpoint);
	            	textView.setText(startPosition);
	            }else if(position == getCount()-2){
	            	imageView.setImageResource(R.drawable.route_crosscity_detail_endpoint);
	            	textView.setText(endPosition);
	            }else if(position == getCount()-1){
	            	convertView.setBackgroundColor(Color.parseColor("#00FFFFFF"));
	            	imageView.setImageResource(R.drawable.none_bg);
	            	textView.setText("");
	            }else{
	            
		            Object step = getItem(position-1);
	                if (step instanceof TransitRouteLine.TransitStep) {
	                	String nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
	                	textView.setText(nodeTitle.trim());
	                	if(nodeTitle.startsWith("乘坐")){
	                		imageView.setImageResource(R.drawable.icon_route_sectional_bus);
	                	}else if(nodeTitle.startsWith("步行")){
	                		imageView.setImageResource(R.drawable.icon_route_sectional_walk);
	                	}
	                }
	            }

			}catch(Exception e){
				e.printStackTrace();
			}
			
			return convertView;
		}
    	
    }

}
