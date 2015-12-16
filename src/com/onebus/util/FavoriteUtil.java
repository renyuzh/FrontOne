package com.onebus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * 线路收藏
 * @author John
 *
 */
public class FavoriteUtil {
	
	public final static String BUS_LINE = "BUS_LINE";
	public final static String TRANSIT_ROUTE = "TRANSIT_ROUTE";

	/**
	 * 收藏
	 * @param context
	 * @param type	收藏类型
	 * @param data busLineId:busLineTitle
	 */
	public static void collect(Context context, String type, String data){
		SharedPreferences preference = context.getSharedPreferences("Favorite", Context.MODE_PRIVATE);
		Editor editor = preference.edit();
		
		if(type.equals(BUS_LINE)){				//BusLine
			String oldValue = preference.getString(BUS_LINE, "");
			if(!oldValue.trim().equals("")){
				String[] busLines = oldValue.split(";");
				for(String value : busLines){
					if(value.split(":")[0].equals(data.split(":")[0])){
						editor.commit();
						return;
					}
				}
			}
			editor.putString(BUS_LINE, oldValue+data+";");
			
		}else if(type.equals(TRANSIT_ROUTE)){	//TransitRoute
			//title, time, distance, walk, list[way]	split by #
			Log.i("collect Route", data);
			String oldValue = preference.getString(TRANSIT_ROUTE, "");
			if(!oldValue.trim().equals("")){
				String[] routes = oldValue.split(";");
				for(String value : routes){
					String tag = value.substring(0, value.indexOf("#"));
					if(tag.equals(data.substring(0, value.indexOf("#")))){
						editor.commit();
						return;
					}
				}
			}
			editor.putString(TRANSIT_ROUTE, oldValue+data+";");
			
		}
		editor.commit();
		return;
	}
	

	/**
	 * 判断此线路是否已经被收藏
	 * @param context
	 * @param busLineId
	 * @return boolean
	 */
	public static boolean favoriteBusLineListContains(Context context, String busLineId){
		SharedPreferences preference = context.getSharedPreferences("Favorite", Context.MODE_PRIVATE);
		String oldValue = preference.getString(BUS_LINE, "");
		if(!oldValue.trim().equals("")){
			String[] busLines = oldValue.split(";");
			for(String value : busLines){
				if(value.split(":")[0].equals(busLineId)){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 判断此换乘线路是否被收藏
	 * @param context
	 * @param title
	 * @return
	 */
	public static boolean favoriteTransitRouteListContains(Context context, String title){
		SharedPreferences preference = context.getSharedPreferences("Favorite", Context.MODE_PRIVATE);
		String oldValue = preference.getString(TRANSIT_ROUTE, "");
		if(!oldValue.trim().equals("")){
			String[] routes = oldValue.split(";");
			for(String value : routes){
				String tag = value.substring(0, value.indexOf("#"));
				if(tag.equals(title)){
					return true;
				}
			}
		}
		return false;
	}
	
	
	
	
	/**
	 * 取消收藏某一线路
	 * @param context
	 * @param busLineId
	 */
	public static void cancelFavoriteBusLine(Context context, String busLineId){
		SharedPreferences preference = context.getSharedPreferences("Favorite", Context.MODE_PRIVATE);
		Editor editor = preference.edit();
		StringBuilder storeData = new StringBuilder();
		String oldValue = preference.getString(BUS_LINE, "");
		if(!oldValue.trim().equals("")){
			String[] busLines = oldValue.split(";");
			for(String value : busLines){
				if(!value.split(":")[0].equals(busLineId)){
					storeData.append(value+";");
				}
			}
			editor.putString(BUS_LINE, storeData.toString());
		}
		editor.commit();
	}
	
	
	/**
	 * 取消收藏换乘线路
	 * @param context
	 * @param title
	 */
	public static void cancelFavoriteTransitRoute(Context context, String title){
		SharedPreferences preference = context.getSharedPreferences("Favorite", Context.MODE_PRIVATE);
		Editor editor = preference.edit();
		StringBuilder storeData = new StringBuilder();
		String oldValue = preference.getString(TRANSIT_ROUTE, "");
		if(!oldValue.trim().equals("")){
			String[] routes = oldValue.split(";");
			for(String value : routes){
				String tag = value.substring(0, value.indexOf("#"));
				if(!tag.equals(title)){
					storeData.append(value+";");
				}
			}
			editor.putString(TRANSIT_ROUTE, storeData.toString());
		}
		editor.commit();
	}
	
	
	

	/**
	 * 获取收藏的所有公交线路
	 * @param context
	 * @return String[]
	 */
	public static String[] getFavoriteBusLineList(Context context){
		SharedPreferences preference = context.getSharedPreferences("Favorite", Context.MODE_PRIVATE);
		return preference.getString(BUS_LINE, "").split(";");
	}
	
	
	/**
	 * 获取收藏的所有换乘线路
	 * @param context
	 * @return String[]
	 */
	public static String[] getFavoriteTransitRouteList(Context context){
		SharedPreferences preference = context.getSharedPreferences("Favorite", Context.MODE_PRIVATE);
		return preference.getString(TRANSIT_ROUTE, "").split(";");
	}
	
	
	
	
	
	
}
