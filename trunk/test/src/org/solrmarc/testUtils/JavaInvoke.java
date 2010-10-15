package org.solrmarc.testUtils;
/*
 * All source code and information in this file is made
 * available under the following licensing terms:
 *
 * Copyright (c) 2009, Palantir Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of Palantir Technologies, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */ 
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Specialized delegate for the invocation of Java processes.
 * 
 * @see ProcessBuilder
 * @author regs
 *
 */
public class JavaInvoke extends ProcessSpawner {

	static final Logger javaInvokeLog = LogManager.getLogger(JavaInvoke.class);
	
	/**
	 * Constructs a new JavaInvoker object. Any of the passed arguments, aside from classToInvoke, may be null.  
	 * @param classToInvoke Fully-qualified classname of the class to invoke as main in the spawned VM.
	 * Does not need to be loaded in this VM (but must be on the classpath of the spawned VM).
	 * @param workingDirectory Directory this should run in.
	 * @param javaProperties Map of Java options as defined by {@link System#getProperties()}.
	 * @param args Command line arguments to be passed to the invoked main.  These are not arguments to the VM
	 * but arguments to the Java class.  These will be the contents of the args[] array in the main method on 
	 * the invoked class.
	 * @param additionalClassPath additional classpath entries to be added to the beginning of the classpath
	 * (to allow for overriding).
	 * @param environmentToMerge Java environment entries to be merged (overwriting) with the current environment
	 * to be used as the environment for spawned processes.
	 */
	public JavaInvoke(String classToInvoke,
	                  File workingDirectory, 
	                  Map<String,String> javaProperties, 
	                  String[] args, 
	                  List<String> additionalClasspath,
	                  Map<String,String> environmentToMerge, 
	                  boolean includeExistingClassPath) 
    {
		super(workingDirectory, 
		      buildCommandLine(classToInvoke, javaProperties, new String[] {}, args), 
		      buildEnvironmentToMerge(additionalClasspath, environmentToMerge, includeExistingClassPath));
	}
	
	public static final String[] buildCommandLine(String classToInvoke,
	                                              Map<String,String> javaProperties,
	                                              String[] vmargs,
	                                              String[] processArgs) 
	{
		String[] javaSysProps = new String[0]; // reasonable default
		// construct the system properties 
		if(javaProperties != null && javaProperties.size() > 0) 
		{
			ArrayList<String> propList = new ArrayList<String>(javaProperties.size());
			for(Entry<String, String> javaProp : javaProperties.entrySet()) 
			{
				propList.add("-D" + javaProp.getKey() + "=" + javaProp.getValue());
			}
			javaSysProps = propList.toArray(javaSysProps);
		}
		
		if(vmargs == null) 
		{
			vmargs = new String[]{};
		}
		if(processArgs == null) 
		{
			processArgs = new String[]{};
		}
		
		// construct the command line
		final String javaPath = System.getProperty("java.home") + 
								File.separator + "bin" + 
								File.separator + "java" + 
								(File.separator.equals("\\") ? ".exe" : "");
		final int cmdLineLength = vmargs.length + 
								  processArgs.length + 
								  javaSysProps.length + 2;
		final String[] cmdarray = new String[cmdLineLength];

		
		// write out the command line
		final int javaPosition = 0;
		final int vmargsPosition = javaPosition + 1;
		final int javaSysPropsPosition = vmargsPosition + vmargs.length;
		final int classPosition = javaSysPropsPosition + javaSysProps.length;
		final int processArgsPosition = classPosition + 1;
		cmdarray[javaPosition] = javaPath;
		System.arraycopy(vmargs,0, cmdarray, vmargsPosition, vmargs.length);
		System.arraycopy(javaSysProps, 0, cmdarray, javaSysPropsPosition, javaSysProps.length);
		cmdarray[classPosition] = classToInvoke;
		System.arraycopy(processArgs, 0, cmdarray, processArgsPosition, processArgs.length);
		
		return cmdarray;
	}
	
	/**
	 * Merges with existing classpath (that this VM was spawned with), and places it into the 
	 * CLASSPATH environment variable (to avoid command line escaping issues).
	 * 
	 * @param additionalClasspath - classpath entries to place at the front of the classpath.
	 * @param environment - entries in the 
	 * @return
	 */
	public static final Map<String,String> buildEnvironmentToMerge(List<String> additionalClasspath,
	                                                               Map<String,String> environment,
	                                                               boolean includeExistingClassPath) 
	{
	    String cp = null;
	    if (includeExistingClassPath)
	    {
	        cp = System.getProperty("java.class.path");
	    }
	    StringBuilder cpath = new StringBuilder(cp == null ? "" : cp);

	    // deal with additional classpath elements (prepend for overrides)
		if(additionalClasspath != null && additionalClasspath.size() > 0) 
		{
			for(String cpathEntry : additionalClasspath) 
			{
				cpath.insert(0,File.pathSeparatorChar).insert(0,cpathEntry);
			}
		}
		if(environment == null) 
		{
			environment = new HashMap<String,String>();
		}
		environment.put("CLASSPATH",cpath.toString());
		
		if(javaInvokeLog.isInfoEnabled()) 
		{
			javaInvokeLog.info("CLASSPATH=" + cpath.toString());
		}
		return environment;
	}
}
