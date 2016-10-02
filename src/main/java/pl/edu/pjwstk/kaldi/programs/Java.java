package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;

import java.io.File;
import java.util.List;

public class Java {
    private final static Logger logger = LoggerFactory.getLogger(Java.class);

    private static String sep = ":";

    public static void java(String class_name, String[] args,
                            List<File> cp_files, boolean async) {

        String class_path = "";
        for (File file : cp_files) {
            if (!class_path.isEmpty())
                class_path += sep;
            class_path += file.getAbsolutePath();
        }

        String[] cmd_start = new String[]{"java", "-Dfile.encoding=UTF-8",
                "-cp", class_path, class_name};
        String cmd[] = new String[cmd_start.length + args.length];

        System.arraycopy(cmd_start, 0, cmd, 0, cmd_start.length);
        System.arraycopy(args, 0, cmd, cmd_start.length + 0, args.length);

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setSuppressOutput(true);
        launcher.setAsynchronous(async);

        logger.trace("Running Java: " + class_name);
        launcher.run();
        logger.trace("Done.");
    }

}
