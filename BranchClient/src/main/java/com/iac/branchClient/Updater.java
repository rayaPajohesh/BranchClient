package com.iac.branchClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

public class Updater{

	final static HashMap<String, Logger> loggers = new HashMap<String, Logger>();
	private static Properties properties = new Properties();
	private static HashMap<String, String> localRepo = new HashMap<String, String>();
	private static HashMap<String, Object> globalContext = new HashMap<String, Object>();
	private static HashMap<String, URLClassLoader> classLoaders = new HashMap<String, URLClassLoader>();
	private static HashMap<String, ArrayList<Object>> objectsInClassLoaders = new HashMap<String, ArrayList<Object>>();

	public static void main(String[] args) throws Exception {
		new File(System.getenv().get("TEMP") + File.separator + "BranchClient").mkdir();
//		System.setErr(new PrintStream(new File("error.log")));
		boolean useDownloadedRepos = true;
		while (true) {
			try {
				loadProperties();
				Thread.sleep(Long.valueOf(properties.getProperty("update.delay")));
				loadMainRepository(downloadRepo(properties.getProperty("repository.path"), useDownloadedRepos), useDownloadedRepos);
				useDownloadedRepos = false;
			} catch (Exception e) {
				error("Updater", e);
			}
		}
	}

	private static void loadProperties() throws Exception {
		properties.clear();
		properties.load(new FileInputStream(new File("config.properties").getAbsolutePath()));
	}

	private static void loadMainRepository(String[] repoURLs, boolean useDownloadedRepos) throws Exception {
		String[] repoURLsWithoutStartSign = restartReposIfNeeded(repoURLs);
		for (String repoURL : repoURLsWithoutStartSign) {
			if(repoURL.endsWith(".repo")){
				String repoName = repoURL.split("/")[repoURL.split("/").length-1].toLowerCase();
				try {
					String repoVersion = repoURL.split("/")[repoURL.split("/").length-2].toLowerCase();
					String existingRepoVersion = null;
					if(localRepo.keySet().contains(repoName)){
						existingRepoVersion = localRepo.get(repoName).split("/")[localRepo.get(repoName).split("/").length-2];
					}
					if(existingRepoVersion==null || !existingRepoVersion.equals(repoVersion)){
						loadRepository(repoName, repoURL, useDownloadedRepos);
					}
				} catch (Exception e) {
					error("Updater", e);
					shutdownClassLoader(repoName);
				}
			}
		}
	}

	private static String[] restartReposIfNeeded(String[] repoURLs) throws Exception {
		for (int i=0; i<repoURLs.length; i++) {
			if(repoURLs[i].endsWith(".repo") || repoURLs[i].endsWith(".repo*")){
				if(repoURLs[i].endsWith(".repo*")){
					repoURLs[i] = repoURLs[i].substring(0, repoURLs[i].length()-2);
					String repoName = repoURLs[i].split("/")[repoURLs[i].split("/").length-1].toLowerCase();
					shutdownClassLoader(repoName);
				}
			}
		}
		return repoURLs;
	}

	private static void loadRepository(String repoName, String repoURL, boolean useDownloadedRepos) throws Exception {
		ArrayList<String> jarFiles = new ArrayList<String>();
		String[] repo = downloadRepo(repoURL, useDownloadedRepos);
		for (String entry : repo) {
			if(entry.endsWith(".jar")){
				String jarTargetPath = getJarTargetPath(repoURL, entry);
				downloadFile(entry, jarTargetPath, true);
				jarFiles.add(jarTargetPath);
			}
		}
		URL[] classLoaderUrls = new URL[jarFiles.size()];
		ArrayList<String> classNames = new ArrayList<String>();
		for (int i=0; i<jarFiles.size(); i++) {
			classLoaderUrls[i] = new URL("file:/" + new File(jarFiles.get(i)).getAbsolutePath());
			JarFile jarFile = new JarFile(jarFiles.get(i));
			Enumeration<JarEntry> entries = jarFile.entries();
		    while (entries.hasMoreElements()) {
		    	String entryName = entries.nextElement().getName();
		    	if(entryName.toLowerCase().endsWith(".class")){
		    		classNames.add(entryName.split("\\.")[0].replaceAll("/", "."));
		    	}
		    }
		    jarFile.close();
		}
		shutdownClassLoader(repoName);
		ArrayList<String> serviceClassNames = getServiceClassNames(classLoaderUrls, classNames);
		URLClassLoader classLoader = startClassLoader(repoName, classLoaderUrls, serviceClassNames);
		classLoaders.put(repoName, classLoader);
		localRepo.put(repoName, repoURL);
		info("Updater", "repository (" + repoURL + ") is loaded.");
	}

	private static ArrayList<String> getServiceClassNames(URL[] classLoaderUrls, ArrayList<String> classNames) throws Exception {
		ArrayList<String> serviceClassNames = new ArrayList<String>();
		URLClassLoader tempClassLoader = new URLClassLoader(classLoaderUrls);
		for (String className : classNames) {
			Class<?> cls;
			try {
				cls = tempClassLoader.loadClass(className);
				if(Arrays.asList(cls.getInterfaces()).contains(BranchClientService.class)){
					serviceClassNames.add(className);
				}
			} catch (NoClassDefFoundError e) {
				//NO OPERATION
			}
		}
		tempClassLoader.close();
		tempClassLoader = null;
		return serviceClassNames;
	}

	private static URLClassLoader startClassLoader(String classLoaderName, URL[] classLoaderUrls, ArrayList<String> classNames) throws Exception {
		ArrayList<Object> objects = null;
		if(objectsInClassLoaders.get(classLoaderName)==null){
			objects = new ArrayList<Object>();
			objectsInClassLoaders.put(classLoaderName, objects);
		}else{
			objects = objectsInClassLoaders.get(classLoaderName);
		}
		URLClassLoader classLoader = new URLClassLoader(classLoaderUrls);
		for (String className : classNames) {
			Class<?> cls = classLoader.loadClass(className);
			if(Arrays.asList(cls.getInterfaces()).contains(BranchClientService.class)){
				Constructor<?> constructor = cls.getConstructor();
				Object object = constructor.newInstance();
				objects.add(object);
				final BranchClientService service = (BranchClientService)object;
				Class<?> tcls = classLoader.loadClass("java.lang.Thread");
				Constructor<?> tconstructor = tcls.getConstructor(Runnable.class);
				Object tobject = tconstructor.newInstance(new Runnable() {
					@Override
					public void run() {
						service.start(globalContext);
					}
				});
				((Thread)tobject).start();
				Thread.sleep(5000);
			}
		}
		return classLoader;
	}

	private static void shutdownClassLoader(String classLoaderName) throws Exception {
		URLClassLoader classLoader = classLoaders.get(classLoaderName);
		if (classLoader != null) {
			ArrayList<Object> objects = objectsInClassLoaders
					.get(classLoaderName);
			for (Object object : objects) {
				if (Arrays.asList(object.getClass().getInterfaces()).contains(
						BranchClientService.class)) {
					BranchClientService service = (BranchClientService) object;
					service.stop(globalContext);
				}
				object = null;
			}
			classLoader.close();
			objects.clear();
			classLoader = null;
			classLoaders.remove(classLoaderName);
			localRepo.remove(classLoaderName);
			info("Updater", "repository (" + classLoaderName + ") is shutdown.");
		}
	}

	private static String[] downloadRepo(String repoURL, boolean useDownloadedRepo) throws Exception {
		String targetPath =  getRepoTargetPath(repoURL);
		downloadFile(repoURL, targetPath , useDownloadedRepo);
		byte[] encoded = Files.readAllBytes(Paths.get(targetPath));
		String[] repo = new String(encoded).replaceAll("\r", "").split("\n");
		return repo;
	}

	public static void downloadFile(String sourcePath, String targetPath, boolean useDownloadedFile) throws Exception {
		if(!useDownloadedFile || !new File(targetPath).exists()){
			URL website = new URL(sourcePath);
			ReadableByteChannel readableByteChannel = Channels.newChannel(website.openStream());
			FileOutputStream fileOutputStream = new FileOutputStream(targetPath);
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			fileOutputStream.close();
		}
	}
	
	private static String getRepoTargetPath(String repoSourcePath){
		String targetPath = "";
		if(repoSourcePath.endsWith(".repo*")){
			repoSourcePath = repoSourcePath.substring(0, repoSourcePath.length()-2);
		}
		String[] parts = repoSourcePath.split("/");
		String repoName = parts[parts.length-1].toLowerCase();
		String repoVersion = parts[parts.length-2].toLowerCase();
		targetPath = System.getenv().get("TEMP") + File.separator + "BranchClient" + File.separator + repoVersion + "_" + repoName ;
		return targetPath;
	}
	
	private static String getJarTargetPath(String repoSourcePath, String jarSourcePath){
		String targetPath = "";
		if(repoSourcePath.endsWith(".repo*")){
			repoSourcePath = repoSourcePath.substring(0, repoSourcePath.length()-2);
		}
		String repoName = repoSourcePath.split("/")[repoSourcePath.split("/").length-1].toLowerCase();
		String repoVersion = repoSourcePath.split("/")[repoSourcePath.split("/").length-2].toLowerCase();
		String jarName = jarSourcePath.split("/")[jarSourcePath.split("/").length-1].toLowerCase();
		String jarVersion = jarSourcePath.split("/")[jarSourcePath.split("/").length-2].toLowerCase();
		targetPath = System.getenv().get("TEMP") + File.separator + "BranchClient" + 
				File.separator + repoVersion + "_" + repoName + "_" + jarVersion + "_" + jarName;
		return targetPath;
	}
	
	public static void info(String category, String message){
		Logger logger = loggers.get(category);
		if(logger==null){
			logger = Logger.getLogger(category);
			loggers.put(category, logger);
		}
		logger.info(message);
	}
	
	public static void error(String category, Throwable throwable){
		Logger logger = loggers.get(category);
		if(logger==null){
			logger = Logger.getLogger(category);
			loggers.put(category, logger);
		}
		logger.error(throwable.getMessage(), throwable);
	}
}
