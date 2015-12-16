package com.onebus.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.onebus.R;

public class NewsInformationActivity extends Activity {

	private ImageButton back;

	private TextView title;
	private TextView author;
	private TextView time;
	private TextView body;

	private String titleString;
	private String authorString;
	private String timeString;
	private String bodyString;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newsinformation);

		titleString = getIntent().getStringExtra("title");
		authorString = getIntent().getStringExtra("author");
		timeString = getIntent().getStringExtra("time");
		bodyString = getIntent().getStringExtra("body");

		initView();
		initMessage();

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();

			}
		});

	}

	private void initView() {

		back = (ImageButton) findViewById(R.id.newsInformation_btn_back);
		title = (TextView) findViewById(R.id.newsTitle);
		author = (TextView) findViewById(R.id.newsAuthor);
		time = (TextView) findViewById(R.id.newsTime);
		body = (TextView) findViewById(R.id.newsBody);

	}

	private void initMessage() {

		title.setText(titleString);
		author.setText(authorString);
		time.setText(timeString);
		body.setText(bodyString);

	}
}
