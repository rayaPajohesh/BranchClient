package com.iac.branchClient.service.printer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iac.branchClient.InterServiceMessage;
import com.iac.branchClient.Updater;

public class PrintAction implements InterServiceMessage{
	
	private final String PRINTER_EVOLIS = "1";
	private final String PRINTER_MABNA = "2";
	private final String PRINTER_HODOO = "3";
	private final String SUCCESS = "00";
	private final String errorInternalApplet = "9999";
	private final String errorInternalDevice = "8888";
	private String errorCode;

	private RequestPrintDetail requestPrintDetail;

	public Map<String, ?> send(Map<String, ?> request) {
		Map<String, String[]> map = new HashMap<String, String[]>();
		String res = issueCard(((String[])request.get("requestHeader"))[0], ((String[])request.get("requestDetail"))[0]);
		map.put("result", new String[]{res});
		return map;
	}
	
	public String issueCard(String requestHeader, String requestDetail) {

		try {

			Updater.info("PrintAction", "issueCard()  <ENTER>");

			requestPrintDetail = new RequestPrintDetail();
			boolean isValidData = isDataValid(requestHeader, requestDetail,
					requestPrintDetail);
			Updater.info("PrintAction",  "Request No : " + requestPrintDetail.getRequestNo());
			if (!isValidData)
				return errorInternalApplet;
			if (requestPrintDetail.getPrinterType().equalsIgnoreCase(PRINTER_MABNA)) {
				Mabna smart = new Mabna();
				errorCode = smart.Process(requestPrintDetail);
			} else if (requestPrintDetail.getPrinterType().equalsIgnoreCase(PRINTER_EVOLIS)) {
				Class<?> evolisClass = Class.forName("Evolis");
				Method processMethod = evolisClass.getMethod("Process", 
						new Class[]{RequestPrintDetail.class});
				errorCode = (String)processMethod.invoke(null, requestPrintDetail);
			} else if (requestPrintDetail.getPrinterType().equalsIgnoreCase(PRINTER_HODOO)){
				//Hodoo pci30c = new Hodoo();                            //************Hodoo printer to Develop******************
				//errorCode = pci30c.Process(requestPrintDetail);
			} else 
				errorCode = errorInternalDevice;
			if (errorCode==null||errorCode.equals(""))
				errorCode = SUCCESS;
		}

		catch (Exception e) {
			Updater.error("PrintAction", e);
			errorCode = e.getMessage();
		}
		Updater.info("PrintAction", "issueCard()  <EXIT>");
		return errorCode;
	}

	private boolean isEmpty(Object str) {

		if (str == null || ((String) str).length() == 0) {
			return true;

		}
		if ((String) str.toString().trim() == null) {

			return true;

		}

		else {

			return false;
		}

	}

	private boolean isDataValid(String header, String detail,
			RequestPrintDetail requestPrintDetail) {
		boolean result = false;
		Updater.info("PrintAction", "isDataValid()  <ENTER>");
		try {
			String[] headerList = header.split(",");
			if (headerList.length == 0)
				return result;
			if (isEmpty(headerList[0]))
				return result;
			requestPrintDetail.setRequestNo(headerList[0]);
			requestPrintDetail.setPrinterType(headerList[1]);
			requestPrintDetail.setPrinterIP(headerList[2]);
			requestPrintDetail.setPrinterKey(headerList[3]);
			requestPrintDetail.setTrack_1_Data(headerList[4]);
			requestPrintDetail.setTrack_2_Data(headerList[5]);
			requestPrintDetail.setTrack_3_Data(headerList[6]);
			requestPrintDetail.setPan(headerList[7]);
			String[] detailList = detail.split("#");
			if (detailList.length == 0)
				return result;
			String[] rowDetailList;
			CardObjectDetail cod;
			List<CardObjectDetail> lstRowDetail = new ArrayList<CardObjectDetail>();
			for (int i = 0; i < detailList.length; i++) {
				cod = new CardObjectDetail();
				rowDetailList = detailList[i].split(",");
				cod.setFieldName(rowDetailList[0]);
				cod.setFieldValue(rowDetailList[1]);
				cod.setFontName(rowDetailList[2]);
				cod.setFontSize(Integer.parseInt(rowDetailList[3]));
				cod.setFontStyle(rowDetailList[4]);
				cod.setColNum(Integer.parseInt(rowDetailList[5]));
				cod.setRowNum(Integer.parseInt(rowDetailList[6]));
				lstRowDetail.add(cod);

			}
			requestPrintDetail.setCardObjectDetail(lstRowDetail);
			result = true;

		} catch (Exception e) {
			result = false;
			Updater.error("PrintAction", e);
		}
		Updater.info("PrintAction", "isDataValid()  <EXIT>");
		return result;
	}
}
