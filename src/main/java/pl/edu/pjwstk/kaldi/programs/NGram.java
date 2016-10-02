package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.utils.LogStream;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

import java.io.File;
import java.io.FileNotFoundException;

public class NGram {

    private final static Logger logger = LoggerFactory.getLogger(NGram.class);
    private final static LogStream logger_stdout = new LogStream(logger);
    private final static LogStream logger_stderr = new LogStream(logger, "ERR>> ");

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

        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("Estimating N-Gram (MITLM)...");
        launcher.run();
        logger.trace("Done.");

    }

    public static void srilm_estimate(File input, File vocab, File model, int order) {

        String[] cmd = new String[]{Settings.ngram_count_bin.getAbsolutePath(), "-order", "" + order, "-unk",
                "-map-unk", "<UNK>", "-text", input.getAbsolutePath(), "-lm", model.getAbsolutePath(), "-write-vocab",
                vocab.getAbsolutePath(), "-wbdiscount", "-gt1min", "1", "-gt2min", "1", "-gt3min", "1"};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("Estimating N-Gram (SRILM)...");
        launcher.run();
        logger.trace("Done.");

    }

}
