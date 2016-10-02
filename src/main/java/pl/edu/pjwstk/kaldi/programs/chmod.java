package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.KaldiMain;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;

import java.io.File;

public class chmod {

    private final static Logger logger = LoggerFactory.getLogger(chmod.class);


    public static void run(String mode, File file) {

        String[] cmd = new String[]{"chmod", mode, file.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        logger.trace("chmod: " + mode + " " + file.getAbsolutePath());
        launcher.run();
        logger.trace("Done.");
    }
}
