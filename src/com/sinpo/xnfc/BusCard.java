package com.sinpo.xnfc;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class BusCard {

	private String name;
	private String balance;
	private String sel;
	private String startDate;
	private String expiryDate;
	private ArrayList<ConsumeLog> consumeLogList;
	
	
	
	
	public BusCard() {
		consumeLogList = new ArrayList<BusCard.ConsumeLog>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}

	public String getSel() {
		return sel;
	}

	public void setSel(String sel) {
		this.sel = sel;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public ArrayList<ConsumeLog> getConsumeLogList() {
		return consumeLogList;
	}

	public void setConsumeLogList(ArrayList<ConsumeLog> consumeLogList) {
		this.consumeLogList = consumeLogList;
	}
	
	
	
	

	@Override
	public String toString() {
		String result = "BusCard [name=" + name + ", balance=" + balance + ", sel="
				+ sel + ", startDate=" + startDate + ", expiryDate="
				+ expiryDate + ", consumeLogList=";
		
		for(ConsumeLog log : consumeLogList){
			result += log.toString();
		}

		return result;
	}





	public static class ConsumeLog implements Parcelable{
		public String districtCode;
		public String time;
		public String cash;
		
		public ConsumeLog(){}
		
		public ConsumeLog(String districtCode, String time, String cash) {
			super();
			this.districtCode = districtCode;
			this.time = time;
			this.cash = cash;
		}

		@Override
		public String toString() {
			return "ResumeLog [districtCode=" + districtCode + ", time=" + time
					+ ", cash=" + cash + "]";
		}

		public static final Parcelable.Creator<ConsumeLog> CREATOR = new Creator<ConsumeLog>() {    
	        public ConsumeLog createFromParcel(Parcel source) {    
	        	ConsumeLog log = new ConsumeLog();    
	        	log.districtCode = source.readString();    
	        	log.time = source.readString();    
	        	log.cash = source.readString();    
	            return log;    
	        }    
	        public ConsumeLog[] newArray(int size) {    
	            return new ConsumeLog[size];    
	        }    
	    };    
	  
	      
	    @Override  
	    public int describeContents() {  
	        // TODO Auto-generated method stub  
	        return 0;  
	    }  
	    @Override  
	    public void writeToParcel(Parcel parcel, int arg1) {  
	        parcel.writeString(districtCode);  
	        parcel.writeString(time);  
	        parcel.writeString(cash);  
	    }  
		
		

	}
	
}
