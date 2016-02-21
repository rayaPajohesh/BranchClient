package com.iac.branchClient.service.printer;


import java.io.File;
import java.util.HashMap;
import com.iac.branchClient.BranchClientService;
import com.iac.branchClient.InterServiceMessage;
import com.iac.branchClient.Updater;

public class PrinterService implements BranchClientService{
	public static final String printerHome = "C:\\BranchClient\\printer\\";
	private static final String repositoryUrl = "http://172.17.18.102:4040/ali/Printer/";
	
	@SuppressWarnings("unchecked")
	public void start(HashMap<String, Object> globalContext) {
		HashMap<String, InterServiceMessage> actions = (HashMap<String, InterServiceMessage>) globalContext.get("JettyActions");
		String[] classNameParts = PrintAction.class.getName().split("\\.");
		actions.put(classNameParts[classNameParts.length-1], new PrintAction());
		install();
		Updater.info("PrinterService", "service (Printer) is started");
	}

	@SuppressWarnings("unchecked")
	public void stop(HashMap<String, Object> globalContext) {
		HashMap<String, InterServiceMessage> actions = (HashMap<String, InterServiceMessage>) globalContext.get("JettyActions");
		String[] classNameParts = PrintAction.class.getName().split("\\.");
		actions.remove(classNameParts[classNameParts.length-1]);
		Updater.info("PrinterService", "service (Printer) is stoped");
	}
	
	private void install(){
		try{
			File printerHomeDir = new File(printerHome);
			if (!printerHomeDir.exists()) {
				printerHomeDir.mkdir();
			}
			String iomemTargetPath = "";
			if(new File(System.getenv("WINDIR") + File.separator + "SysWOW64").exists()){
				iomemTargetPath = System.getenv("WINDIR") + File.separator + "SysWOW64" + File.separator + "iomem.dll";
			}else{
				iomemTargetPath = System.getenv("WINDIR") + File.separator + "System32" + File.separator + "iomem.dll";
			}
			Updater.downloadFile(repositoryUrl + "evolis/iomem.dll", iomemTargetPath, true);
			Updater.downloadFile(repositoryUrl + "mabna/MCASmart.jar", printerHome + "MCASmart.jar", true);
			Updater.downloadFile(repositoryUrl + "evolis/iomemJNI.dll", printerHome + "iomemJNI.dll", true);
			Updater.downloadFile(repositoryUrl + "evolis/prn_adapter_2.0.dll", printerHome + "prn_adapter_2.0.dll", true);
		}catch(Exception e){
			Updater.error("PrinterService", e);
		}
	}
}