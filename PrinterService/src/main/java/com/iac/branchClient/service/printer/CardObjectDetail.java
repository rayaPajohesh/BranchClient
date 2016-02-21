package com.iac.branchClient.service.printer;

  
  

public class CardObjectDetail {
	private String fieldName;
	private String fieldValue;
	private String fontName;
	private int fontSize;
	private String fontStyle;
	private int rowNum;
	private int colNum;

	public String getFieldName() {
		return this.fieldName;
	}

	public void setFieldName(String val) {
		this.fieldName = val;
	}

	public String getFieldValue() {
		return this.fieldValue;
	}

	public void setFieldValue(String val) {
		this.fieldValue = val;
	}

	public String getFontName() {
		return this.fontName;
	}

	public void setFontName(String val) {
		this.fontName = val;
	}

	public int getFontSize() {
		return this.fontSize;
	}

	public void setFontSize(int val) {
		this.fontSize = val;
	}

	public String getFontStyle() {
		return this.fontStyle;
	}

	public void setFontStyle(String val) {
		this.fontStyle = val;
	}

	public int getRowNum() {
		return this.rowNum;
	}

	public void setRowNum(int val) {
		this.rowNum = val;
	}

	public int getColNum() {
		return this.colNum;
	}

	public void setColNum(int val) {
		this.colNum = val;
	}

}
