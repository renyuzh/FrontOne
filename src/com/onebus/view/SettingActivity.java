package com.onebus.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.onebus.R;

public class SettingActivity extends Activity {
	
	
	private ImageButton mImageButtonBack;
	
	private TextView remindType;
	private TextView remindAheadType;
	
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		init();
	}

	private void init(){
		preferences = getSharedPreferences("LastLocation",Context.MODE_PRIVATE);
		
		remindType = (TextView) findViewById(R.id.setting_remindType);
		remindType.setText(preferences.getString("remindType", "闹铃"));
		remindAheadType = (TextView) findViewById(R.id.setting_aheadType);
		remindAheadType.setText(preferences.getString("remindAhead", "到站提醒"));
		
		mImageButtonBack = (ImageButton)findViewById(R.id.setting_btn_back);
		mImageButtonBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

	}
	
	
	
	public void processAction(View v){
		switch(v.getId()){
		case R.id.setting_switchcity:{
			Intent switchCityIntent = new Intent(SettingActivity.this, SwitchCityActivity.class);
			startActivity(switchCityIntent);
			break;
		}
		case R.id.setting_offlineMap:{
			Intent offlineMapIntent = new Intent(SettingActivity.this, OfflineMapActivity.class);
			startActivity(offlineMapIntent);
			break;
		}
		case R.id.setting_alarm_type:{
			remindDialog();
			break;
		}
		case R.id.setting_alarm_ahead:{
			remindAheadDialog();
			break;
		}
		case R.id.setting_complain:{
			Intent complainIntent = new Intent(SettingActivity.this, ComplaintsActivity.class);
			startActivity(complainIntent);
			break;
		}
		case R.id.setting_suggest:{
			Intent suggestIntent = new Intent(SettingActivity.this, FeedBackActivity.class);
			startActivity(suggestIntent);
			break;
		}
		case R.id.setting_notice:{
			Intent noticeIntent = new Intent(SettingActivity.this, NoticeActivity.class);
			startActivity(noticeIntent);
			break;
		}
		case R.id.setting_about:{
			//Intent offlineMapIntent = new Intent(SettingActivity.this, OfflineMapActivity.class);
			//startActivity(offlineMapIntent);
			break;
		}
		case R.id.setting_update:{
			//Intent offlineMapIntent = new Intent(SettingActivity.this, OfflineMapActivity.class);
			//startActivity(offlineMapIntent);
			break;
		}
		default:
			return;
		}
	}
	
	/**
	 * 更改用户提醒方式
	 */
	private void remindDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				SettingActivity.this);

		final AlertDialog dialogBuilder = builder.show();

		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("提醒方式修改");
		final String[] sex = { "短信", "闹铃" };

		int i = 0;
		final int remindInt[] = { 0 };

		String remind_str = remindType.getText().toString();
		if (remind_str.equals("短信")) {

			i = 0;
			remindInt[0] = 0;

		} else if (remind_str.equals("闹铃")) {

			i = 1;
			remindInt[0] = 1;

		} else {

			i = 0;
			remindInt[0] = 0;

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

						remindInt[0] = which;

					}
				});
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (remindInt[0] == 0) {

					//remind_int = 1;
					remindType.setText("短信");
					Editor editor=preferences.edit();
					editor.putString("remindType", "短信");
					editor.commit();
				} else if (remindInt[0] == 1) {

					//remind_int = 2;
					remindType.setText("闹铃");
					Editor editor=preferences.edit();
					editor.putString("remindType", "闹铃");
					editor.commit();
				}

				dialogBuilder.dismiss();

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
	
	
	/**
	 * 更改用户提醒提前量
	 */
	private void remindAheadDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				SettingActivity.this);
		final AlertDialog dialogBuilder = builder.show();
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("提醒提前");
		final String[] sex = { "到站提醒", "提前一站", "提前二站", "提前三站", "提前四站" };
		int i = 0;
		final int remindInt[] = { 0 };
		String remind_str = remindAheadType.getText().toString();
		if (remind_str.equals("到站提醒")) {
			i = 0;
			remindInt[0] = 0;
		} else if (remind_str.equals("提前一站")) {
			i = 1;
			remindInt[0] = 1;
		}else if (remind_str.equals("提前二站")) {
			i = 2;
			remindInt[0] = 2;
		}else if (remind_str.equals("提前三站")) {
			i = 3;
			remindInt[0] = 3;
		}else if (remind_str.equals("提前四站")) {
			i = 4;
			remindInt[0] = 4;
		} else {
			i = 0;
			remindInt[0] = 0;
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
						remindInt[0] = which;
					}
				});
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (remindInt[0] == 0) {
					remindAheadType.setText("到站提醒");
					Editor editor=preferences.edit();
					editor.putString("remindAhead", "到站提醒");
					editor.commit();
				} else if (remindInt[0] == 1) {
					remindAheadType.setText("提前一站");
					Editor editor=preferences.edit();
					editor.putString("remindAhead", "提前一站");
					editor.commit();
				} else if (remindInt[0] == 2) {
					remindAheadType.setText("提前二站");
					Editor editor=preferences.edit();
					editor.putString("remindAhead", "提前二站");
					editor.commit();
				} else if (remindInt[0] == 3) {
					remindAheadType.setText("提前三站");
					Editor editor=preferences.edit();
					editor.putString("remindAhead", "提前三站");
					editor.commit();
				} else if (remindInt[0] == 4) {
					remindAheadType.setText("提前四站");
					Editor editor=preferences.edit();
					editor.putString("remindAhead", "提前四站");
					editor.commit();
				}

				dialogBuilder.dismiss();
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
	
	
}
