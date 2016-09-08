package pl.edu.pjwstk.kaldi.files.julius;

import pl.edu.pjwstk.kaldi.utils.Settings;

import java.io.*;

public class MLF {

    private File file;

    public MLF(File file) {
        this.file = file;
    }

    public WordSequence load(String labfile) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), Settings.julius_default_encoding));

        int pos = labfile.lastIndexOf('.');
        labfile = labfile.substring(0, pos) + ".lab";

        String line;
        while ((line = reader.readLine()) != null) {

            if (line.startsWith("#"))
                continue;

            if (line.startsWith("\"") && line.contains(labfile)) {// TODO: weak
                // solution
                WordSequence ret = new WordSequence();
                while ((line = reader.readLine()) != null) {
                    if (line.equals(".")) {
                        reader.close();
                        return ret;
                    }
                    ret.addWord(line);
                }
            }
        }

        reader.close();
        return null;
    }
}
