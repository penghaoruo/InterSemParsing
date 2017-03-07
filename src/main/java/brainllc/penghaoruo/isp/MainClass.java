package brainllc.penghaoruo.isp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class MainClass {
	public static void main(String[] args) {
		annotateQueries();
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
}
