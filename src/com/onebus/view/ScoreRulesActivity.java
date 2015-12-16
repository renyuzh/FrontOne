package com.onebus.view;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.onebus.R;

public class ScoreRulesActivity extends Activity {

	private ImageButton back;

	private TextView rules;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scorerules);

		initView();

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();

			}
		});

		rules.setText(Html.fromHtml(getResources().getString(
				R.string.rulesScore)));

		// rules.setText(Html
		// .fromHtml("<h3>积分兑换规则</h3><ol><li>每日登录获得积分</li><li>使用扫码支付车费获取积分</li><li>使用此软件查询线路获取积分</li><li>查看公交实时信息获取积分</li><li>在商城购物获取积分</li><li>提出意见反馈并被采纳获取积分</li><li>提交投诉并情况属实获取积分</li></ol>"));

	}

	private void initView() {

		back = (ImageButton) findViewById(R.id.rules_btn_back);
		rules = (TextView) findViewById(R.id.rulesText);

	}

}
