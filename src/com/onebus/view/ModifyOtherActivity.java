package com.onebus.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onebus.IPAdress;
import com.onebus.R;
import com.onebus.util.BitmapAndString;
import com.onebus.util.CacheUtil;
import com.onebus.util.MD5;
import com.onebus.util.NetWorkUtil;

public class ModifyOtherActivity extends Activity {

	private String URL = "/onebus/android/modifyUser";
	private String URLPHONE = "/onebus/android/setPhone";
	private String URLPMODIFYHONE = "";
	private IPAdress IPadress;

	private final static int LOGINFAIL = 0x115;
	private final static int LOGINSUCCESS = 0x116;
	private final static int LOGININTER = 0x117;

	private final static int MODIFYSUCCESS = 0x118;
	private final static int MODIFYUSEREXIST = 0x119;
	private final static int MODIFYPASSWORD = 0x120;

	private boolean isClickSecurity = false;

	private TextView modifyOther;
	private Button okButton;
	private ImageButton backButton;

	private LinearLayout modify_userName;
	private LinearLayout modify_userPhone;

	private EditText modifyUserName;

	private EditText modifyUserPhone;
	private EditText modifyPassword;
	private Button securityCode;
	private EditText inputsecurityCode;

	private int type;

	private TimeCount time;

	private SharedPreferences preferences_user;

	private Handler modifyHandler;
	private ProgressDialog mProgressDialog;

	private String newPhone;
	private String newCode;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modifyuser);

		type = getIntent().getIntExtra("modifyType", 0);
		time = new TimeCount(30000, 1000);

		initView();

		if (type == 1) {

			modifyOther.setText("修改用户名");
			modify_userName.setVisibility(View.VISIBLE);
			modify_userPhone.setVisibility(View.GONE);

		} else if (type == 2) {

			modifyOther.setText("绑定手机号");
			modify_userName.setVisibility(View.GONE);
			modify_userPhone.setVisibility(View.VISIBLE);

		}

		// 点击确定按钮
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!NetWorkUtil.isNetWorkConnected(getApplicationContext())) {

					Toast.makeText(getApplicationContext(),
							"网络连接失败，请检查网络是否可用!", Toast.LENGTH_SHORT).show();

					return;

				}

				if (type == 1) {

					if (isSuitablyUserName()) {

						modifyUserName();

						mProgressDialog = new ProgressDialog(
								ModifyOtherActivity.this);
						mProgressDialog
								.setIcon(R.drawable.common_topbar_route_foot_pressed);
						mProgressDialog.setMessage("请稍等....");
						mProgressDialog.show();

					}

				} else if (type == 2) {

					if (!isClickSecurity) {

						Toast.makeText(getApplicationContext(), "请先获取验证码!",
								Toast.LENGTH_SHORT).show();

						return;

					}

					if (isSuitablyUserPhone()) {

						if (!modifyUserPhone.getText().toString()
								.equals(newPhone)) {

							Toast.makeText(getApplicationContext(),
									"绑定手机号有误，获取验证码后请勿随意更改!", Toast.LENGTH_SHORT)
									.show();

							return;

						}

						if (!inputsecurityCode.getText().toString()
								.equals(newCode)) {

							Toast.makeText(getApplicationContext(),
									"验证码输入有误请重新输入!", Toast.LENGTH_SHORT).show();

							return;

						}

						modifyPhone();

						mProgressDialog = new ProgressDialog(
								ModifyOtherActivity.this);
						mProgressDialog
								.setIcon(R.drawable.common_topbar_route_foot_pressed);
						mProgressDialog.setMessage("请稍等....");
						mProgressDialog.show();

					}

				}

			}
		});

		// 点击回退按钮
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onBackPressed();

			}
		});

		// 点击获取验证码信息
		securityCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!NetWorkUtil.isNetWorkConnected(getApplicationContext())) {

					Toast.makeText(getApplicationContext(),
							"网络连接失败，请检查网络是否可用!", Toast.LENGTH_SHORT).show();

					return;

				}

				if (modifyUserPhone.getText().toString().equals("")) {

					Toast.makeText(getApplicationContext(), "请先输入绑定手机号!",
							Toast.LENGTH_SHORT).show();

					return;

				}

				if (modifyPassword.getText().toString().equals("")) {

					Toast.makeText(getApplicationContext(), "请输入用户密码!",
							Toast.LENGTH_SHORT).show();

					return;

				}

				getSecurityCode();

				isClickSecurity = true;

				time.start();

			}
		});

		// 线程交互
		modifyHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case LOGINFAIL: {

					mProgressDialog.dismiss();

					Toast.makeText(getApplicationContext(), "信息修改失败!",
							Toast.LENGTH_SHORT).show();

					break;
				}
				case LOGINSUCCESS: {

					try {
						mProgressDialog.dismiss();

						Toast.makeText(getApplicationContext(), "用户名修改成功!",
								Toast.LENGTH_SHORT).show();

						Bitmap bitmap = CacheUtil.GetFromFile(
								preferences_user.getString("username", "用户名"),
								"Avatar");
						String username_old = preferences_user.getString(
								"username", "用户名");

						Editor editor = preferences_user.edit();
						editor.putString("username", modifyUserName.getText()
								.toString());
						editor.commit();

						CacheUtil.WriteToFile(
								preferences_user.getString("username", "用户名"),
								bitmap, "Avatar");

						CacheUtil.DeleteFile(username_old, "Avatar");

						finish();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;

				}
				case LOGININTER: {

					mProgressDialog.dismiss();

					Toast.makeText(getApplicationContext(), "后台服务器故障,请之后重试!",
							Toast.LENGTH_SHORT).show();
					break;
				}
				case MODIFYSUCCESS: {

					try {
						Toast.makeText(getApplicationContext(),
								"短信已经发到新绑定的手机，请注意查收!", Toast.LENGTH_SHORT)
								.show();

						JSONArray jsonArray = new JSONArray(msg.obj.toString());
						JSONObject jsonObject = (JSONObject) jsonArray.get(0);

						newPhone = jsonObject.optString("newPhone");
						newCode = jsonObject.optString("securityCode");

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				}
				case MODIFYUSEREXIST: {

					Toast.makeText(getApplicationContext(),
							"新绑定手机号已经有用户注册，请重新更换绑定手机!", Toast.LENGTH_SHORT)
							.show();

					break;
				}
				case MODIFYPASSWORD: {

					Toast.makeText(getApplicationContext(),
							"输入密码错误，无法更换绑定手机号!", Toast.LENGTH_SHORT).show();

					break;

				}
				default:
					super.handleMessage(msg);
				}
			}

		};

	}

	/**
	 * 初始化控件
	 */
	private void initView() {

		IPadress = new IPAdress();
		URL = "http://" + IPadress.getIP() + ":" + IPadress.getPORT() + URL;
		URLPHONE = "http://" + IPadress.getIP() + ":" + IPadress.getPORT()
				+ URLPHONE;

		URLPMODIFYHONE = "http://" + IPadress.getIP() + ":"
				+ IPadress.getPORT() + URLPMODIFYHONE;

		preferences_user = getSharedPreferences("USERMESSAGE",
				Context.MODE_PRIVATE);

		modifyOther = (TextView) findViewById(R.id.modifyOtherTitle_edittext);
		okButton = (Button) findViewById(R.id.modifyOther_button);
		backButton = (ImageButton) findViewById(R.id.modifyOther_btn_back);
		modify_userName = (LinearLayout) findViewById(R.id.modify_userName);
		modify_userPhone = (LinearLayout) findViewById(R.id.modify_userPhone);
		modifyUserName = (EditText) findViewById(R.id.modifyUserName_textView);
		modifyUserPhone = (EditText) findViewById(R.id.modifyUserPhone_textView);
		modifyPassword = (EditText) findViewById(R.id.UserPasswordModify_textView);
		securityCode = (Button) findViewById(R.id.securityCode_button);
		inputsecurityCode = (EditText) findViewById(R.id.inputsecurityCode_textView);

	}

	/**
	 * 当用户点击确定按钮时，修改用户名内容是否合法
	 * 
	 * @return
	 */
	private boolean isSuitablyUserName() {

		String userName = modifyUserName.getText().toString();

		if (userName.equals("")) {

			Toast.makeText(getApplicationContext(), "用户名不能为空!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		return true;

	}

	/**
	 * 当用户点击确定按钮时，验证绑定手机号内容是否合法
	 * 
	 * @return
	 */
	private boolean isSuitablyUserPhone() {

		String userPhoneStr = modifyUserPhone.getText().toString();
		String securityCodeStr = inputsecurityCode.getText().toString();

		if (userPhoneStr.equals("")) {

			Toast.makeText(getApplicationContext(), "绑定新手机号不能为空!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (securityCodeStr.equals("")) {

			Toast.makeText(getApplicationContext(), "请输入验证码!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		return true;

	}

	/**
	 * 修改用户名
	 */
	private void modifyUserName() {

		new Thread() {

			public void run() {

				try {
					String phoneStr = modifyUserName.getText().toString();

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("username", phoneStr);
					jsonObject.put("phone",
							preferences_user.getString("phone", "18392120258"));
					jsonObject.put("sex", preferences_user.getInt("sex", 0));

					Bitmap photo = CacheUtil.GetFromFile(
							preferences_user.getString("username", "用户名"),
							"Avatar");
					jsonObject.put("avatar",
							BitmapAndString.bitmapToString(photo));

					jsonArray.put(jsonObject);

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URL);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair("modify",
							jsonArray + "");
					paramList.add(param);

					httpPost.setEntity(new UrlEncodedFormEntity(paramList,
							"UTF_8"));

					HttpResponse httpResponse = client.execute(httpPost);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						String result = EntityUtils.toString(
								httpResponse.getEntity(), HTTP.UTF_8);

						JSONArray jsonArray2 = new JSONArray(result);
						JSONObject jsonObject2 = (JSONObject) jsonArray2.get(0);

						if (jsonObject2.optString("status").equals("success")) {

							Message msg = new Message();
							msg.what = LOGINSUCCESS;
							modifyHandler.sendMessage(msg);

						} else {

							Message msg = new Message();
							msg.what = LOGINFAIL;
							modifyHandler.sendMessage(msg);

						}

					} else {

						Message msg = new Message();
						msg.what = LOGININTER;
						modifyHandler.sendMessage(msg);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {

					Message msg = new Message();
					msg.what = LOGININTER;
					modifyHandler.sendMessage(msg);

					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}.start();

	}

	/**
	 * 修改用户绑定手机号
	 */
	private void getSecurityCode() {

		new Thread() {

			public void run() {

				try {
					String userNewPhoneStr = modifyUserPhone.getText()
							.toString();
					String usermodifyPassword = modifyPassword.getText()
							.toString();

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("newPhone", userNewPhoneStr);
					jsonObject.put("password", MD5.getMD5(usermodifyPassword));
					jsonObject.put("phone",
							preferences_user.getString("phone", "18392120258"));

					jsonArray.put(jsonObject);

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URLPHONE);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair(
							"setPhone", jsonArray + "");
					paramList.add(param);

					httpPost.setEntity(new UrlEncodedFormEntity(paramList,
							"UTF_8"));

					HttpResponse httpResponse = client.execute(httpPost);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						String result = EntityUtils.toString(
								httpResponse.getEntity(), HTTP.UTF_8);

						JSONArray jsonArray2 = new JSONArray(result);
						JSONObject jsonObject2 = (JSONObject) jsonArray2.get(0);

						if (jsonObject2.optString("status").equals("success")) {

							Message msg = new Message();
							msg.what = MODIFYSUCCESS;
							msg.obj = result;
							modifyHandler.sendMessage(msg);

						} else if (jsonObject2.optString("status").equals(
								"userExist")) {

							Message msg = new Message();
							msg.what = MODIFYUSEREXIST;
							modifyHandler.sendMessage(msg);

						} else if (jsonObject2.optString("status").equals(
								"wrongPassword")) {

							Message msg = new Message();
							msg.what = MODIFYPASSWORD;
							modifyHandler.sendMessage(msg);

						}

					} else {

						Message msg = new Message();
						msg.what = LOGININTER;
						modifyHandler.sendMessage(msg);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {

					Message msg = new Message();
					msg.what = LOGININTER;
					modifyHandler.sendMessage(msg);

					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}.start();

	}

	private void modifyPhone() {

		new Thread() {

			public void run() {

				try {
					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("newPhone", newPhone);
					jsonObject.put("phone",
							preferences_user.getString("phone", "18392120258"));

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URLPMODIFYHONE);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair(
							"setPhone", jsonArray + "");
					paramList.add(param);

					httpPost.setEntity(new UrlEncodedFormEntity(paramList,
							"UTF_8"));

					HttpResponse httpResponse = client.execute(httpPost);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						String result = EntityUtils.toString(
								httpResponse.getEntity(), HTTP.UTF_8);
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {

					Message msg = new Message();
					msg.what = LOGININTER;
					modifyHandler.sendMessage(msg);

					e.printStackTrace();
				} catch (ParseException e) {

					Message msg = new Message();
					msg.what = LOGININTER;
					modifyHandler.sendMessage(msg);

					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}.start();

	}

	class TimeCount extends CountDownTimer {

		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			securityCode.setBackgroundColor(Color.parseColor("#A2B5CD"));
			securityCode.setClickable(false);
			securityCode.setText(millisUntilFinished / 1000 + "秒后可重新获取");
		}

		@Override
		public void onFinish() {
			securityCode.setText("获取验证码");
			securityCode.setClickable(true);
			securityCode.setBackgroundResource(R.drawable.shape_modify_btn);
		}
	}
}
