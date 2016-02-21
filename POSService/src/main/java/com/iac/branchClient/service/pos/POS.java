package com.iac.branchClient.service.pos;

import gnu.io.CommPortIdentifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import com.iac.branchClient.Updater;
import com.tosan.serialport.*;
import com.tosan.serialport.Protocol.POSMessage;
import com.tosan.serialport.Protocol.TosanPOSMessage;

public class POS {

	private static final String SpliterError = ";";
	private static final String Success = "00";
	private static final String POSError = "6666";

	public static ArrayList<String> findComPorts() {
		ArrayList<String> comPorts = new ArrayList<String>();
		try{
			Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
	        while (ports.hasMoreElements()){
	            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();
	            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL){
	                comPorts.add(curPort.getName());
	            }
	        }
		}catch(Exception e){
			Updater.error("POS", e);
		}
        return comPorts;
	}
	
	public static String setComPort(String comPort, String configFile){
		String res = Success;
		Properties props = new Properties();
		FileInputStream in = null;
		FileOutputStream out = null;
        try {
			in = new FileInputStream(new File(configFile));
			props.load(in);
			props.setProperty("comport", comPort);
	        out = new FileOutputStream(new File(configFile));
			props.store(out, "");
		} catch (Exception e) {
			res = POSError;
			Updater.error("POS", e);
		} finally{
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				Updater.error("POS", e);
			}
		}
        return res;
	}
	
	public static String getPhysicalId() {
		String res = "";
		Updater.info("POS", "getPhysicalId  <ENTER>");
		try {
			res = sendToPOS("74126a10a106910101920101a106910102920140");
		} catch (Throwable e) {
			res = e.getMessage();
			Updater.error("POS", e);
		}
		Updater.info("POS", "getPhysicalId : " + res +	" <EXIT>");
		return res;
	}

	public static String commandPOS(String command) {
		String res = "";
		Updater.info("POS", "commandPOS  <ENTER>");
		try {
			res = sendToPOS(command);
		} catch (Throwable e) {
			res = e.getMessage();
			Updater.error("POS", e);
		}
		Updater.info("POS", "commandPOS : " + res +	" <EXIT>");
		return res;
	}

	public static String sendToPOS(String command) throws Throwable {
		String res = "";
		try {
			TosanPOSMessage requestMessage = new TosanPOSMessage();
			TosanPOSMessage responseMessage = new TosanPOSMessage();
			Object byaPOSRequestData = null;
			byaPOSRequestData = Utility.hexStringToByteArray(command);
			requestMessage.setData((byte[]) byaPOSRequestData);
			AppProperties.fileName = POSService.posHome + "config.properties";
			PortConfig portConfig = SerialPortHelper.getPortConfig();
			PortResult portResult = SerialPortHelper.SendAndReceive(
					(POSMessage) requestMessage, (POSMessage) responseMessage,
					(PortConfig) portConfig);

			if (portResult.getStatus() == Status.SUCCESS) {
				res = Success + SpliterError
						+ byteArrayToHexString(responseMessage.getData());
			} else {
				throw new Exception(portResult.getStatus().toString());
			}
		} catch (Exception e) {
			res = POSError + SpliterError + e.getMessage();
			Updater.error("POS", e);
		}
		return res;
	}

	public static String byteArrayToHexString(byte[] a) {
		if (a == null)
			return "";
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}

	public static String byteArrayToString(byte[] a) {
		String res = "";
		for (byte b : a)
			res += b;
		return res;
	}
}