package net.trustyuri.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;

public class CheckSortedRdf {

	public static void main(String[] args) throws IOException, TrustyUriException {
		File file = new File(args[0]);
		CheckSortedRdf ch = new CheckSortedRdf(file);
		boolean isCorrect = ch.check();
		if (isCorrect) {
			System.out.println("Correct hash: " + ch.getArtifactCode());
		} else {
			System.out.println("*** INCORRECT HASH ***");
		}
	}

	private File file;
	private MessageDigest md;
	private Statement previous;
	private TrustyUriResource r;

	public CheckSortedRdf(File file) {
		this.file = file;
	}

	public boolean check() throws IOException, TrustyUriException {
		md = RdfHasher.getDigest();
		r = new TrustyUriResource(file);
		if (r.getArtifactCode() == null) {
			System.out.println("ERROR: Not a trusty URI or unknown module");
			System.exit(1);
		}
		String moduleId = r.getModuleId();
		if (!moduleId.equals(RdfModule.MODULE_ID)) {
			System.out.println("ERROR: Unsupported module: " + moduleId +
					" (this function only supports " + RdfModule.MODULE_ID + ")");
			System.exit(1);
		}
		RDFFormat format = r.getFormat(RDFFormat.TURTLE);

		RDFParser p = RdfUtils.getParser(format);
		previous = null;
		p.setRDFHandler(new RdfPreprocessor(new AbstractRDFHandler() {

			@Override
			public void handleStatement(Statement st) throws RDFHandlerException {
				if (previous != null && StatementComparator.compareStatement(previous, st) > 0) {
					throw new RuntimeException("File not sorted");
				}
				if (!st.equals(previous)) {
					RdfHasher.digest(st, md);
				}
				previous = st;
			}

		}, r.getArtifactCode()));
		BufferedReader reader = new BufferedReader(r.getInputStreamReader(), 64*1024);
		try {
			p.parse(reader, "");
		} catch (RDF4JException ex) {
			throw new TrustyUriException(ex);
		} finally {
			reader.close();
		}

		String artifactCode = RdfHasher.getArtifactCode(md);
		return artifactCode.equals(r.getArtifactCode());
	}

	public String getArtifactCode() {
		return r.getArtifactCode();
	}

}