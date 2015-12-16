package com.onebus.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult.BusStation;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.onebus.R;
import com.onebus.model.Bus;
import com.onebus.util.BusCheckUtil;

public class BusLineListViewAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;  
	private Context mContext;
	private ArrayList<BusStation> mBusLineList;
	private ArrayList<Bus> runBusList;
	private int selectIndex = -1; 
	

	private int currentCheckBus = 0;	//当前检查位置的车辆，防止重复检查
	
	public BusLineListViewAdapter(Context context, ArrayList<BusStation> list, ArrayList<Bus> runList){
		mContext = context;
		mBusLineList = list;
		runBusList = runList;
		try{
			if(mBusLineList == null){
				mBusLineList = new ArrayList<BusStation>();
			}
			mInflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

	@Override
	public int getCount() {
		return mBusLineList.size();
	}

	@Override
	public Object getItem(int position) {
		return mBusLineList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 	try{

	 			convertView = mInflater.inflate(R.layout.busline_list_item, null);
	 			View lineView = (View)convertView.findViewById(R.id.busline_listitem_line);
	 			TextView mstationName=(TextView)convertView.findViewById(R.id.busline_listitem_stationName); 
	 			TextView mOrderNumber=(TextView)convertView.findViewById(R.id.busline_listitem_orderNumber); 
	 			ImageView mLocationImage=(ImageView)convertView.findViewById(R.id.busline_listitem_locateImg);
	 			ImageView mBusImage=(ImageView)convertView.findViewById(R.id.busline_listitem_busImg);
	 			ImageView mDotImage=(ImageView)convertView.findViewById(R.id.busline_listitem_dotImg);

	            
		 		mOrderNumber.setText(""+(position+1));
            	int curIndex = mBusLineList.get(position).getTitle().indexOf("（");
            	if(curIndex!=-1){
            		mstationName.setText(mBusLineList.get(position).getTitle().substring(0, curIndex)+"");
            	}else{
            		curIndex = mBusLineList.get(position).getTitle().indexOf("(");
            		if(curIndex!=-1){
            			mstationName.setText(mBusLineList.get(position).getTitle().substring(0, curIndex)+"");
            		}else{
            			mstationName.setText(mBusLineList.get(position).getTitle()+"");
            		}
            	}
            	
            	
            	if(position==0){
            		lineView.setVisibility(View.GONE);
            		mBusImage.setImageResource(R.drawable.none_bg);
            	}
            	
            	boolean isBusInStation = false;
            	
            	if(position == selectIndex){
    	            convertView.setSelected(true);
    	            mDotImage.setImageResource(R.drawable.ic_dot_red);
    	            mOrderNumber.setTextColor(Color.RED);
    	            mstationName.setTextColor(Color.RED);
    	            mLocationImage.setImageResource(R.drawable.ic_locate);
	            	mBusImage.setImageResource(R.drawable.none_bg);
	            	
	            	for(Bus bus : runBusList){
	            		if(position!=0){
	            			int code = BusCheckUtil.getBusPosition(position-1, position, 
	            					new LatLng(bus.getLatitude(),bus.getLongitude()));
	            			if(code==0){	//near
	            				mBusImage.setImageResource(R.drawable.busnormal);
	            			}else if(code==1){ //in station
	            				mLocationImage.setImageResource(R.drawable.bushighlight);
    	            			isBusInStation = true;
	            			}
	            		}
	            	}
	            	
	            	/*for(Bus bus : runBusList){
    	            	if(position!=0 && isBusNear(bus, position)){
    	            		if(!isBusInStation && isBusInStation(bus, mBusLineList.get(position).getLocation())){
    	            			mLocationImage.setImageResource(R.drawable.bushighlight);
    	            			isBusInStation = true;
    	            		}else{
    	            			mBusImage.setImageResource(R.drawable.busnormal);
    	            		}
        	            }
    	            }*/

            	}else{
    	            convertView.setSelected(false);
    	            mDotImage.setImageResource(R.drawable.ic_dot_gray);
    	            mLocationImage.setImageResource(R.drawable.none_bg);
    	            mBusImage.setImageResource(R.drawable.none_bg);

    	            for(Bus bus : runBusList){
	            		if(position!=0){
	            			int code = BusCheckUtil.getBusPosition(position-1, position, 
	            					new LatLng(bus.getLatitude(),bus.getLongitude()));
	            			if(code==0){	//near
	            				mBusImage.setImageResource(R.drawable.busnormal);
	            			}else if(code==1){ //in station
	            				mLocationImage.setImageResource(R.drawable.busnormal);
    	            			isBusInStation = true;
	            			}
	            		}
	            	}
    	            
    	            /*for(Bus bus : runBusList){
    	            	if(position!=0 && isBusNear(bus, position)){
    	            		if(!isBusInStation && isBusInStation(bus, mBusLineList.get(position).getLocation())){
    	            			mLocationImage.setImageResource(R.drawable.busnormal);
    	            			isBusInStation = true;
    	            		}else{
    	            			mBusImage.setImageResource(R.drawable.busnormal);
    	            		}
        	            }
    	            }*/
	            	
    	        }	
		            	

			}catch(Exception e){
				Log.e("HListViewAdpater getView", ""+e.getMessage());
				e.printStackTrace();
			}
	        
	        return convertView;  
	}
	
	
	/**
	 * 判断车辆是否进站
	 * @param stationLocation
	 * @return
	 */
	private boolean isBusInStation(Bus bus, LatLng position){
		//TODO 判断公交车是否进站
		double cDis = DistanceUtil.getDistance(new LatLng(bus.getLatitude(),bus.getLongitude()), position);
		if(cDis <= 10){
			currentCheckBus = bus.getId();
			return true;
		}
		return false;
	}
	
	/**
	 * 判断车辆是否快抵达此站
	 * @return
	 */
	private boolean isBusNear(Bus bus, int position){
		//TODO 判断公交车是否在此站之前，上一个站点之后
		LatLng startPos = mBusLineList.get(position-1).getLocation();
		LatLng endPos = mBusLineList.get(position).getLocation();
		
		LatLng cricleCenter = new LatLng((startPos.latitude+endPos.latitude)/2,
				(startPos.longitude+endPos.longitude)/2);
		
		if(SpatialRelationUtil.isCircleContainsPoint(cricleCenter,
				(int) DistanceUtil.getDistance(startPos, endPos)/2, 
				new LatLng(bus.getLatitude(),bus.getLongitude()))
				&& DistanceUtil.getDistance(new LatLng(bus.getLatitude(),bus.getLongitude()), startPos)>10){
			return true;
		}
		
		return false;
	}
	
	
	public void setSelectIndex(int i){  
        selectIndex = i;
    }
	

}
