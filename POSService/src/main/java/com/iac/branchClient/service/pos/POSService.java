package com.iac.branchClient.service.pos;


import java.io.File;
import java.util.HashMap;
import com.iac.branchClient.BranchClientService;
import com.iac.branchClient.InterServiceMessage;
import com.iac.branchClient.Updater;

public class POSService implements BranchClientService{
	public static String posHome = "C:\\BranchClient\\pos\\";
	private static final String repositoryUrl = "http://172.17.18.102:4040/ali/POS/";
	
	@SuppressWarnings("unchecked")
	public void start(HashMap<String, Object> globalContext) {
		HashMap<String, InterServiceMessage> actions = (HashMap<String, InterServiceMessage>) globalContext.get("JettyActions");
		String[] classNameParts = POSAction.class.getName().split("\\.");
		actions.put(classNameParts[classNameParts.length-1], new POSAction());
		install();
		Updater.info("POSService", "service (POS) is started");
	}

	@SuppressWarnings("unchecked")
	public void stop(HashMap<String, Object> globalContext) {
		HashMap<String, InterServiceMessage> actions = (HashMap<String, InterServiceMessage>) globalContext.get("JettyActions");
		String[] classNameParts = POSAction.class.getName().split("\\.");
		actions.remove(classNameParts[classNameParts.length-1]);
		Updater.info("POSService", "service (POS) is stopted");
	}
	
	private void install(){
		try{
			File printerHomeDir = new File(posHome);
			if (!printerHomeDir.exists()) {
				printerHomeDir.mkdir();
			}
			String rxtxSerialDllFile = "rxtxSerial64.dll";
			if(System.getenv("PROCESSOR_ARCHITECTURE").toLowerCase().equals("x86")){
				rxtxSerialDllFile = "rxtxSerial32.dll";
			}
			Updater.downloadFile(repositoryUrl + rxtxSerialDllFile, System.getenv("WINDIR") + File.separator + "System32" + File.separator + "rxtxSerial.dll", true);
			Updater.downloadFile(repositoryUrl + "config.properties", posHome + "config.properties", true);
		}catch(Exception e){
			Updater.error("POSService", e);
		}
	}
}