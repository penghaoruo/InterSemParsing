package brainllc.penghaoruo.isp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class MainClass {
	public static void main(String[] args) {
		//annotateQueries();
		//getSemanticParses();
		annotateTest();
	}
	
	public static void annotateTest() {
		AnnotateText annotator = new AnnotateText();
		try {
			annotator.initialize("config/pipeline-config.properties");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Annotator Failure!");
			System.exit(-1);
		}
		
		String path = "../../data/queries/";
		File f = new File(path);
		String[] files = f.list();
		for (String file : files) {
			ArrayList<String> lines = IOManager.readLines(path + file);
			Integer index = 0;
			for (String line : lines) {
				line = line.trim();
				char c = line.charAt(line.length()-1);
				if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
					line = line + ".";
				}
				try {
					TextAnnotation ta = annotator.annotate(file, index.toString(), line);
					if (ta.hasView(ViewNames.SRL_VERB)) {
						System.out.println(line);
						System.out.println(ta.getView(ViewNames.SRL_VERB));
						System.out.flush();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				index += 1;
			}
		}
	}
	
	public static void annotateQueries() {
		AnnotateText annotator = new AnnotateText();
		try {
			annotator.initialize("config/pipeline-config.properties");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Annotator Failure!");
			System.exit(-1);
		}
		
		String path = "../../data/queries/";
		File f = new File(path);
		String[] files = f.list();
		BufferedWriter bw = IOManager.openWriter("annotate_miss.txt");
		for (String file : files) {
			ArrayList<String> lines = IOManager.readLines(path + file);
			ArrayList<TextAnnotation> tas = new ArrayList<TextAnnotation>();
			Integer index = 0;
			for (String line : lines) {
				line = line.trim();
				char c = line.charAt(line.length()-1);
				if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
					line = line + ".";
				}
				try {
					TextAnnotation ta = annotator.annotate(file, index.toString(), line);
					tas.add(ta);
				} catch (Exception e) {
					try {
						bw.write(line + "\n");
						bw.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
				index += 1;
				System.out.println(index);
			}
			try {
				FileSerialization.serialize(file + "-TA", tas);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getSemanticParses() {
		ArrayList<String> lines = IOManager.readLines("list.txt");
		ArrayList<TextAnnotation> tas = null;
		for (String line: lines) {
			try {
				tas = FileSerialization.deserialize(line + "-TA");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedWriter bw = IOManager.openWriter(line + "-ann");
			for (TextAnnotation ta: tas) {
				ArrayList<String> strs = SemanticParse.getParse(ta);
				if (strs == null) {
					continue;
				}
				try {
					for (String str : strs) {
						bw.write(str + "\n");
						bw.flush();
					}
					bw.write("\n");
					bw.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Processing Done!");
		}
	}
}
