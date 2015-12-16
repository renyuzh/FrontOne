package com.onebus.util;

/**  
 * All rights Reserved, Designed By HKQ   
 * @Title:  GuideViewPager    
 * @author: HKQ     
 * @date:   2015   
 * @version V1.0     
 */

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.onebus.R;
import com.onebus.view.MainActivity;

public class GuideViewPager extends Activity implements OnPageChangeListener {

	private ViewPager viewPager;
	private List<ImageView> imageViews;

	private int[] imageResId;
	private List<View> dots;

	private int currentItem = 0;

	private Button guideView_button;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.i("GuideViewPager", "onCreate");
		
		
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		
		
		setContentView(R.layout.activity_guide);

		guideView_button = (Button) findViewById(R.id.guideView_button);
		guideView_button.setVisibility(View.GONE);
		guideView_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GuideViewPager.this,
						MainActivity.class);
				startActivity(intent);
				finish();

			}
		});

		/*
		 * imageResId = new int[] { R.drawable.welcome_frist_image,
		 * R.drawable.welcome_two_image, R.drawable.welcome_three_image,
		 * R.drawable.welcome_four_image };
		 */

		imageResId = new int[] { R.drawable.w1,
				R.drawable.w2, R.drawable.w3,
				R.drawable.w4 };

		imageViews = new ArrayList<ImageView>();

		// 初始化图片
		for (int i = 0; i < imageResId.length; i++) {
			ImageView imageView_new = new ImageView(this);
			imageView_new.setImageResource(imageResId[i]);
			imageView_new.setScaleType(ScaleType.CENTER_CROP);
			imageViews.add(imageView_new);
		}

		dots = new ArrayList<View>();
		dots.add(findViewById(R.id.guide_dot0));
		dots.add(findViewById(R.id.guide_dot1));
		dots.add(findViewById(R.id.guide_dot2));
		dots.add(findViewById(R.id.guide_dot3));

		viewPager = (ViewPager) findViewById(R.id.viewpager_guide);
		viewPager.setAdapter(new MyAdapter());

		viewPager.setOnPageChangeListener(new MyPageChangeListener());

	}

	/**
	 * ViewPager监听
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyPageChangeListener implements OnPageChangeListener {
		private int oldPosition = 0;

		/**
		 * This method will be invoked when a new page becomes selected.
		 * position: Position index of the new selected page.
		 */
		public void onPageSelected(int position) {
			currentItem = position;
			dots.get(oldPosition).setBackgroundResource(R.drawable.dot_normal);
			dots.get(position).setBackgroundResource(R.drawable.dot_focused);
			oldPosition = position;
			if (position == imageViews.size() - 1) {
				guideView_button.setVisibility(View.VISIBLE);
			}
		}

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}
	}

	/**
	 * ViewPager的适配器
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return imageResId.length;
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(imageViews.get(arg1));
			return imageViews.get(arg1);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public void finishUpdate(View arg0) {

		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:

			Intent intent = new Intent(GuideViewPager.this, MainActivity.class);
			startActivity(intent);
			finish();

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

}
