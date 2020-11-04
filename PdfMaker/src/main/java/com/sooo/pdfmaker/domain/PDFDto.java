package com.sooo.pdfmaker.domain;
import java.util.ArrayList;
import java.util.HashMap;

public class PDFDto {

	private int pageNum;
	private HashMap<String, Object> pageSetting;
	private ArrayList<ContentDto> contents;
	private String Message;
	
	public int getPageNum() {
		return pageNum;
	}
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}
	public HashMap<String, Object> getPageSetting() {
		return pageSetting;
	}
	public void setPageSetting(HashMap<String, Object> pageSetting) {
		this.pageSetting = pageSetting;
	}
	public ArrayList<ContentDto> getContents() {
		return contents;
	}
	public void setContents(ArrayList<ContentDto> contents) {
		this.contents = contents;
	}
	public String getMessage() {
		return Message;
	}
	public void setMessage(String message) {
		Message = message;
	}	
}