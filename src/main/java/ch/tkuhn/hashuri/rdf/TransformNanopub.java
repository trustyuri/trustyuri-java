package ch.tkuhn.hashuri.rdf;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;

import ch.tkuhn.hashuri.HashUriResource;
import ch.tkuhn.nanopub.MalformedNanopubException;
import ch.tkuhn.nanopub.Nanopub;
import ch.tkuhn.nanopub.NanopubImpl;

public class TransformNanopub {

	public static void main(String[] args) throws Exception {
		File inputFile = new File(args[0]);
		HashUriResource r = new HashUriResource(inputFile);
		RdfFileContent content = RdfUtils.load(r);
		Nanopub nanopub = null;
		try {
			nanopub = new NanopubImpl(content.getStatements());
		} catch (MalformedNanopubException ex) {
			System.out.println("ERROR: Malformed nanopub: " + ex.getMessage());
			System.exit(1);
		}
		TransformRdfFile.transform(content, inputFile.getParent(), nanopub.getUri().toString());
	}

	public static URI transform(InputStream in, RDFFormat format, OutputStream out, String baseName) throws Exception {
		return TransformRdfFile.transform(in, format, out, baseName);
	}

	public static URI transform(InputStream in, OutputStream out, String baseName) throws Exception {
		return transform(in, RDFFormat.TRIG, out, baseName);
	}

}
