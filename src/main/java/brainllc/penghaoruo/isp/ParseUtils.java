package brainllc.penghaoruo.isp;

import java.util.ArrayList;
import java.util.List;

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

}
