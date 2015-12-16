package com.onebus.view;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.onebus.R;
import com.sinpo.xnfc.BusCard;

public class CardLogActivity extends Activity {
	
	private ImageView back;
	
	private ListView mResumeLogListView;
	private ArrayList<BusCard.ConsumeLog> mResumeLogList;
	private ResumeLogListAdapter mResumeLogListAdapter;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card_log);
		init();
	}

	
	public void init(){
		back = (ImageView) findViewById(R.id.cardlog_btn_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mResumeLogList = getIntent().getParcelableArrayListExtra("resumeLogList");
		
		if(mResumeLogList==null){
			Log.e("CardLogActivity", "List is NULL");
			mResumeLogList = new ArrayList<BusCard.ConsumeLog>();
		}
		
		/*
		String logStr = "";
		for(BusCard.ConsumeLog log : mResumeLogList){
			logStr += log.toString();
		}
		Log.e("mBusCard.getConsumeLogList", logStr);
		*/
		
		mResumeLogListView = (ListView)findViewById(R.id.cardlog_resumeLog);
		mResumeLogListAdapter = new ResumeLogListAdapter();
		mResumeLogListView.setAdapter(mResumeLogListAdapter);
	}
	
	
	
	
	
	
	public class ResumeLogListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mResumeLogList.size();
		}

		@Override
		public Object getItem(int position) {
			return mResumeLogList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			convertView = LayoutInflater.from(CardLogActivity.this).inflate(R.layout.cardlog_list_item, null);
			
			TextView cashTextView = (TextView)convertView.findViewById(R.id.cardlog_listitem_cash);
			TextView timeTextView = (TextView)convertView.findViewById(R.id.cardlog_listitem_time);
			TextView districtTextView = (TextView)convertView.findViewById(R.id.cardlog_listitem_district);
			cashTextView.setText(((BusCard.ConsumeLog)getItem(position)).cash);
			timeTextView.setText(((BusCard.ConsumeLog)getItem(position)).time);
			districtTextView.setText(((BusCard.ConsumeLog)getItem(position)).districtCode);
			
			
			return convertView;
		}
		
	}
	
	
	
	
	
	
	
}
