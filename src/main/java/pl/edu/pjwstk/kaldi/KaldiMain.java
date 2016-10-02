package pl.edu.pjwstk.kaldi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.files.EAF;
import pl.edu.pjwstk.kaldi.files.LAB;
import pl.edu.pjwstk.kaldi.files.Segmentation;
import pl.edu.pjwstk.kaldi.files.Segmentation.Segment;
import pl.edu.pjwstk.kaldi.files.TextGrid;
import pl.edu.pjwstk.kaldi.programs.*;
import pl.edu.pjwstk.kaldi.utils.*;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.regex.Pattern;

public class KaldiMain {

    private final static Logger logger = LoggerFactory.getLogger(KaldiMain.class);

    public static void main(String[] args) {

        try {

            Locale.setDefault(Locale.ENGLISH);

            ParseOptions po = new ParseOptions("KaldiJava", "Scripts for KLADI written in JAVA.");

            po.addArgument(File.class, "input.wav|input_dir", "input WAV file or directory");
            po.addArgument("align", 'a', Boolean.class, "perform alignment of a single file", "false");
            po.addArgument("forced", 'f', Boolean.class, "perform forced alignment", "false");
            po.addArgument("align-dir", 'A', Boolean.class, "perform alignment of all the files within a directory",
                    "false");
            po.addArgument("align-elan", 'E', Boolean.class,
                    "perform alignment of an Elan file (given as transcription)", "false");
            po.addArgument("diarize", 'D', Boolean.class, "perform speaker diarization", "false");
            po.addArgument("trans", 't', File.class, "text file with transcription", "trans.txt");
            po.addArgument("gridfile", 'g', File.class, "save result as TextGrid", null);
            po.addArgument("labfile", 'l', File.class, "save result as HTK label file", null);
            po.addArgument("settings", 's', File.class, "Load program settings from a file", null);
            po.addArgument("dump-settings", 'd', File.class, "Save default program settings to a file", null);

            if (!po.parse(args))
                return;

            po.printOptions();

            if (po.getArgument("dump-settings") != null) {
                logger.info("Dumping settings and exitting.");
                Settings.dumpSettings((File) po.getArgument("dump-settings"));
                return;
            }

            if (po.getArgument("settings") != null) {
                logger.info("Loading settings...");
                Settings.loadSettings((File) po.getArgument("settings"));
            }

            Settings.dumpSettings();

            boolean forced_alignment = (boolean) po.getArgument("forced");

            if ((boolean) po.getArgument("align-elan")) {

                KaldiUtils.init();
                KaldiUtils.test();
                KaldiScripts.init();
                KaldiScripts.test();
                Transcriber.init();
                Transcriber.test();
                NGram.test_srilm();

                logger.info("Processing ELAN file...");

                File input_wav = (File) po.getArgument(0);
                File input_eaf = (File) po.getArgument("trans");
                File textgrid = (File) po.getArgument("gridfile");
                File labfile = (File) po.getArgument("labfile");

                EAF eaf = new EAF();

                eaf.read(input_eaf);

                Segmentation final_segmentation = null;

                for (Segment seg : eaf.tiers.get(0).segments) {

                    if (seg.name.split(" ").length < 2) {
                        // TODO
                        logger.warn("one segment");
                        continue;
                    }

                    File temp_wav = new File(Settings.temp_dir, "eaf_tmp.wav");

                    // Sox.extract(input_wav, temp_wav, seg.start_time,
                    // seg.end_time);
                    WAV.extract(input_wav, temp_wav, seg.start_time, seg.end_time);

                    File temp_txt = new File(Settings.temp_dir, "eaf_tmp.txt");
                    PrintWriter temp_writer = new PrintWriter(temp_txt);
                    temp_writer.println(seg.name);
                    temp_writer.close();

                    Segmentation segmentation = alignFile(temp_wav, temp_txt, null, null);

                    if (final_segmentation == null) {
                        final_segmentation = segmentation;
                        final_segmentation.offsetSegments(seg.start_time);
                    } else
                        final_segmentation.appendSegmenation(segmentation, seg.start_time);
                }

                if (textgrid != null) {
                    logger.info("Saving " + textgrid.getName());
                    TextGrid tg = new TextGrid(final_segmentation);
                    tg.write(textgrid);
                }

                if (labfile != null) {
                    logger.info("Saving " + labfile.getName());
                    LAB lab = new LAB(final_segmentation.tiers.get(0));
                    lab.write(labfile);
                }

            } else if ((boolean) po.getArgument("align-dir")) {

                KaldiUtils.init();
                KaldiUtils.test();
                KaldiScripts.init();
                KaldiScripts.test();
                Transcriber.init();
                Transcriber.test();
                NGram.test_srilm();

                File align_dir = (File) po.getArgument(0);

                for (File file : align_dir.listFiles()) {
                    if (file.getName().endsWith(".wav")) {
                        String name = file.getName();
                        name = name.substring(0, name.length() - 4);

                        File input_wav = file;
                        File input_txt = new File(align_dir, name + ".txt");
                        File textgrid = new File(align_dir, name + ".TextGrid");
                        File labfile = new File(align_dir, name + ".lab");

                        logger.info("Aligning file: " + file.getAbsolutePath());

                        if (labfile.exists() && textgrid.exists()) {
                            logger.warn("File seems already aligned! Skipping...");
                            continue;
                        }

                        if (!input_wav.canRead()) {
                            logger.warn("Cannot read file " + input_wav.getName() + "! Skipping...");
                            continue;
                        }

                        if (!input_txt.canRead()) {
                            logger.warn("Cannot read file " + input_txt.getName() + "! Skipping...");
                            continue;
                        }

                        try {
                            if (forced_alignment)
                                forcedAlignFile(input_wav, input_txt, textgrid, labfile);
                            else
                                alignFile(input_wav, input_txt, textgrid, labfile);
                        } catch (Exception e) {
                            logger.error("Exception processing " + input_wav.getName() + "!", e);
                        }

                    }
                }

            } else if ((boolean) po.getArgument("align")) {

                KaldiUtils.init();
                KaldiUtils.test();
                KaldiScripts.init();
                KaldiScripts.test();
                Transcriber.init();
                Transcriber.test();
                NGram.test_srilm();

                logger.info("Starting alignment...");

                File input_wav = (File) po.getArgument(0);
                File input_txt = (File) po.getArgument("trans");
                File textgrid = (File) po.getArgument("gridfile");
                File labfile = (File) po.getArgument("labfile");

                if (forced_alignment)
                    forcedAlignFile(input_wav, input_txt, textgrid, labfile);
                else
                    alignFile(input_wav, input_txt, textgrid, labfile);

            } else if ((boolean) po.getArgument("diarize")) {

                File input_wav = (File) po.getArgument(0);

                diarize(input_wav);

            } else
                logger.error("Don't know what to do");

        } catch (FileNotFoundException fne) {
            logger.error("Cannot find a file needed by this program: " + fne.getMessage());
        } catch (Exception e) {
            logger.error("Main error", e);
        }

        logger.info("Program finished!");
    }

    public static void diarize(File input_wav) throws FileNotFoundException {

        Shout.test();

        String name = input_wav.getName();
        name = name.substring(0, name.length() - 4);
        File input_dir = input_wav.getParentFile();
        File seg_model = new File(Settings.shout_models, "shout.sad");
        File seg_out = new File(input_dir, name + ".seg");
        File dia_out = new File(input_dir, name + ".dia");

        Shout.shout_segment(input_wav, seg_model, seg_out);

        Shout.shout_cluster(input_wav, seg_out, dia_out, 2);

    }

    public static Segmentation forcedAlignFile(File input_wav, File input_txt, File textgrid, File labfile)
            throws IOException {

        Segmentation segmentation;

        logger.info("Starting forced alignment...");

        KaldiScripts.makeL(input_txt);
        segmentation = KaldiScripts.align(input_wav, input_txt, false);

        for (Segment seg : segmentation.tiers.get(1).segments) {
            seg.name = fixPhSegment(seg.name);
        }

        logger.info("Saving segmentation...");
        if (textgrid != null) {
            TextGrid outgrid = new TextGrid(segmentation);
            logger.info("Saving " + textgrid.getName());
            outgrid.write(textgrid);
        }

        if (labfile != null) {
            LAB lab = new LAB(segmentation.tiers.get(1));
            logger.info("Saving " + labfile.getName());
            lab.write(labfile);
        }

        return segmentation;
    }

    private static Pattern pDisambSym = Pattern.compile("_[ISBE]$");

    private static String fixPhSegment(String ph) {
        if (pDisambSym.matcher(ph).matches())
            return ph.substring(0, ph.length() - 2);
        else
            return ph;
    }

    public static Segmentation alignFile(File input_wav, File input_txt, File textgrid, File labfile)
            throws IOException, UnsupportedAudioFileException {
        return alignFile(input_wav, input_txt, textgrid, labfile, 0);
    }

    public static Segmentation alignFile(File input_wav, File input_txt, File textgrid, File labfile, int depth)
            throws IOException, UnsupportedAudioFileException {
        Segmentation segmentation;

        float file_len = WAV.getLength(input_wav);

        if (depth > 5) {
            throw new RuntimeException("Recursion depth too large...");
        }

        logger.info("Starting decoding process...");

        KaldiScripts.makeHCLG(input_txt);
        segmentation = KaldiScripts.decode(input_wav, false);
        //segmentation = KaldiScripts.decode_oracle(input_wav, input_txt);

        if (textgrid != null) {
            TextGrid decode_temp = new TextGrid(segmentation);
            decode_temp.write(new File(textgrid.getParentFile(), "decode_" + textgrid.getName()));
        } else {
            TextGrid decode_temp = new TextGrid(segmentation);
            decode_temp.write(new File("temp.TextGrid"));
        }

        String ref = FileUtils.readFile(input_txt, " ");

        logger.info("Comparing decoding output to ref...");

        Segmentation fix_seg = Diff.diff(segmentation, ref, file_len);

        if (textgrid != null) {
            TextGrid diff_temp = new TextGrid(fix_seg);
            diff_temp.write(new File(textgrid.getParentFile(), "diff_" + textgrid.getName()));
        }

        TextGrid outgrid = new TextGrid();
        int w_tier = 0;
        int ph_tier = 1;

        outgrid.renameTier(w_tier, "words");
        outgrid.renameTier(ph_tier, "phonemes");

        Settings.temp_dir2.mkdirs();
        File temp_wav = File.createTempFile("seg", ".wav", Settings.temp_dir2);
        File temp_txt = File.createTempFile("seg", ".txt", Settings.temp_dir2);

        logger.info("Adding correctly recognized segments...");
        if (fix_seg.tiers.size() < 3) {
            logger.warn("Diff segmentation incorrect!");
            return outgrid;
        } else {
            for (Segment seg : fix_seg.tiers.get(1).segments)
                outgrid.addSegment(w_tier, seg.start_time, seg.end_time, seg.name, seg.confidence);

            for (Segment seg : fix_seg.tiers.get(2).segments)
                outgrid.addSegment(ph_tier, seg.start_time, seg.end_time, fixPhSegment(seg.name), seg.confidence);
        }

        logger.info("Re-aligning mismatched segments...");

        for (Segment seg : fix_seg.tiers.get(0).segments) {

            logger.info("Aligning: " + seg.name + "(" + seg.start_time + "," + seg.end_time + ")");

            if (seg.name.trim().length() == 0) {
                logger.warn("Empty segment! " + seg.start_time + " to " + seg.end_time);
                continue;
            }

            // Sox.extract(input_wav, temp_wav, seg.start_time, seg.end_time);
            WAV.extract(input_wav, temp_wav, seg.start_time, seg.end_time);

            PrintWriter writer = new PrintWriter(temp_txt);
            writer.println(seg.name);
            writer.close();

            try {

                KaldiScripts.makeL(temp_txt);
                segmentation = KaldiScripts.align(temp_wav, temp_txt, false);

            } catch (Exception e) {

                logger.warn("Exc: " + e);
                logger.warn("Failed to force align segment: " + seg.name + "(" + temp_wav.getName() + ")");

                try {

                    segmentation = alignFile(temp_wav, temp_txt, null, null, depth + 1);

                } catch (Exception e1) {

                    logger.warn("Exc: " + e1);
                    logger.warn("Failed to re-align segment: " + seg.name);
                    logger.warn("Abandoning!");
                    continue;
                }
            }

            for (Segment seg2 : segmentation.tiers.get(w_tier).segments)
                outgrid.addSegment(w_tier, seg.start_time + seg2.start_time, seg.start_time + seg2.end_time, seg2.name,
                        seg2.confidence);

            for (Segment seg2 : segmentation.tiers.get(ph_tier).segments)
                outgrid.addSegment(ph_tier, seg.start_time + seg2.start_time, seg.start_time + seg2.end_time,
                        fixPhSegment(seg2.name), seg2.confidence);

        }

        logger.info("Saving segmentation...");
        outgrid.sort();
        if (textgrid != null) {
            logger.info("Saving " + textgrid.getName());
            outgrid.write(textgrid);
        }

        if (labfile != null && !outgrid.tiers.isEmpty()) {
            LAB lab = new LAB(outgrid.tiers.get(0));
            logger.info("Saving " + labfile.getName());
            lab.write(labfile);
        }

        return outgrid;
    }
}
