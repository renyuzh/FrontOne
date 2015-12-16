package com.onebus.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onebus.IPAdress;
import com.onebus.R;
import com.onebus.util.BitmapAndString;
import com.onebus.util.CacheUtil;

public class UserInformationActivity extends Activity {

	private String URL = "/onebus/android/modifyUser";
	private IPAdress IPadress;

	private final static int LOGINFAIL = 0x115;
	private final static int LOGINSUCCESS = 0x116;
	private final static int LOGININTER = 0x117;

	private SharedPreferences preferences;
	private SharedPreferences preferences_user;

	private ImageButton backButton;

	private RelativeLayout re_avatar;
	private RelativeLayout re_name;
	private RelativeLayout re_fxid;
	private RelativeLayout re_sex;
	private RelativeLayout re_region;

	private Bitmap photo;
	private ImageView iv_avatar;
	private TextView tv_name;
	private TextView tv_sex;
	private TextView tv_fxid;

	private Button logout_button;

	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESIZE_REQUEST_CODE = 2;

	private static final String IMAGE_FILE_NAME = "header.jpg";

	private int sex_int = 0;

	private Handler modifyHandler;
	private ProgressDialog mProgressDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myinfo);

		initView();
		initMessage();

		// 点击后退按钮
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();

			}
		});

		// 点击头像按钮
		re_avatar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onImageChoiceDialog();

			}
		});

		// 点击用户名按钮
		re_name.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(),
						ModifyOtherActivity.class);
				intent.putExtra("modifyType", 1);
				startActivity(intent);

			}
		});

		// 点击用户手机号按钮
		re_fxid.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(),
						ModifyOtherActivity.class);
				intent.putExtra("modifyType", 2);
				startActivity(intent);

			}
		});

		// 点击用户性别按钮
		re_sex.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				sexDialog();

			}
		});

		// 点击用户修改密码按钮
		re_region.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				passwordDialog();

			}
		});

		// 点击退出登录按钮
		logout_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Editor editor = preferences.edit();
				editor.putBoolean("isLogin", false);
				editor.commit();

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), UserActivity.class);
				startActivity(intent);
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

					Toast.makeText(getApplicationContext(), "信息修改失败!",
							Toast.LENGTH_SHORT).show();

					break;
				}
				case LOGINSUCCESS: {

					mProgressDialog.dismiss();

					if (msg.obj.toString().equals("2")) {

						if (sex_int == 1) {

							tv_sex.setText("男");

						} else if (sex_int == 2) {

							tv_sex.setText("女");

						} else {

							tv_sex.setText("未知");

						}

						Toast.makeText(getApplicationContext(), "性别修改成功!",
								Toast.LENGTH_SHORT).show();

						Editor editor = preferences_user.edit();
						editor.putInt("sex", sex_int);
						editor.commit();

					} else {

						try {
							Drawable drawable = new BitmapDrawable(photo);
							iv_avatar.setImageDrawable(drawable);

							Toast.makeText(getApplicationContext(), "头像修改成功!",
									Toast.LENGTH_SHORT).show();

							CacheUtil.WriteToFile(preferences_user.getString(
									"username", "用户名"), photo, "Avatar");
						} catch (IOException e) {

							e.printStackTrace();
						}

					}

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

	/**
	 * 初始化控件
	 */
	private void initView() {

		IPadress = new IPAdress();
		URL = "http://" + IPadress.getIP() + ":" + IPadress.getPORT() + URL;

		preferences = getSharedPreferences("Login", MODE_PRIVATE);

		preferences_user = getSharedPreferences("USERMESSAGE",
				Context.MODE_PRIVATE);

		backButton = (ImageButton) findViewById(R.id.user_btn_back);
		iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_fxid = (TextView) findViewById(R.id.tv_fxid);
		re_avatar = (RelativeLayout) findViewById(R.id.re_avatar);
		re_name = (RelativeLayout) findViewById(R.id.re_name);
		re_fxid = (RelativeLayout) findViewById(R.id.re_fxid);
		re_sex = (RelativeLayout) findViewById(R.id.re_sex);
		re_region = (RelativeLayout) findViewById(R.id.re_region);
		logout_button = (Button) findViewById(R.id.logout_button);
		tv_sex = (TextView) findViewById(R.id.tv_sex);

	}

	private void initMessage() {

		Bitmap photo = CacheUtil.GetFromFile(
				preferences_user.getString("username", "用户名"), "Avatar");
		Drawable drawable = new BitmapDrawable(photo);

		iv_avatar.setImageDrawable(drawable);
		tv_name.setText(preferences_user.getString("username", "用户名"));
		tv_fxid.setText(preferences_user.getString("phone", "电话"));

		if (preferences_user.getInt("sex", 0) == 1) {

			tv_sex.setText("男");

		} else if (preferences_user.getInt("sex", 0) == 2) {

			tv_sex.setText("女");

		} else {

			tv_sex.setText("未知");

		}

	}

	/**
	 * 设置用户头像
	 */
	private void onImageChoiceDialog() {

		new AlertDialog.Builder(this)
				.setTitle("设置用户头像")
				.setItems(new String[] { "从相册中选择", "相机" },
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								switch (which) {
								case 0:

									Intent galleryIntent = new Intent(
											Intent.ACTION_GET_CONTENT);
									galleryIntent
											.addCategory(Intent.CATEGORY_OPENABLE);
									galleryIntent.setType("image/*");
									startActivityForResult(galleryIntent,
											IMAGE_REQUEST_CODE);

									break;
								case 1:

									if (isSdcardExisting()) {
										Intent cameraIntent = new Intent(
												"android.media.action.IMAGE_CAPTURE");
										cameraIntent.putExtra(
												MediaStore.EXTRA_OUTPUT,
												getImageUri());
										cameraIntent.putExtra(
												MediaStore.EXTRA_VIDEO_QUALITY,
												0);
										startActivityForResult(cameraIntent,
												CAMERA_REQUEST_CODE);
									} else {
										Toast.makeText(getApplicationContext(),
												"Please insert the sd card!",
												Toast.LENGTH_LONG).show();
									}

									break;

								}

							}
						}

				).show();

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		} else {
			switch (requestCode) {
			case IMAGE_REQUEST_CODE:
				resizeImage(data.getData());
				break;
			case CAMERA_REQUEST_CODE:
				if (isSdcardExisting()) {
					resizeImage(getImageUri());
				} else {
					Toast.makeText(UserInformationActivity.this, "sd未找到!",
							Toast.LENGTH_LONG).show();
				}
				break;

			case RESIZE_REQUEST_CODE:
				if (data != null) {
					showResizeImage(data);
				}
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean isSdcardExisting() {
		final String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	private Uri getImageUri() {
		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				IMAGE_FILE_NAME));
	}

	public void resizeImage(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, RESIZE_REQUEST_CODE);
	}

	private void showResizeImage(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {

			// 设置用户头像
			photo = extras.getParcelable("data");

			modifyMessage(1, 3);

			mProgressDialog = new ProgressDialog(UserInformationActivity.this);
			mProgressDialog
					.setIcon(R.drawable.common_topbar_route_foot_pressed);
			mProgressDialog.setMessage("请稍等....");
			mProgressDialog.show();

		}
	}

	/**
	 * 重写onResume()方法
	 */
	protected void onResume() {

		initMessage();
		super.onResume();

	}

	/**
	 * 更改用户性别
	 */
	private void sexDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				UserInformationActivity.this);

		builder.setCancelable(false);

		final AlertDialog dialogBuilder = builder.show();
		dialogBuilder.setCanceledOnTouchOutside(false);

		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("性别修改");
		final String[] sex = { "男", "女", "未知" };

		int i = 0;
		final int sexInt[] = { 0 };

		String sex_str = tv_sex.getText().toString();
		if (sex_str.equals("未知")) {

			i = 2;
			sexInt[0] = 2;

		} else if (sex_str.equals("男")) {

			i = 0;
			sexInt[0] = 0;

		} else if (sex_str.equals("女")) {

			i = 1;
			sexInt[0] = 1;

		} else {

			i = 0;
			sexInt[0] = 0;

		}

		// 设置一个单项选择下拉框
		/**
		 * 第一个参数指定我们要显示的一组下拉单选框的数据集合 第二个参数代表索引，指定默认哪一个单选框被勾选上，1表示默认'女' 会被勾选上
		 * 第三个参数给每一个单选项绑定一个监听器
		 */
		builder.setSingleChoiceItems(sex, i,
				new DialogInterface.OnClickListener() {
					@SuppressWarnings("null")
					@Override
					public void onClick(DialogInterface dialog, int which) {

						sexInt[0] = which;

					}
				});
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (sexInt[0] == 0) {

					sex_int = 1;
					modifyMessage(2, 1);

				} else if (sexInt[0] == 1) {

					sex_int = 2;
					modifyMessage(2, 2);

				} else if (sexInt[0] == 2) {

					sex_int = 0;
					modifyMessage(2, 0);

				}

				dialogBuilder.dismiss();

				mProgressDialog = new ProgressDialog(
						UserInformationActivity.this);
				mProgressDialog
						.setIcon(R.drawable.common_topbar_route_foot_pressed);
				mProgressDialog.setMessage("请稍等....");
				mProgressDialog.show();

			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialogBuilder.dismiss();

			}
		});
		builder.show();

	}

	private void passwordDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				UserInformationActivity.this);

		builder.setCancelable(false);

		final AlertDialog dialogBuilder = builder.show();
		dialogBuilder.setCanceledOnTouchOutside(false);

		// 设置Title的图标
		builder.setIcon(R.drawable.ic_launcher);
		// 设置Title的内容
		builder.setTitle("提示");
		// 设置Content来显示一个信息
		builder.setMessage("确定要修改密码吗？");
		// 设置一个PositiveButton
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(),
						ModifyPasswordActivity.class);
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

	/**
	 * 修改用户头像和性别，与服务器交互
	 * 
	 * @param type
	 * @param value
	 */
	private void modifyMessage(final int type, final int value) {

		new Thread() {

			@Override
			public void run() {

				try {
					JSONArray jsonArray = new JSONArray();
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("phone",
							preferences_user.getString("phone", "18392120258"));
					jsonObject.put("username",
							preferences_user.getString("username", "用户名"));

					if (type == 1) {

						jsonObject
								.put("sex", preferences_user.getInt("sex", 0));
						jsonObject.put("avatar",
								BitmapAndString.bitmapToString(photo));

					} else {

						jsonObject.put("sex", value);
						Bitmap photo_old = CacheUtil.GetFromFile(
								preferences_user.getString("username", "用户名"),
								"Avatar");
						jsonObject.put("avatar",
								BitmapAndString.bitmapToString(photo_old));

					}

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
							msg.obj = type;
							modifyHandler.sendMessage(msg);

						} else {

							Message msg = new Message();
							msg.what = LOGINFAIL;
							msg.obj = type;
							modifyHandler.sendMessage(msg);

						}

					} else {

						Message msg = new Message();
						msg.what = LOGININTER;
						msg.obj = type;
						modifyHandler.sendMessage(msg);

					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
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
