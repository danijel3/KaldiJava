package pl.edu.pjwstk.kaldi.programs;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

public class Essentia {

    public static void pitch(File input, File output) {

        String[] cmd = new String[]{
                Settings.essentia_pitch_bin.getAbsolutePath(),
                input.getAbsolutePath(), output.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);

        Log.verbose("Calculating Pitch using Essentia: "
                + input.getAbsolutePath() + " " + output.getAbsolutePath());
        launcher.run();
        Log.verbose("Done.");
    }

    public static class PitchMark {
        public double time;
        public double pitch;
        public double confidence;

        public PitchMark(double t, double p, double c) {
            time = t;
            pitch = p;
            confidence = c;
        }
    }

    public static Vector<PitchMark> loadPitchYaml(File file) throws IOException {
        Vector<PitchMark> ret = new Vector<>();

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;

        double time_step = 0.01;
        double time_off = 0.125;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("pitch:")) {
                int beg = line.indexOf('[');
                if (beg < 0) {
                    reader.close();
                    throw new IOException("Parsing error");
                }
                int end = line.indexOf(']');
                if (end < 0) {
                    reader.close();
                    throw new IOException("Parsing error");
                }

                line = line.substring(beg + 1, end).trim();

                String tok[] = line.split("\\s*,\\s*");

                int i = 0;
                for (String num : tok) {
                    double val = Double.parseDouble(num);
                    if (ret.size() <= i) {
                        ret.addElement(new PitchMark(time_off + i * time_step,
                                val, 1));
                    } else {
                        ret.get(i).pitch = val;
                    }
                    i++;
                }

            }

            if (line.startsWith("pitch_confidence:")) {
                int beg = line.indexOf('[');
                if (beg < 0) {
                    reader.close();
                    throw new IOException("Parsing error");
                }
                int end = line.indexOf(']');
                if (end < 0) {
                    reader.close();
                    throw new IOException("Parsing error");
                }

                line = line.substring(beg + 1, end).trim();

                String tok[] = line.split("\\s*,\\s*");

                int i = 0;
                for (String num : tok) {
                    double val = Double.parseDouble(num);
                    if (ret.size() <= i) {
                        ret.addElement(new PitchMark(time_off + i * time_step,
                                0, val));
                    } else {
                        ret.get(i).confidence = val;
                    }
                    i++;
                }

            }
        }

        reader.close();

        return ret;
    }

    public static void savePitchMarksToXML(String audio_id,
                                           Vector<PitchMark> pitch, File xml)
            throws TransformerFactoryConfigurationError,
            ParserConfigurationException, TransformerException {

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();

        Element elRoot = doc.createElement("audio-segment");
        elRoot.setAttribute("id", audio_id);
        doc.appendChild(elRoot);

        for (PitchMark p : pitch) {
            Element elPitch = doc.createElement("pitch");
            elPitch.setAttribute("t", String.format("%2.3f", p.time));
            elPitch.setAttribute("c", String.format("%2.3f", p.confidence));
            elPitch.setTextContent(String.format("%2.3f", p.pitch));
            elRoot.appendChild(elPitch);
        }

        Transformer trans = TransformerFactory.newInstance().newTransformer();

        trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                "4");
        trans.setOutputProperty(OutputKeys.METHOD, "xml");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        Source source = new DOMSource(doc);
        Result result = new StreamResult(xml);
        trans.transform(source, result);
    }

    public static void generateSignalFromPitchMarks(Vector<PitchMark> pitch,
                                                    File sound_file) throws IOException {
        double win_dur = 0.025;
        double Fs = 16000;
        double T = pitch.lastElement().time + win_dur / 2;

        ByteBuffer sound_buf = ByteBuffer.allocate((int) (2.0 * T * Fs));

        sound_buf.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < pitch.size() - 1; i++) {
            PitchMark a = pitch.get(i);
            PitchMark b = pitch.get(i + 1);

            if (b.time - a.time > 0.1)
                continue;

            int s = (int) (a.time * Fs);
            int num = (int) ((b.time - a.time) * Fs);
            double f = a.pitch;
            double df = (b.pitch - a.pitch) / num;
            for (int j = s; j < s + num; j++) {
                short sval;

                sval = (short) (Short.MAX_VALUE / 2.0 * Math.sin(2 * Math.PI
                        * f * j / Fs));
                sound_buf.putShort(2 * j, sval);

                j++;
                sval = (short) (Short.MAX_VALUE / 2.0 * Math.sin(2 * Math.PI
                        * f * j / Fs));
                sound_buf.putShort(2 * j, sval);

                f += df;
                f += df;
            }
        }

        FileOutputStream fos = new FileOutputStream(sound_file);

        fos.write(sound_buf.array());

        fos.flush();

        fos.close();
    }
}
