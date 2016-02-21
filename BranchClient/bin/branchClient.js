var branchClient = {};
branchClient["result"] = null;
branchClient["appletCodeBase"] = 'http://172.17.19.147:9416/Content';

branchClient["issueCard"] = function(requestHeader, requestDetail, callback) {
	return branchClient.callAction(callback, 'PrintAction?callback=branchClient.actionCallback', {'requestHeader': requestHeader, 'requestDetail': requestDetail});
}

branchClient["getPhysicalId"] = function(callback) {
	return branchClient.callAction(callback, 'POSAction?method=getPhysicalId&callback=branchClient.actionCallback');
}

branchClient["commandPOS"] = function(command, callback) {
	return branchClient.callAction(callback, 'POSAction?method=commandPOS&callback=branchClient.actionCallback', command);
}

branchClient["callAction"] = function(callback, action, req) {
	branchClient["actionCallback"] = function(res) {
		callback(res);
	}
	branchClient["salamCallback"] = function(res) {
		if(res.result=":)"){
			branchClient.callBranchClient(60000, 'http://localhost:8080/'+ action, req);
		}else{
			branchClient.callApplet(callback, action, req);
		}
	}
	branchClient.callBranchClient(3000, 'http://localhost:8080/SalamAction?callback=branchClient.salamCallback', {}, 
			function() {branchClient.callApplet(callback, action, req);});
}

branchClient["callBranchClient"] = function(timeout, url, data, failCallback) {
	branchClient.result = null;
	$.ajax({
		type: 'POST',
		url: url,
		jsonp: false,
		contentType: "application/javascript",
		data: data,
		timeout: timeout,
		error: function(a,b,c) {
			if(failCallback instanceof Function){
				failCallback();
			}
		},
		dataType: 'script'
	});
}

branchClient["callApplet"] = function(callback, action, req) {
	var res = null;
	$("#AppletPrinter").remove();
	$(document.body).append(' <applet  id="AppletPrinter" code = "com.iac.applet.AppletDevices.class" ' 
			+ ' archive="appletDevices.jar,rxtx-2.1.7.jar,serialPort-941.0.0.2.jar,MCASmart.jar" width="0" height="0" '
			+ ' codebase="' + branchClient.appletCodeBase + '"> </applet> ');
	switch(action){
	case 'PrintAction?callback=branchClient.actionCallback':
		res = document.AppletPrinter.issueCard(req.requestHeader, req.requestDetail);
		break;
	case 'POSAction?method=getPhysicalId&callback=branchClient.actionCallback':
		res = document.AppletPrinter.getPhysicalId();
		break;
	case 'POSAction?method=commandPOS&callback=branchClient.actionCallback':
		res = document.AppletPrinter.commandPOS(req);
		break;
	}
	
	callback({"result": res});
}