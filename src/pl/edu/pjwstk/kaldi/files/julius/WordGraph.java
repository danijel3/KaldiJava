package pl.edu.pjwstk.kaldi.files.julius;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import pl.edu.pjwstk.kaldi.utils.Log;

public class WordGraph {

	public Map<Integer, LatticeNode> lattice;
	public Map<Object, LatticeNode> nodes;

	public WordGraph() {
		lattice = new HashMap<Integer, LatticeNode>();
		nodes = new HashMap<Object, LatticeNode>();
	}

	public int getNodeNum()
	{
		return lattice.size();
	}

	public int getLength() {
		int len = 0;
		for (LatticeNode node : lattice.values()) {
			if (node.time_end > len)
				len = node.time_end;
		}
		return len;
	}

	public LatticeNode getFirst() throws RuntimeException {
		Vector<LatticeNode> found = new Vector<LatticeNode>();
		for (LatticeNode node : lattice.values()) {
			if (node.left.size() == 0)
				found.add(node);
		}

		if (found.size() == 0)
			throw new RuntimeException("Cannot find first node! Loop maybe?");

		if (found.size() == 1)
			return found.get(0);

		Log.info("WARNING: found more than 1 first node! Returning one with highest score...");

		double score = -999999999;
		LatticeNode ret = found.get(0);
		for (LatticeNode node : found) {
			if (score < node.lscore) {
				ret = node;
				score = node.lscore;
			}
		}

		return ret;
	}

	public Hypo findBestScore() {
		LatticeNode node = getFirst();
		Hypo ret = new Hypo();

		while (node.right.size() > 0) {
			ret.add(node);

			LatticeNode next = lattice.get(node.right.get(0));
			double score = next.lscore;
			for (int i = 1; i < node.right.size(); i++) {
				LatticeNode test = lattice.get(node.right.get(i));
				if (test.lscore > score) {
					score = test.lscore;
					next = test;
				}
			}
			node = next;
		}

		return ret;
	}

	public static class Hypo {
		public Vector<LatticeNode> sequence;
		public LatticeNode tail;
		public WordSequence words;

		public Hypo() {
			words = new WordSequence();
			sequence = new Vector<LatticeNode>();
			tail = null;
		}

		public Hypo(LatticeNode node) {
			words = new WordSequence();
			sequence = new Vector<LatticeNode>();
			sequence.add(node);
			words.addWord(node.word);
			tail = node;
		}

		public Hypo(Hypo hypo, LatticeNode node) {
			sequence = new Vector<LatticeNode>();
			sequence.addAll(hypo.sequence);
			words = new WordSequence();
			words.copy(hypo.words);
			words.addWord(node.word);
			sequence.add(node);
			tail = node;
		}

		public void add(LatticeNode node) {
			sequence.add(node);
			words.addWord(node.word);
			tail = node;
		}
	}

	class HypoList {
		public Map<Integer, Hypo> hypos = new HashMap<Integer, Hypo>();
	}

	public Hypo findOracleSequence(WordSequence correct_sequence) {
		Vector<Hypo> found_hypos = new Vector<Hypo>();

		Stack<Hypo> stack = new Stack<Hypo>();
		Map<LatticeNode, HypoList> vite = new HashMap<LatticeNode, HypoList>();
		// TODO: not 100% of viterbi criterion applies to Levenshtein
		// the criterion assumes that hypos occupying the same node and of same
		// length can be compared

		Hypo first = new Hypo(getFirst());

		first.words.setupLevenshtein(correct_sequence);

		stack.push(first);

		while (!stack.empty()) {
			Hypo hypo = stack.pop();

			HypoList list = vite.get(hypo.tail);
			if (list != null) {
				Hypo other = list.hypos.get(hypo.sequence.size());
				if (other != null) {
					hypo.words.updateLevenshtein();
					other.words.updateLevenshtein();
					if (hypo.words.error_sum >= other.words.error_sum)
						continue;
				}
			} else {
				list = new HypoList();
				vite.put(hypo.tail, list);
			}

			list.hypos.put(hypo.sequence.size(), hypo);

			if (hypo.tail.right.size() == 0) {
				found_hypos.add(hypo);
			} else {
				for (Integer i : hypo.tail.right) {
					LatticeNode child = lattice.get(i);
					stack.push(new Hypo(hypo, child));
				}
			}
		}

		int best_score = 999999999;
		Hypo ret = null;

		Log.info("Hypo num: " + found_hypos.size());

		for (Hypo h : found_hypos) {
			h.words.updateLevenshtein();
			if (h.words.error_sum < best_score) {
				best_score = h.words.error_sum;
				ret = h;
			}
		}

		return ret;
	}

	public static Object[] graphNodesFromLattice(
			Vector<LatticeNode> lattice_nodes) {
		Object[] nodes = new Object[lattice_nodes.size()];

		int i = 0;
		for (LatticeNode n : lattice_nodes)
			nodes[i++] = n.object;

		return nodes;
	}

}
