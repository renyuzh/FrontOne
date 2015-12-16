package com.onebus.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.baidu.mapapi.search.route.TransitRouteLine;
import com.onebus.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CollectTransitRouteResultActivity extends Activity {
	
	private ImageButton mImageButtonBack;
    
    private TextView mTransitTitleTextView;
    private TextView mTransitTimeTextView;
    private TextView mTransitDistanceTextView;
    private TextView mTransitWalkTextView;
    
    private ListView mTransitRouteListView;
    private CollectRouteListAdapter mTransitRouteListAdpater;
    private String[] mTransitRouteListData;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collect_transit_route_result);
		
		initView();
	}

	private void initView(){
		mImageButtonBack = (ImageButton)findViewById(R.id.collect_transit_btn_back);
		mImageButtonBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		
		mTransitTitleTextView = (TextView)findViewById(R.id.collect_transit_title);
		mTransitTimeTextView = (TextView)findViewById(R.id.collect_transit_time);
		mTransitDistanceTextView = (TextView)findViewById(R.id.collect_transit_distance);
		mTransitWalkTextView = (TextView)findViewById(R.id.collect_transit_walk);
		
		try {
			fillData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mTransitRouteListView = (ListView)findViewById(R.id.collect_transit_result_listview);
		mTransitRouteListAdpater = new CollectRouteListAdapter(getApplicationContext());
		mTransitRouteListView.setAdapter(mTransitRouteListAdpater);
		
	}

	
	/*
	 *我的位置→大明宫 707路#707路#58分钟#10.0公里#步行772米#[我的位置#步行499米,到达西工大站#乘坐707路
	 *,经过16站,到达太华路站#步行273米,到达终点站#大明宫] 
	 */
	private void fillData() throws Exception{
		
		String data = getIntent().getStringExtra("RouteData");

		String[] head = data.substring(data.indexOf("#")+1, data.indexOf("[")-1).split("#");
		Log.i("Collect Head", Arrays.toString(head));
		mTransitTitleTextView.setText(head[0]);
		mTransitTimeTextView.setText(head[1]);
		mTransitDistanceTextView.setText(head[2]);
		mTransitWalkTextView.setText(head[3]);
		
		mTransitRouteListData = data.substring(data.indexOf("[")+1, data.length()-1).split("#");
		Log.i("Collect body", Arrays.toString(mTransitRouteListData));
	}

	
	
	public class CollectRouteListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;  
		
		public CollectRouteListAdapter(Context context) {
			mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return mTransitRouteListData.length+1;
		}

		@Override
		public Object getItem(int position) {
			return mTransitRouteListData[position];
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
	            	textView.setText(mTransitRouteListData[0]);
	            }else if(position == getCount()-2){
	            	imageView.setImageResource(R.drawable.route_crosscity_detail_endpoint);
	            	textView.setText(mTransitRouteListData[mTransitRouteListData.length-1]);
	            }else if(position == getCount()-1){
	            	convertView.setBackgroundColor(Color.parseColor("#00FFFFFF"));
	            	imageView.setImageResource(R.drawable.none_bg);
	            	textView.setText("");
	            }else{
	            	String nodeTitle = mTransitRouteListData[position];
	            	textView.setText(nodeTitle.trim());
	            	if(nodeTitle.startsWith("乘坐")){
	            		imageView.setImageResource(R.drawable.icon_route_sectional_bus);
	            	}else if(nodeTitle.startsWith("步行")){
	            		imageView.setImageResource(R.drawable.icon_route_sectional_walk);
	            	}
	            }
			}catch(Exception e){
				e.printStackTrace();
			}
			return convertView;
		}
		
	}
	
	
	
	
}
