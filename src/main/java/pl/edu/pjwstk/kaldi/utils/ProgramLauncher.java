package pl.edu.pjwstk.kaldi.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;

public class ProgramLauncher implements Runnable {

	private CommandLine cmd;
	private DefaultExecutor exec = new DefaultExecutor();
	private OutputStream ostr = System.out;
	private OutputStream estr = System.err;
	private int retVal = -1;
	private boolean suppressOutput = false;
	private boolean running = false;
	private boolean async = false;
	private String addPath = null;
	private String lib_env = null;

	public ProgramLauncher(String[] cmd_arr) {
		cmd = new CommandLine(cmd_arr[0]);
		for (int i = 1; i < cmd_arr.length; i++)
			cmd.addArgument(cmd_arr[i]);
	}

	public void setLibraries(List<File> libraries) {
		lib_env = "LD_LIBRARY_PATH=";
		for (File lib : libraries) {
			if (lib_env.length() > 0)
				lib_env += ";";
			lib_env += lib.getAbsolutePath();
		}
	}

	public void addPath(File path) {
		if (addPath != null)
			addPath += ";";
		addPath += path.getAbsolutePath();
	}

	public void setStdoutStream(OutputStream ostr) {
		this.ostr = ostr;
	}

	public void setStderrStream(OutputStream estr) {
		this.estr = estr;
	}

	public void setStdoutFile(File stdout) throws FileNotFoundException {
		setStdoutStream(new FileOutputStream(stdout));
	}

	public void setCwd(File cwd) {
		exec.setWorkingDirectory(cwd);
	}

	public void setSuppressOutput(boolean suppressOutput) {
		this.suppressOutput = suppressOutput;
	}

	public void setAsynchronous(boolean async) {
		this.async = async;
	}

	public void run() {

		running = true;

		try {
			if (!suppressOutput) {
				PumpStreamHandler stream = new PumpStreamHandler(ostr, estr);
				exec.setStreamHandler(stream);
			}

			if (lib_env != null || addPath != null) {
				@SuppressWarnings("rawtypes")
				Map env = EnvironmentUtils.getProcEnvironment();
				if (lib_env != null)
					EnvironmentUtils.addVariableToEnvironment(env, lib_env);
				if (addPath != null) {
					String path = (String) env.get("PATH");
					if (path == null)
						path = (String) env.get("path");
					if (path == null)
						path = (String) env.get("Path");
					if (path == null)
						path = "";
					if (path.length() > 0 && !path.endsWith(";"))
						path += ";";
					path = "PATH=" + path + addPath;
					EnvironmentUtils.addVariableToEnvironment(env, path);
				}

				retVal = 0;

				if (async)
					exec.execute(cmd, env, blankHandler);
				else
					retVal = exec.execute(cmd, env);

			} else {

				retVal = 0;

				if (async)
					exec.execute(cmd, blankHandler);
				else
					retVal = exec.execute(cmd);
			}

		} catch (ExecuteException e) {
			// ignore
		} catch (Exception e) {
			Log.error("Running program: " + cmd, e);
		}

		running = false;

	}

	public int getReturnValue() {
		return retVal;
	}

	public boolean isRunning() {
		return running;
	}

	ExecuteResultHandler blankHandler = new ExecuteResultHandler() {
		public void onProcessFailed(ExecuteException e) {
		}

		public void onProcessComplete(int exitValue) {
		}
	};
}
