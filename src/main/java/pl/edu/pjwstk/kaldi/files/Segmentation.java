package pl.edu.pjwstk.kaldi.files;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class Segmentation {

    public static class Segment implements Comparable<Segment> {
        public double start_time;
        public double end_time;
        public String name;
        public double confidence;
        public boolean used = false;

        @Override
        public int compareTo(Segment o) {
            if (start_time < o.start_time)
                return -1;
            else if (start_time > o.start_time)
                return 1;
            else
                return 0;
        }
    }

    public static class Tier {
        public String name;
        public List<Segment> segments = new LinkedList<>();

        public double min() {

            if (segments.isEmpty())
                return 0;

            double min = segments.get(0).start_time;
            for (Segment s : segments)
                if (min > s.start_time)
                    min = s.start_time;
            return min;
        }

        public double max() {

            if (segments.isEmpty())
                return 0;

            double max = segments.get(0).end_time;
            for (Segment s : segments)
                if (max < s.end_time)
                    max = s.end_time;
            return max;
        }

        public void add(double start, double end, String name) {
            add(start, end, name, 1.0);
        }

        public void add(double start, double end, String name, double confidence) {
            Segment segment = new Segment();
            segment.start_time = start;
            segment.end_time = end;
            segment.name = name;
            segment.confidence = confidence;
            segments.add(segment);
        }

        public void sort() {
            Collections.sort(segments);
        }

        public void mergeOverlappingAndAdjecent() {
            sort();
            Segment old = null;
            Iterator<Segment> iter = segments.iterator();
            while (iter.hasNext()) {
                Segment seg = iter.next();
                if (old != null && old.name != null && seg.name != null) {
                    if (seg.end_time > old.end_time)
                        old.end_time = seg.end_time;
                    old.confidence = (seg.confidence + old.confidence) / 2;
                    old.name = old.name + " " + seg.name;
                    iter.remove();
                } else {

                    if (old != null && old.name == null && seg.name != null) {
                        seg.start_time = old.end_time;
                    }

                    if (seg.name == null && old != null && old.name != null) {
                        old.end_time = seg.start_time;
                    }

                    old = seg;
                }

            }
        }

        public void removeNull() {
            Iterator<Segment> iter = segments.iterator();
            while (iter.hasNext()) {
                Segment seg = iter.next();
                if (seg.name == null)
                    iter.remove();
            }
        }
    }

    public List<Tier> tiers = new LinkedList<>();

    public double min() {
        if (tiers.isEmpty())
            return 0;
        double min = tiers.get(0).min();
        for (Tier t : tiers) {
            double d = t.min();
            if (min > d)
                min = d;
        }
        return min;
    }

    public double max() {
        if (tiers.isEmpty())
            return 0;
        double max = tiers.get(0).max();
        for (Tier t : tiers) {
            double d = t.max();
            if (max < d)
                max = d;
        }
        return max;
    }

    public void sort() {
        for (Tier tier : tiers)
            tier.sort();
    }

    public void renameTier(int tier, String name) {
        while (tiers.size() <= tier)
            tiers.add(new Tier());

        tiers.get(tier).name = name;
    }

    public void addSegment(int tier, double start, double end, String name) {
        while (tiers.size() <= tier)
            tiers.add(new Tier());

        tiers.get(tier).add(start, end, name);
    }

    public void addSegment(int tier, double start, double end, String name, double confidence) {
        while (tiers.size() <= tier)
            tiers.add(new Tier());

        tiers.get(tier).add(start, end, name, confidence);
    }

    public void addTiers(Segmentation segmentation) {
        tiers.addAll(segmentation.tiers);
    }

    public void addTier(Segmentation segmentation, int idx) {
        if (idx >= 0 && idx < segmentation.tiers.size())
            tiers.add(segmentation.tiers.get(idx));
    }

    public abstract void read(File file) throws IOException;

    public abstract void write(File file) throws IOException;

    public void dump() {
        for (Tier tier : tiers) {
            System.out.println("<tier>" + tier.name + "</tier>");
            for (Segment segment : tier.segments) {
                System.out.format("%4.4f %4.4f %s", segment.start_time, segment.end_time, segment.name);
                System.out.println();
            }
        }
    }

    public String getLabel(int tier_idx, double fr_start, double fr_end) {

        String ret = null;
        double max_overlap = 0;
        Tier tier = tiers.get(tier_idx);
        for (Segment segment : tier.segments) {
            double o = overlap(fr_start, fr_end, segment.start_time, segment.end_time);
            if (o > max_overlap) {
                max_overlap = o;
                ret = segment.name;
            }
        }

        return ret;
    }

    private double overlap(double a_start, double a_end, double b_start, double b_end) {

        if (a_end < b_start || a_start > b_end)
            return 0;

        if (a_start < b_start) {
            if (a_end < b_end)
                return a_end - b_start;
            else
                return b_end - b_start;
        }

        if (a_end > b_end) {
            return b_end - a_start;
        }

        return a_end - a_start;

    }

    public void link(Segmentation other) {
        tiers = other.tiers;
    }

    public void mergeOverlappingAndAdjecent(int tier) {

        Segment old = null;
        Iterator<Segment> iter = tiers.get(tier).segments.iterator();
        while (iter.hasNext()) {
            Segment seg = iter.next();
            if (old != null && (old.end_time - seg.start_time) > -0.2) {
                old.end_time = seg.end_time;
                old.confidence = (seg.confidence + old.confidence) / 2;
                old.name = old.name + " " + seg.name;
                iter.remove();
            } else {
                old = seg;
            }

        }

    }

    public void appendSegmenation(Segmentation segmentation, double offset) throws RuntimeException {

        if (segmentation.tiers.size() != tiers.size())
            throw new RuntimeException("Segmentations must match in tier count!");

		/*
         * for (int i = 0; i < tiers.size(); i++) if
		 * (!segmentation.tiers.get(i).name.equals(tiers.get(i).name)) throw new
		 * RuntimeException("Segmentations tiers must match in namet!");
		 */

        for (int i = 0; i < tiers.size(); i++) {
            Tier thistier = tiers.get(i);
            Tier othertier = segmentation.tiers.get(i);

            for (Segment oseg : othertier.segments) {
                Segment tseg = new Segment();
                tseg.confidence = oseg.confidence;
                tseg.name = oseg.name;
                tseg.start_time = oseg.start_time + offset;
                tseg.end_time = oseg.end_time + offset;
                thistier.segments.add(tseg);
            }
        }
    }

    public void offsetSegments(double offset) {
        for (Tier tier : tiers)
            for (Segment seg : tier.segments) {
                seg.start_time += offset;
                seg.end_time += offset;
            }
    }

}
