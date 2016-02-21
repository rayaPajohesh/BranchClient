package com.iac.branchClient.service.printer;

import java.util.List;
 
public class RequestPrintDetail {
	private String requestNo;
	private String printerType;
	private String printerIP;
	private String printerKey;
	private String track_1_Data;
	private String track_2_Data;
	private String track_3_Data;
	private String pan;
	private List<CardObjectDetail> cardObjectDetail;

	public String getRequestNo() {
		return this.requestNo;
	}

	public void setRequestNo(String val) {
		this.requestNo = val;
	}
	
	public String getPrinterType() {
		return this.printerType;
	}

	public void setPrinterType(String val) {
		this.printerType = val;
	}

	public String getPrinterIP() {
		return this.printerIP;
	}

	public void setPrinterIP(String val) {
		this.printerIP = val;
	}

	public String getPrinterKey() {
		return this.printerKey;
	}

	public void setPrinterKey(String val) {
		this.printerKey = val;
	}

	public String getTrack_1_Data() {
		return this.track_1_Data;
	}

	public void setTrack_1_Data(String val) {
		this.track_1_Data = val;
	}

	public String getTrack_2_Data() {
		return this.track_2_Data;
	}

	public void setTrack_2_Data(String val) {
		this.track_2_Data = val;
	}

	public String getTrack_3_Data() {
		return this.track_3_Data;
	}

	public void setTrack_3_Data(String val) {
		this.track_3_Data = val;
	}

	public List<CardObjectDetail> getCardObjectDetail() {
		return this.cardObjectDetail;
	}

	public void setCardObjectDetail(List<CardObjectDetail> val) {
		this.cardObjectDetail = val;
	}
	public String getPan() {
		return this.pan;
	}

	public void setPan(String val) {
		pan = val;
	}
	
}
