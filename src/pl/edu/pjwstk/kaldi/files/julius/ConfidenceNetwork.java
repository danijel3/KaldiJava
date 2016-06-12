package pl.edu.pjwstk.kaldi.files.julius;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class ConfidenceNetwork {
	
	public static class Word
	{
		public String word;
		public double weight;
		public Object object; 

		public Word(String word, double weight)
		{
			this.word=word;
			this.weight=weight;

			object=null;
		}

		public String toString()
		{
			return word+":"+weight;
		}
	}

	public static class Section
	{
		public List<Word> words;

		public Section()
		{
			words=new LinkedList<>();
		}

		public String toString()
		{
			String ret="";

			for(Word w:words)
			{
				ret+="("+w+") ";
			}

			return ret;
		}
	}
	
	public Vector<Section> sections;
	
	public ConfidenceNetwork()
	{
		sections=new Vector<Section>();
	}
	

	public double getAverageSectionWidth()
	{
		int ret=0;
		int count=0;
		
		for(Section s:sections)
		{
			ret+=s.words.size();
			count++;
		}
		
		return ret/(double)count;
	}

}
