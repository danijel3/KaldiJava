package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;

public class Kill {

    private final static Logger logger = LoggerFactory.getLogger(Julius.class);


    public static void kill(int pid) {

        String[] cmd = {"kill", "-9", "" + pid};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        logger.trace("Killing process: " + pid);
        launcher.run();
        logger.trace("Done.");
    }

}
