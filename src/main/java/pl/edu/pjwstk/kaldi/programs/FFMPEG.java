package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.KaldiMain;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

import java.io.File;

public class FFMPEG {
    private final static Logger logger = LoggerFactory.getLogger(FFMPEG.class);


    public static void convertTo16k(File input, File output) {

        String[] cmd = new String[]{Settings.ffmpeg_bin.getAbsolutePath(),
                "-i", input.getAbsolutePath(), "-acodec", "pcm_s16le", "-ac",
                "1", "-ar", "16k", output.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        logger.trace("FFMPEG: " + input.getAbsolutePath() + " -> "
                + output.getAbsolutePath());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("FFMPEG Retval: " + launcher.getReturnValue());
    }

}
