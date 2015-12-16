package com.onebus.view;

/**  
 * All rights Reserved, Designed By HKQ   
 * @Title:  WelcomeActivity   
 * @Package:	com.onebus.view
 * @Description:  	欢迎界面
 * @author: HKQ     
 * @date:   2015   
 * @version V1.0     
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.onebus.R;
import com.onebus.util.GuideViewPager;

public class WelcomeActivity extends Activity {

	private SharedPreferences preferences;

	boolean isLogin;
	public static final int DISPLAY_LENGTH = 500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try{
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.activity_welcome);
	
			preferences = getSharedPreferences("Welcome", MODE_PRIVATE);
			isLogin = preferences.getBoolean("isLogin", true);

			final Intent intent = new Intent();
			if (isLogin) {
				Log.i("First Use", "TO Guide ViewPager");
				new Handler().postDelayed(new Runnable() {
					public void run() {
	
						try{
							intent.setClass(WelcomeActivity.this, GuideViewPager.class);
							WelcomeActivity.this.startActivity(intent);
							WelcomeActivity.this.finish();
							overridePendingTransition(R.anim.fade, R.anim.hold);
						} catch(Exception e){
							e.printStackTrace();
						}
					}
				}, DISPLAY_LENGTH);
	
				Editor editor = preferences.edit();
				editor.putBoolean("isLogin", false);
				editor.commit();
	
			} else {
				Log.i("First Use", "TO MainActivity Directly");
				new Handler().postDelayed(new Runnable() {
					public void run() {
						try{
							intent.setClass(WelcomeActivity.this, MainActivity.class);
							WelcomeActivity.this.startActivity(intent);
							WelcomeActivity.this.finish();
							overridePendingTransition(R.anim.fade, R.anim.hold);
						} catch(Exception e){
							e.printStackTrace();
						}
					}
				}, DISPLAY_LENGTH);
	
			}
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
