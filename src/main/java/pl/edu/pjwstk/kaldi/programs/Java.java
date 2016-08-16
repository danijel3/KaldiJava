package pl.edu.pjwstk.kaldi.programs;

import java.io.File;
import java.util.List;

import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;

public class Java {

	private static String sep = ":";

	public static void java(String class_name, String[] args,
			List<File> cp_files, boolean async) {

		String class_path = "";
		for (File file : cp_files) {
			if (!class_path.isEmpty())
				class_path += sep;
			class_path += file.getAbsolutePath();
		}

		String[] cmd_start = new String[] { "java", "-Dfile.encoding=UTF-8",
				"-cp", class_path, class_name };
		String cmd[] = new String[cmd_start.length + args.length];

		for (int i = 0; i < cmd_start.length ; i++)
			cmd[i] = cmd_start[i];
		for (int i = 0; i < args.length; i++)
			cmd[cmd_start.length + i] = args[i];

		ProgramLauncher launcher = new ProgramLauncher(cmd);
		launcher.setSuppressOutput(true);
		launcher.setAsynchronous(async);

		Log.verbose("Running Java: " + class_name);
		launcher.run();
		Log.verbose("Done.");
	}

}
