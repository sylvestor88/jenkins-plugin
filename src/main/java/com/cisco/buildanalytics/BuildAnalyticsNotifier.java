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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
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

	public boolean failBuild;
	public boolean uploadOnlyOnFail;
	public String serverIp;

	@DataBoundConstructor
	public BuildAnalyticsNotifier(String serverIp, boolean uploadOnlyOnFail, boolean failBuild) {
		this.failBuild = failBuild;
		this.serverIp = serverIp;
		this.uploadOnlyOnFail = uploadOnlyOnFail;
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

	private void uploadFile(File file, String ip, boolean success) {
		try {
			LOG.info("Trying to upload");
			MultiPartUtility mp = new MultiPartUtility(ip, CHARSET);
			mp.addFilePart("file", file);
			List<String> response = mp.finish();
			System.out.println("SERVER REPLIED:");

			for (String line : response) {
				System.out.println(line);
			}
		} catch (IOException e) {
			LOG.warning("ERROR while uploading " + e.toString());
		}
	}

	/**
	 * Using this only for testing 
	 **/
	private void test(Reader reader) {
		try {
			BufferedReader bf = new BufferedReader(reader);
			String curr;
			while ((curr = bf.readLine()) != null) {
				LOG.info("From reader" + curr);
			}
		} catch (IOException e) {
			LOG.info("AKK error occured " + e.toString());
		}

	}

	private boolean perform(Run<?, ?> run, TaskListener listener) {
		LOG.info("LOGSTASH performing");
		LOG.info("Val " + this.serverIp + this.uploadOnlyOnFail);
		File file = run.getLogFile();
		Result r = run.getResult();
		if (r != null) {
			LOG.info("Finished with Result:" + " :: " + r.toString());
			if (r.isCompleteBuild()) {
				LOG.info("Build was successful: " + r.toString());
			}

		} else {
			LOG.info("In case of failure" + r.toString());
		}
		if(!this.uploadOnlyOnFail){
			uploadFile(file, this.serverIp, r.isCompleteBuild());
		}else{
			LOG.info("Skipping upload as requested");
		}
		return !(failBuild);
	}

	public BuildStepMonitor getRequiredMonitorService() {
		// We don't call Run#getPreviousBuild() so no external synchronization
		// between builds is required
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
			return "Upload Logs";
		}
	}
}
