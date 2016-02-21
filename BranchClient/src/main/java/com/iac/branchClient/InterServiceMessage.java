package com.iac.branchClient;

import java.util.Map;

public interface InterServiceMessage {
	public Map<String, ?> send(Map<String, ?> request);
}
