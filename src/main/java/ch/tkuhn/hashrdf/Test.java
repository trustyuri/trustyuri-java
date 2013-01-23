package ch.tkuhn.hashrdf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.trig.TriGParser;
import org.openrdf.rio.trig.TriGWriter;

public class Test {

	public static void main(String[] args) throws Exception {
		String inputFileName = args[0];
		String baseName = "";
		if (args.length > 1) {
			baseName = args[1];
		}
		String name = baseName;
		URI baseURI = null;
		if (baseName.indexOf("/") > 0) {
			baseURI = new URIImpl(baseName);
			name = baseName.replaceFirst("^.*[^A-Za-z0-9.-_]([A-Za-z0-9.-_]*)$", "$1");
		}
		
		InputStream in = new FileInputStream(inputFileName);
		TriGParser p = new TriGParser();
		RDFGraphs graphs = new RDFGraphs();
		p.setRDFHandler(graphs);
		p.parse(in, "");
		in.close();
		Hasher hasher = new Hasher(baseURI);
		String h = hasher.makeHash(graphs.getStatements());
		String suffix;
		if (name.length() == 0) {
			suffix = h;
		} else {
			suffix = "." + h;
		}
		OutputStream out = new FileOutputStream(name + suffix);
		URI hashURI = null;
		if (baseURI != null) {
			hashURI = new URIImpl(baseURI + suffix);
		}
		TriGWriter writer = new CustomTriGWriter(out);
		URIReplacer replacer = new URIReplacer(baseURI, hashURI, writer);
		graphs.propagate(replacer);
		out.close();
	}

}
