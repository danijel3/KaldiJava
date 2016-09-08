package pl.edu.pjwstk.kaldi.utils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {

    /**
     * Convert encoding of files.
     *
     * @param input      input file
     * @param input_enc  encoding of input file
     * @param output     output file
     * @param output_enc encoding of output file
     * @throws IOException
     */

    public static void iconv(File input, String input_enc, File output, String output_enc) throws IOException {

        InputStreamReader reader = new InputStreamReader(new FileInputStream(input), input_enc);
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(output), output_enc);

        char buf[] = new char[512];

        int ret;
        while ((ret = reader.read(buf)) >= 0)
            writer.write(buf, 0, ret);

        reader.close();
        writer.close();
    }

    /**
     * Removes lines from file based on a set of conditions.
     *
     * @param input      input file
     * @param input_enc  input file encoding
     * @param output     output file
     * @param output_enc output file encoding
     * @param lines      lines removed from the file
     * @param equals     if true remove only of lines are equal; if false remove if
     *                   file line contains line given as argument
     * @throws IOException
     */

    public static void removeLines(File input, String input_enc, File output, String output_enc, List<String> lines,
                                   boolean equals) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), input_enc));
        PrintWriter writer = new PrintWriter(output, output_enc);

        String line;
        while ((line = reader.readLine()) != null) {

            boolean print = true;
            for (String l : lines)
                if (equals) {
                    if (line.equals(l)) {
                        print = false;
                        break;
                    }
                } else {
                    if (line.contains(l)) {
                        print = false;
                        break;
                    }
                }

            if (print)
                writer.println(line);
        }

        reader.close();
        writer.close();
    }

    /**
     * Append lines to a file at end or start of file.
     *
     * @param input      input file
     * @param input_enc  input file encoding
     * @param output     output file
     * @param output_enc output file encoding
     * @param lines      lines being appended
     * @param prepend    if true add to beginning; if false add to end of file
     * @throws IOException
     */

    public static void appendLines(File input, String input_enc, File output, String output_enc, List<String> lines,
                                   boolean prepend) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), input_enc));
        PrintWriter writer = new PrintWriter(output, output_enc);

        String line;

        if (prepend) {
            for (String l : lines)
                writer.println(l);
        }

        while ((line = reader.readLine()) != null)
            writer.println(line);

        if (!prepend) {
            for (String l : lines)
                writer.println(l);
        }

        reader.close();
        writer.close();
    }

    /**
     * Writes a list with file paths into a SCP file.
     *
     * @param scp_file     the file containing the paths
     * @param file_list    a list of paths to save
     * @param prepend_name if true a name is prepended before each file
     * @throws IOException
     */

    public static void makeSCPFile(File scp_file, File[] file_list, boolean prepend_name) throws IOException {
        PrintWriter writer = new PrintWriter(scp_file);

        for (File file : file_list) {
            if (prepend_name)
                writer.print(file.getName() + " ");
            writer.println(file.getAbsolutePath());
        }

        writer.close();
    }

    /**
     * Reads a file into a string.
     *
     * @param file  file to read
     * @param delim delimiter to use instead of new-lines
     * @return string
     * @throws IOException
     */
    public static String readFile(File file, String delim) throws IOException {
        String ret = "";
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        while ((line = reader.readLine()) != null) {
            ret += line + delim;
        }

        reader.close();

        return ret;

    }

    public static void writelnFile(File file, String string) throws IOException {
        PrintWriter writer = new PrintWriter(file);
        writer.println(string);
        writer.close();
    }

    public static void appendName(String name, File input, File output) throws IOException {

        PrintWriter writer = new PrintWriter(output);

        writer.print(name + " ");

        BufferedReader reader = new BufferedReader(new FileReader(input));

        String line = reader.readLine();

        writer.println(line);

        writer.close();
        reader.close();

    }

    public static void makeVocab(File input_file, File vocab_file) throws IOException {
        HashSet<String> words = new HashSet<>();

        BufferedReader reader = new BufferedReader(new FileReader(input_file));

        String line;
        while ((line = reader.readLine()) != null) {
            String[] tok = line.split("\\s+");
            Collections.addAll(words, tok);
        }

        reader.close();

        PrintWriter writer = new PrintWriter(vocab_file);

        for (String w : words) {
            writer.println(w);
        }

        writer.close();
    }

    /**
     * Cleans the contenst of a directory.
     *
     * @param dir       directory to clean
     * @param exclude   files and folders to skip
     * @param max_depth maximum depth
     */
    public static void cleanup(File dir, File[] exclude, int max_depth) {
        if (max_depth < 0)
            return;

        Log.verbose("Cleaning up " + dir.getName() + " dir...");

        for (File file : dir.listFiles()) {
            boolean skip = false;
            for (File exc : exclude) {
                if (file.getAbsolutePath().equals(exc.getAbsolutePath())) {
                    Log.verbose("Skipping " + file.getName());
                    skip = true;
                    break;
                }
            }

            if (!skip) {

                if (file.isDirectory())
                    cleanup(file, exclude, max_depth - 1);

                Log.verbose("Deleting " + file.getName());
                file.delete();
            }
        }
    }

    public static void reverse(File input, File output) throws IOException {

        LinkedList<String> words = new LinkedList<>();

        BufferedReader reader = new BufferedReader(new FileReader(input));

        String line;
        while ((line = reader.readLine()) != null) {
            StringTokenizer strtok = new StringTokenizer(line);
            while (strtok.hasMoreTokens()) {
                words.addFirst(strtok.nextToken());
            }
        }
        reader.close();

        PrintWriter writer = new PrintWriter(output);

        for (String w : words) {
            writer.print(w + " ");
        }
        writer.println();
        writer.close();
    }

    public static void bracket(String prepend, File file, String postpend, File out) throws IOException {

        PrintWriter writer = new PrintWriter(out);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            writer.print(prepend);
            writer.print(line);
            writer.println(postpend);
        }

        reader.close();
        writer.close();
    }

    public static void sort_uniq(File input, File output, String encoding) throws IOException {
        HashSet<String> lines = new HashSet<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), encoding));

        String line;
        while ((line = reader.readLine()) != null)
            lines.add(line);

        reader.close();

        PrintWriter writer = new PrintWriter(output, encoding);

        for (String line2 : lines) {
            writer.println(line2);
        }

        writer.close();

    }

    public static Vector<String> readLines(File file, String encoding) throws IOException {
        Vector<String> ret = new Vector<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

        String line;
        while ((line = reader.readLine()) != null)
            ret.add(line);

        reader.close();

        return ret;
    }

    public static Vector<String> readTokens(File file, String encoding) throws IOException {
        Vector<String> ret = new Vector<>();

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        ) {

            String line;
            while ((line = reader.readLine()) != null) {
                String tok[] = line.split("\\s+");
                Collections.addAll(ret, tok);
            }

        }

        return ret;
    }


    public static void writeLines(Vector<String> lines, File file, String encoding) throws IOException {

        PrintWriter writer = new PrintWriter(file, encoding);

        for (String line : lines)
            writer.println(line);
        writer.close();

    }

    public static void mergeFiles(File[] input_files, File output, String encoding, boolean skip_blanks)
            throws IOException {
        PrintWriter writer = new PrintWriter(output, encoding);
        for (File file : input_files) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

            String line;
            while ((line = reader.readLine()) != null) {
                if (skip_blanks && line.isEmpty())
                    continue;
                writer.println(line);
            }

            reader.close();

        }

        writer.close();
    }

    private static Pattern pClean = Pattern
            .compile("[^a-zA-Z0-9ąĄćĆęĘłŁńŃóÓśŚżŻźŹšŠťŤžŽşŞľĽŕŔáÁâÂăĂäÄĺĹçÇčČéÉëËěĚíÍîÎďĎđĐňŇôÔőŐöÖřŘůŮúÚűŰüÜýÝţŢ ]");
    private static Pattern pCleanNl = Pattern
            .compile("[^a-zA-Z0-9ąĄćĆęĘłŁńŃóÓśŚżŻźŹšŠťŤžŽşŞľĽŕŔáÁâÂăĂäÄĺĹçÇčČéÉëËěĚíÍîÎďĎđĐňŇôÔőŐöÖřŘůŮúÚűŰüÜýÝţŢ \n]");
    private static Pattern pCleanDs = Pattern.compile("\\s+");

    public static void cleanChars(File input, File output, boolean leave_newlines, boolean remove_doublespace,
                                  String encoding) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), encoding));
        PrintWriter writer = new PrintWriter(output, encoding);

        Pattern p = pClean;
        if (leave_newlines)
            p = pCleanNl;
        Matcher m;

        String line;
        while ((line = reader.readLine()) != null) {
            m = p.matcher(line);
            line = m.replaceAll("");

            if (remove_doublespace) {
                m = pCleanDs.matcher(line);
                line = m.replaceAll(" ");
            }

            if (leave_newlines)
                writer.println(line);
            else
                writer.print(line + " ");
        }

        reader.close();
        writer.close();
    }

    public static void add_probs_to_lexicon(File lex_in, File lex_out) throws IOException {

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lex_in)));
                PrintWriter writer = new PrintWriter(lex_out);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                int i = line.indexOf(' ');
                writer.println(line.substring(0, i).trim() + " 1.0 " + line.substring(i + 1).trim());
            }
        }
    }

    public static void add_besi_to_lexicon(File lex_in, File lex_out) throws IOException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lex_in)));
                PrintWriter writer = new PrintWriter(lex_out);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tok = line.split("\\s+");

                if (tok.length < 3)
                    throw new IOException("Error reading lexicon - line has <3 tokens: " + line);

                String w = tok[0];
                String p = tok[1];

                if (tok.length == 3) {
                    writer.println(w + " " + p + " " + tok[2] + "_S");
                } else {
                    writer.print(w + " " + p + " " + tok[2] + "_B ");
                    for (int i = 3; i < tok.length - 1; i++)
                        writer.print(tok[i] + "_I ");
                    writer.println(tok[tok.length - 1] + "_E");
                }
            }
        }
    }

    public static void make_words_list(File vocab, File words) throws IOException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vocab)));
                PrintWriter writer = new PrintWriter(words);
        ) {
            writer.println("<eps> 0");
            int cnt = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("<s>") || line.equals("</s>")) continue;
                if (line.equals("-pau-")) line = "SIL";
                writer.println(line + " " + (cnt++));
            }
            writer.println("#0 " + (cnt++));
            writer.println("<s> " + (cnt++));
            writer.println("</s> " + (cnt++));
        }
    }

    public static void make_phones_list(File silence_phones, File nonsilence_phones, int disambig_num, File phones) throws IOException {
        try (
                BufferedReader silreader = new BufferedReader(new InputStreamReader(new FileInputStream(silence_phones)));
                BufferedReader nonsilreader = new BufferedReader(new InputStreamReader(new FileInputStream(nonsilence_phones)));
                PrintWriter writer = new PrintWriter(phones);
        ) {
            writer.println("<eps> 0");
            int cnt = 1;
            String line;

            while ((line = silreader.readLine()) != null) {
                writer.println(line + " " + (cnt++));
                writer.println(line + "_B " + (cnt++));
                writer.println(line + "_E " + (cnt++));
                writer.println(line + "_S " + (cnt++));
                writer.println(line + "_I " + (cnt++));
            }

            while ((line = nonsilreader.readLine()) != null) {
                writer.println(line + "_B " + (cnt++));
                writer.println(line + "_E " + (cnt++));
                writer.println(line + "_S " + (cnt++));
                writer.println(line + "_I " + (cnt++));
            }

            for (int i = 0; i <= disambig_num; i++) {
                writer.println("#" + i + " " + (cnt++));
            }
        }
    }

    public static int get_id_from_table(File table, String key) throws IOException, NumberFormatException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(table)));
        ) {
            key = key.trim();
            String line;
            while ((line = reader.readLine()) != null)
                if (line.startsWith(key)) {
                    return Integer.parseInt(line.substring(key.length() + 1).trim());
                }
        }

        return -1;
    }

    public static Vector<String> get_ids_from_table(File table, String key_regex) throws IOException, NumberFormatException {
        Vector<String> ret = new Vector<>();

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(table)));
        ) {
            String line;
            while ((line = reader.readLine()) != null)
                if (line.matches(key_regex)) {
                    ret.add(line.split("\\s+")[1].trim());
                }
        }

        return ret;
    }

    public static void convert_besi_to_desc(File besi, File desc) throws IOException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(besi)));
                PrintWriter writer = new PrintWriter(desc);
        ) {

            String line;
            while ((line = reader.readLine()) != null) {
                String tok[] = line.split("\\s");
                String k = tok[0];
                String id = tok[1];

                if (k.equals("<eps>")) continue;
                if (k.charAt(0) == '#') continue;

                String d = "nonword";
                if (k.endsWith("_B")) d = "begin";
                if (k.endsWith("_E")) d = "end";
                if (k.endsWith("_S")) d = "singleton";
                if (k.endsWith("_I")) d = "internal";

                writer.println(id + " " + d);
            }

        }
    }

    public static void getSymsFromList(File syms, File out, Set<String> skip) throws IOException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(syms)));
                PrintWriter writer = new PrintWriter(out);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String tok[] = line.split("\\s");

                if (skip.contains(tok[0])) continue;

                writer.println(tok[0]);
            }
        }
    }

    public static void tail(File in, File out, int skip_num) throws IOException {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in)));
                PrintWriter writer = new PrintWriter(out);
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (skip_num > 0) {
                    skip_num--;
                    continue;
                }
                writer.println(line);
            }
        }
    }

    public static Vector<String> cut(File in, int field) throws IOException {
        Vector<String> ret = new Vector<>();
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in)));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String tok[] = line.split("\\s+");
                if (tok.length > field) {
                    ret.add(tok[field]);
                }
            }
        }

        return ret;
    }
}
