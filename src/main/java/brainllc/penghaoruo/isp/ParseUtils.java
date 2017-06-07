package brainllc.penghaoruo.isp;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

// https://gitlab-beta.engr.illinois.edu/cogcomp/illinois-semantic-language-model/blob/master/src/main/java/edu/illinois/cs/cogcomp/slm/preprocess/SRLChain.java
public class ParseUtils {
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

	public static TextAnnotation getSRLTA(TextAnnotation ta, ArrayList<TextAnnotation> tas_srl) {
		String text = ta.getText();
		for (TextAnnotation ta_srl : tas_srl) {
			if (ta_srl.getText().equals(text)) {
				System.out.println("Match");
				return ta_srl;
			}
		}
		System.out.println("No Match");
		return null;
	}

}
