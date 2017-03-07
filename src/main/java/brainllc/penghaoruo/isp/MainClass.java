package brainllc.penghaoruo.isp;

import java.io.IOException;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.pipeline.main.PipelineFactory;

public class MainClass {
	public static void main(String[] args) throws IOException, AnnotatorException {
		String docId = "APW-20140101.3018"; // arbitrary string identifier
		String textId = "body"; // arbitrary string identifier
		String text = "A squirrel is storing a lot of nuts to prepare for a seasonal change in the environment."; // contains plain text to be annotated

		ResourceManager userConfig = new ResourceManager("config/pipeline-config.properties");
		AnnotatorService pipeline = PipelineFactory.buildPipeline(userConfig);
		TextAnnotation ta = pipeline.createAnnotatedTextAnnotation(docId, textId, text);
		System.out.println(ta.getAvailableViews());
	}

}
