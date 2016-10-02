package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.utils.LogStream;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

import java.io.File;

public class Python {

    private final static Logger logger = LoggerFactory.getLogger(Julius.class);
    private final static LogStream logger_stdout = new LogStream(logger);
    private final static LogStream logger_stderr = new LogStream(logger, "ERR>> ");

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
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("Running python script: " + script.getName());
        launcher.run();
        logger.trace("Done.");
    }
}
