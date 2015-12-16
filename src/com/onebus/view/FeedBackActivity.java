package com.onebus.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.onebus.IPAdress;
import com.onebus.R;
import com.onebus.util.IncreaseIntegral;
import com.onebus.util.TimeChange;

public class FeedBackActivity extends Activity {

	private String URL = "/onebus/android/feedback";
	private IPAdress IPadress;

	private final static int COMPLAINSUCCESS = 0x114;
	private final static int COMPLAINFAIL = 0x115;
	private final static int COMPLAININTNET = 0x116;
	private Handler feedbackHandler;
	private ProgressDialog mProgressDialog;

	private ImageButton backButton;

	private Button feedBackButton;

	private TextView customerSuggestion;
	private TextView customerContactInfo;

	private TextView customerService;

	private SharedPreferences preferences_user;
	private SharedPreferences preferences;

	private Handler scorehandler;
	private final static int UPDATE_SCORE = 0x002;
	private Context context;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);

		context = this;
		initView();

		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();

			}
		});

		feedBackButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isSuitablyMessage()) {

					complaintsToReceive();
					sendScore();

					mProgressDialog = new ProgressDialog(FeedBackActivity.this);
					mProgressDialog
							.setIcon(R.drawable.common_topbar_route_foot_pressed);
					mProgressDialog.setMessage("请稍等....");
					mProgressDialog.show();

				}

			}
		});

		customerService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				messageDialog();

			}
		});

		// 线程交互
		feedbackHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case COMPLAINSUCCESS: {

					mProgressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "意见提交成功!",
							Toast.LENGTH_SHORT).show();

					finish();

					break;
				}
				case COMPLAINFAIL: {

					mProgressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "投意见提交失败，请稍候重试!",
							Toast.LENGTH_SHORT).show();

					break;

				}
				case COMPLAININTNET: {

					mProgressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "后台服务器故障,请之后重试!",
							Toast.LENGTH_SHORT).show();
					break;
				}
				default:
					super.handleMessage(msg);
				}
			}

		};

		scorehandler = new Handler() {

			@Override
			public void handleMessage(final Message msg) {
				switch (msg.what) {
				case UPDATE_SCORE: {

					/*
					 * Toast.makeText(getApplicationContext(), "积分更新成功！",
					 * Toast.LENGTH_SHORT).show();
					 */

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

		preferences = getSharedPreferences("Login", MODE_PRIVATE);
		preferences_user = getSharedPreferences("USERMESSAGE",
				Context.MODE_PRIVATE);

		backButton = (ImageButton) findViewById(R.id.feedback_btn_back);
		feedBackButton = (Button) findViewById(R.id.feedBack_button);
		customerSuggestion = (TextView) findViewById(R.id.txt_customer_suggestion);
		customerContactInfo = (TextView) findViewById(R.id.txt_customer_contact_info);
		customerService = (TextView) findViewById(R.id.customer_service);

	}

	private boolean isSuitablyMessage() {

		String suggestion = customerSuggestion.getText().toString();
		String contactInfo = customerContactInfo.getText().toString();

		if (suggestion.equals("") || contactInfo.equals("")) {

			Toast.makeText(getApplicationContext(), "请完善信息!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		return true;

	}

	private void messageDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				FeedBackActivity.this);

		final AlertDialog dialogBuilder = builder.show();

		// 设置Title的图标
		builder.setIcon(R.drawable.ic_launcher);
		// 设置Title的内容
		builder.setTitle("提示");
		// 设置Content来显示一个信息
		builder.setMessage("确定要给“一号公交”的客服打电话吗？");
		// 设置一个PositiveButton
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + "18392120258"));
				startActivity(intent);

				dialogBuilder.dismiss();

			}
		});
		// 设置一个NegativeButton
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialogBuilder.dismiss();
			}
		});
		// 显示出该对话框
		builder.show();

	}

	private void complaintsToReceive() {

		new Thread() {

			public void run() {

				try {
					boolean isLogin = preferences.getBoolean("isLogin", false);

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					if (isLogin) {

						jsonObject.put("userPhone",
								preferences_user.getString("phone", ""));

					} else {

						jsonObject.put("userPhone", "");

					}

					jsonObject.put("content", customerSuggestion.getText()
							.toString());
					jsonObject.put("contactWay", customerContactInfo.getText()
							.toString());
					jsonObject.put("feedbackType", "意见");
					jsonObject.put("busnumber", "0");
					jsonObject.put("feedbackTime", TimeChange.getNowDate());

					jsonArray.put(jsonObject);

					Log.e("...........", jsonArray.toString());

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URL);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair(
							"feedback", jsonArray + "");
					paramList.add(param);

					httpPost.setEntity(new UrlEncodedFormEntity(paramList,
							"UTF_8"));

					HttpResponse httpResponse = client.execute(httpPost);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						String result = EntityUtils.toString(
								httpResponse.getEntity(), HTTP.UTF_8);

						JSONArray jsonArray3 = new JSONArray(result);
						JSONObject jsonObject3 = (JSONObject) jsonArray3.get(0);

						if (jsonObject3.optString("status").equals("success")) {

							Message msg = new Message();
							msg.what = COMPLAINSUCCESS;
							feedbackHandler.sendMessage(msg);

						} else {

							Message msg = new Message();
							msg.what = COMPLAINFAIL;
							feedbackHandler.sendMessage(msg);

						}

					} else {

						Message msg = new Message();
						msg.what = COMPLAININTNET;
						feedbackHandler.sendMessage(msg);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {

					Message msg = new Message();
					msg.what = COMPLAININTNET;
					feedbackHandler.sendMessage(msg);

					e.printStackTrace();
				} catch (ClientProtocolException e) {

					Message msg = new Message();
					msg.what = COMPLAININTNET;
					feedbackHandler.sendMessage(msg);

					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}.start();

	}

	public void sendScore() {

		new Thread() {

			public void run() {

				try {

					String str = IncreaseIntegral.getJson(context, 1);

					JSONArray jsonArray = new JSONArray(str);
					JSONObject jsonObject = (JSONObject) jsonArray.get(0);

					if (jsonObject.optString("status").equals("success")) {

						Message msg = new Message();
						msg.what = UPDATE_SCORE;
						scorehandler.sendMessage(msg);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}.start();

	}
}
