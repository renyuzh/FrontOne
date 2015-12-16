package com.onebus.util;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult.BusStation;
import com.baidu.mapapi.search.busline.BusLineResult.BusStep;
import com.baidu.mapapi.utils.DistanceUtil;

public class BusCheckUtil {
	
	public static List<LatLng> linePoints;
	public static List<Integer> stationPosition;	//每个站点在linePoints中的位置
	
	static {
		Log.i("BusCheckUtil", "Static Block");
		linePoints = new ArrayList<LatLng>();
		stationPosition = new ArrayList<Integer>();
	}
	
	/**
	 * 初始化线路上的点和对应关系
	 * @param busStepList
	 */
	public static void initBusLineData(List<BusStep> busStepList, ArrayList<BusStation> busStationList){
		linePoints.clear();
		stationPosition.clear();
		for(BusStep step : busStepList){
			linePoints.addAll(step.getWayPoints());
		}
		
		
		/** 找到每个站点在linePoints中的位置*/
		double defaultDis;
		int last = 0, match = -1, range = 0;
		for(int i=0; i<busStationList.size(); i++){
			range = 0;
			defaultDis = Integer.MAX_VALUE;
			LatLng curPos = busStationList.get(i).getLocation();
			for(int j=last; j<linePoints.size(); j++){
				double dis;
				while((dis = DistanceUtil.getDistance(linePoints.get(j), curPos))==-1);
				
				if(dis <= defaultDis){
					defaultDis = dis;
					match = j;
					range = 0;
				}else {
					range++;
					if(range>=35){
						break;
					}
				}
			}
			stationPosition.add(match);
			last = match;
			//Log.i("BusCheckUtil", stationPosition.size()+"stationPosition:"+match);
		}

	}

	
	
	/**
	 * 
	 * @param pre
	 * @param last
	 * @param busLatLng 传入的位置肯定是linePoints中的一个
	 * @return -1不存在 		0在其中		 1等于last的位置
	 */
	public static int getBusPosition(int pre, int last, LatLng busLatLng){
		for(int i=stationPosition.get(pre); i<=stationPosition.get(last); i++){
			if(linePoints.get(i).latitude == busLatLng.latitude && 
					linePoints.get(i).longitude == busLatLng.longitude){
				if(i>=stationPosition.get(last)-2 && i<=stationPosition.get(last)+2){	//前后一个点
					return 1;
				}else if(i>stationPosition.get(pre)+2 && i<stationPosition.get(last)-2){
					return 0;
				}
			}
		}
		return -1;
	}
	

}
