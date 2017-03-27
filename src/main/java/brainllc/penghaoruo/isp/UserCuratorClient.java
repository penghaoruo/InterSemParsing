package brainllc.penghaoruo.isp;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;

public class UserCuratorClient {
	static AnnotatorService annotator = null; 
	
	public static void init() {
		try {
			annotator = CuratorFactory.buildCuratorClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static TextAnnotation annotate(String corpusID, String taID, String text) {
		TextAnnotation ta = null;
		try {
			ta = annotator.createBasicTextAnnotation(corpusID, taID, text);
			annotator.addView(ta, ViewNames.SRL_VERB);
		} catch (AnnotatorException e) {
			e.printStackTrace();
		}
		return ta;
	}
}

