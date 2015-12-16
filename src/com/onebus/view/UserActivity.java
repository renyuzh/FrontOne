package com.onebus.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onebus.R;
import com.onebus.util.CacheUtil;
import com.onebus.zxing.MipcaActivityCapture;

public class UserActivity extends Activity {

	private SharedPreferences preferences;
	private SharedPreferences preferences_user;
	private boolean isLogin;

	private ImageButton backButton;

	private LinearLayout userInformation;
	private LinearLayout userLogin;

	private Button loginButton;

	private ImageView userPhoto;
	private TextView userName;
	private TextView userPhone;

	private ImageButton shopping;
	private ImageButton busCard;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);

		preferences = getSharedPreferences("Login", MODE_PRIVATE);
		preferences_user = getSharedPreferences("USERMESSAGE",
				Context.MODE_PRIVATE);
		isLogin = preferences.getBoolean("isLogin", false);

		initView();
		initMessage();

		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent loginIntent = new Intent(UserActivity.this,
						LoginActivity.class);
				startActivity(loginIntent);
			}
		});

		userInformation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent();
				intent.setClass(getApplicationContext(),
						UserInformationActivity.class);
				startActivity(intent);

			}
		});

	}

	/**
	 * 初始化控件
	 */
	private void initView() {

		backButton = (ImageButton) findViewById(R.id.user_btn_back);
		userInformation = (LinearLayout) findViewById(R.id.userInformation_LinearLayout);
		userLogin = (LinearLayout) findViewById(R.id.userLogin_LinearLayout);
		loginButton = (Button) findViewById(R.id.login);
		userPhoto = (ImageView) findViewById(R.id.userPhoto);
		userPhone = (TextView) findViewById(R.id.userPhone);
		userName = (TextView) findViewById(R.id.userName);

		if (isLogin) {

			userInformation.setVisibility(View.VISIBLE);
			userLogin.setVisibility(View.GONE);

		} else {

			userInformation.setVisibility(View.GONE);
			userLogin.setVisibility(View.VISIBLE);

		}

	}

	/**
	 * 初始化界面信息
	 */
	private void initMessage() {

		if (isLogin) {

			userInformation.setVisibility(View.VISIBLE);
			userLogin.setVisibility(View.GONE);

			Bitmap photo = CacheUtil.GetFromFile(
					preferences_user.getString("username", "用户名"), "Avatar");
			Drawable drawable = new BitmapDrawable(photo);

			userPhoto.setImageDrawable(drawable);
			userName.setText(preferences_user.getString("username", "用户名"));
			userPhone.setText(preferences_user.getString("phone", "电话"));

		} else {

			userInformation.setVisibility(View.GONE);
			userLogin.setVisibility(View.VISIBLE);

		}

	}

	protected void onResume() {

		isLogin = preferences.getBoolean("isLogin", false);
		initMessage();
		super.onResume();

	}

	public void processAction(View v) {
		switch (v.getId()) {
		case R.id.user_favorite: {
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), CollectActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.user_coin: {

			if (!preferences.getBoolean("isLogin", false)) {

				Toast.makeText(getApplicationContext(), "您尚未登录，无法进行积分操作！",
						Toast.LENGTH_SHORT).show();

				return;

			}

			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), ScoreActivity.class);
			startActivity(intent);

			break;
		}
		case R.id.user_buscard: {
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), CardActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.user_payoff: {
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), MipcaActivityCapture.class);
			intent.putExtra("INTENT_ACTION",
					MipcaActivityCapture.EnumAction.MSG_PAYOFF);
			startActivity(intent);
			break;
		}
		case R.id.user_shopping: {
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), ShopActivity.class);
			startActivity(intent);
			break;
		}
		default:
			return;
		}
	}

}
