package ch.tkuhn.hashrdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.openrdf.rio.trig.TriGParser;

public class FileUtils {

	private FileUtils() {}  // no instances allowed

	public static RDFGraphs loadFile(File file) throws Exception {
		InputStream in = new FileInputStream(file);
		TriGParser p = new TriGParser();
		RDFGraphs graphs = new RDFGraphs();
		p.setRDFHandler(graphs);
		p.parse(in, "");
		in.close();
		return graphs;
	}

}
