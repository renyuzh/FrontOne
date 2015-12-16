package com.onebus.model;


/**
 * 通告实体类
 * 
 * @author hkq
 * @version 1.0
 * @created 2015-7-20
 */
public class Notice {

	private String title;
	private String body;
	private String author;
	private String pubDate;
	private boolean newType;

	public Notice() {

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPubDate() {
		return pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}

	public boolean isNewType() {
		return newType;
	}

	public void setNewType(boolean newType) {
		this.newType = newType;
	}

}
