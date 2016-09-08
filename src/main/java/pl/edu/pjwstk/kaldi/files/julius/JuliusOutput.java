package pl.edu.pjwstk.kaldi.files.julius;

import pl.edu.pjwstk.kaldi.files.Segmentation;
import pl.edu.pjwstk.kaldi.files.TextGrid;
import pl.edu.pjwstk.kaldi.files.julius.ConfidenceNetwork.Section;
import pl.edu.pjwstk.kaldi.files.julius.ConfidenceNetwork.Word;
import pl.edu.pjwstk.kaldi.utils.Settings;

import java.io.*;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JuliusOutput {

    public ConfidenceNetwork confidence_network;
    public WordGraph word_graph;
    public WordSequence sentence;
    public AlignedSequence aligned;

    public int time_offset;

    public JuliusOutput() {
        confidence_network = new ConfidenceNetwork();
        word_graph = new WordGraph();
        aligned = new AlignedSequence();
        time_offset = 0;
    }

    public static Vector<JuliusOutput> loadFromJulius(File file) throws IOException, RuntimeException {
        Vector<JuliusOutput> ret = new Vector<>();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), Settings.default_encoding));

        String line = reader.readLine();

        if (line == null || !line.startsWith("sentence1:")) {
            reader.close();
            throw new RuntimeException("Error reading file. Expected each sentence to start with \"sentence1:\"!");
        }

        int off = 0;

        while (reader.ready()) {
            JuliusOutput sentence = new JuliusOutput();
            sentence.time_offset = off;
            sentence.setSentence(line);
            line = sentence.parseFile(reader);
            if (sentence.word_graph.getNodeNum() > 0)
                off += sentence.word_graph.getLength();
            else if (sentence.aligned.sequence.size() > 0)
                off += sentence.aligned.getLength();
            ret.add(sentence);
        }

        reader.close();

        return ret;

    }

    public void setSentence(String line) {
        sentence = new WordSequence();

        String[] words = line.substring(10).trim().split("\\s+");
        for (String word : words) {
            if (word.equals("<s>") || word.equals("</s>"))
                continue;

            sentence.addWord(word);
        }

    }

    private String parseFile(BufferedReader reader) throws IOException, RuntimeException {

        confidence_network.sections.clear();
        word_graph.lattice.clear();

        Pattern pattern = Pattern.compile("\\s*\\(([^)\\]*)\\)");

        int line_num = 0;
        boolean read_conf_net = false;
        boolean read_word_graph = false;
        boolean read_word_alignment = false;
        String line;
        String[] word_opt;
        double weight;
        while ((line = reader.readLine()) != null) {
            line_num++;
            if (line.contains("begin confusion network")) {
                read_conf_net = true;
                continue;
            }

            if (line.contains("end confusion network")) {
                read_conf_net = false;
                continue;
            }

            if (line.contains("begin wordgraph data")) {
                read_word_graph = true;
                continue;
            }

            if (line.contains("end wordgraph data")) {
                read_word_graph = false;
                continue;
            }

            if (line.contains("word alignment")) {
                read_word_alignment = true;
                continue;
            }

            if (line.contains("end forced alignment")) {
                read_word_alignment = false;
                continue;
            }

            if (line.startsWith("sentence1:"))
                return line;

            if (read_conf_net) {
                Section section = new Section();
                confidence_network.sections.add(section);

                Matcher matcher = pattern.matcher(line);

                while (matcher.find()) {
                    String opt = matcher.group(1);

                    word_opt = opt.split(":");
                    if (word_opt.length != 2) {
                        throw new RuntimeException("Cannot parse line " + line_num + " [wrong colon]: " + line);
                    }

                    try {
                        weight = Double.parseDouble(word_opt[1]);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Cannot parse line [wrong weight]: " + line_num);
                    }

                    section.words.add(new Word(word_opt[0], weight));
                }
            }

            if (read_word_graph) {
                LatticeNode node = new LatticeNode(line);

                if (word_graph.lattice.containsKey(node.id)) {
                    throw new RuntimeException("Cannot parse line " + line_num + " [duplicate word node]");
                }

                word_graph.lattice.put(node.id, node);
            }

            if (read_word_alignment) {
                aligned.addWord(line);
            }
        }

        return "";
    }

    public static void main(String[] args) {

        try {

            Vector<JuliusOutput> julouts = JuliusOutput
                    .loadFromJulius(new File("/home/guest/Desktop/Respeaking/kopacz.out"));

            Segmentation ret = julouts.get(0).aligned.toSegmentation(Settings.julius_win_offset);

            for (int i = 1; i < julouts.size(); i++) {
                double offset = ret.tiers.get(0).max();
                ret.appendSegmenation(julouts.get(i).aligned.toSegmentation(Settings.julius_win_offset), offset);
            }

            TextGrid grid = new TextGrid(ret);

            grid.write(new File("/home/guest/Desktop/Respeaking/out.TextGrid"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
