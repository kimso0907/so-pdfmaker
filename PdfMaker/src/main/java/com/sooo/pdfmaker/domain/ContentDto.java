package com.sooo.pdfmaker.domain;

import java.util.HashMap;

public abstract class ContentDto {

	private HashMap<String, Object> contentSetting;
	private String contentType;
	
	public HashMap<String, Object> getContentSetting() {
		return contentSetting;
	}
	public void setContentSetting(HashMap<String, Object> contentSetting) {
		this.contentSetting = contentSetting;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
