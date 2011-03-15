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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ProcessSpawner {

//	static final Logger log = LogManager.getLogger(ProcessSpawner.class);
	
	ProcessBuilder pb = null;
    OutputPiper in = null;
    OutputPiper out = null;
	OutputPiper err = null;
	Process p = null;
	String[] args;

	/**
	 * @param workingDirectory Directory this should run in.
	 * @param args Command line to be executed 
	 * @param environmentToMerge environment entries to be merged (overwriting) with the current environment
	 * to be used as the environment for spawned processes.
	 */
	public ProcessSpawner(File workingDirectory, String[] cmdarray,Map<String,String> environmentToMerge) {
		pb = new ProcessBuilder();
		// set the working directory
		if(workingDirectory != null) {
			pb.directory(workingDirectory);	
		}
		pb.command(cmdarray);
	//	if(log.isInfoEnabled()) {
			StringBuilder cmdLine = new StringBuilder();
			for(String token : cmdarray) {
				cmdLine.append(token).append(" ");
			}
	//		log.info("Build process spawner for the following command line:");
//            log.info(cmdLine.toString());
//            System.out.println("starting process with command line: "+ cmdLine.toString());
//		}
		// deal with the environment
		Map<String,String> env = pb.environment();
		if(environmentToMerge != null && environmentToMerge.size() > 0) {
			env.putAll(environmentToMerge);
		}
	
//		if(log.isDebugEnabled()) {
//			log.debug("Environment for new processses: \n");
//			for(Map.Entry<String,String> envEntry : env.entrySet()) {
//				log.debug("\t" + envEntry.getKey() + "\t=\t" + envEntry.getValue() );
//			}
//		}
	}

	@SuppressWarnings("unused")
	private ProcessSpawner() {/**/}

    public static class OutputPiper extends Thread  {
        InputStream in;
        PrintStream out;
        OutputStream outraw;
        String tag = null;

        public OutputPiper(String tag, InputStream in, PrintStream out) {
            this.in = in;
            this.out = out;
            this.tag = tag; 
            // make sure that we don't keep the VM alive
            this.setDaemon(true);
            this.setName("OutputPiper-" + tag);
            out.println("Starting output piper for tag: " + tag);
            this.start();
        }
        
        public OutputPiper(String tag, InputStream in, OutputStream out) {
            this.in = in;
            this.outraw = out;
            this.tag = tag; 
            // make sure that we don't keep the VM alive
            this.setDaemon(true);
            this.setName("OutputPiper-" + tag);
          //  out.println("Starting output piper for tag: " + tag);
            this.start();
        }

        @Override
        public void run() 
        {
            if (in == null)
            {
                // do nothing, be happy
            }
            else if (outraw != null)
            {
                try {
                    BufferedInputStream is = new BufferedInputStream(in);
                    int charRead;
                    do {
                        charRead = is.read();
                        if(charRead != -1) 
                        {
                            outraw.write(charRead);
                        }
                    } while(charRead != -1);
                    outraw.flush();
                    outraw.close();
                }
                catch (Exception e) 
                {
                    System.err.println("Error reading from piped input " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }
            else
            {
                try {
            
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line = null;
                    do {
                        line = reader.readLine();
                        if(line != null) 
                        {
                            out.println(tag + ": " + line);
                        }
                    } while(line != null);
                }
                catch (Exception e) {
                    //
                }
                out.println("Output piper exiting for tag: " + tag);
                out.close();
            }
        }

        public static OutputPiper createOutputPiper(String tag, InputStream in, PrintStream out) 
        {
            OutputPiper rc = new OutputPiper(tag, in, out);
            return rc;
        }
        public static OutputPiper createRawOutputPiper(String tag, InputStream in, OutputStream out) 
        {
            OutputPiper rc = new OutputPiper(tag, in, out);
            return rc;
        }
    }  
    
//    public static class InputPiper extends Thread  {
//        InputStream in;
//        OutputStream out;
//        String tag = null;
//
//        public InputPiper(String tag, InputStream in, OutputStream out) {
//            this.in = in;
//            this.out = out;
//            this.tag = tag; 
//            // make sure that we don't keep the VM alive
//            this.setDaemon(true);
//            this.setName("InputPiper-" + tag);
//        //    out.println("Starting input piper for tag: " + tag);
//            this.start();
//        }
//
//        @Override
//        public void run() 
//        {
//            try {
//                BufferedInputStream is = new BufferedInputStream(in);
//                int charRead;
//                do {
//                    charRead = is.read();
//                    if(charRead != -1) 
//                    {
//                        out.write(charRead);
//                    }
//                } while(charRead != -1);
//            }
//            catch (Exception e) {
//                //
//            }
//          //  out.println("Output piper exiting for tag: " + tag);
//        }
//
//        public static InputPiper createInputPiper(String tag, InputStream in, OutputStream out) 
//        {
//            InputPiper rc = new InputPiper(tag, in, out);
//            return rc;
//        }
//    }       

	public List<String> command() {
		return pb.command();
	}

	public File directory() {
		return pb.directory();
	}

	public ProcessBuilder directory(File directory) {
		return pb.directory(directory);
	}

	public Map<String, String> environment() {
		return pb.environment();
	}

	public boolean redirectErrorStream() {
		return pb.redirectErrorStream();
	}

	public ProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
		return pb.redirectErrorStream(redirectErrorStream);
	}

	public Process start() throws IOException {
		p = pb.start();
		return p;
	}		

    /**
     * Like calling start, but uses {@link OutputPiper} classes
     * to redirect ouptut back to this JVM's console.
     * 
     * @param tag to tag the Outpiper's output with
     * @return
     * @throws IOException
     */
    public Process startStdinStderrInstance(String tag) throws IOException {
        start();
        out = OutputPiper.createOutputPiper(tag+ "-stdout", p.getInputStream(), System.out);
        err = OutputPiper.createOutputPiper(tag+ "-stderr", p.getErrorStream(), System.err);
        return p;
    }
    
    /**
     * Like calling start, but uses {@link OutputPiper} classes
     * to redirect to the provided output streams
     * 
     * @param tag to tag the Outpiper's output with
     * @return
     * @throws IOException
     */
    public Process startStdinStderrInstance(String tag, OutputStream outRedirect, OutputStream errRedirect) throws IOException 
    {
        start();
        out = OutputPiper.createRawOutputPiper(tag+ "-stdout", p.getInputStream(), outRedirect);
        if (errRedirect != null)  err = OutputPiper.createRawOutputPiper(tag+ "-stderr", p.getErrorStream(), errRedirect);
        return p;
    }
    
    /**
     * Like calling start, but uses {@link OutputPiper} classes
     * to redirect to the provided output streams
     * 
     * @param tag to tag the Outpiper's output with
     * @return
     * @throws IOException
     */
    public Process startStdinStdoutStderrInstance(String tag, InputStream inRedirect, OutputStream outRedirect, OutputStream errRedirect) throws IOException 
    {
        start();
        in = OutputPiper.createRawOutputPiper(tag+ "-stdin", inRedirect, p.getOutputStream());
        out = OutputPiper.createRawOutputPiper(tag+ "-stdout", p.getInputStream(), outRedirect);
        if (errRedirect != null)  err = OutputPiper.createRawOutputPiper(tag+ "-stderr", p.getErrorStream(), errRedirect);
        return p;
    }
    
	public void waitForProcess() throws Exception {
		if(p != null) {
			try {
				if(out != null) {
					out.join();
				}
				if(err != null) {
					err.join();
				}
				p.waitFor();
			} catch (InterruptedException e) {
				throw new Exception("Interrupted while waiting for process to exit",e);
			}
		} else {
			throw new Exception("Process not yet started!");
		}
	}

	public static String exec(String cmdLine) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(baos);
		String[] cmdArray = cmdLine.split("\\s+");
		ProcessSpawner ps = new ProcessSpawner(null,cmdArray,null);
		Process p = ps.start();
		OutputPiper pipe = OutputPiper.createOutputPiper(null, p.getInputStream(), out);
		pipe.join();
		p.waitFor();
		out.close();
		byte[] bytes = baos.toByteArray();
		return new String(bytes);
	}
}

