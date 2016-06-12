package pl.edu.pjwstk.kaldi.programs;

import java.io.File;

import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

public class Python {

	public static void run(File script, String[] args) {

		String[] cmd = new String[args.length + 2];
		cmd[0] = Settings.python_bin.getAbsolutePath();
		cmd[1] = script.getAbsolutePath();
		int i = 2;
		for (String arg : args) {
			cmd[i] = arg;
			i++;
		}

		ProgramLauncher launcher = new ProgramLauncher(cmd);
		launcher.setStdoutStream(new Log.Stream());
		launcher.setStderrStream(new Log.Stream("ERR>>"));

		Log.verbose("Running python script: " + script.getName());
		launcher.run();
		Log.verbose("Done.");
	}
}
