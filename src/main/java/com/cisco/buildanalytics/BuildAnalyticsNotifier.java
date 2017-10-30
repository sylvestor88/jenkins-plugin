/*
 * The MIT License
 *
 * Copyright 2014 Rusty Gerard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cisco.buildanalytics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;

public class BuildAnalyticsNotifier extends Notifier implements SimpleBuildStep {

	private static final Logger LOG = Logger.getLogger(BuildAnalyticsNotifier.class.getName());
	private static String CHARSET = "UTF-8";

	public String serverIp;
	public String buildStageType;
	public String filebeatsDirectory;
	public String userPrefix;
	public String jenkinsServerIp;
	public boolean uploadOnlyOnFail;
	public boolean failBuild;

	@DataBoundConstructor
	public BuildAnalyticsNotifier(String serverIp, String buildStageType, String filebeatsDirectory, String userPrefix,
			String jenkinsServerIp, boolean uploadOblyOnFail, boolean failBuild) {
		this.serverIp = serverIp;
		this.buildStageType = buildStageType;
		this.filebeatsDirectory = filebeatsDirectory;
		this.userPrefix = userPrefix;
		this.jenkinsServerIp = jenkinsServerIp;
		this.uploadOnlyOnFail = uploadOblyOnFail;
		this.failBuild = failBuild;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
		return perform(build, listener);
	}

	@Override
	public boolean needsToRunAfterFinalized() {
		return true;
	}

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
			throws InterruptedException, IOException {
		if (!perform(run, listener)) {
			run.setResult(Result.FAILURE);
		}
	}

	private void createSymbolicLink(Path src, Path target) {
		LOG.info("Creating symbolic link from " + target + " to " + src);
		try {
			Files.createSymbolicLink(src, target);
		} catch (IOException e) {
			LOG.info(e.toString());
		}
	}
	
	private void invokeAnalyticsAPI(String buildUrl, String filename){
		//build DTO and invoke REST API here
		
	}

	private boolean perform(Run<?, ?> run, TaskListener listener) {
		LOG.info("Build Analytics Plugin - Running");
		LOG.info("Parameters " + this.serverIp + this.uploadOnlyOnFail + this.buildStageType + this.jenkinsServerIp
				+ this.userPrefix);

		boolean success = false;
		Result r = run.getResult();
		if (r != null) {
			success = r.isCompleteBuild();
		} else {
			LOG.info("Result is null" + r.toString());
		}

		if (!this.uploadOnlyOnFail || (this.uploadOnlyOnFail && !success)) {
			File file = run.getLogFile();
			String filename = this.userPrefix + "-x-" + this.buildStageType + "-x-" + run.getId();
			Path newLink = Paths.get(this.filebeatsDirectory + "/" + filename + ".log");
			Path target = Paths.get(file.getAbsolutePath());
			createSymbolicLink(newLink, target);
			
			String buildUrl = run.getUrl();
			invokeAnalyticsAPI(buildUrl, filename);
		} else {
			LOG.info("Skipping upload as requested");
		}

		return !(failBuild);
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public Descriptor getDescriptor() {
		return (Descriptor) super.getDescriptor();
	}

	@Extension
	@Symbol("logstashSend")
	public static class Descriptor extends BuildStepDescriptor<Publisher> {

		@Override
		public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
			return true;
		}

		public String getDisplayName() {
			return "Upload Logs For Analysis";
		}
	}
}
