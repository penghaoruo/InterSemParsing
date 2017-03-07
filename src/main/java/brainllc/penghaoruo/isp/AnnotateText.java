package brainllc.penghaoruo.isp;

import java.io.IOException;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;

public class AnnotateText {
	AnnotatorService pipeline = null;
	
	public void initialize(String configFile) throws IOException, AnnotatorException {
		ResourceManager userConfig = new ResourceManager(configFile);
		pipeline = PipelineFactory.buildPipeline(userConfig);
		System.out.println("Annotator Built!");
	}
	
	public TextAnnotation annotate(String docId, String textId, String text) throws AnnotatorException {
		TextAnnotation ta = pipeline.createAnnotatedTextAnnotation(docId, textId, text);
		return ta;
	}
}
