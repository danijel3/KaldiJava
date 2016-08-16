package pl.edu.pjwstk.kaldi.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import pl.edu.pjwstk.kaldi.files.Segmentation;
import pl.edu.pjwstk.kaldi.files.Segmentation.Segment;
import pl.edu.pjwstk.kaldi.files.Segmentation.Tier;
import pl.edu.pjwstk.kaldi.files.SegmentationList;
import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;
import difflib.Patch;

public class Diff {

	private static int clamp(int val, int min, int max) {
		if (val < min)
			return min;
		if (val >= max)
			return max - 1;
		return val;
	}

	/**
	 * Computes the diff between hypothesis and refrence. Returns a
	 * segmentations with 3 tiers: first containing the incorrect segments,
	 * second and third containing correct segments with words and phonemes
	 * accordingly.
	 * 
	 * @param hyp
	 *            hypothesis returned by the decoder (2 tiers: words and
	 *            phonemes)
	 * @param ref
	 *            reference known to be true
	 * @return segmentation with 3 tiers
	 */
	public static Segmentation diff(Segmentation hyp, String ref, double file_len) {

		SegmentationList ret = new SegmentationList();
		List<Segment> hypsegs = hyp.tiers.get(0).segments;
		List<Segment> phsegs = hyp.tiers.get(1).segments;

		LinkedList<String> hyp_words = new LinkedList<String>();
		LinkedList<String> ref_words = new LinkedList<String>();
		Vector<String> ref_vec = new Vector<String>();

		String hypstr = "";
		for (Segment seg : hypsegs) {
			hypstr += seg.name + " ";
		}

		StringTokenizer strtok = new StringTokenizer(hypstr, " \t\n\r");
		while (strtok.hasMoreTokens())
			hyp_words.add(strtok.nextToken());

		strtok = new StringTokenizer(ref, " \t");
		while (strtok.hasMoreTokens()) {
			String str = strtok.nextToken();
			ref_words.add(str);
			ref_vec.add(str);
		}

		Patch<String> patch = DiffUtils.diff(ref_words, hyp_words);

		int rev_beg, rev_end;
		int orig_beg, orig_end;
		double start, end;
		String text;
		double eps = 0.001; // epsilon in seconds

		System.out.println("hypsegs.size(): "+hypsegs.size());
		System.out.println("ref_vec.size(): "+ref_vec.size());
		
		if(hypsegs.get(hypsegs.size()-1).end_time<file_len)
		{
			Segment end_seg=new Segment();
			end_seg.start_time=hypsegs.get(hypsegs.size()-1).end_time;
			end_seg.end_time=file_len;
			hypsegs.add(end_seg);
		}
		
		ret.tiers.add(new Tier());
		for (Delta<String> delta : patch.getDeltas()) {

			System.out.println(delta.getType());
			System.out.println("o " + delta.getOriginal());
			System.out.println("r " + delta.getRevised());

			rev_beg = delta.getRevised().getPosition();
			rev_end = rev_beg + delta.getRevised().size();
			orig_beg = delta.getOriginal().getPosition();
			orig_end = orig_beg + delta.getOriginal().size();

			/*
			 * if (rev_beg > 0) rev_beg--; if (rev_end < hypsegs.size() - 1)
			 * rev_end++; if (orig_beg > 0) orig_beg--; if (orig_end <
			 * ref_words.size() - 1) orig_end++;
			 */

			rev_beg = clamp(rev_beg, 0, hypsegs.size());
			rev_end = clamp(rev_end, 0, hypsegs.size());
			orig_beg = clamp(orig_beg, 0, ref_vec.size());
			orig_end = clamp(orig_end, 0, ref_vec.size());

			start = hypsegs.get(rev_beg).start_time;
			end = hypsegs.get(rev_end).end_time;
			text = extract(ref_vec, orig_beg, orig_end);
			ret.addSegment(0, start, end, text);

			for (int i = rev_beg; i <= rev_end; i++)
				hypsegs.get(i).name = null;
		}

		Tier wtier = new Tier();
		Tier ptier = new Tier();
		for (Segment seg : hypsegs)
			if (seg.name != null) {
				wtier.segments.add(seg);
				for (Segment pseg : phsegs) {
					if (pseg.start_time >= seg.start_time - eps && pseg.end_time <= seg.end_time + eps)
						ptier.segments.add(pseg);
				}
			}

		ret.tiers.add(wtier);
		ret.tiers.add(ptier);

		Tier t = ret.tiers.get(0);
		for (Segment seg : hypsegs)
			if (seg.name != null) {
				t.add(seg.start_time, seg.end_time, null);
			}

		// ret.mergeOverlappingAndAdjecent(0);
		t.mergeOverlappingAndAdjecent();
		t.removeNull();

		ret.renameTier(0, "incorrect");
		ret.renameTier(1, "correct words");
		ret.renameTier(2, "correct phonemes");

		return ret;
	}

	public static String extract(Vector<String> vec, int from, int to) {
		String ret = "";

		if (from < 0)
			from = 0;
		if (to >= vec.size())
			to = vec.size() - 1;

		for (int i = from; i <= to; i++) {
			String str = vec.get(i);
			vec.set(i, "");
			if (str.length() > 0)
				ret += str + " ";
		}
		return ret.trim();
	}

	/**
	 * Computes the diff between hypothesis and refrence. Returns a
	 * segmentations with 2 tiers: first containing the incorrect segments,
	 * second containing correct segments accordingly.
	 * 
	 * @param hyp
	 *            hypothesis returned by the decoder (single tier)
	 * @param ref
	 *            reference known to be true
	 * @param strict
	 *            if false, allow expanding word context
	 * @return segmentation with 2 tiers
	 */
	public static Segmentation diff2(Segmentation hyp, String ref, boolean strict) {
		SegmentationList ret = new SegmentationList();
		List<Segment> hypsegs = hyp.tiers.get(0).segments;

		for (Segment seg : hypsegs)
			seg.used = false;

		LinkedList<String> hyp_words = new LinkedList<String>();
		LinkedList<String> ref_words = new LinkedList<String>();
		Vector<String> ref_vec = new Vector<String>();

		String hypstr = "";
		for (Segment seg : hypsegs) {
			hypstr += seg.name + " ";
		}

		StringTokenizer strtok = new StringTokenizer(hypstr, " \t\n\r");
		while (strtok.hasMoreTokens())
			hyp_words.add(strtok.nextToken());

		strtok = new StringTokenizer(ref, " \t");
		while (strtok.hasMoreTokens()) {
			String str = strtok.nextToken();
			ref_words.add(str);
			ref_vec.add(str);
		}

		Patch<String> patch = DiffUtils.diff(ref_words, hyp_words);

		int rev_beg, rev_end;
		int orig_beg, orig_end;
		double start, end;
		String text;

		for (Delta<String> delta : patch.getDeltas()) {

			Log.verbose("" + delta.getType());
			Log.verbose("o " + delta.getOriginal());
			Log.verbose("r " + delta.getRevised());

			rev_beg = delta.getRevised().getPosition();
			rev_end = rev_beg + delta.getRevised().size() - 1;
			orig_beg = delta.getOriginal().getPosition();
			orig_end = orig_beg + delta.getOriginal().size() - 1;

			if (delta.getType() == TYPE.INSERT) {
				if (delta.getOriginal().getPosition() == 0 || delta.getOriginal().getPosition() == ref_words.size()) {
					Log.verbose("Skipping insertion at start/end!");

					for (int i = rev_beg; i <= rev_end; i++)
						hypsegs.get(i).used = true;

					continue;
				}
			}

			if (!strict) {
				if (orig_beg > 0)
					orig_beg--;
				if (orig_end < ref_words.size() - 1)
					orig_end++;
				if (rev_beg > 0)
					rev_beg--;
				if (rev_end < hypsegs.size() - 1)
					rev_end++;
			} else {
				if (rev_beg < 0)
					rev_beg = 0;
				if (rev_beg >= hypsegs.size())
					rev_beg = hypsegs.size() - 1;
				if (rev_end < 0)
					rev_end = 0;
				if (rev_end >= hypsegs.size())
					rev_end = hypsegs.size() - 1;
			}

			start = hypsegs.get(rev_beg).start_time;
			end = hypsegs.get(rev_end).end_time;
			text = extract(ref_vec, orig_beg, orig_end);
			ret.addSegment(0, start, end, text);

			for (int i = rev_beg; i <= rev_end; i++)
				hypsegs.get(i).used = true;
		}

		ret.mergeOverlappingAndAdjecent(0);

		Tier corrtier = new Tier();
		for (Segment seg : hypsegs)
			if (seg.used == false) {
				corrtier.segments.add(seg);
			}
		ret.tiers.add(corrtier);

		ret.renameTier(0, "incorrect");
		ret.renameTier(1, "correct");

		return ret;
	}
}
