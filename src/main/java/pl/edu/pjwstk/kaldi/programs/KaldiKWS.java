package pl.edu.pjwstk.kaldi.programs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

public class KaldiKWS {

	public static void test() throws FileNotFoundException {
		if (!Settings.kaldi_kws_bin.exists())
			throw new FileNotFoundException(Settings.kaldi_kws_bin.getAbsolutePath());
	}

	public static void get_vocab(File lattice, File vocab) throws IOException {

		String[] cmd = new String[] { Settings.kaldi_kws_bin.getAbsolutePath(), lattice.getAbsolutePath(),
				vocab.getAbsolutePath() };

		ProgramLauncher launcher = new ProgramLauncher(cmd);

		Log.verbose("KWS generating vocab: " + lattice.getAbsolutePath() + " -> " + vocab.getAbsolutePath());
		launcher.run();
		Log.verbose("Done.");

	}

	public static void detect(File lattice, File keywords, File dict, File out) throws IOException {

		String[] cmd = new String[] { Settings.kaldi_kws_bin.getAbsolutePath(), lattice.getAbsolutePath(),
				keywords.getAbsolutePath(), dict.getAbsolutePath() };

		ProgramLauncher launcher = new ProgramLauncher(cmd);

		launcher.setStdoutFile(out);

		Log.verbose("KWS detecting keywords: " + lattice.getAbsolutePath() + " + " + keywords.getAbsolutePath() + " + "
				+ dict.getAbsolutePath() + " -> " + out.getAbsolutePath());
		launcher.run();
		Log.verbose("Done.");

	}

	public static void main(String[] args) {
		try {
			Locale.setDefault(Locale.ENGLISH);

			Log.init("debug", true);

			get_vocab(new File("/home/guest/Desktop/lucas_kws/paris/130514_NWSU_120A0_O.txt"),
					new File("/home/guest/Desktop/lucas_kws/paris/vocab.txt"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
