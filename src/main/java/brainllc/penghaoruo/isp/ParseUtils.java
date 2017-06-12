package brainllc.penghaoruo.isp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import brainllc.penghaoruo.isp.QueryTree.QueryNode;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;

// https://gitlab-beta.engr.illinois.edu/cogcomp/illinois-semantic-language-model/blob/master/src/main/java/edu/illinois/cs/cogcomp/slm/preprocess/SRLChain.java
public class ParseUtils {
	private static ArrayList<String> connectives = null;
	
	public static TextAnnotation getSRLTA(TextAnnotation ta, ArrayList<TextAnnotation> tas_srl) {
		String text = ta.getText();
		for (TextAnnotation ta_srl : tas_srl) {
			if (ta_srl.getText().equals(text)) {
				return ta_srl;
			}
		}
		return null;
	}
	
	public static String augmentVerb(Constituent c) {
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
	
	public static String regularize(String str) {
		if (str.startsWith("\'")) {
			str = str.substring(1, str.length());
		}
		return str;
	}
	
	public static boolean isIgnoreVerb(Constituent c) {
		String token = c.getAttribute("predicate");
		if (token.equals("be") || token.equals("do") || token.equals("have") || token.equals("can") || token.equals("may") || token.equals("dare") || token.equals("must")
				|| token.equals("ought") || token.equals("shall") || token.equals("will") || token.equals("may")) {
			return true;
		}
		return false;
	}
	
	public static boolean isConnective(String str) {
		if (connectives == null) {
			connectives = IOManager.readLines("data/functionwords/EnglishConjunctions.txt");
		}
		if (connectives.contains(str.toLowerCase())) {
			return true;
		}
		return false;
	}
	
	public static String getHeadNoun(String vp) {
		String np = "";
		String[] strs = vp.split(" ");
		np = strs[strs.length-1];
		if (np.equals("that")) {
			np = strs[strs.length-2];
		}
		return np;
	}

	public static ArrayList<Constituent> getPredicates(View srl) {
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

	public static ArrayList<Constituent> getConstituentsCovering(View view, Constituent pred) {
		ArrayList<Constituent> list = new ArrayList<Constituent>();
		for (Constituent c : view.getConstituents()) {
			if ((c.getStartCharOffset() <= pred.getStartCharOffset() && c.getEndCharOffset() >= pred.getEndCharOffset()) || 
				(c.getStartCharOffset() >= pred.getStartCharOffset() && c.getStartCharOffset() < pred.getEndCharOffset()) ||
				(c.getEndCharOffset() > pred.getStartCharOffset() && c.getEndCharOffset() <= pred.getEndCharOffset())
					) {
				list.add(c);
			}
		}
		if (list.size() != 1) {
			System.out.println("Imperfect Match!");
		}
		return list;
	}

	public static int getLevel(ArrayList<Constituent> nodes_dep) {
		int min = 100;
		for (Constituent c : nodes_dep) {
			int k = getLevel(c, 0);
			if (k < min) {
				min = k;
			}
		}
		return min;
	}

	public static int getLevel(Constituent c, int k) {
		List<Relation> relations = c.getIncomingRelations();
		if (relations == null || relations.size() == 0) {
			return k;
		} else {
			return getLevel(relations.get(0).getSource(), k+1);
		}
	}

	public static PredArg populateNode(Constituent pred, View phrases, View pos, View ner) {
		// TODO: more hack
		PredArg pa = new PredArg();
		pa.pred = pred;
		pa.args = new ArrayList<String>();
		for (Relation r : pred.getOutgoingRelations()) {
			String arg = r.getTarget().getTokenizedSurfaceForm();
			String role = r.getTarget().getLabel();
			String type = "";
			ArrayList<Constituent> cons = getConstituentsCovering(ner, pred);
			if (cons != null) {
				type = cons.get(0).getLabel();
			}
			String res = arg + "|" + role + "|" + type;
			pa.args.add(res);
		}
		return pa;
	}

	public static QueryNode getParent(QueryTree tree, HashMap<Constituent, Integer> pred_map, int prev_level, View dep, Constituent pred) {
		if (prev_level == -1) {
			return tree.root;
		}
		ArrayList<Constituent> pred_candidates = new ArrayList<Constituent>();
		for (Constituent c : pred_map.keySet()) {
			if (pred_map.get(c) == prev_level) {
				pred_candidates.add(c);
			}
		}
		if (pred_candidates.size() == 0) {
			System.out.println("Get Parent Error One!");
			return null;
		}
		if (pred_candidates.size() == 1) {
			return findNodeInTree(pred_candidates.get(0), tree.root);
		}
		for (Constituent c : pred_candidates) {
			if (checkInDepPath(dep, c, pred)) {
				return findNodeInTree(c, tree.root);
			}
		}
		System.out.println("Get Parent Error Two!");
		return null;
	}

	public static boolean checkInDepPath(View dep, Constituent c, Constituent pred) {
		// The Simple Way
		ArrayList<Constituent> c_list = getConstituentsCovering(dep, c);
		ArrayList<Constituent> pred_list = getConstituentsCovering(dep, pred);
		for (Constituent ancestor : c_list) {
			for (Constituent child : pred_list) {
				if (checkDepAncestor(dep, ancestor, child)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean checkDepAncestor(View dep, Constituent ancestor, Constituent child) {
		while (child.getIncomingRelations() != null && child.getIncomingRelations().size() > 0) {
			child = child.getIncomingRelations().get(0).getSource();
			if (child == ancestor) {
				return true;
			}
		}
		return false;
	}

	public static QueryNode findNodeInTree(Constituent c, QueryNode node) {
		if (node.data.pred == c) {
			return node;
		}
		for (QueryNode child : node.children) {
			QueryNode res = findNodeInTree(c, child);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

}
