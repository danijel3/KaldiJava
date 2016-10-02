package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;

import java.io.File;

public class Sox {

    private final static Logger logger = LoggerFactory.getLogger(Sox.class);

    public static void convert(File src_file, File dest_file) {

        String[] cmd = new String[]{"sox", src_file.getAbsolutePath(), "-c",
                "1", dest_file.getAbsolutePath(), "norm", "-3", "rate", "-h",
                "16k"};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        logger.trace("Coverting using SoX...");
        launcher.run();
        logger.trace("Done.");

    }

    public static void extract(File src_file, File dest_file,
                               double time_start, double time_end) {

        double duration = time_end - time_start;

        String[] cmd = new String[]{"sox", src_file.getAbsolutePath(),
                dest_file.getAbsolutePath(), "trim", "" + time_start,
                "" + duration};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        logger.trace("Extracting using SoX...");
        launcher.run();
        logger.trace("Done.");

    }
}
