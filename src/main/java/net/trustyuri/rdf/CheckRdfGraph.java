package net.trustyuri.rdf;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import net.trustyuri.TrustyUriResource;
import net.trustyuri.TrustyUriUtils;

public class CheckRdfGraph {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			throw new Exception("Not enough arguments: <file> <graph-uri1> (<graph-uri2> ...)");
		}
		String fileName = args[0];
		CheckRdfGraph c;
		try {
			URL url = new URL(fileName);
			c = new CheckRdfGraph(url);
		} catch (MalformedURLException ex) {
			c = new CheckRdfGraph(new File(fileName));
		}
		for (int i = 1 ; i < args.length ; i++) {
			URI graphUri = new URIImpl(args[i]);
			boolean valid = c.check(graphUri);
			if (valid) {
				System.out.println("Correct hash: " + getArtifactCode(graphUri));
			} else {
				System.out.println("*** INCORRECT HASH ***");
			}
		}
	}

	private TrustyUriResource r;
	private RdfFileContent content;

	public CheckRdfGraph(URL url) throws Exception {
		r = new TrustyUriResource(url);
		init();
	}

	public CheckRdfGraph(File file) throws Exception {
		r = new TrustyUriResource(file);
		init();
	}

	private void init() throws Exception {
		content = RdfUtils.load(r);
	}

	public boolean check(URI graphUri) throws Exception {
		String artifactCode = getArtifactCode(graphUri);
		if (artifactCode == null) {
			throw new Exception("Not a trusty URI: " + graphUri);
		}
		if (!TrustyUriUtils.getModuleId(artifactCode).equals(RdfGraphModule.MODULE_ID)) {
			throw new Exception("Not a trusty URI of type " + RdfGraphModule.MODULE_ID + ": " + graphUri);
		}
		List<Statement> graph = new ArrayList<Statement>();
		for (Statement st : content.getStatements()) {
			if (graphUri.equals(st.getContext())) graph.add(st);
		}
		graph = RdfPreprocessor.run(graph, artifactCode);
		String ac = RdfHasher.makeGraphArtifactCode(graph);
		return artifactCode.equals(ac);
	}

	private static String getArtifactCode(URI graphUri) {
		return TrustyUriUtils.getArtifactCode(graphUri.stringValue());
	}

}
