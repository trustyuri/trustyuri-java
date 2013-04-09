package ch.tkuhn.hashuri.rdf;

import java.io.File;

import ch.tkuhn.nanopub.MalformedNanopubException;
import ch.tkuhn.nanopub.Nanopub;
import ch.tkuhn.nanopub.NanopubImpl;

public class TransformNanopub {

	public static void main(String[] args) throws Exception {
		File inputFile = new File(args[0]);
		RdfFileContent content = RdfUtils.load(inputFile);
		Nanopub nanopub = null;
		try {
			nanopub = new NanopubImpl(content.getStatements());
		} catch (MalformedNanopubException ex) {
			System.out.println("ERROR: Malformed nanopub: " + ex.getMessage());
			System.exit(1);
		}
		TransformRdfFile.transform(content, inputFile.getParent(), nanopub.getUri().toString());
	}

}
