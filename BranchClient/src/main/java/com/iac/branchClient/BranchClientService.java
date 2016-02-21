package com.iac.branchClient;

import java.util.HashMap;

public interface BranchClientService {
	public void start(HashMap<String, Object> globalContext);
	public void stop(HashMap<String, Object> globalContext);
}
