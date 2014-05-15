package net.trustyuri.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;

import org.nanopub.MultiNanopubRdfHandler;
import org.nanopub.MultiNanopubRdfHandler.NanopubHandler;
import org.nanopub.Nanopub;
import org.nanopub.NanopubUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;


public class TransformMultiNanopub {

	public static void main(String[] args) throws IOException, TrustyUriException {
		if (args.length == 0) {
			System.out.println("ERROR: No file given");
		}
		for (String arg : args) {
			File file = new File(arg);
			InputStream in = new FileInputStream(file);
			OutputStream out = new FileOutputStream(file.getParent() + "/trusty." + file.getName());
			TrustyUriResource r = new TrustyUriResource(new File(arg));
			RDFFormat format = r.getFormat(RDFFormat.TRIG);
			transform(format, in, out);
		}
	}

	public static void transform(final RDFFormat format, InputStream in, final OutputStream out)
			throws IOException, TrustyUriException {
		RDFParser p = RdfUtils.getParser(format);
		p.setRDFHandler(new MultiNanopubRdfHandler(new NanopubHandler() {

			@Override
			public void handleNanopub(Nanopub np) {
				try {
					np = TransformNanopub.transform(np);
					RDFWriter w = Rio.createWriter(format, out);
					NanopubUtils.propagateToHandler(np, w);
				} catch (RDFHandlerException ex) {
					throw new RuntimeException(ex);
				} catch (TrustyUriException ex) {
					throw new RuntimeException(ex);
				}
			}

		}));
		try {
			p.parse(in, "");
		} catch (OpenRDFException ex) {
			throw new RuntimeException(ex);
		}
		in.close();
		out.close();
	}

}
