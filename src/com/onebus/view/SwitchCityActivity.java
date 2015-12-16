package com.onebus.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.onebus.util.MyLetterListView.OnTouchingLetterChangedListener;
import com.onebus.OneBusApplication;
import com.onebus.R;
import com.onebus.util.MyLetterListView;

public class SwitchCityActivity extends Activity {

	private ImageButton mImageButtonBack;

	private LinearLayout headerLayout;
	LayoutInflater inflater;

	private ListView mCityListView;
	private SwitchCityListAdapter mCityListAdapter;
	private List<String> cityList;
	private List<String> cityBackList;

	private TextView mCurrentCityTextView;
	private TextView mGpsCityTextView;
	private ImageView mCheckGpsCityImageView;

	private EditText mSearchText;

	private int currentCheckIndex = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_switch_city);

		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void init() throws Exception {
		mImageButtonBack = (ImageButton) findViewById(R.id.switchcity_btn_back);
		mImageButtonBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		headerLayout = (LinearLayout) inflater.inflate(
				R.layout.switchcity_list_header_view, null);

		mSearchText = (EditText) headerLayout
				.findViewById(R.id.switchcity_searchbox);
		TextWatcher watcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				searchInList(mSearchText.getText().toString());
			}
		};
		mSearchText.addTextChangedListener(watcher);

		mCurrentCityTextView = (TextView) headerLayout
				.findViewById(R.id.switchcity_currentcity);
		mCurrentCityTextView.setText(OneBusApplication.CURRENT_CITY);
		mGpsCityTextView = (TextView) headerLayout
				.findViewById(R.id.switchcity_gpscity);
		if (!OneBusApplication.GPS_CITY.trim().equals("")) {
			mGpsCityTextView.setText(OneBusApplication.GPS_CITY);
		}

		mCheckGpsCityImageView = (ImageView) headerLayout
				.findViewById(R.id.switchcity_gpscity_checkon);
		if (OneBusApplication.GPS_CITY.contains(OneBusApplication.CURRENT_CITY)) {
			mCheckGpsCityImageView.setVisibility(View.VISIBLE);
		}

		cityBackList = new ArrayList<String>();
		cityList = new ArrayList<String>();

		// 从文件加载城市列表
		readCityList();
		Comparator<Object> com = Collator.getInstance(java.util.Locale.CHINA);
		Collections.sort(cityList, com);

		mCityListView = (ListView) findViewById(R.id.switchcity_citylist);
		mCityListView.addHeaderView(headerLayout);

		mCityListAdapter = new SwitchCityListAdapter();
		mCityListView.setAdapter(mCityListAdapter);
		mCityListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {

					ImageView cityCheck = (ImageView) view
							.findViewById(R.id.switchcity_listitem_checkon);
					cityCheck.setVisibility(View.VISIBLE);
					OneBusApplication.CURRENT_CITY = cityList.get(position - 1);
					// Save CurrentCity
					SharedPreferences preferences = getSharedPreferences(
							"LastLocation", Context.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putString("currentCity",
							OneBusApplication.CURRENT_CITY);
					editor.commit();
					mCurrentCityTextView
							.setText(OneBusApplication.CURRENT_CITY);
					if (OneBusApplication.GPS_CITY
							.contains(OneBusApplication.CURRENT_CITY)) {
						mCheckGpsCityImageView.setVisibility(View.VISIBLE);
					} else {
						mCheckGpsCityImageView.setVisibility(View.GONE);
					}

					mCityListAdapter.notifyDataSetChanged();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void searchInList(String cityName) {
		cityList.clear();
		mCityListAdapter.notifyDataSetChanged();
		for (String name : cityBackList) {
			if (name.contains(cityName)) {
				cityList.add(name);
			}
		}
		mCityListAdapter.notifyDataSetChanged();
	}

	/**
	 * 读取城市数据
	 */
	private void readCityList() {
		InputStream myFile = null;
		BufferedReader br = null;
		myFile = getResources().openRawResource(R.raw.province);
		try {
			br = new BufferedReader(new InputStreamReader(myFile, "utf-8"));
			String cityName;
			while ((cityName = br.readLine()) != null) {
				if (!cityName.trim().equals("")) {
					cityList.add(cityName);
					cityBackList.add(cityName);
				}
			}
			br.close();
			myFile.close();
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void changeToGPSCity(View v) {
		if (v.getId() == R.id.switchcity_currentcityLayout
				&& !OneBusApplication.GPS_CITY.trim().equals("")) {
			OneBusApplication.CURRENT_CITY = OneBusApplication.GPS_CITY;
			mCurrentCityTextView.setText(OneBusApplication.CURRENT_CITY);
			mCheckGpsCityImageView.setVisibility(View.VISIBLE);
			
			mCityListAdapter.notifyDataSetChanged();

			SharedPreferences preferences = getSharedPreferences(
					"LastLocation", Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putString("currentCity", OneBusApplication.CURRENT_CITY);
			editor.commit();
		}
	}

	class SwitchCityListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return cityList.size();
		}

		@Override
		public Object getItem(int position) {
			return cityList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			try {
				convertView = View.inflate(SwitchCityActivity.this,
						R.layout.switchcity_list_item, null);

				TextView cityName = (TextView) convertView
						.findViewById(R.id.switchcity_listitem_name);
				ImageView cityCheck = (ImageView) convertView
						.findViewById(R.id.switchcity_listitem_checkon);
				String curCityName = (String) getItem(position);
				cityName.setText(curCityName);
				if (curCityName.contains(OneBusApplication.CURRENT_CITY)) {
					cityCheck.setVisibility(View.VISIBLE);
					currentCheckIndex = position;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return convertView;
		}

	}

}
