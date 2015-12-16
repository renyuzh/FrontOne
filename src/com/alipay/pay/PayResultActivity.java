package com.alipay.pay;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.onebus.R;

public class PayResultActivity extends Activity {
	
	private ImageButton mImageButtonBack;
	
	private TextView plateNumberTextView;
	private TextView priceTextView;
	private TextView statusTextView;
	private TextView timeTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pay_result);
		
		
		plateNumberTextView = (TextView)findViewById(R.id.payresult_plateNumber);
		priceTextView = (TextView)findViewById(R.id.payresult_price);
		statusTextView = (TextView)findViewById(R.id.payresult_status);
		timeTextView = (TextView) findViewById(R.id.payresult_time);
		
		plateNumberTextView.setText(getIntent().getStringExtra("plateNumber")+"");
		priceTextView.setText(getIntent().getStringExtra("price")+"");
		statusTextView.setText(getIntent().getStringExtra("status")+"");
		
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
		String dateString = formatter.format(currentTime);
		
		timeTextView.setText(dateString);
		
		mImageButtonBack = (ImageButton)findViewById(R.id.payresult_btn_back);
		mImageButtonBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		
	}

	
	
}
