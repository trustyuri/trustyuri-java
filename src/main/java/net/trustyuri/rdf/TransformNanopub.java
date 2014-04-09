package net.trustyuri.rdf;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import net.trustyuri.TrustyUriResource;

import org.nanopub.MalformedNanopubException;
import org.nanopub.Nanopub;
import org.nanopub.NanopubImpl;
import org.nanopub.NanopubRdfHandler;
import org.nanopub.NanopubUtils;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;


public class TransformNanopub {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println("ERROR: No file given");
		}
		for (String arg : args) {
			File inputFile = new File(arg);
			TrustyUriResource r = new TrustyUriResource(inputFile);
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

	public static Nanopub transform(Nanopub nanopub) throws Exception {
		RdfFileContent r = new RdfFileContent(RDFFormat.TRIG);
		NanopubUtils.propagateToHandler(nanopub, r);
		NanopubRdfHandler h = new NanopubRdfHandler();
		TransformRdf.transform(r, h, nanopub.getUri().toString());
		return h.getNanopub();
	}

}
