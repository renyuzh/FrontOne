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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onebus.IPAdress;
import com.onebus.R;
import com.onebus.model.ScoreExchange;

public class ScoreActivity extends Activity {

	private String URL = "/onebus/android/getScore";
	private IPAdress IPadress;
	private Handler mhandler;
	private final static int UPDATE_SCORE = 0x002;

	private ProgressDialog mProgressDialog;

	private ImageButton back;

	private RelativeLayout scoreExchange;
	private RelativeLayout exchangeRules;

	private TextView score;

	private SharedPreferences preferences_user;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score);

		initView();

		mProgressDialog = new ProgressDialog(ScoreActivity.this);
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

		scoreExchange.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(),
						ScoreExchangeActivity.class);
				startActivity(intent);

			}
		});

		exchangeRules.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(),
						ScoreRulesActivity.class);
				startActivity(intent);

			}
		});

		mhandler = new Handler() {

			@Override
			public void handleMessage(final Message msg) {
				switch (msg.what) {
				case UPDATE_SCORE: {

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {

								String str = msg.obj.toString();
								JSONArray jsonArray = new JSONArray(str);
								JSONObject jsonObject = (JSONObject) jsonArray
										.get(0);

								score.setText(jsonObject.optInt("score") + "");
								mProgressDialog.dismiss();

							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
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

		preferences_user = getSharedPreferences("USERMESSAGE",
				Context.MODE_PRIVATE);

		back = (ImageButton) findViewById(R.id.score_btn_back);

		scoreExchange = (RelativeLayout) findViewById(R.id.score_name);
		exchangeRules = (RelativeLayout) findViewById(R.id.card_score);

		score = (TextView) findViewById(R.id.userScore);

	}

	public void initInformation() {
		new Thread() {

			public void run() {

				try {

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("phone",
							preferences_user.getString("phone", "电话"));

					jsonArray.put(jsonObject);

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URL);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair(
							"getScore", jsonArray + "");
					paramList.add(param);

					httpPost.setEntity(new UrlEncodedFormEntity(paramList,
							"UTF_8"));

					HttpResponse httpResponse = client.execute(httpPost);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						String result = EntityUtils.toString(
								httpResponse.getEntity(), HTTP.UTF_8);

						jsonArray = new JSONArray(result);

						Message msg = new Message();
						msg.what = UPDATE_SCORE;
						msg.obj = jsonArray.toString();
						mhandler.sendMessage(msg);
					}

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
}
