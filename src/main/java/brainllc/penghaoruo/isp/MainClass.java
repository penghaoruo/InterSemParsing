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
		//annotateTest();
		outputSignals("srl");
	}
	
	public static void annotateTest() {
		UserCuratorClient.init();
		
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
					TextAnnotation ta = UserCuratorClient.annotate(file, index.toString(), line);
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
	
	public static void outputSignals(String p) {
		ArrayList<String> lines = IOManager.readLines("list.txt");
		ArrayList<TextAnnotation> tas = null;
		for (String line: lines) {
			String file = line + "-TA";
			if (p.equals("srl")) {
				file = line.replace("txt", "ta-srl");
			}
			try {
				tas = FileSerialization.deserialize(file);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			line = line.substring(0,line.length()-4);
			line = line + "-" + p + ".txt";
			BufferedWriter bw = IOManager.openWriter(line);
			for (TextAnnotation ta: tas) {
				String str = null;
				if (p.equals("phrases")) {
					str = SemanticParse.getPhrases(ta);
				}
				if (p.equals("ner")) {
					str = SemanticParse.getNER(ta);
				}
				if (p.equals("srl")) {
					str = SemanticParse.getSRL(ta);
				}
				try {
					if (str != null) {
						bw.write(ta.getText() + "\n");
						bw.write(str + "\n");
						bw.write("\n");
						bw.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Processing Done!");
		}
	}
}
