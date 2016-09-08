package pl.edu.pjwstk.kaldi.programs;

import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;

import java.io.File;

public class Sox {

    public static void convert(File src_file, File dest_file) {

        String[] cmd = new String[]{"sox", src_file.getAbsolutePath(), "-c",
                "1", dest_file.getAbsolutePath(), "norm", "-3", "rate", "-h",
                "16k"};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        Log.verbose("Coverting using SoX...");
        launcher.run();
        Log.verbose("Done.");

    }

    public static void extract(File src_file, File dest_file,
                               double time_start, double time_end) {

        double duration = time_end - time_start;

        String[] cmd = new String[]{"sox", src_file.getAbsolutePath(),
                dest_file.getAbsolutePath(), "trim", "" + time_start,
                "" + duration};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        Log.verbose("Extracting using SoX...");
        launcher.run();
        Log.verbose("Done.");

    }
}
