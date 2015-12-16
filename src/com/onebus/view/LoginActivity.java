package com.onebus.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.onebus.IPAdress;
import com.onebus.R;
import com.onebus.util.BitmapAndString;
import com.onebus.util.CacheUtil;
import com.onebus.util.MD5;
import com.onebus.util.NetWorkUtil;

public class LoginActivity extends Activity {

	private String URL = "/onebus/android/userLogin";
	private IPAdress IPadress;

	private final static int LOGINNOUSER = 0x114;
	private final static int LOGINWRONG = 0x115;
	private final static int LOGINSUCCESS = 0x116;
	private final static int LOGININTER = 0x117;
	private Handler loginHandler;

	private ImageButton backButton;
	private Button registerButton;

	private EditText userPhone;
	private EditText userPassword;
	private TextView forgetPassword;
	private Button login;

	private SharedPreferences preferences_user;
	private SharedPreferences preferences_login;
	private ProgressDialog mProgressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		try {
			initView();

			forgetPassword.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); // 下划线
			forgetPassword.getPaint().setAntiAlias(true);// 抗锯齿

			// 点击登录按钮
			login.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						if (isSuitablyLoginMessage()) {

							if (!NetWorkUtil
									.isNetWorkConnected(getApplicationContext())) {

								Toast.makeText(getApplicationContext(),
										"网络连接失败，请检查网络是否可用!", Toast.LENGTH_SHORT)
										.show();

								return;

							}

							loginThread();

							mProgressDialog = new ProgressDialog(
									LoginActivity.this);
							mProgressDialog
									.setIcon(R.drawable.common_topbar_route_foot_pressed);
							mProgressDialog.setMessage("请稍等....");
							mProgressDialog.show();

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

		forgetPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(),
						ForgetPasswordActivity.class);
				startActivity(intent);

			}
		});

		// 线程交互
		loginHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case LOGINNOUSER: {

					mProgressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "用户不存在,请先进行注册!",
							Toast.LENGTH_SHORT).show();

					break;
				}
				case LOGINWRONG: {

					mProgressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "密码错误,请重新输入!",
							Toast.LENGTH_SHORT).show();

					break;

				}
				case LOGINSUCCESS: {

					try {
						mProgressDialog.dismiss();

						Editor editor_login = preferences_login.edit();
						editor_login.putBoolean("isLogin", true);
						editor_login.commit();

						JSONArray jsonArray = new JSONArray(msg.obj.toString());
						JSONObject jsonObject = (JSONObject) jsonArray.get(0);

						Editor editor_user = preferences_user.edit();
						editor_user.putString("username",
								jsonObject.optString("username"));
						editor_user.putInt("sex", jsonObject.optInt("sex"));
						editor_user.putString("phone",
								jsonObject.optString("phone"));
						editor_user.commit();

						Bitmap bitmap = BitmapAndString
								.stringtoBitmap(jsonObject.optString("avatar"));
						CacheUtil.WriteToFile(jsonObject.optString("username"),
								bitmap, "Avatar");

						Toast.makeText(getApplicationContext(), "登录成功!",
								Toast.LENGTH_SHORT).show();

						finish();
					} catch (Exception e) {

						e.printStackTrace();
					}

					break;

				}
				case LOGININTER: {

					Toast.makeText(getApplicationContext(), "后台服务器故障,请之后重试!",
							Toast.LENGTH_SHORT).show();
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
		preferences_login = getSharedPreferences("Login", MODE_PRIVATE);

		backButton = (ImageButton) findViewById(R.id.login_btn_back);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		registerButton = (Button) findViewById(R.id.login_register_button);
		registerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				try {
					Intent registIntent = new Intent(LoginActivity.this,
							RegisterActivity.class);
					startActivity(registIntent);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		userPhone = (EditText) findViewById(R.id.login_userPhone);
		userPassword = (EditText) findViewById(R.id.login_userPassword);
		forgetPassword = (TextView) findViewById(R.id.forgetPassword);
		login = (Button) findViewById(R.id.login_button);

	}

	/**
	 * 验证点击登录按钮前用户信息是否正确
	 * 
	 * @return
	 */
	private boolean isSuitablyLoginMessage() {

		String userPhoneStr = userPhone.getText().toString();
		String userPasswordStr = userPassword.getText().toString();

		if (userPhoneStr.equals("") || userPasswordStr.equals("")) {

			Toast.makeText(getApplicationContext(), "请填写登录信息!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (!isMobileNO(userPhoneStr)) {

			Toast.makeText(getApplicationContext(), "手机号格式错误!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (userPasswordStr.length() < 6 || userPasswordStr.length() > 16) {

			Toast.makeText(getApplicationContext(), "密码格式错误!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		return true;

	}

	/**
	 * 验证手机号码是否合法
	 * 
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNO(String mobiles) {
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	private void loginThread() {

		new Thread() {

			@Override
			public void run() {

				try {
					String userPhoneStr = userPhone.getText().toString();
					String userPasswordStr = MD5.getMD5(userPassword.getText()
							.toString());

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("userphone", userPhoneStr);
					jsonObject.put("userpassword", userPasswordStr);

					jsonArray.put(jsonObject);

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URL);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair("login",
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

						if (jsonObject2.getString("status").equals("noUser")) {

							Message msg = new Message();
							msg.what = LOGINNOUSER;
							msg.obj = result;
							loginHandler.sendMessage(msg);

						} else if (jsonObject2.getString("status").equals(
								"wrongPassword")) {

							Message msg = new Message();
							msg.what = LOGINWRONG;
							msg.obj = result;
							loginHandler.sendMessage(msg);

						} else {

							Log.e("message", result);
							Message msg = new Message();
							msg.what = LOGINSUCCESS;
							msg.obj = result;
							loginHandler.sendMessage(msg);
						}

					} else {

						Message msg = new Message();
						msg.what = LOGININTER;
						loginHandler.sendMessage(msg);

					}

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {

					Message msg = new Message();
					msg.what = LOGININTER;
					loginHandler.sendMessage(msg);

					e.printStackTrace();
				} catch (ParseException e) {

					Message msg = new Message();
					msg.what = LOGININTER;
					loginHandler.sendMessage(msg);
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
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

}
