package brainllc.penghaoruo.isp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class FileSerialization {
	public static void serialize(String fileName, ArrayList<TextAnnotation> tas) throws IOException {	
		FileOutputStream fileOut = new FileOutputStream(fileName);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(tas);
		System.out.println("Done writing the data to " + fileName);
		out.close();
		fileOut.close();
	}
	
	public static ArrayList<TextAnnotation> deserialize(String fileName) throws IOException, ClassNotFoundException {	
		FileInputStream fileIn = new FileInputStream(fileName);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		ArrayList<TextAnnotation> tas = (ArrayList<TextAnnotation>) in.readObject();
		System.out.println("Done reading the data from " + fileName);
		in.close();
		fileIn.close();
		return tas;
	}

	public static void serializeTAS(String fileName, ArrayList<ArrayList<TextAnnotation>> tas) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(fileName);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(tas);
		System.out.println("Done writing the data to " + fileName);
		out.close();
		fileOut.close();
	}
	
	public static ArrayList<ArrayList<TextAnnotation>> deserializeTAS(String fileName) throws IOException, ClassNotFoundException {	
		FileInputStream fileIn = new FileInputStream(fileName);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		ArrayList<ArrayList<TextAnnotation>> tas = (ArrayList<ArrayList<TextAnnotation>>) in.readObject();
		System.out.println("Done reading the data from " + fileName);
		in.close();
		fileIn.close();
		return tas;
	}
}
