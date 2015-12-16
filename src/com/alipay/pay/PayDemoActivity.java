package com.alipay.pay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.onebus.R;
import com.onebus.util.NetWorkUtil;


public class PayDemoActivity extends FragmentActivity {

	//商户PID
	public static final String PARTNER = "2088411278834183";
	//商户收款账号
	public static final String SELLER = "2376625228@qq.com";
	//商户私钥，pkcs8格式
	public static final String RSA_PRIVATE = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBALeK1vCRF7SPC2LYbNH0jtiAUuXvksKGGDHbemcbdvK4lJcJUvzrD44Soa5z3AQO0rGCaiU2KthzMnHIyAPJBavIKu7gmCsxz410eEwRrfQG9i3KpiJ0eDqVNkp84TcTe1XyasZ7sR7jsU0xIIEqWvx1cEnXWpggWiD4L4HGoZvFAgMBAAECgYAC9dFLpfts5RW47War6DbZlRBBNyD11DXVsAWEXjQH7CQxX2wgOiZRE0naR3IX+qk5RbIykXIVdZ4tqaBYN0jJGn4ccYzjXhkdo4XHaBXRE44cFKseofb+clmZ/RsrBzV9aCE3xG1I7+53vCqfxgNrfpTUd1vOTTTAI67CnlR1wQJBANx7J8RdZI0zOpKCuQKhy7v7+Y+OXCpIGBUeVZVo4pRCkinTmEWmGYRJ/gSkNhXUEqp+kU9yWbA46/IFfXb8XbECQQDVHEvqQdHJjKUTBg+UcVsQUjV+2+EnKQLuhdTDWFPr137Zopau4rp7ZmvXY1ybI/mI/Req9aWcMlG9h6xfXoBVAkEAvCJSkppmnJ3kEvR8ziQlZqjiEH8XYfTWp96UhMNU49HSSuKFAplzGJDSsj8073dg3h7V+4bFxTstCf1pS8iYYQJBAIkQnQb69FoEOnukz0pzKdWyejOwafyM1RTiQLivS7KJNE3306CBtNz/P+cVQg+KMMlZtuo++hgfCRG0fDoOhs0CQQCcY/UJVyP1lFnZnZZP8F8166gXBRI9hM98/k4mpc+JF/qA7H1/jyrYZWVGlbQDMOAIVp/yqwE0rkKUyp8x+j5q";
	//支付宝公钥
	public static final String RSA_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";


	private static final int SDK_PAY_FLAG = 1;

	private static final int SDK_CHECK_FLAG = 2;
	
	
	private String price;
	private String plateNumber;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SDK_PAY_FLAG: {
				PayResult payResult = new PayResult((String) msg.obj);
				
				// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
				String resultInfo = payResult.getResult();
				
				String resultStatus = payResult.getResultStatus();

				// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
				if (TextUtils.equals(resultStatus, "9000")) {
//					Toast.makeText(PayDemoActivity.this, "支付成功",
//							Toast.LENGTH_SHORT).show();
					Intent payResultIntent = new Intent(PayDemoActivity.this, PayResultActivity.class);
					payResultIntent.putExtra("plateNumber", plateNumber);
					payResultIntent.putExtra("price", price);
					payResultIntent.putExtra("status", "支付成功");
					startActivity(payResultIntent);	
					finish();

				} else {
					// 判断resultStatus 为非“9000”则代表可能支付失败
					// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						Toast.makeText(PayDemoActivity.this, "支付结果确认中",
								Toast.LENGTH_SHORT).show();

					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
//						Toast.makeText(PayDemoActivity.this, "支付失败",
//								Toast.LENGTH_SHORT).show();

						Intent payResultIntent = new Intent(PayDemoActivity.this, PayResultActivity.class);
						payResultIntent.putExtra("plateNumber", plateNumber);
						payResultIntent.putExtra("price", price);
						payResultIntent.putExtra("status", "支付成功");
						startActivity(payResultIntent);	
						finish();
					}
				}
				break;
			}
			case SDK_CHECK_FLAG: {
				Log.i("Account Check", (boolean) (msg.obj) +"");
				if((boolean) (msg.obj)){
					pay();
				}else{
					Toast.makeText(PayDemoActivity.this, ""+getResources().getString(R.string.pay_AccountFailure),
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
			default:
				break;
			}
		};
	};
	
	private TextView mBusPriceTextView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_main);
		
		//解析公交车费等信息
		mBusPriceTextView = (TextView)findViewById(R.id.alipay_price);
		plateNumber = getIntent().getStringExtra("plateNumber");
		price = getIntent().getStringExtra("busPrice");
		if(price==null || price.trim().equals("")){
			price = "1.00";
		}
		mBusPriceTextView.setText(price);
	}

	/**
	 * call alipay sdk pay. 调用SDK支付
	 * 
	 */
	public void pay() {
		// 订单
		String orderInfo = getOrderInfo("测试的商品", "该测试商品的详细描述", price);
		// 对订单做RSA 签名
		String sign = sign(orderInfo);

		try {
			// 仅需对sign 做URL编码
			sign = URLEncoder.encode(sign, "UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 完整的符合支付宝参数规范的订单信息
		final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
				+ getSignType();

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(PayDemoActivity.this);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo);

				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	/**
	 * check whether the device has authentication alipay account.
	 * 查询终端设备是否存在支付宝认证账户
	 * 
	 */
	public void check(View v) {
		
		if(!NetWorkUtil.isNetWorkConnected(getApplicationContext())){
			Toast.makeText(PayDemoActivity.this, ""+getResources().getString(R.string.networkdisabled),
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		Runnable checkRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask payTask = new PayTask(PayDemoActivity.this);
				// 调用查询接口，获取查询结果
				boolean isExist = payTask.checkAccountIfExist();
				Log.i("Account Check1", isExist +"");
				
				Message msg = new Message();
				msg.what = SDK_CHECK_FLAG;
				msg.obj = isExist;
				mHandler.sendMessage(msg);
			}
		};

		Thread checkThread = new Thread(checkRunnable);
		checkThread.start();

	}

	/**
	 * get the sdk version. 获取SDK版本号
	 * 
	 */
	public void getSDKVersion() {
		PayTask payTask = new PayTask(this);
		String version = payTask.getVersion();
		Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
	}

	/**
	 * create the order info. 创建订单信息
	 * 
	 */
	public String getOrderInfo(String subject, String body, String price) {
		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + PARTNER + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + "http://notify.msp.hk/notify.htm"
				+ "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}

	/**
	 * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
	 * 
	 */
	public String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
				Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}

	/**
	 * sign the order info. 对订单信息进行签名
	 * 
	 * @param content
	 *            待签名订单信息
	 */
	public String sign(String content) {
		String s=SignUtils.sign(content, RSA_PRIVATE);
		return s;
	}

	/**
	 * get the sign type we use. 获取签名方式
	 * 
	 */
	public String getSignType() {
		return "sign_type=\"RSA\"";
	}

}
