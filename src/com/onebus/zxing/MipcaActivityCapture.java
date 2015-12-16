package com.onebus.zxing;

import java.io.IOException;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

import com.alipay.pay.PayDemoActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.onebus.IPAdress;
import com.onebus.R;
import com.onebus.util.HttpUtil;
import com.onebus.util.IncreaseIntegral;
import com.onebus.util.NetWorkUtil;
import com.onebus.zxing.camera.CameraManager;
import com.onebus.zxing.decoding.CaptureActivityHandler;
import com.onebus.zxing.decoding.InactivityTimer;
import com.onebus.zxing.view.ViewfinderView;

/**
 * Initial the camera
 * 
 * @author Ryan.Tang
 */
public class MipcaActivityCapture extends Activity implements Callback {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private Bundle bundle;

	private ProgressDialog mProgressDialog;
	private Intent mIntent;

	private Context context;
	private Handler scorehandler;
	private final static int UPDATE_SCORE = 0x002;

	public static class EnumAction {
		public final static String MSG_PAYOFF = "payoff";
		public final static String MSG_COMPLAINT = "complaint";
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);
		// ViewUtil.addTopView(getApplicationContext(), this,
		// R.string.scan_card);

		context = this;
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		mIntent = getIntent();

		scorehandler = new Handler() {

			@Override
			public void handleMessage(final Message msg) {
				switch (msg.what) {
				case UPDATE_SCORE: {
//
//					Toast.makeText(getApplicationContext(), "积分更新成功！",
//							Toast.LENGTH_SHORT).show();

					break;
				}
				default:
					super.handleMessage(msg);
				}
			}

		};
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private final static int MSG_SCAN_FAILED = 0x1;
	private final static int MSG_SCAN_SUCCESS = 0x2;

	public Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case MSG_SCAN_FAILED: {
				Toast.makeText(MipcaActivityCapture.this,
						getResources().getString(R.string.scan_failed),
						Toast.LENGTH_SHORT).show();
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MipcaActivityCapture.this.finish();
				break;
			}
			case MSG_SCAN_SUCCESS: {

				sendScore();

				Intent intent = new Intent(MipcaActivityCapture.this,
						PayDemoActivity.class);
				intent.putExtra("busPrice",
						((Bundle) msg.obj).getString("price"));
				intent.putExtra("plateNumber",
						((Bundle) msg.obj).getString("plateNumber"));
				startActivity(intent);
				MipcaActivityCapture.this.finish();

				break;
			}
			}

			super.handleMessage(msg);
		};
	};

	/**
	 * 处理扫描结果
	 * 
	 * @param result
	 * @param barcode
	 * @throws JSONException
	 */
	public void handleDecode(Result result, Bitmap barcode)
			throws JSONException {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		final String resultString = result.getText();
		if (resultString.equals("")) {
			Toast.makeText(MipcaActivityCapture.this,
					getResources().getString(R.string.scan_failed),
					Toast.LENGTH_SHORT).show();
		} else {

			Log.e("MipcaActivity", "Scan: " + resultString);

			/**
			 * [OneBus]:[{"busId":"2","busnumber":"陕A43521"}]
			 */

			if (resultString.startsWith("[OneBus]:")) {
				if (mIntent.getStringExtra("INTENT_ACTION").equals(
						MipcaActivityCapture.EnumAction.MSG_PAYOFF)) {
					JSONArray jsonArray = null;
					JSONObject jsonObject = null;
					jsonArray = new JSONArray(resultString.substring(9));
					jsonObject = (JSONObject) jsonArray.get(0);
					final String busNumber = jsonObject.optString("busnumber");
					Log.e("MipcaActivity", "busNumber: " + busNumber);
					// 先通过扫描结果获取车费
					mProgressDialog = new ProgressDialog(
							MipcaActivityCapture.this);
					mProgressDialog
							.setIcon(R.drawable.common_topbar_route_foot_pressed);
					mProgressDialog.setMessage("请稍等....");
					mProgressDialog.show();
					if (NetWorkUtil.isNetWorkConnected(getApplicationContext())) {
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									String priceResult = HttpUtil
											.getBusPrice(
													"http://"
															+ IPAdress.IP
															+ ":"
															+ IPAdress.PORT
															+ "/onebus/android/getBusPrice",
													busNumber);
									if (priceResult != null
											&& !priceResult.trim().equals("")) {
										Message msg = mHandler.obtainMessage();
										msg.what = MSG_SCAN_SUCCESS;
										double busPrice = Double
												.parseDouble(priceResult);
										priceResult = String.format("%.2f",
												busPrice);
										Bundle bundle = new Bundle();
										bundle.putString("price", priceResult);
										bundle.putString("plateNumber",
												busNumber);
										msg.obj = bundle;
										mHandler.sendMessage(msg);

									} else {
										Message msg = mHandler.obtainMessage();
										msg.what = MSG_SCAN_FAILED;
										mHandler.sendMessage(msg);
									}

								} catch (Exception e) {
									Log.e("MipcaActivity",
											"Scan Failed " + e.getMessage());
									e.printStackTrace();
									Message msg = mHandler.obtainMessage();
									msg.what = MSG_SCAN_FAILED;
									mHandler.sendMessage(msg);
								} finally {
									if (mProgressDialog != null
											&& mProgressDialog.isShowing()) {
										mProgressDialog.dismiss();
									}
								}
							}

						}).start();
					} else {
						mProgressDialog.dismiss();
						Message msg = mHandler.obtainMessage();
						msg.what = MSG_SCAN_FAILED;
						mHandler.sendMessage(msg);
					}

				} else if (mIntent.getStringExtra("INTENT_ACTION").equals(
						MipcaActivityCapture.EnumAction.MSG_COMPLAINT)) {
					mProgressDialog = new ProgressDialog(
							MipcaActivityCapture.this);
					mProgressDialog
							.setIcon(R.drawable.common_topbar_route_foot_pressed);
					mProgressDialog.setMessage("请稍等....");
					mProgressDialog.show();

					SharedPreferences preference_feedback = getSharedPreferences(
							"Feedback", Context.MODE_PRIVATE);
					Editor edit = preference_feedback.edit();
					edit.putString("Feedback", resultString.substring(9));
					edit.commit();

					mProgressDialog.dismiss();

					MipcaActivityCapture.this.finish();
				}

			} else {
				Message msg = mHandler.obtainMessage();
				msg.what = MSG_SCAN_FAILED;
				mHandler.sendMessage(msg);
			}

		}

	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	public void sendScore() {

		new Thread() {

			public void run() {

				try {

					String str = IncreaseIntegral.getJson(context, 1);

					JSONArray jsonArray = new JSONArray(str);
					JSONObject jsonObject = (JSONObject) jsonArray.get(0);

					if (jsonObject.optString("status").equals("success")) {

						Message msg = new Message();
						msg.what = UPDATE_SCORE;
						scorehandler.sendMessage(msg);

					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}.start();

	}
}