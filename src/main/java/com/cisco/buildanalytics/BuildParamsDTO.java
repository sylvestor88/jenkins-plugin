package com.cisco.buildanalytics;

public class BuildParamsDTO {
	private String jenkinsServer;
	private String buildStageType;
	private String prefixUser;
	private String buildUrl;
	private String fileName;
	
	public String getJenkinsServer(){
		return jenkinsServer;
	}
	
	public void setJenkinsServer(String jenkinsServer){
		this.jenkinsServer = jenkinsServer;
	}
	
	public String getBuildStageType(){
		return buildStageType;
	}
	
	public void setBuildStageType(String buildStageType){
		this.buildStageType = buildStageType;
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
