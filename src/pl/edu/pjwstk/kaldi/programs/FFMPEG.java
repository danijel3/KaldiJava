package pl.edu.pjwstk.kaldi.programs;

import java.io.File;

import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

public class FFMPEG {

	public static void convertTo16k(File input, File output) {

		String[] cmd = new String[] { Settings.ffmpeg_bin.getAbsolutePath(),
				"-i", input.getAbsolutePath(), "-acodec", "pcm_s16le", "-ac",
				"1", "-ar", "16k", output.getAbsolutePath() };

		ProgramLauncher launcher = new ProgramLauncher(cmd);

		Log.verbose("FFMPEG: " + input.getAbsolutePath() + " -> "
				+ output.getAbsolutePath());
		launcher.run();
		Log.verbose("Done.");

		if (launcher.getReturnValue() != 0)
			throw new RuntimeException("FFMPEG Retval: " + launcher.getReturnValue());
	}

}
