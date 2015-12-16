package com.onebus.view;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.onebus.view.CardLogActivity;
import com.onebus.R;
import com.sinpo.xnfc.BusCard;
import com.sinpo.xnfc.card.CardManager;

public class CardActivity extends Activity {

	private ImageView back;
	
	private LinearLayout scanLayout;
	private LinearLayout scanResultLayout;
	
	private TextView userCardMoney;
	private TextView userCardName;
	private RelativeLayout card_name;
	private RelativeLayout card_money;
	private RelativeLayout card_Records;
	private Button rechargeButton;
	
	
	/**
	 * NFC 
	 */
	NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;
	public static BusCard mBusCard;
	private Resources res;
	
	private TextView board;
	
	private enum ContentType {
		HINT, DATA, MSG
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_card);
		
		try{
			initView();
			
			if(mBusCard==null){
				mBusCard = new BusCard();
			}
	
			nfcAdapter = NfcAdapter.getDefaultAdapter(this);
			pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()),  0);
			
			onNewIntent(getIntent());
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void initView() {
		final Resources res = getResources();
		this.res = res;
		
		board = (TextView)findViewById(R.id.card_scanHint);
		
		scanLayout = (LinearLayout)findViewById(R.id.card_scanLayout);
		scanResultLayout = (LinearLayout)findViewById(R.id.card_resultLayout);
		scanResultLayout.setVisibility(View.GONE);
		

		back = (ImageView) findViewById(R.id.card_btn_back);
		userCardMoney = (TextView) findViewById(R.id.userCardMoney);
		userCardName = (TextView) findViewById(R.id.tv_cardID);
		card_name = (RelativeLayout) findViewById(R.id.card_name);
		card_money = (RelativeLayout) findViewById(R.id.card_money);
		card_Records = (RelativeLayout) findViewById(R.id.card_Records);
		rechargeButton = (Button) findViewById(R.id.recharge_button);

		
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		card_Records.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent logIntent = new Intent(CardActivity.this, CardLogActivity.class);
				logIntent.putParcelableArrayListExtra("resumeLogList", mBusCard.getConsumeLogList());
				
				startActivity(logIntent);
			}
		});

		rechargeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "抱歉，此卡暂不支持充值",
						Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
	
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		final Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Log.i("Card Activity NFCTAG", intent.getAction()+"");
		showData((p != null) ? CardManager.load(p, res) : null);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if (nfcAdapter != null)
			nfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (nfcAdapter != null)
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					CardManager.FILTERS, CardManager.TECHLISTS);
		refreshStatus();
	}
	
	
	/*
	 * 
	 
	private void initCard(String data){
		
		try{
		
			mBusCard = null;
			mBusCard = new BusCard();
			
			Log.e("NFC DATA", data);
			
			String[] strArray = data.split("\n");
			String logStr = "";
			for(int i=0; i<strArray.length; i++){
				logStr = i+": "+strArray[i]+"\n";
			}
			Log.i("NFC ArrayDATA", logStr);
			
			
			
			String cardName;
			int startpos = data.indexOf("Card");
			if(startpos!=-1){
				mBusCard.setName(data.substring(0, startpos));
			}else{
				mBusCard.setName("未识别卡片");
			}
			startpos = data.indexOf("Balance:");
			if(startpos!=-1){
				mBusCard.setBalance(data.substring(startpos+8, data.indexOf(" ",startpos)));
			}else{
				mBusCard.setBalance("0.0");
			}
			
			int logstartpos = data.indexOf("Log:");
			startpos = data.indexOf("]", logstartpos);
			mBusCard.getConsumeLogList().clear();
			//循环遍历
			while(startpos!=-1){
				String oneLog = data.substring(logstartpos+4, startpos);
				Log.i("oneLog", oneLog+"");
				
				String time = oneLog.substring(0, 17);
				String cash = oneLog.substring(17, oneLog.indexOf("["));
				String districtCode = oneLog.substring(oneLog.indexOf("[")+1);
				BusCard.ResumeLog nLog = new BusCard.ResumeLog(districtCode, time, cash);
				mBusCard.getConsumeLogList().add(nLog);
				
				logstartpos = startpos+1;
				startpos=data.indexOf("]", logstartpos);
			}
			
			startpos = data.indexOf("SEL:");
			if(startpos!=-1){
				mBusCard.setSel(data.substring(startpos+4, data.indexOf("\n",startpos+4)));
			}else{
				mBusCard.setSel("000000");
			}
			
			startpos = data.indexOf("DAT:");
			if(startpos!=-1){
				mBusCard.setStartDate(data.substring(startpos+4, data.indexOf("-", startpos+4)));
				mBusCard.setExpiryDate(data.substring(data.indexOf("-", startpos+4)));
			}else{
				mBusCard.setStartDate("0000.00.00");
				mBusCard.setExpiryDate("0000.00.00");
			}
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
*/
	/**
	 * 将卡片信息展示到界面上
	 */
	private void showResultOnView(){
		userCardMoney.setText(mBusCard.getBalance());
		userCardName.setText(mBusCard.getName());
	}
	

	private void showData(String data) {
		if (data == null || data.length() == 0) {
			showHint();
			return;
		}
		//initCard(data);
		try{
			
			//更换界面显示
			scanLayout.setVisibility(View.GONE);
			scanResultLayout.setVisibility(View.VISIBLE);
			showResultOnView();
	
		}catch(Exception e){
			e.printStackTrace();
		}

		
	}
	
	
	private void refreshStatus() {
		final Resources r = this.res;

		final String tip;
		if (nfcAdapter == null)
			tip = r.getString(R.string.tip_nfc_notfound);
		else if (nfcAdapter.isEnabled())
			tip = r.getString(R.string.tip_nfc_enabled);
		else
			tip = r.getString(R.string.tip_nfc_disabled);

		final StringBuilder s = new StringBuilder(
				r.getString(R.string.app_name));

		s.append("  --  ").append(tip);
		setTitle(s);

		final CharSequence text = board.getText();
		if (text == null || board.getTag() == ContentType.HINT)
			showHint();
	}
	
	private void showHint() {
		final String hint;
		
		if (nfcAdapter == null)
			hint = res.getString(R.string.msg_nonfc);
		else if (nfcAdapter.isEnabled())
			hint = res.getString(R.string.msg_nocard);
		else
			hint = res.getString(R.string.msg_nfcdisabled);
		
		board.setText(hint);
	}

}
