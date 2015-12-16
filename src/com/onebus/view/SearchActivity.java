package com.onebus.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.DistanceUtil;
import com.onebus.OneBusApplication;
import com.onebus.R;
import com.onebus.util.NetWorkUtil;

/**
 * SearchActivity 只能根据公交线路搜索
 * 
 * @author Think-Usar
 *
 */
public class SearchActivity extends Activity implements
		OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

	private PoiSearch mPoiSearch = null;
	private SuggestionSearch mSuggestionSearch;

	private AutoCompleteTextView mSearchTextView;
	private ImageButton mImageButtonBack;
	private Button mButtonSearch;

	LayoutInflater inflater;
	private View listFooterView;
	private ListView mResultListView;
	private ArrayList<HashMap<String, Object>> mResultListData;
	private ArrayList<HashMap<String, Object>> mHistoryListData;
	private SimpleAdapter mResultListViewAdapter;

	private ProgressDialog mProgressDialog;

	private SharedPreferences preference;
	private JSONArray historyJsonArray;

	private InputMethodManager manager;

	private int tag;
	private String strMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		tag = getIntent().getIntExtra("tag", 0);

		if (tag == 1) {

			strMessage = getIntent().getStringExtra("strMessage");

		}

		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		initView();
	}

	/**
	 * 隐藏软键盘
	 */
	public void hideKeyboard() {
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null) {
				manager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	public void initView() {
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);

		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);

		mImageButtonBack = (ImageButton) findViewById(R.id.search_btn_back);
		mImageButtonBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		mButtonSearch = (Button) findViewById(R.id.search_btn_search);
		mButtonSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String searchKey = mSearchTextView.getText().toString();
				if (!searchKey.trim().equals("")) {
					// 隐藏键盘
					hideKeyboard();

					if (NetWorkUtil.isNetWorkConnected(getApplicationContext())) {
						showPoiResult(OneBusApplication.CURRENT_CITY, searchKey
								+ " 公交");
					} else {
						Toast.makeText(
								SearchActivity.this,
								getResources().getString(
										R.string.networkdisabled),
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		mSearchTextView = (AutoCompleteTextView) findViewById(R.id.search_edittext);

		if (tag == 1) {

			mSearchTextView.setText(strMessage);

		}

		mSearchTextView.setOnEditorActionListener(new OnEditorActionListener() {
			@SuppressLint("NewApi")
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (event != null
						&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					return true;
				}
				return false;
			}
		});
		TextWatcher watcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mResultListData.clear();
				if (mResultListView != null)
					mResultListView.removeFooterView(listFooterView);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() <= 1) {
					return;
				}
				String searchKey = mSearchTextView.getText().toString();
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(searchKey + " 公交").city(
										OneBusApplication.CURRENT_CITY));

			}
		};
		mSearchTextView.addTextChangedListener(watcher);

		preference = getSharedPreferences("History", Context.MODE_PRIVATE);
		mResultListView = (ListView) findViewById(R.id.search_result_listview);
		mResultListData = new ArrayList<HashMap<String, Object>>();
		mHistoryListData = new ArrayList<HashMap<String, Object>>();
		// load the history search list
		try {
			String historySearchData = preference.getString("SearchHistory",
					"[]");
			JSONArray jsonArray = new JSONArray(historySearchData);
			if (jsonArray != null) {
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject obj = jsonArray.optJSONObject(i);
					if (obj != null) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						Object type = obj.opt("type");
						map.put("uid", obj.opt("uid"));
						if (type != null
								&& type.toString().contains("BUS_STATION")) {
							map.put("type", obj.opt("type"));
							map.put("title", obj.opt("title"));
							map.put("city", obj.opt("city"));
							map.put("district", obj.opt("district"));
							map.put("address", obj.opt("address"));
							map.put("distance", obj.opt("distance"));
						} else {
							map.put("type", obj.opt("type"));
							map.put("title", obj.opt("title"));
							map.put("city", obj.opt("city"));
							map.put("district", obj.opt("district"));
							map.put("buslineId", obj.opt("buslineId"));
						}
						mResultListData.add(map);
						mHistoryListData.add(map);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (mResultListData.size() != 0) {
			// ListView 底部View
			listFooterView = inflater
					.inflate(R.layout.search_list_footer, null);
			mResultListView.addFooterView(listFooterView);
			listFooterView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mResultListData.clear();
					mHistoryListData.clear();
					mResultListViewAdapter.notifyDataSetChanged();
					mResultListView.removeFooterView(listFooterView);
				}
			});
		}

		mResultListViewAdapter = new SimpleAdapter(this, mResultListData,
				R.layout.search_result_list_item, new String[] { "title",
						"city", "district" }, new int[] {
						R.id.search_list_item_title,
						R.id.search_list_item_city,
						R.id.search_list_item_district });
		mResultListView.setAdapter(mResultListViewAdapter);
		mResultListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long arg3) {
				try {
					if (!isAlreadyHas(mResultListData.get(position))) {
						mHistoryListData.add(mResultListData.get(position)); // 添加记录
					}
					if ((mResultListData.get(position).get("type").toString()
							.contains("BUS_STATION"))) { // 公交站点
						Intent stationBusLineIntent = new Intent(
								SearchActivity.this,
								StationBusLineActivity.class);
						String[] busLineNameList = ((String) (mResultListData
								.get(position).get("address"))).split(";");
						stationBusLineIntent.putExtra("busLineNameList",
								busLineNameList);
						stationBusLineIntent.putExtra(
								"stationName",
								(String) (mResultListData.get(position)
										.get("title")) + "-公交车站");
						startActivity(stationBusLineIntent);
					} else {
						Intent busLineIntent = new Intent(SearchActivity.this,
								BusLineActivity.class);
						busLineIntent.putExtra("busLineId",
								(String) (mResultListData.get(position)
										.get("buslineId")));
						startActivity(busLineIntent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// 语音输入，直接搜索
		String searchKey = getIntent().getStringExtra("searchKey");
		if (searchKey != null && !searchKey.trim().equals("")) {
			mSearchTextView.setText(searchKey);
			hideKeyboard();
			if (NetWorkUtil.isNetWorkConnected(getApplicationContext())) {
				showPoiResult(OneBusApplication.CURRENT_CITY, searchKey + " 公交");
			} else {
				Toast.makeText(SearchActivity.this,
						getResources().getString(R.string.networkdisabled),
						Toast.LENGTH_LONG).show();
			}
		}

	}

	public boolean isAlreadyHas(HashMap<String, Object> map) {
		for (HashMap<String, Object> cMap : mHistoryListData) {
			if (cMap.get("title").toString()
					.equals(map.get("title").toString()))
				return true;
		}
		return false;
	}

	/**
	 * 搜索
	 */
	private void showPoiResult(String city, String key) {
		mProgressDialog = new ProgressDialog(SearchActivity.this);
		mProgressDialog.setMessage("请稍等....");
		mProgressDialog.show();

		mPoiSearch.searchInCity((new PoiCitySearchOption()).city(city)
				.keyword(key).pageNum(0));
	}

	@Override
	public void onGetSuggestionResult(SuggestionResult result) {
		if (result == null || result.getAllSuggestions() == null) {
			return;
		}
		for (SuggestionResult.SuggestionInfo info : result.getAllSuggestions()) {
			if (info.key != null)
				showPoiResult(OneBusApplication.CURRENT_CITY, info.key);
		}

	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			// Toast.makeText(SearchActivity.this,
			// getResources().getString(R.string.notFoundResult),
			// Toast.LENGTH_LONG)
			// .show();
			if (mProgressDialog != null && mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			return;
		}

		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			for (PoiInfo poi : result.getAllPoi()) {
				Log.i("SearchResult", "" + poi.name + ":" + poi.type.toString()
						+ " address:" + poi.address);
				if (poi.type == PoiInfo.POITYPE.BUS_LINE
						|| poi.type == PoiInfo.POITYPE.SUBWAY_LINE
						|| poi.type == PoiInfo.POITYPE.BUS_STATION) {
					if (isAlreadyAdd(poi.name)) {
						continue;
					}
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("uid", poi.uid);
					if (poi.type == PoiInfo.POITYPE.BUS_STATION) {
						map.put("type", poi.type);
						map.put("title", poi.name);
						map.put("city", poi.city);
						map.put("district", "");
						map.put("address", poi.address);
						// 计算距离当前位置的距离
						double distance = DistanceUtil.getDistance(
								OneBusApplication.CURRENT_LOCATION,
								poi.location);
						map.put("distance", (int) distance + "米");
					} else {
						map.put("type", poi.type);
						map.put("buslineId", poi.uid);
						map.put("title", poi.name);
						map.put("city", poi.city);
						map.put("district", "");
					}
					mResultListData.add(map);
					continue;
				}
			}

			if (mResultListData.size() == 0) {
				// Toast.makeText(SearchActivity.this,
				// getResources().getString(R.string.notFoundResult),
				// Toast.LENGTH_SHORT).show();
			}

			mResultListViewAdapter.notifyDataSetChanged();
		}

		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

			// TODO
			String strInfo = "";
			for (CityInfo cityInfo : result.getSuggestCityList()) {
				strInfo += cityInfo.city;
				strInfo += ",";
			}
			// Toast.makeText(SearchActivity.this, strInfo, Toast.LENGTH_LONG)
			// .show();
		}
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	/**
	 * 判断列表是否已经添加过同样的关键词
	 * 
	 * @param str
	 * @return
	 */
	public boolean isAlreadyAdd(String str) {
		for (HashMap<String, Object> map : mResultListData) {
			if (map.get("title").toString().equals(str))
				return true;
		}
		return false;
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult arg0) {

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		Editor editor = preference.edit();
		historyJsonArray = new JSONArray();
		try {
			for (int i = mHistoryListData.size() - 1, number = 1; i >= 0
					&& number <= 10; i--, number++) {
				Object type = mHistoryListData.get(i).get("type");
				HashMap<String, Object> map = mHistoryListData.get(i);
				JSONObject obj = new JSONObject();
				obj.put("uid", map.get("uid"));
				if (type.toString().contains("BUS_STATION")) {
					obj.putOpt("type", map.get("type"));
					obj.putOpt("title", map.get("title"));
					obj.putOpt("city", map.get("city"));
					obj.putOpt("district", map.get("district"));
					obj.putOpt("address", map.get("address"));
					obj.putOpt("distance", map.get("distance"));
				} else {
					obj.putOpt("type", map.get("type"));
					obj.putOpt("title", map.get("title"));
					obj.putOpt("city", map.get("city"));
					obj.putOpt("district", map.get("district"));
					obj.putOpt("buslineId", map.get("buslineId"));
				}
				historyJsonArray.put(obj);
			}
			editor.putString("SearchHistory", historyJsonArray.toString());
		} catch (Exception e) {
			editor.putString("", "[]");
			e.printStackTrace();
		}
		editor.commit();

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mPoiSearch.destroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

}
