package pl.edu.pjwstk.kaldi.programs;

import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;

import java.io.File;

public class chmod {

    public static void run(String mode, File file) {

        String[] cmd = new String[]{"chmod", mode, file.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        Log.verbose("chmod: " + mode + " " + file.getAbsolutePath());
        launcher.run();
        Log.verbose("Done.");
    }
}
