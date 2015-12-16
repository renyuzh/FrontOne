package com.onebus.model;

import java.io.Serializable;

public class Bus implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4717927168879916028L;
	
	private int id;
	private String number;	//车牌号
	private String type;	//车的类型
	private String company;
	private String busLine;
	private int price;
	private String arriveTime;
	private String distance;
	private float speed;
	private double latitude;
	private double longitude;
	
	
	public String getArriveTime() {
		return arriveTime;
	}

	public void setArriveTime(String arriveTime) {
		this.arriveTime = arriveTime;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setNumber(String number) {
		this.number = number;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getCompany() {
		return company;
	}
	
	public void setCompany(String company) {
		this.company = company;
	}
	
	public String getBusLine() {
		return busLine;
	}
	
	public void setBusLine(String busLine) {
		this.busLine = busLine;
	}
	
	public int getPrice() {
		return price;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Bus [id=" + id + ", number=" + number + ", type=" + type
				+ ", company=" + company + ", busLine=" + busLine + ", price="
				+ price + ", speed=" + speed + ", latitude=" + latitude
				+ ", longitude=" + longitude + "]";
	}
	
	

}
