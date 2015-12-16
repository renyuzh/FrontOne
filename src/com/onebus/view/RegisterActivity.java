package com.onebus.view;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
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
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.onebus.IPAdress;
import com.onebus.OneBusApplication;
import com.onebus.R;
import com.onebus.util.MD5;
import com.onebus.util.NetWorkUtil;

public class RegisterActivity extends Activity {

	private String URLGETCODE = "/onebus/android/registerGetCode";
	private String URLREGISTER = "/onebus/android/register";
	private IPAdress IPadress;

	private final static int SENDSUCCESS = 0x114;
	private final static int USEREXIST = 0x115;
	private final static int REGISTERSUCCESS = 0x116;
	private final static int REGISTERFAIL = 0x117;
	private final static int REGISTERINTER = 0x118;

	private Handler registerHandler;

	private ImageButton backButton;

	private EditText userName;
	private EditText passWord;
	private EditText passWord2;
	private EditText userPhone;

	private Button getCode;
	private EditText inputCode;

	private Button register;
	private TextView userDeal;

	private TimeCount time;

	private String securityCode;
	private String securityPhone;
	private ProgressDialog mProgressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		time = new TimeCount(30000, 1000);

		initView();

		// 点击回退按钮
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		// 点击获取验证码按钮
		getCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				try {
					if (isSuitablyGetCodeMessage()) {

						if (!NetWorkUtil
								.isNetWorkConnected(getApplicationContext())) {

							Toast.makeText(getApplicationContext(),
									"网络连接失败，请检查网络是否可用!", Toast.LENGTH_SHORT)
									.show();

							return;

						}

						securityPhone = userPhone.getText().toString();

						getCodeFromServer();

						time.start();

					}
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		});

		register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isSuitablyRegisterMessage()) {

					if (!NetWorkUtil
							.isNetWorkConnected(getApplicationContext())) {

						Toast.makeText(getApplicationContext(),
								"网络连接失败，请检查网络是否可用!", Toast.LENGTH_SHORT).show();

						return;

					}

					if (inputCode.getText().toString().equals(securityCode)) {

						if (inputCode.getText().toString().equals(securityCode)
								&& userPhone.getText().toString()
										.equals(securityPhone)) {

							RegisterUser();

							mProgressDialog = new ProgressDialog(
									RegisterActivity.this);
							mProgressDialog
									.setIcon(R.drawable.common_topbar_route_foot_pressed);
							mProgressDialog.setMessage("请稍等....");
							mProgressDialog.show();

						} else {

							Toast.makeText(getApplicationContext(),
									"手机号与验证码不匹配!", Toast.LENGTH_SHORT).show();

						}
					} else {

						Toast.makeText(getApplicationContext(), "验证码错误!",
								Toast.LENGTH_SHORT).show();

					}

				}

			}
		});

		userDeal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Toast.makeText(getApplicationContext(), "用户服务协议!",
						Toast.LENGTH_SHORT).show();

			}
		});

		registerHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SENDSUCCESS: {

					// mProgressDialog.dismiss();

					try {
						JSONArray jsonArray = new JSONArray(msg.obj.toString());
						JSONObject jsonObject = (JSONObject) jsonArray.get(0);

						securityCode = jsonObject.optString("securityCode");

						Toast.makeText(getApplicationContext(),
								"验证码已经发送，请注意查收!", Toast.LENGTH_SHORT).show();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				}
				case USEREXIST: {

					// mProgressDialog.dismiss();

					Toast.makeText(getApplicationContext(), "该手机号已经注册，可直接登录!",
							Toast.LENGTH_SHORT).show();

					break;

				}
				case REGISTERSUCCESS: {

					mProgressDialog.dismiss();

					Toast.makeText(getApplicationContext(), "用户注册成功，请进行登录!",
							Toast.LENGTH_SHORT).show();

					finish();

					break;

				}
				case REGISTERFAIL: {

					mProgressDialog.dismiss();

					Toast.makeText(getApplicationContext(), "用户注册失败，请稍后重试!",
							Toast.LENGTH_SHORT).show();

					break;

				}
				case REGISTERINTER: {

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

	}

	private void initView() {

		IPadress = new IPAdress();
		URLGETCODE = "http://" + IPadress.getIP() + ":" + IPadress.getPORT()
				+ URLGETCODE;

		URLREGISTER = "http://" + IPadress.getIP() + ":" + IPadress.getPORT()
				+ URLREGISTER;

		backButton = (ImageButton) findViewById(R.id.register_btn_back);
		userName = (EditText) findViewById(R.id.register_userName);
		passWord = (EditText) findViewById(R.id.register_userPassword1);
		passWord2 = (EditText) findViewById(R.id.register_userPassword2);
		userPhone = (EditText) findViewById(R.id.register_userPhone);
		getCode = (Button) findViewById(R.id.getCode_register);
		inputCode = (EditText) findViewById(R.id.register_yanzheng);
		register = (Button) findViewById(R.id.register_button);
		userDeal = (TextView) findViewById(R.id.registerTips);

	}

	/**
	 * 验证点击注册按钮前信息是否完整
	 * 
	 * @return
	 */
	private boolean isSuitablyRegisterMessage() {

		String userNameStr = userName.getText().toString();
		String passWordStr = passWord.getText().toString();
		String passWord2Str = passWord2.getText().toString();
		String userPhoneStr = userPhone.getText().toString();
		String userCode = inputCode.getText().toString();

		if (userNameStr.equals("") || passWordStr.equals("")
				|| passWord2Str.equals("") || userPhoneStr.equals("")) {

			Toast.makeText(getApplicationContext(), "请填写完整信息!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (passWordStr.length() < 6 || passWordStr.length() > 16) {

			Toast.makeText(getApplicationContext(), "密码格式错误!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (!passWordStr.equals(passWord2Str)) {

			Toast.makeText(getApplicationContext(), "两次密码输入不一致!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (!isMobileNO(userPhoneStr)) {

			Toast.makeText(getApplicationContext(), "手机号格式错误!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (userCode.equals("")) {

			Toast.makeText(getApplicationContext(), "请输入验证码!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		return true;

	}

	/**
	 * 验证点击获取验证码按钮前信息是否完整
	 * 
	 * @return
	 */
	private boolean isSuitablyGetCodeMessage() {

		String userNameStr = userName.getText().toString();
		String passWordStr = passWord.getText().toString();
		String passWord2Str = passWord2.getText().toString();
		String userPhoneStr = userPhone.getText().toString();

		if (userNameStr.equals("") || passWordStr.equals("")
				|| passWord2Str.equals("") || userPhoneStr.equals("")) {

			Toast.makeText(getApplicationContext(), "请填写完整信息!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (passWordStr.length() < 6 || passWordStr.length() > 16) {

			Toast.makeText(getApplicationContext(), "密码格式错误!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (!passWordStr.equals(passWord2Str)) {

			Toast.makeText(getApplicationContext(), "两次密码输入不一致!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (!isMobileNO(userPhoneStr)) {

			Toast.makeText(getApplicationContext(), "手机号格式错误!",
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

	class TimeCount extends CountDownTimer {

		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			getCode.setBackgroundColor(Color.parseColor("#A2B5CD"));
			getCode.setClickable(false);
			getCode.setText(millisUntilFinished / 1000 + "秒后可重新获取");
		}

		@Override
		public void onFinish() {
			getCode.setText("获取验证码");
			getCode.setClickable(true);
			getCode.setBackgroundResource(R.drawable.shape_modify_btn);
		}
	}

	/**
	 * 向服务器请求验证码
	 */
	private void getCodeFromServer() {

		new Thread() {

			public void run() {

				try {
					String phoneStr = userPhone.getText().toString();

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("phone", phoneStr);
					jsonObject.put("type", 1);

					jsonArray.put(jsonObject);

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URLGETCODE);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair(
							"registerGetCode", jsonArray + "");
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
							msg.what = SENDSUCCESS;
							msg.obj = result;
							registerHandler.sendMessage(msg);

						} else {

							Message msg = new Message();
							msg.what = USEREXIST;
							registerHandler.sendMessage(msg);

						}
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
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

	/**
	 * 向服务器发送注册请求
	 */
	private void RegisterUser() {

		new Thread() {

			public void run() {

				try {
					String userNameStr = userName.getText().toString();
					String passWordStr = passWord.getText().toString();
					String userPhoneStr = userPhone.getText().toString();

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("username", userNameStr);
					jsonObject.put("password", MD5.getMD5(passWordStr));
					jsonObject.put("phone", userPhoneStr);
					jsonObject.put("city", OneBusApplication.CURRENT_CITY);

					jsonArray.put(jsonObject);

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URLREGISTER);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair(
							"register", jsonArray + "");
					paramList.add(param);

					httpPost.setEntity(new UrlEncodedFormEntity(paramList,
							"UTF_8"));

					HttpResponse httpResponse = client.execute(httpPost);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						String result = EntityUtils.toString(
								httpResponse.getEntity(), HTTP.UTF_8);

						JSONArray jsonArray2 = new JSONArray(result);
						JSONObject jsonObject2 = (JSONObject) jsonArray2.get(0);

						if (jsonObject2.getString("status").equals(
								"registerSuccess")) {

							Message msg = new Message();
							msg.what = REGISTERSUCCESS;
							registerHandler.sendMessage(msg);

						} else if (jsonObject2.getString("status").equals(
								"wrongPassword")) {

							Message msg = new Message();
							msg.what = REGISTERFAIL;
							registerHandler.sendMessage(msg);

						}

					} else {

						Message msg = new Message();
						msg.what = REGISTERINTER;
						registerHandler.sendMessage(msg);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {

					Message msg = new Message();
					msg.what = REGISTERINTER;
					registerHandler.sendMessage(msg);

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
