package brainllc.penghaoruo.isp;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;

public class UserCuratorClient {
	static AnnotatorService annotator = null; 
	
	public static void init() {
		try {
			ResourceManager rm = new ResourceManager("curator-config.properties");
			annotator = CuratorFactory.buildCuratorClient(rm);
			System.out.println(rm.getCuratorHost());
			System.out.println(rm.getCuratorPort());
			//annotator = CuratorFactory.buildCuratorClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static TextAnnotation annotate(String corpusID, String taID, String text) {
		TextAnnotation ta = null;
		try {
			ta = annotator.createBasicTextAnnotation(corpusID, taID, text);
			annotator.addView(ta, ViewNames.LEMMA);
			annotator.addView(ta, ViewNames.POS);
			annotator.addView(ta, ViewNames.SHALLOW_PARSE);
			annotator.addView(ta, ViewNames.NER_CONLL);
			annotator.addView(ta, ViewNames.DEPENDENCY_STANFORD);
			annotator.addView(ta, ViewNames.SRL_VERB);
			annotator.addView(ta, ViewNames.SRL_NOM);
		} catch (AnnotatorException e) {
			e.printStackTrace();
		}
		return ta;
	}
}

