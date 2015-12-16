package com.onebus.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.onebus.IPAdress;
import com.onebus.R;
import com.onebus.util.CacheUtil;
import com.onebus.util.MD5;

public class ModifyPasswordActivity extends Activity {

	private String URL = "/onebus/android/setUserPassword";
	private IPAdress IPadress;

	private SharedPreferences preferences_user;

	private final static int LOGINFAIL = 0x115;
	private final static int LOGINSUCCESS = 0x116;
	private final static int LOGININTER = 0x117;

	private Button okButton;

	private ImageButton backButton;

	private EditText oldPassword;
	private EditText newPassword;
	private EditText newPasswordConfirm;

	private ProgressDialog mProgressDialog;
	private Handler modifyHandler;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modifypassword);

		initView();

		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isSuitablyMessage()) {

					ModifyPassword();

					mProgressDialog = new ProgressDialog(
							ModifyPasswordActivity.this);
					mProgressDialog
							.setIcon(R.drawable.common_topbar_route_foot_pressed);
					mProgressDialog.setMessage("请稍等....");
					mProgressDialog.show();

				}

			}
		});

		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();

			}
		});

		// 线程交互
		modifyHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case LOGINFAIL: {

					mProgressDialog.dismiss();

					Toast.makeText(getApplicationContext(), "密码修改失败!",
							Toast.LENGTH_SHORT).show();

					break;
				}
				case LOGINSUCCESS: {

					mProgressDialog.dismiss();

					Toast.makeText(getApplicationContext(), "密码修改成功!",
							Toast.LENGTH_SHORT).show();

					finish();

					break;

				}
				case LOGININTER: {

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
		URL = "http://" + IPadress.getIP() + ":" + IPadress.getPORT() + URL;

		preferences_user = getSharedPreferences("USERMESSAGE",
				Context.MODE_PRIVATE);

		okButton = (Button) findViewById(R.id.modifyPassword_button);
		backButton = (ImageButton) findViewById(R.id.modifyPassword_btn_back);
		oldPassword = (EditText) findViewById(R.id.modifyPassword1_textView);
		newPassword = (EditText) findViewById(R.id.modifyPassword2_textView);
		newPasswordConfirm = (EditText) findViewById(R.id.modifyPassword3_textView);

	}

	private boolean isSuitablyMessage() {

		String oldPasswordStr = oldPassword.getText().toString();
		String newPasswordStr = newPassword.getText().toString();
		String newPasswordStr2 = newPasswordConfirm.getText().toString();

		if (oldPasswordStr.equals("") || newPasswordStr.equals("")
				|| newPasswordStr2.equals("")) {

			Toast.makeText(getApplicationContext(), "请填写完整信息!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (oldPasswordStr.length() < 6 || newPasswordStr.length() < 6
				|| newPasswordStr2.length() < 6) {

			Toast.makeText(getApplicationContext(), "密码长度过短!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (oldPasswordStr.length() > 16 || newPasswordStr.length() > 16
				|| newPasswordStr2.length() > 16) {

			Toast.makeText(getApplicationContext(), "密码长度过长!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		if (!newPasswordStr.equals(newPasswordStr2)) {

			Toast.makeText(getApplicationContext(), "两次输入的密码不一致!",
					Toast.LENGTH_SHORT).show();

			return false;

		}

		return true;

	}

	private void ModifyPassword() {

		new Thread() {

			public void run() {

				try {
					String oldPasswordStr = MD5.getMD5(oldPassword.getText()
							.toString());
					String newPasswordStr = MD5.getMD5(newPassword.getText()
							.toString());

					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("phone",
							preferences_user.getString("phone", "18392120258"));
					jsonObject.put("password", oldPasswordStr);
					jsonObject.put("newPassword", newPasswordStr);

					jsonArray.put(jsonObject);

					HttpClient client = new DefaultHttpClient();

					HttpPost httpPost = new HttpPost(URL);

					// 设置参数，仿html表单提交
					List<NameValuePair> paramList = new ArrayList<NameValuePair>();
					BasicNameValuePair param = new BasicNameValuePair(
							"setUserPassword", jsonArray + "");
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

				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
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
