package pl.edu.pjwstk.kaldi.grammars;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Grammar {

    static class Arc {
        public int from, to, id;

        public Arc(int from, int to, int id) {
            this.from = from;
            this.to = to;
            this.id = id;
        }
    }

    static Map<String, Integer> eps_map;
    static List<String> eps_list;

    static {
        eps_map = new TreeMap<>();
        eps_map.put("<eps>", 0);
        eps_list = new LinkedList<>();
        eps_list.add("<eps>");

    }

    List<Arc> arcs = new LinkedList<>();
    int node_count = 1;
    Set<Integer> end_nodes = new HashSet<>();
    Map<String, Integer> word_map = new HashMap<>(eps_map);
    List<String> word_list = new ArrayList<>(eps_list);

    public int getLastNode() {
        return node_count - 1;
    }

    public void copySymbols(Grammar other) {
        word_map.putAll(other.word_map);
        word_list.addAll(other.word_list);
    }

    private int getWordID(String word) {
        if (word_map.containsKey(word))
            return word_map.get(word);
        else {
            word_list.add(word);
            word_map.put(word, word_list.size() - 1);
            return word_list.size() - 1;
        }
    }

    public void setWord(String word) {

        assert (arcs.isEmpty());

        int wid = getWordID(word);
        arcs.add(new Arc(0, 1, wid));
        end_nodes.add(1);
        node_count = 2;
    }

    public void setWordList(List<String> words) {

        assert (arcs.isEmpty());

        for (String word : words) {
            int wid = getWordID(word);
            arcs.add(new Arc(0, 1, wid));
        }
        end_nodes.add(1);
        node_count = 2;
    }

    public void setWordSequence(List<String> words) {

        assert (arcs.isEmpty());

        int node = 0;
        for (String word : words) {
            int wid = getWordID(word);
            arcs.add(new Arc(node, node + 1, wid));
            node++;
        }
        end_nodes.add(node);
        node_count = node;
    }


    public Map<Integer, Integer> getWordListMapping(Grammar other) {

        Map<Integer, Integer> ret = new TreeMap<>();
        int id = 0;
        for (String word : other.word_list) {
            ret.put(id, getWordID(word));
            id++;
        }
        return ret;
    }

    public void attach(Grammar other) {
        attach(other, node_count - 1);
    }

    public void attach(Grammar other, int node) {

        Map<Integer, Integer> id_map = getWordListMapping(other);

        int offset = node_count - 1;

        for (Arc arc : other.arcs) {
            int from = arc.from;
            if (from == 0)
                from = node;
            else
                from += offset;
            if (from >= node_count)
                node_count = from + 1;

            int to = arc.to;
            if (to == 0)
                to = node;
            else
                to += offset;
            if (to >= node_count)
                node_count = to + 1;

            int id = id_map.get(arc.id);

            arcs.add(new Arc(from, to, id));
        }

        end_nodes.remove(node);

        for (Integer id : other.end_nodes)
            end_nodes.add(id + offset);
    }

    public void merge(Grammar other, Map<Integer, Integer> links) {
        Map<Integer, Integer> id_map = getWordListMapping(other);

        int offset = node_count - 1;

        for (Arc arc : other.arcs) {
            int from = arc.from;
            if (links.containsKey(from))
                from = links.get(from);
            else
                from += offset;
            if (from >= node_count)
                node_count = from + 1;

            int to = arc.to;
            if (links.containsKey(to))
                to = links.get(to);
            else
                to += offset;
            if (to >= node_count)
                node_count = to + 1;

            int id = id_map.get(arc.id);

            arcs.add(new Arc(from, to, id));
        }

        for (Map.Entry<Integer, Integer> e : links.entrySet())
            end_nodes.remove(e.getValue());

        for (Integer id : other.end_nodes) {
            if (links.containsKey(id))
                end_nodes.add(links.get(id));
            else
                end_nodes.add(id + offset);
        }
    }

    public Grammar clone() {
        Grammar ret = new Grammar();
        ret.arcs.addAll(arcs);
        ret.node_count = node_count;
        ret.end_nodes.addAll(end_nodes);
        ret.word_map = new HashMap<>(word_map);
        ret.word_list = new ArrayList<>(word_list);
        return ret;
    }

    public int fixend() {
        int end = node_count;
        node_count++;
        for (Integer id : end_nodes) {
            arcs.add(new Arc(id, end, 0));
        }
        end_nodes.clear();
        end_nodes.add(end);
        return end;
    }


    public void save(File fst, File wordlist) throws IOException {

        PrintWriter writer = new PrintWriter(fst);

        for (Arc arc : arcs) {
            String w = word_list.get(arc.id);
            writer.println(arc.from + " " + arc.to + " " + w + " " + w);
        }

        for (Integer id : end_nodes) {
            writer.println(id);
        }

        writer.close();

        if (wordlist != null) {
            writer = new PrintWriter(wordlist);

            int id = 0;
            for (String w : word_list) {
                writer.println(w + " " + id);
                id++;
            }

            writer.close();
        }
    }

}
