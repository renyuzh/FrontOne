package com.onebus.widget;


import com.onebus.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;


public class MainWidgetImageButton extends View {

	private int mTextColor = 0xFF777777;
	private Bitmap mIconBitmap;
	private String mText = "What";
	private int mTextSize = 
			(int) 
			TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12,
					getResources().getDisplayMetrics());
	private int mTextMarginLeft = (int) 
			TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
					getResources().getDisplayMetrics());
	
	private Rect mIconRect;
	private int iconWidth;
	private int iconLeft;
	private Rect mTextBound;
	private Paint mTextPaint;

	public MainWidgetImageButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MainWidgetImageButton(Context context) {
		this(context,null);
	}

	/**
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyleAttr
	 */
	public MainWidgetImageButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		//TODO
		this.setFocusable(true);
		this.setClickable(true);
		
		Log.i("MainWidgetImageButton", "constructor");
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.MainWidgetImageButton);
		
		int n = a.getIndexCount();
		
		for(int i=0; i<n; i++){
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.MainWidgetImageButton_icon:
				BitmapDrawable drawable = (BitmapDrawable)a.getDrawable(attr);
				mIconBitmap = drawable.getBitmap();
				break;
			case R.styleable.MainWidgetImageButton_text_color:
				mTextColor = a.getColor(attr, 0xFF000000);
				break;
			case R.styleable.MainWidgetImageButton_text:
				mText = a.getString(attr);
				break;
			case R.styleable.MainWidgetImageButton_text_size:
				mTextSize = (int)a.getDimension(attr, 
			TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14,
					getResources().getDisplayMetrics()));
				break;
			case R.styleable.MainWidgetImageButton_text_marginLeft:
				mTextMarginLeft = (int)a.getDimension(attr, 
			TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
					getResources().getDisplayMetrics()));
				break;
			
			}
		}
		//���յ�a
		a.recycle();
		
		mTextBound = new Rect();
		mTextPaint = new Paint();
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setColor(0xFF000000);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setFakeBoldText(true);
		mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBound);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		iconWidth = (int) (0.9*Math.min(getMeasuredWidth()-getPaddingLeft()-getPaddingRight()
								-mTextBound.width(),
								getMeasuredHeight()-getPaddingTop()-getPaddingBottom()));
		iconLeft =	getMeasuredWidth()/2 - (iconWidth+mTextBound.width()+mTextMarginLeft)/2 - getPaddingLeft()/2;
		int top =  getMeasuredHeight()/2 - iconWidth/2;
		
		mIconRect = new Rect(iconLeft, top, iconLeft+iconWidth, top+iconWidth);
		
	}
	
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		canvas.drawBitmap(mIconBitmap, null, mIconRect, null);
		int x = iconLeft+iconWidth+mTextMarginLeft;
		int y = getMeasuredHeight()/2 + mTextBound.height()/2;
		canvas.drawText(mText, x, y, mTextPaint);
		
		//Log.i("MainWidgetImageButton", "onDraw");
	}

}
