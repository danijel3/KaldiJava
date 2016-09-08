package pl.edu.pjwstk.kaldi.files;

import java.io.*;

public class TextGrid extends Segmentation {

    public TextGrid() {

    }

    public TextGrid(Segmentation segmentation) {
        this.tiers = segmentation.tiers;
    }

    private String readUntil(BufferedReader reader, String prefix) throws IOException, RuntimeException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith(prefix))
                break;
        }

        if (line == null)
            throw new RuntimeException("couldn't find required line " + prefix);

        return line;
    }

    private String removeQuotes(String str) {
        if (str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"')
            return str.substring(1, str.length() - 1);
        return str;
    }

    @Override
    public void read(File file) throws IOException, RuntimeException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

        String line;

        line = readUntil(reader, "size = ");

        int tierNum = Integer.parseInt(line.trim().substring(7).trim());

        reader.readLine();

        for (int t = 0; t < tierNum; t++) {
            Tier tier = new Tier();
            tiers.add(tier);

            line = readUntil(reader, "name = ");

            tier.name = removeQuotes(line.trim().substring(7).trim());

            line = readUntil(reader, "intervals: size = ");

            int segNum = Integer.parseInt(line.trim().substring(18).trim());

            for (int s = 0; s < segNum; s++) {
                line = reader.readLine();
                if (!line.trim().equals("intervals [" + (s + 1) + "]:"))
                    throw new RuntimeException("error in file " + file.getName() + " in line " + line);

                Segment segment = new Segment();

                line = reader.readLine();
                segment.start_time = Double.parseDouble(line.trim().substring(7).trim());
                line = reader.readLine();
                segment.end_time = Double.parseDouble(line.trim().substring(7).trim());
                line = reader.readLine();
                segment.name = removeQuotes(line.trim().substring(7).trim());

                tier.segments.add(segment);
            }
        }

        reader.close();

    }

    @Override
    public void write(File file) throws IOException {

        PrintWriter writer = new PrintWriter(file);

        writer.println("File type = \"ooTextFile\"");
        writer.println("Object class = \"TextGrid\"");
        writer.println();
        writer.println("xmin = " + min());
        writer.println("xmax = " + max());
        writer.println("tiers? <exists>");
        writer.println("size = " + tiers.size());
        writer.println("item []:");

        for (int i = 0; i < tiers.size(); i++) {
            Tier tier = tiers.get(i);

            writer.println("\titem [" + (i + 1) + "]:");

            writer.println("\t\tclass = \"IntervalTier\"");
            writer.println("\t\tname = \"" + tier.name + "\"");

            writer.println("\t\txmin = " + tier.min());
            writer.println("\t\txmax = " + tier.max());
            writer.println("\t\tintervals: size = " + tier.segments.size());

            for (int j = 0; j < tier.segments.size(); j++) {
                Segment segment = tier.segments.get(j);

                writer.println("\t\tintervals [" + (j + 1) + "]:");
                writer.println("\t\t\txmin = " + segment.start_time);
                writer.println("\t\t\txmax = " + segment.end_time);
                writer.println("\t\t\ttext = \"" + segment.name + "\"");
            }
        }

        writer.close();

    }
}
