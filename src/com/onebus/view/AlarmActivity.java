package com.onebus.view;

import com.onebus.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.media.MediaPlayer;
import android.os.Bundle;

public class AlarmActivity extends Activity {

	MediaPlayer alarmMusic;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 加载指定音乐，并为之创建MediaPlayer对象
		alarmMusic = MediaPlayer.create(this, R.raw.wait_alarm);
		alarmMusic.setLooping(true);

		// 播放音乐
		alarmMusic.start();

		// 创建一个对话框
		new AlertDialog.Builder(AlarmActivity.this).setTitle("提醒")
				.setIcon(R.drawable.ic_launcher).setMessage("您好，您预约的车辆快到站了！")
				.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						alarmMusic.stop();
//						Intent intent = new Intent(AlarmActivity.this, MainActivity.class);
//						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
//						startActivity(intent);
						AlarmActivity.this.finish();

					}
				}).create().show();

	}

}