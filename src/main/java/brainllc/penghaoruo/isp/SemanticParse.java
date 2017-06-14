package brainllc.penghaoruo.isp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import brainllc.penghaoruo.isp.QueryTree.QueryNode;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

public class SemanticParse {
	
	public static String getSRL(TextAnnotation ta) {
		if (!ta.hasView(ViewNames.SRL_VERB)) {
			return null;
		}
		View srl = ta.getView(ViewNames.SRL_VERB);
		if (srl == null) {
			return null;
		}
		
		String res = "";
		for (Constituent c : srl.getConstituents()) {
			if (c.getLabel().equals("Predicate")) {
				String pred = c.getSurfaceForm();
				String res_tmp = pred + "(";
				for (Relation r : c.getOutgoingRelations()) {
					String arg = r.getTarget().getSurfaceForm();
					String label = r.getRelationName();
					res_tmp = res_tmp + "[" + label + "]" + arg + " ### ";
				}
				if (c.getOutgoingRelations().size() == 0) {
					res_tmp = res_tmp + ")\n";
				} else {
					res_tmp = res_tmp.substring(0, res_tmp.length()-5) + ")\n";
				}
				res = res + res_tmp;
			}
		}
		if (res.length() == 0) {
			return null;
		}
		res = res.substring(0, res.length()-1);
		return res;
	}
	
	public static QueryTree getParse(TextAnnotation ta, TextAnnotation ta_srl) {
		// Check if the query can be processed
		if (ta.getNumberOfSentences() > 1 || ta_srl.getNumberOfSentences() > 1) {
			return null;
		}
		if (!(ta.hasView(ViewNames.DEPENDENCY_STANFORD) && ta_srl.hasView(ViewNames.SRL_VERB))) {
			return null;
		}

		View phrases = ta.getView(ViewNames.SHALLOW_PARSE);
		View srl = ta_srl.getView(ViewNames.SRL_VERB);
		View pos = ta.getView(ViewNames.POS);
		View dep = ta.getView(ViewNames.DEPENDENCY_STANFORD);
		View ner = ta.getView(ViewNames.NER_CONLL);
		
		if (phrases == null || srl == null || pos == null || dep == null) {
			return null;
		}
		
		// Query can be processed!
		System.out.print(".");
		
		ArrayList<Constituent> predicates = ParseUtils.getPredicates(srl);
		if (predicates.size() == 0) {
			return null;
		}

		HashMap<Constituent, Integer> pred_map = new HashMap<Constituent, Integer>();
		for (Constituent pred : predicates) {
			ArrayList<Constituent> nodes_dep = ParseUtils.getConstituentsCovering(dep,pred);
			int level = ParseUtils.getLevel(nodes_dep);
			pred_map.put(pred, level);
		}
		pred_map = (HashMap<Constituent, Integer>) MapUtil.sortByValue(pred_map);
		
		QueryTree tree = new QueryTree();
		int prev_level = -1;
		int cur_level = -1;
		for (Entry<Constituent, Integer> pair : pred_map.entrySet()) {
			Constituent pred = pair.getKey();
			int level = pair.getValue();
			if (level > cur_level) {
				prev_level = cur_level;
				cur_level = level;
			}
			QueryNode node = new QueryNode();
			node.data = ParseUtils.populateNode(pred, phrases, pos, ner);
			QueryNode parent_node = ParseUtils.getParent(tree, pred_map, prev_level, dep, pred);
			node.parent = parent_node;
			parent_node.children.add(node);
		}
		return tree;
		
		/*
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
		
		String np = ParseUtils.getHeadNoun(vp);
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
		}
		*/
	}

	private static void generateFromSRL(View srl) throws Exception {
		ArrayList<String> chain = new ArrayList<String>();
		ArrayList<Integer> start = new ArrayList<Integer>();
		ArrayList<Integer> end = new ArrayList<Integer>();
		for (Constituent c : srl.getConstituents()) {
			if (c.getLabel().equals("Predicate")) {
				// ignore
				if (ParseUtils.isIgnoreVerb(c)) {
					continue;
				}
				// augment
				String predicate = ParseUtils.augmentVerb(c);
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
			if (c.getLabel().equals("AM-DIS") && ParseUtils.isConnective(c.getSurfaceForm())) {
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
	

}