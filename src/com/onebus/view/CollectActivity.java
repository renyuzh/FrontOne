package com.onebus.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.onebus.R;
import com.onebus.util.FavoriteUtil;

public class CollectActivity extends Activity {

	private ImageButton mBackImageButton;
	
	private ListView mCollectListView;
	private ArrayList<String> mCollectList;
	private ArrayList<String> mCollectDataList;
	private CollectListAdapter mCollectListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collect);
		
		try {
			initView();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void initView() throws Exception{
		mBackImageButton = (ImageButton)findViewById(R.id.collect_btn_back);
		mBackImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mCollectList =  new ArrayList<String>();
		mCollectDataList = new ArrayList<String>();
		fillListData();
		
		mCollectListView = (ListView)findViewById(R.id.collect_listview);
		mCollectListAdapter = new CollectListAdapter(getApplicationContext(), R.layout.collect_list_item, mCollectList);
		mCollectListView.setAdapter(mCollectListAdapter);
		mCollectListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String text = mCollectDataList.get(position);
				if(text.contains("→")){	//换乘线路
					try{
						Intent intent = new Intent(CollectActivity.this, CollectTransitRouteResultActivity.class);
						intent.putExtra("RouteData", text);
						startActivity(intent);
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{	//公交线路
					Intent intent = new Intent(CollectActivity.this, BusLineActivity.class);
					intent.putExtra("busLineId",text.split(":")[0]);
					startActivity(intent);
				}
			}
		});
		
	}
	
	protected void onResume(){
		
		super.onResume();
		
		mCollectDataList.clear();
		mCollectList.clear();
		fillListData();
		
		mCollectListAdapter.notifyDataSetChanged();
		
	}


	private void fillListData() {
		String[] busData = FavoriteUtil.getFavoriteBusLineList(getApplicationContext());
		String[] transitData = FavoriteUtil.getFavoriteTransitRouteList(getApplicationContext());
		for(String str : busData){
			if(!str.trim().equals("")){
				mCollectList.add(str.split(":")[1]);
				mCollectDataList.add(str);
			}
		}
		for(String str : transitData){
			int index = str.indexOf("#");
			if(index!=-1){
				String tag = str.substring(0, index);
				mCollectList.add(tag);
				mCollectDataList.add(str);
			}
		}
	}
	
	
	public class CollectListAdapter extends ArrayAdapter {

		private int resource;
		
		public CollectListAdapter(Context context, int resource, List<String> objects) {
			super(context, resource, objects);
			this.resource = resource;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			convertView = inflater.inflate(resource, null);

            TextView text = (TextView) convertView.findViewById(R.id.collect_list_item_textview);
            text.setText(mCollectList.get(position));
            
            return convertView;
		}
		
	}
	
	
}
