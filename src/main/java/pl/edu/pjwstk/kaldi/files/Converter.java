package pl.edu.pjwstk.kaldi.files;

import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ParseOptions;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

public class Converter {

    public enum Format {
        CTM, EAF, LAB, RTTM, TextGrid
    }

    public static void main(String[] args) {

        Locale.setDefault(Locale.ENGLISH);

        String fmt_str = "";
        for (Format fmt : Format.values())
            fmt_str += fmt.name() + " ";

        ParseOptions po = new ParseOptions(
                "Converter",
                "Converts between several file formats.\nSupported formats: "
                        + fmt_str
                        + "\nIf no types are given, program tries to guess from the file extenation only!");

        po.addArgument(File.class, "from", "source file");
        po.addArgument(File.class, "to", "destination file");

        po.addArgument("from-type", 'f', String.class, "Sets the source type",
                null);

        po.addArgument("to-type", 't', String.class, "Sets the source type",
                null);

        po.addArgument("rttm-tb", 'r', Double.class,
                "Sets the RTTM time-base.", "1.0");

        po.addArgument("tier", 't', Integer.class,
                "Use specific tier only, or <0 for all tiers.", "-1");

        if (!po.parse(args))
            return;

        try {
            Log.init("Converter", true);
        } catch (SecurityException | FileNotFoundException e1) {
            e1.printStackTrace();
            return;
        }

        try {

            Format from_fmt, to_fmt;

            String from_str = (String) po.getArgument("from-type");
            String to_str = (String) po.getArgument("to-type");
            double timebase = (Double) po.getArgument("rttm-tb");
            File from_file = (File) po.getArgument(0);
            File to_file = (File) po.getArgument(1);

            if (from_str != null) {
                try {
                    from_fmt = Format.valueOf(from_str);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Type " + from_str
                            + " doesn't exist!");
                }
            } else {
                from_fmt = guessFormat(from_file);
                if (from_fmt == null) {
                    throw new RuntimeException(
                            "Couldn't guess format for file: "
                                    + from_file.getName()
                                    + "\nEnter it manually!");
                }
            }

            if (to_str != null) {
                try {
                    to_fmt = Format.valueOf(to_str);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Type " + from_str
                            + " doesn't exist!");
                }
            } else {
                to_fmt = guessFormat(to_file);
                if (to_fmt == null) {
                    throw new RuntimeException(
                            "Couldn't guess format for file: "
                                    + to_file.getName()
                                    + "\nEnter it manually!");
                }
            }

            Log.info("Converting " + from_file.getName() + " ["
                    + from_fmt.name() + "] -> " + to_file.getName() + " ["
                    + to_fmt.name() + "] t=" + timebase);

            Segmentation from_seg = fromFmt(from_fmt, timebase);
            from_seg.read(from_file);
            Segmentation to_seg = fromFmt(to_fmt, timebase);

            int t = (Integer) po.getArgument("tier");
            if (t < 0)
                to_seg.tiers.addAll(from_seg.tiers);
            else
                to_seg.tiers.add(from_seg.tiers.get(t));

            to_seg.write(to_file);

        } catch (Exception e) {
            Log.error("Main error", e);
        }

    }

    private static Format guessFormat(File file) {

        String name = file.getName();
        int pos = name.lastIndexOf('.');
        if (pos < 0)
            return null;

        String ext = name.substring(pos + 1);

        if (ext.equals("TextGrid"))
            return Format.TextGrid;

        if (ext.toLowerCase().equals("lab"))
            return Format.LAB;

        if (ext.toLowerCase().equals("rttm"))
            return Format.RTTM;

        if (ext.toLowerCase().equals("ctm"))
            return Format.CTM;

        if (ext.toLowerCase().equals("eaf"))
            return Format.EAF;

        return null;
    }

    public static Segmentation fromFmt(Format fmt, double timebase)
            throws ParserConfigurationException {
        switch (fmt) {
            case CTM:
                return new CTM();
            case EAF:
                return new EAF();
            case LAB:
                return new LAB();
            case RTTM:
                return new RTTM(timebase);
            case TextGrid:
                return new TextGrid();
            default:
                return null;
        }
    }
}
