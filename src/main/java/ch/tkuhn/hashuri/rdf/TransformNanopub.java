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
import ch.tkuhn.nanopub.NanopubRdfHandler;
import ch.tkuhn.nanopub.NanopubUtils;

public class TransformNanopub {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("ERROR: No file given");
		}
		for (String arg : args) {
			File inputFile = new File(arg);
			HashUriResource r = new HashUriResource(inputFile);
			RdfFileContent content = RdfUtils.load(r);
			Nanopub nanopub = null;
			try {
				nanopub = new NanopubImpl(content.getStatements());
			} catch (MalformedNanopubException ex) {
				System.out.println("ERROR: Malformed nanopub: " + ex.getMessage());
				System.exit(1);
			}
			TransformRdf.transform(content, inputFile.getParent(), nanopub.getUri().toString());
		}
	}

	public static URI transform(InputStream in, RDFFormat format, OutputStream out, String baseName) throws Exception {
		return TransformRdf.transform(in, format, out, baseName);
	}

	public static URI transform(InputStream in, OutputStream out, String baseName) throws Exception {
		return transform(in, RDFFormat.TRIG, out, baseName);
	}

	public static Nanopub transform(Nanopub nanopub, String baseName) throws Exception {
		RdfFileContent r = new RdfFileContent(RDFFormat.TRIG);
		NanopubUtils.propagateToHandler(nanopub, r);
		NanopubRdfHandler h = new NanopubRdfHandler();
		TransformRdf.transform(r, h, baseName);
		return h.getNanopub();
	}

}
