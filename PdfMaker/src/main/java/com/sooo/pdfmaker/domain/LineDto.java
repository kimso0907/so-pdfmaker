package com.sooo.pdfmaker.domain;

public class LineDto {
	private int startX;
	private int endX;
	private int startY;
	private int endY;
	private float border;
	private String borderColor;
	
	public LineDto(int startX, int endX, int startY, int endY, float border, String borderColor) {
		super();
		this.startX = startX;
		this.endX = endX;
		this.startY = startY;
		this.endY = endY;
		this.border = border;
		this.borderColor = borderColor;
	}
	
	public int getStartX() {
		return startX;
	}
	public void setStartX(int startX) {
		this.startX = startX;
	}
	public int getEndX() {
		return endX;
	}
	public void setEndX(int endX) {
		this.endX = endX;
	}
	public int getStartY() {
		return startY;
	}
	public void setStartY(int startY) {
		this.startY = startY;
	}
	public int getEndY() {
		return endY;
	}
	public void setEndY(int endY) {
		this.endY = endY;
	}
	public float getBorder() {
		return border;
	}
	public void setBorder(float border) {
		this.border = border;
	}
	public String getBorderColor() {
		return borderColor;
	}
	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}
}
