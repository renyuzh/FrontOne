package com.onebus.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult.BusStep;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.onebus.IPAdress;
import com.onebus.model.Bus;

public class HttpUtil {

	/**
	 * method: GET 
	 * @param url 
	 * @return String
	 */
	public static String getBusPrice(String url, String plateNumber){
		try
        {
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			BasicNameValuePair param = new BasicNameValuePair("plateNumber",
					plateNumber + "");
			paramList.add(param);
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(paramList,
					"UTF_8"));
			
	        HttpClient httpClient = new DefaultHttpClient();
	        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,3000);
	        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,3000);
	        HttpResponse response;
        
            response = httpClient.execute(httpPost);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            	String result = EntityUtils.toString(response.getEntity());
            	Log.i("HttpUtil","Result:"+result);

            	return result;
            }
            else{
            	return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
		return null;
	}
	
	
	
	/**
	 * 获取此线路正在运行的车辆
	 * @param busLineId
	 * @return
	 */
	public static ArrayList<Bus> getRunBusList(String busLineId, List<BusStep>  mBusLineStepList, boolean sendData){
		
		ArrayList<Bus> busList = new ArrayList<Bus>();
		
		try {
			StringBuilder simulator = new StringBuilder();
			simulator.append("{");
			simulator.append("\"busLineId\":\""+busLineId+"\",");

			StringBuilder points = new StringBuilder();
			points.append("\"points\":\"");
			if(sendData){		//第一次发送，以后就不用发送了
				for(BusStep step : mBusLineStepList){
					List<LatLng> spotList = step.getWayPoints();
					for(LatLng pos : spotList){	
						points.append(pos.longitude + "," + pos.latitude + ";");
					}
				}
			}
			points.append("\"");
			
			simulator.append(points.toString()+"}");

			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			BasicNameValuePair param = new BasicNameValuePair("simulator",
					simulator + "");
			paramList.add(param);
			HttpPost httpPost = new HttpPost("http://"+IPAdress.IP+":"+IPAdress.PORT+"/onebus/android/simu");
			httpPost.setEntity(new UrlEncodedFormEntity(paramList,
					"UTF_8"));
	        HttpClient httpClient = new DefaultHttpClient();
	        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,5000);//连接时间
	        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,5000);//数据传输时间
	        HttpResponse response;

            response = httpClient.execute(httpPost);
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            	String result = EntityUtils.toString(response.getEntity());
            	
            	if(result==null || result.trim().equals("")){
            		return busList;
            	}
            	
            	String[] postionArray = result.split(";");
            	for(int i=0; i<postionArray.length; i++){
            		
            		double lat = Double.parseDouble(postionArray[i].split(",")[1]);
                	double lng = Double.parseDouble(postionArray[i].split(",")[0]);
                	
                	//Log.i("HttpUtil","Latitude:"+lat + " - Longitude"+lng);
                	
                	Bus cBus = new Bus();
                	cBus.setId(i);
                	cBus.setLongitude(lng);
                	cBus.setLatitude(lat);
                	busList.add(cBus);
            	}
            	
            }
            else{
            	return busList;
            }
        }catch(ConnectTimeoutException cte){
        	cte.printStackTrace();
            return busList;
            
        } catch (Exception e){
            e.printStackTrace();
            return busList;
        }
		
        return busList;
	}
	
}
