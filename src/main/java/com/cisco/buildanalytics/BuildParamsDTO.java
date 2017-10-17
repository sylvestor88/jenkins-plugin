package com.cisco.buildanalytics;

public class BuildParamsDTO {
	private String serverIp;
	private String buildStageType;
	private String jenkinsServerIp;
	private String prefixUser;
	private String buildUrl;
	private String fileName;
	
	public String getServerIp(){
		return serverIp;
	}
	
	public void setServerIp(String serverIp){
		this.serverIp = serverIp;
	}
	
	public String getBuildStageType(){
		return buildStageType;
	}
	
	public void setBuildStageType(String buildStageType){
		this.buildStageType = buildStageType;
	}
	
	public String getJenkinsServerIp(){
		return jenkinsServerIp;
	}
	
	public void setJenkinsServerIp(String jenkinsServerIp){
		this.jenkinsServerIp = jenkinsServerIp;
	}

	public String getPrefixUser(){
		return prefixUser;
	}
	
	public void setPrefixUser(String prefixUser){
		this.prefixUser = prefixUser;
	}
	
	public String getBuildUrl(){
		return buildUrl;
	}
	
	public void setBuildUrl(String buildUrl){
		this.buildUrl = buildUrl;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
}
