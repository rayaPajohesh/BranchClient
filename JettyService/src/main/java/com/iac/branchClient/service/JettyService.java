package com.iac.branchClient.service;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iac.branchClient.BranchClientService;
import com.iac.branchClient.InterServiceMessage;
import com.iac.branchClient.Updater;

public class JettyService extends HttpServlet implements BranchClientService{
	private static final long serialVersionUID = 1L;
	static Server server;
	static HashMap<String, InterServiceMessage> actions = new HashMap<String, InterServiceMessage>();

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGetPost(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		doGetPost(request, response);
	}

	@SuppressWarnings("unchecked")
	private void doGetPost(HttpServletRequest request,	HttpServletResponse response) throws IOException {
		try{
			InterServiceMessage action = actions.get(request.getPathInfo().substring(1, request.getPathInfo().length()));
			String json = "";
			Map<String, String[]> parameters = request.getParameterMap();
			if(action!=null){
				Updater.info("JettyService", "received message " + mapToJson(parameters));
				Map<String, String[]> res = (Map<String, String[]>) action.send(parameters);
				json  = mapToJson(res);
			}else{
				json = "{\"errorMessage\":\"There is no action by name " + request.getPathInfo().substring(1) + ".\"}";
			}
			response.setContentType("application/javascript");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			out.print(parameters.get("callback")[0] + "(" + json + ");");
			out.flush();
			Updater.info("JettyService", "sent message " + json);
		}catch(Exception e){
			throw new IOException(e);
		}
	}
	
	String mapToJson(Map<String, String[]> map){
		HashMap<String, Object> tempMap = new HashMap<String, Object>();
		Set<String> keys = map.keySet();
		for (String key : keys) {
			if(map.get(key).length==1){
				tempMap.put(key, map.get(key)[0]);
			}else{
				tempMap.put(key, map.get(key));
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return (tempMap.isEmpty() ? "" : JsonUnflattener.unflatten(gson.toJson(tempMap)));
	}
		
	public void start(HashMap<String, Object> globalContext) {
		System.setOut(createLoggingProxy());
        System.setErr(createLoggingProxy());
		server = new Server(8080);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(new JettyService()), "/*");
		globalContext.put("JettyActions", actions);
		try {
			Updater.info("JettyService", "service (Jetty) is starting...");
			String[] classNameParts = SalamAction.class.getName().split("\\.");
			actions.put(classNameParts[classNameParts.length-1], new SalamAction());
			server.start();
			server.join();
		} catch (Exception e) {
			Updater.error("JettyService", e);
		}
	}

	public void stop(HashMap<String, Object> globalContext) {
		try {
			globalContext.remove("JettyActions");
			server.stop();
			Updater.info("JettyService", "service (Jetty) is stoped.");
		} catch (Exception e) {
			Updater.error("JettyService", e);
		}
	}
	
    private static PrintStream createLoggingProxy() {
		return new PrintStream(System.out) {
            public void print(final String message) {
                Updater.info("JettyService", message);
            }
        };
    }
    
    public static void main(String[] args) {
		HashMap<String, Object> globalContext = new HashMap<String, Object>();
		new JettyService().start(globalContext);
	}
}