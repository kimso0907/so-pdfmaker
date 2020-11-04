package com.sooo.pdfmaker.domain;
import java.util.ArrayList;

public class TableDto extends ContentDto {

	private ArrayList<ArrayList<TextDto>> cells;
	private int tableRow;
	private int tableCol;
	
	public ArrayList<ArrayList<TextDto>> getCells() {
		return cells;
	}
	public void setCells(ArrayList<ArrayList<TextDto>> cells) {
		this.cells = cells;
	}
	public int getTableRow() {
		return tableRow;
	}
	public void setTableRow(int tableRow) {
		this.tableRow = tableRow;
	}
	public int getTableCol() {
		return tableCol;
	}
	public void setTableCol(int tableCol) {
		this.tableCol = tableCol;
	}
}
