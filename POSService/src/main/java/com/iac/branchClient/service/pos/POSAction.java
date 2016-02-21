package com.iac.branchClient.service.pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.iac.branchClient.InterServiceMessage;

public class POSAction implements InterServiceMessage{
	
	public Map<String, ?> send(Map<String, ?> request) {
		Map<String, String[]> map = new HashMap<String, String[]>();
		String res = "";
		if(((String[])request.get("method"))[0].equals("getPhysicalId")){
			res  = POS.getPhysicalId();
		}else if(((String[])request.get("method"))[0].equals("commandPOS")){
			res = POS.commandPOS(((String[])request.get("command"))[0]);
		}else if(((String[])request.get("method"))[0].equals("findComPorts")){
			ArrayList<String> portsArray = POS.findComPorts();
			if(!portsArray.isEmpty()){
				String[] ports = new String[portsArray.size()];
				for (int i=0; i<ports.length; i++) {
					ports[i] = portsArray.get(i);
				}
				res = "00";
				map.put("comPorts", ports);
			}else{
				res = "6666";
			}
		}else if(((String[])request.get("method"))[0].equals("setComPort")){
			res = POS.setComPort(((String[])request.get("method"))[0], POSService.posHome + "config.properties");
		}else{
			res = "6666";
		}
		map.put("result", new String[]{res});
		return map;
	}
	
}
