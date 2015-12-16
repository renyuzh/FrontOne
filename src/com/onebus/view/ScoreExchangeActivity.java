package com.onebus.view;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.onebus.IPAdress;
import com.onebus.R;
import com.onebus.model.ScoreExchange;
import com.onebus.util.BitmapAndString;

public class ScoreExchangeActivity extends Activity {

	private String URL = "/onebus/android/getAllGoods";
	private IPAdress IPadress;
	private List<ScoreExchange> scoreExchanges;
	private Handler mhandler;
	private final static int UPDATE_LIST = 0x002;
	private GridViewAdapter gridViewAdapter;

	private ProgressDialog mProgressDialog;

	private ImageButton back;

	private GridView gridView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scoreexchange);

		scoreExchanges = new ArrayList<ScoreExchange>();
		gridViewAdapter = new GridViewAdapter();

		initView();

		mProgressDialog = new ProgressDialog(ScoreExchangeActivity.this);
		mProgressDialog.setIcon(R.drawable.common_topbar_route_foot_pressed);
		mProgressDialog.setMessage("请稍等....");
		mProgressDialog.show();

		initInformation();

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();

			}
		});

		mhandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_LIST: {

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							gridViewAdapter.notifyDataSetChanged();
							mProgressDialog.dismiss();
						}
					});

					break;
				}
				default:
					super.handleMessage(msg);
				}
			}

		};
	}

	private void initView() {

		IPadress = new IPAdress();
		URL = "http://" + IPadress.getIP() + ":" + IPadress.getPORT() + URL;

		back = (ImageButton) findViewById(R.id.scoreExchange_btn_back);
		gridView = (GridView) findViewById(R.id.scoreGrid);

		gridView.setAdapter(gridViewAdapter);
		// 为GridView设定监听器
		gridView.setOnItemClickListener(new gridViewListener());

	}

	public void initInformation() {
		new Thread() {

			public void run() {

				try {

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();
					int i;

					jsonObject.put("user", "user");

					jsonArray.put(jsonObject);

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URL);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair(
							"getAllGoods", jsonArray + "");
					paramList.add(param);

					httpPost.setEntity(new UrlEncodedFormEntity(paramList,
							"UTF_8"));

					HttpResponse httpResponse = client.execute(httpPost);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						String result = EntityUtils.toString(
								httpResponse.getEntity(), HTTP.UTF_8);

						jsonArray = new JSONArray(result);
					}

					ScoreExchange scoreExchange;

					for (i = 0; i < jsonArray.length(); i++) {

						scoreExchange = new ScoreExchange();

						jsonObject = (JSONObject) jsonArray.get(i);

						scoreExchange.setImageStr(jsonObject
								.optString("goodsPicture"));
						scoreExchange
								.setName(jsonObject.optString("goodsName"));

						scoreExchange.setScore(jsonObject.getString("score"));

						scoreExchanges.add(scoreExchange);

					}

					Message msg = new Message();
					msg.what = UPDATE_LIST;
					mhandler.sendMessage(msg);

				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	public class GridViewAdapter extends BaseAdapter {

		@SuppressLint("NewApi")
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			LayoutInflater mInflator = ScoreExchangeActivity.this
					.getLayoutInflater();
			convertView = mInflator.inflate(R.layout.gridviewitem, null);

			/* 根据parent动态设置convertview的大小 */
			convertView.setLayoutParams(new AbsListView.LayoutParams(
					(int) (parent.getWidth() / 2) - 1, (int) (parent
							.getHeight() / 3)));// 动态设置item的高度

			ScoreExchange scoreExchange = scoreExchanges.get(position);

			ImageView imageScore = (ImageView) convertView
					.findViewById(R.id.image_score);
			TextView nameScore = (TextView) convertView
					.findViewById(R.id.name_score);
			TextView shopScore = (TextView) convertView
					.findViewById(R.id.shop_score);

			Bitmap photo = BitmapAndString.stringtoBitmap(scoreExchange
					.getImageStr());
			Drawable drawable = new BitmapDrawable(photo);
			// imageScore.setImageDrawable(drawable);
			imageScore.setBackground(drawable);

			nameScore.setText(scoreExchange.getName());
			shopScore.setText(Html.fromHtml("<font color=\"red\">"+scoreExchange.getScore() + "</font>积分"));

			return convertView;
		}

		@Override
		public int getCount() {
			return scoreExchanges.size();
		}

		@Override
		public Object getItem(int position) {
			return scoreExchanges.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	}

	class gridViewListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			System.out.println("arg2 = " + position); // 打印出点击的位置

		}

	}
}
