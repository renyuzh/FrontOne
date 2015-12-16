package com.onebus.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.onebus.IPAdress;

public class IncreaseIntegral {

	private static IPAdress IPadress = new IPAdress();

	private static String URL = "http://" + IPadress.getIP() + ":"
			+ IPadress.getPORT() + "/onebus/android/setScore";

	private static SharedPreferences preferences_user;

	public static String getJson(Context context, int score) {

		if (preferences_user == null) {

			preferences_user = context.getSharedPreferences("USERMESSAGE",
					Context.MODE_PRIVATE);

		}

		try {
			JSONArray jsonArray = new JSONArray();
			JSONObject jsonObject = new JSONObject();

			jsonObject.put("phone", preferences_user.getString("phone", "电话"));
			jsonObject.put("score", score);

			jsonArray.put(jsonObject);

			HttpClient client = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost(URL);

			// 设置参数，仿html表单提交
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			BasicNameValuePair param = new BasicNameValuePair("setScore",
					jsonArray + "");
			paramList.add(param);

			httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF_8"));

			HttpResponse httpResponse = client.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = httpResponse.getEntity();

				if (entity != null) {
					String result = EntityUtils.toString(
							httpResponse.getEntity(), HTTP.UTF_8);
					return result;
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
