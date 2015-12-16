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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.onebus.IPAdress;
import com.onebus.OneBusApplication;
import com.onebus.R;
import com.onebus.util.MD5;
import com.onebus.util.NetWorkUtil;
import com.onebus.view.RegisterActivity.TimeCount;

public class ForgetPasswordActivity extends Activity {

	private String URLGETCODE = "/onebus/android/registerGetCode";
	private String URLREGISTER = "/onebus/android/initializePassword";
	private IPAdress IPadress;

	private final static int SENDSUCCESS = 0x114;
	private final static int FAIL = 0x115;
	private final static int FORGETSUCCESS = 0x116;
	private final static int FORGETFAIL = 0x117;
	private final static int FORGETINTER = 0x118;

	private Handler forgetHandler;

	private EditText forgetPhone;
	private EditText forgetCode;
	private Button getCode;
	private Button submit;

	private String securityCode;
	private String securityPhone;

	private TimeCount time;
	private ProgressDialog mProgressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgetpassword);

		time = new TimeCount(30000, 1000);

		initView();

		submit.setClickable(false);

		getCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isSuitablyGetCodeMessage()) {

					if (!NetWorkUtil
							.isNetWorkConnected(getApplicationContext())) {

						Toast.makeText(getApplicationContext(),
								"网络连接失败，请检查网络是否可用!", Toast.LENGTH_SHORT).show();

						return;

					}

					securityPhone = forgetPhone.getText().toString();

					getCodeFromServer();

					time.start();
					submit.setClickable(true);

				}

			}
		});

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isSuitablyRegisterMessage()) {

					if (!NetWorkUtil
							.isNetWorkConnected(getApplicationContext())) {

						Toast.makeText(getApplicationContext(),
								"网络连接失败，请检查网络是否可用!", Toast.LENGTH_SHORT).show();

						return;

					}

					if (forgetCode.getText().toString().equals(securityCode)) {

						if (forgetCode.getText().toString()
								.equals(securityCode)
								&& forgetPhone.getText().toString()
										.equals(securityPhone)) {

							RegisterUser();

							mProgressDialog = new ProgressDialog(
									ForgetPasswordActivity.this);
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

		forgetHandler = new Handler() {

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
				case FAIL: {

					// mProgressDialog.dismiss();

					Toast.makeText(getApplicationContext(), "该手机号用户不存在，请重新发送!",
							Toast.LENGTH_SHORT).show();

					break;

				}
				case FORGETSUCCESS: {

					mProgressDialog.dismiss();

					Toast.makeText(getApplicationContext(), "密码初始化成功，请进行登录!",
							Toast.LENGTH_SHORT).show();

					finish();

					break;

				}
				case FORGETFAIL: {

					mProgressDialog.dismiss();

					Toast.makeText(getApplicationContext(), "找回密码，请稍后重试!",
							Toast.LENGTH_SHORT).show();

					break;

				}
				case FORGETINTER: {

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

		forgetPhone = (EditText) findViewById(R.id.forgetpassword_userName);
		forgetCode = (EditText) findViewById(R.id.forgetpassword_yanzheng);
		getCode = (Button) findViewById(R.id.getCode_forgetpassword);
		submit = (Button) findViewById(R.id.forgetpassword_button);

	}

	/**
	 * 验证点击获取验证码按钮前信息是否完整
	 * 
	 * @return
	 */
	private boolean isSuitablyGetCodeMessage() {

		if (forgetPhone.getText().toString().equals(null)) {

			Toast.makeText(getApplicationContext(), "请填写手机号",
					Toast.LENGTH_SHORT).show();
			return false;

		}

		if (!isMobileNO(forgetPhone.getText().toString())) {

			Toast.makeText(getApplicationContext(), "手机号格式错误!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		return true;

	}

	/**
	 * 验证点击注册按钮前信息是否完整
	 * 
	 * @return
	 */
	private boolean isSuitablyRegisterMessage() {

		if (forgetPhone.getText().toString().equals("")) {

			Toast.makeText(getApplicationContext(), "请填写手机号!",
					Toast.LENGTH_SHORT).show();
			return false;

		}

		if (forgetCode.getText().toString().equals("")) {

			Toast.makeText(getApplicationContext(), "请输入验证码!",
					Toast.LENGTH_SHORT).show();
			return false;

		}

		if (!isMobileNO(forgetPhone.getText().toString())) {

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

	/**
	 * 向服务器请求验证码
	 */
	private void getCodeFromServer() {

		new Thread() {

			public void run() {

				try {
					String phoneStr = forgetPhone.getText().toString();

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("phone", phoneStr);
					jsonObject.put("type", 3);

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
							forgetHandler.sendMessage(msg);

						} else {

							Message msg = new Message();
							msg.what = FAIL;
							forgetHandler.sendMessage(msg);

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
	 * 向服务器发送找回密码请求
	 */
	private void RegisterUser() {

		new Thread() {

			public void run() {

				try {

					String userPhoneStr = forgetPhone.getText().toString();

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("password", MD5.getMD5("123456"));
					jsonObject.put("phone", userPhoneStr);

					jsonArray.put(jsonObject);

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URLREGISTER);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair(
							"initializePassword", jsonArray + "");
					paramList.add(param);

					httpPost.setEntity(new UrlEncodedFormEntity(paramList,
							"UTF_8"));

					HttpResponse httpResponse = client.execute(httpPost);

					if (httpResponse.getStatusLine().getStatusCode() == 200) {

						String result = EntityUtils.toString(
								httpResponse.getEntity(), HTTP.UTF_8);

						JSONArray jsonArray2 = new JSONArray(result);
						JSONObject jsonObject2 = (JSONObject) jsonArray2.get(0);

						if (jsonObject2.getString("status").equals("success")) {

							Message msg = new Message();
							msg.what = FORGETSUCCESS;
							forgetHandler.sendMessage(msg);

						} else if (jsonObject2.getString("status").equals(
								"wrongPassword")) {

							Message msg = new Message();
							msg.what = FORGETFAIL;
							forgetHandler.sendMessage(msg);

						}

					} else {

						Message msg = new Message();
						msg.what = FORGETINTER;
						forgetHandler.sendMessage(msg);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {

					Message msg = new Message();
					msg.what = FORGETINTER;
					forgetHandler.sendMessage(msg);

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

}
