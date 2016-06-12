package pl.edu.pjwstk.kaldi.programs;

import java.io.File;
import java.io.FileNotFoundException;

import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

public class NGram {

    public static void test_mitlm() throws FileNotFoundException {
        if (!Settings.estimate_ngram_bin.exists())
            throw new
                    FileNotFoundException(Settings.estimate_ngram_bin.getAbsolutePath()
            );
    }

    public static void test_srilm() throws FileNotFoundException {
        if (!Settings.ngram_count_bin.exists())
            throw new FileNotFoundException(Settings.ngram_count_bin.getAbsolutePath());
    }

    public static void estimate(File input, File vocab, File model, int order) {

        String[] cmd = new String[]{Settings.estimate_ngram_bin.getAbsolutePath(), "-t", input.getAbsolutePath(),
                "-o", "" + order, "-wv", vocab.getAbsolutePath(), "-wl", model.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        launcher.setStdoutStream(new Log.Stream());
        launcher.setStderrStream(new Log.Stream("ERR>>"));

        Log.verbose("Estimating N-Gram (MITLM)...");
        launcher.run();
        Log.verbose("Done.");

    }

    public static void srilm_estimate(File input, File vocab, File model, int order) {

        String[] cmd = new String[]{Settings.ngram_count_bin.getAbsolutePath(), "-order", "" + order, "-unk",
                "-map-unk", "<UNK>", "-text", input.getAbsolutePath(), "-lm", model.getAbsolutePath(), "-write-vocab",
                vocab.getAbsolutePath(), "-wbdiscount", "-gt1min", "1", "-gt2min", "1", "-gt3min", "1"};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        launcher.setStdoutStream(new Log.Stream());
        launcher.setStderrStream(new Log.Stream("ERR>>"));

        Log.verbose("Estimating N-Gram (SRILM)...");
        launcher.run();
        Log.verbose("Done.");

    }

}
