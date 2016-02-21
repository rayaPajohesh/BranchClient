package com.iac.branchClient.service;

import java.util.HashMap;
import java.util.Map;

import com.iac.branchClient.InterServiceMessage;

public class SalamAction implements InterServiceMessage{
	
	public Map<String, ?> send(Map<String, ?> request) {
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("result", new String[]{":)"});
		return map;
	}
}
