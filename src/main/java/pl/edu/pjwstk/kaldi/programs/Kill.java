package pl.edu.pjwstk.kaldi.programs;

import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;

public class Kill {

    public static void kill(int pid) {

        String[] cmd = {"kill", "-9", "" + pid};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        Log.verbose("Killing process: " + pid);
        launcher.run();
        Log.verbose("Done.");
    }

}
