package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.files.Segmentation;
import pl.edu.pjwstk.kaldi.files.TextGrid;
import pl.edu.pjwstk.kaldi.files.julius.JuliusOutput;
import pl.edu.pjwstk.kaldi.utils.FileUtils;
import pl.edu.pjwstk.kaldi.utils.LogStream;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Vector;

public class Julius {

    private final static Logger logger = LoggerFactory.getLogger(Julius.class);
    private final static LogStream logger_stdout=new LogStream(logger);
    private final static LogStream logger_stderr=new LogStream(logger,"ERR>> ");


    public static void test() throws FileNotFoundException {
        if (!Settings.julius_bin.exists())
            throw new FileNotFoundException(Settings.julius_bin.getAbsolutePath());
        if (!Settings.julius_mklm_bin.exists())
            throw new FileNotFoundException(Settings.julius_mklm_bin.getAbsolutePath());
        // TODO: check config files, etc...
    }

    public static void julius(File conf, File filelist, File dic, File binlm) throws RuntimeException {

        String[] cmd = new String[]{Settings.julius_bin.getAbsolutePath(), "-C", conf.getAbsolutePath(), "-filelist",
                filelist.getAbsolutePath(), "-v", dic.getAbsolutePath(), "-d", binlm.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("julius: " + filelist.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public static void mkbingram(File model_bkwd, File binlm) throws RuntimeException {
        String[] cmd = new String[]{Settings.julius_mklm_bin.getAbsolutePath(), "-nrl", model_bkwd.getAbsolutePath(),
                binlm.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("mkbingram: " + model_bkwd.getName() + " -> " + binlm.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public static Segmentation align(File sound, File text) throws IOException, RuntimeException {

        File files[] = new File[]{sound};
        File conf = new File("julius_model/julius.jconf");

        File scp = new File(Settings.temp_dir, "julius.scp");
        File vocab = new File(Settings.temp_dir, "julius.voc");
        File dict = new File(Settings.temp_dir, "julius.dic");
        File model = new File(Settings.temp_dir, "julius.lm");
        File text_b = new File(Settings.temp_dir, "julius_rev.txt");
        File binlm = new File(Settings.temp_dir, "julius.jlm");

        // FileUtils.makeVocab(text, vocab);

        FileUtils.reverse(text, text_b);

        NGram.srilm_estimate(text_b, vocab, model, 3);

        mkbingram(model, binlm);

        Transcriber.transcribe(vocab, Settings.default_encoding, dict, Settings.default_encoding, true);
        FileUtils.makeSCPFile(scp, files, false);

        logger.trace("Running julius...");
        julius(conf, scp, dict, binlm);

        logger.trace("Parsing julius output...");
        String soundname = sound.getAbsolutePath();
        soundname = soundname.substring(0, soundname.lastIndexOf('.'));
        File outfile = new File(soundname + ".out");
        Vector<JuliusOutput> julouts = null;

        julouts = JuliusOutput.loadFromJulius(outfile);

        if (julouts.isEmpty())
            throw new RuntimeException("Julius didn't provide any outputs!");

        Segmentation ret = julouts.get(0).aligned.toSegmentation(Settings.julius_win_offset);

        for (int i = 1; i < julouts.size(); i++) {
            double offset = ret.tiers.get(0).max();
            ret.appendSegmenation(julouts.get(i).aligned.toSegmentation(Settings.julius_win_offset), offset);
        }

        return ret;
    }

    /**
     * Unit tests.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {

            Locale.setDefault(Locale.ENGLISH);

            Transcriber.init();
            Transcriber.test();

            Segmentation seg = align(new File("/home/guest/Desktop/Respeaking/test/kopacz.wav"),
                    new File("/home/guest/Desktop/Respeaking/test/kopacz.txt"));

            TextGrid grid = new TextGrid(seg);

            grid.write(new File("/home/guest/Desktop/Respeaking/test/out.TextGrid"));

            logger.info("Julius Test complete!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
