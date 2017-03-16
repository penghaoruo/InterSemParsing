package brainllc.penghaoruo.isp;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

public class SemanticParse {
	private static ArrayList<String> connectives = null;
	
	public static ArrayList<String> getParse(TextAnnotation ta) {
		ArrayList<String> res = new ArrayList<String>();
		res.add("Query: " + ta.getText());
		if (ta.getNumberOfSentences() > 1) {
			return null;
		}
		if (!(ta.hasView(ViewNames.SHALLOW_PARSE) && ta.hasView(ViewNames.SRL_VERB))) {
			return null;
		}
		
		View phrases = ta.getView(ViewNames.SHALLOW_PARSE);
		View srl = ta.getView(ViewNames.SRL_VERB);
		View pos = ta.getView(ViewNames.POS);
		View dep = ta.getView(ViewNames.DEPENDENCY_STANFORD);
		View ner = ta.getView(ViewNames.NER_CONLL);
		
		if (phrases == null || srl == null || pos == null || dep == null) {
			return null;
		}
		res.add("Phrases: " + phrases.toString());
		res.add("POS: " + pos.toString());
		res.add("DEP: " + dep.toString());
		res.add("SRL: " + srl.toString());
		res.add("NER: " + ner.toString());
		
		ArrayList<Constituent> predicates = getPredicates(srl);
		if (predicates.size() == 0) {
			return null;
		}
		if (predicates.size() >= 1) {
			List<Constituent> tokens = phrases.getConstituents();
			// Get Verb Phrase
			String vp = null;
			int index = 0;
			while (index < tokens.size()) {
				Constituent token = tokens.get(index);
				if (token.getLabel().equals("VP")) {
					vp = token.getTokenizedSurfaceForm();
					break;
				}
				index += 1;
			}
			if (vp == null) {
				return null;
			}
			index += 1;
			while (index < tokens.size()) {
				Constituent token = tokens.get(index);
				if (token.getLabel().equals("NP")) {
					vp += " " + token.getTokenizedSurfaceForm();
				} else {
					break;
				}
				index += 1;
			}
			if (vp.endsWith(" that")) {
				vp = vp.substring(0, vp.length()-5);
			}
			res.add(vp);
			
			String np = getHeadNoun(vp);
			// Get Properties
			while (index < tokens.size()) {
				while (index < tokens.size() && !(tokens.get(index).getLabel().equals("PP") || tokens.get(index).getLabel().equals("PP"))) {
					index += 1;
				}
				if (index >= tokens.size()) {
					break;
				}
				String pp = tokens.get(index).getTokenizedSurfaceForm();
				index += 1;
				while (index < tokens.size() && tokens.get(index).getLabel().equals("NP")) {
					pp += " " + tokens.get(index).getTokenizedSurfaceForm();
					index += 1;
				}
				if (pp.endsWith(" that")) {
					pp = pp.substring(0, pp.length()-5);
				}
				res.add(np + " " + pp);
			}
		}
		/*
		if (predicates.size() > 1) {
			System.out.println(ta.getText());
			System.out.println(predicates);
			return null;
		}
		*/
		return res;
	}
	
	private static String getHeadNoun(String vp) {
		String np = "";
		String[] strs = vp.split(" ");
		np = strs[strs.length-1];
		if (np.equals("that")) {
			np = strs[strs.length-2];
		}
		return np;
	}

	private static ArrayList<Constituent> getPredicates(View srl) {
		ArrayList<Constituent> chain = new ArrayList<Constituent>();
		ArrayList<Integer> start = new ArrayList<Integer>();
		ArrayList<Integer> end = new ArrayList<Integer>();
		
		for (Constituent c : srl.getConstituents()) {
			if (c.getLabel().equals("Predicate")) {
				if (isIgnoreVerb(c)) {
					continue;
				}
				chain.add(c);
				start.add(c.getStartSpan());
				end.add(c.getEndSpan());
			}
			for (int j = 0; j < chain.size() - 1; j++) {
				if (start.get(j+1) - end.get(j) <= 1) {
					//System.out.println("Compound!!! " + chain.get(j) + "\t" + chain.get(j+1));
					chain.remove(j);
					start.remove(j);
					end.remove(j);
					j--;
				}
			}
		}
		return chain;
	}

	private static void generateFromSRL(View srl) throws Exception {
		ArrayList<String> chain = new ArrayList<String>();
		ArrayList<Integer> start = new ArrayList<Integer>();
		ArrayList<Integer> end = new ArrayList<Integer>();
		for (Constituent c : srl.getConstituents()) {
			if (c.getLabel().equals("Predicate")) {
				// ignore
				if (isIgnoreVerb(c)) {
					continue;
				}
				// augment
				String predicate = augmentVerb(c);
				chain.add(predicate);
				start.add(c.getStartSpan());
				end.add(c.getEndSpan());
			}	
		}
		// check compound (gap <= 1)
		for (int j = 0; j < chain.size() - 1; j++) {
			if (start.get(j+1) - end.get(j) <= 1) {
				//System.out.println("Compound!!! " + chain.get(j) + "\t" + chain.get(j+1));
				String str = chain.get(j) + "#" + chain.get(j+1);
				chain.set(j, str);
				end.set(j, end.get(j+1));
				chain.remove(j+1);
				start.remove(j+1);
				end.remove(j+1);
				j--;
			}
		}
		// add discourse
		for (Constituent c : srl.getConstituents()) {
			if (c.getLabel().equals("AM-DIS") && isConnective(c.getSurfaceForm())) {
				//System.out.println(c.getSurfaceForm()+"\t"+c.getStartSpan()+"\t"+c.getEndSpan());
				//System.out.println(start);
				//System.out.println(end);
				int a = 0;
				int b = 0;
				for (int j = 0; j < chain.size(); j++) {
					if (j == 0) a = 0;
					else a = end.get(j-1);
					b = start.get(j);
					if (c.getStartSpan() >= a && c.getEndSpan() <= b) {
						chain.add(""); start.add(0); end.add(0);
						for (int k = chain.size() - 1; k >= j+1; k--) {
							chain.set(k, chain.get(k-1));
							start.set(k, start.get(k-1));
							end.set(k, end.get(k-1));
						}
						chain.set(j, "c:" + c.getSurfaceForm().replaceAll("\n", " ").toLowerCase());
						start.set(j, c.getStartSpan());
						end.set(j, c.getEndSpan());
						break;
					}
				}
				if (c.getStartSpan() >= end.get(chain.size()-1)) {
					chain.add("c:" + c.getSurfaceForm().replaceAll("\n", " ").toLowerCase());
					start.add(c.getStartSpan());
					end.add(c.getEndSpan());
				}
			}
		}
	}
	
	private static boolean isIgnoreVerb(Constituent c) {
		String token = c.getAttribute("predicate");
		if (token.equals("be") || token.equals("do") || token.equals("have") || token.equals("can") || token.equals("may") || token.equals("dare") || token.equals("must")
				|| token.equals("ought") || token.equals("shall") || token.equals("will") || token.equals("may")) {
			return true;
		}
		return false;
	}
	
	private static String augmentVerb(Constituent c) {
		String res = regularize(c.getAttribute("predicate")) + "." + c.getAttribute("SenseNumber");
		List<Relation> rels = c.getOutgoingRelations();
		for (Relation r : rels) {
			String label = r.getTarget().getLabel();
			if (label.equals("AM-NEG")) {
				res = res + "(not)";
			}
			if (label.equals("C-V")) {
				res = res + "[" + r.getTarget().getTokenizedSurfaceForm().replaceAll("\n", " ") + "]";
			}
		}
		return res;
	}
	
	private static String regularize(String str) {
		if (str.startsWith("\'")) {
			str = str.substring(1, str.length());
		}
		return str;
	}
	
	private static boolean isConnective(String str) {
		if (connectives == null) {
			connectives = IOManager.readLines("data/functionwords/EnglishConjunctions.txt");
		}
		if (connectives.contains(str.toLowerCase())) {
			return true;
		}
		return false;
	}
}