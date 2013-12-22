package ch.tkuhn.hashuri.rdf;

import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.helpers.RDFaParserSettings;

import ch.tkuhn.hashuri.HashUriResource;

public class CheckSortedRdf {

	public static void main(String[] args) throws Exception {
		File file = new File(args[0]);
		CheckSortedRdf ch = new CheckSortedRdf(file);
		boolean isCorrect = ch.check();
		if (isCorrect) {
			System.out.println("Correct hash: " + ch.getHash());
		} else {
			System.out.println("*** INCORRECT HASH ***");
		}
	}

	private File file;
	private MessageDigest md;
	private Statement previous;
	private HashUriResource r;

	public CheckSortedRdf(File file) {
		this.file = file;
	}

	public boolean check() throws Exception {
		md = RdfHasher.getDigest();
		r = new HashUriResource(file);
		if (r.getHash() == null) {
			System.out.println("ERROR: Not a hash-URI or unknown algorithm");
			System.exit(1);
		}
		String algorithmID = r.getHash().substring(0, 2);
		if (!algorithmID.equals(RdfModule.MODULE_ID)) {
			System.out.println("ERROR: Unsupported algorithm: " + algorithmID +
					" (this function only supports " + RdfModule.MODULE_ID + ")");
			System.exit(1);
		}
		RDFFormat format = r.getFormat(RDFFormat.TURTLE);

		InputStream in = r.getInputStream();
		RDFParser p = Rio.createParser(format);
		previous = null;
		p.getParserConfig().set(RDFaParserSettings.FAIL_ON_RDFA_UNDEFINED_PREFIXES, true);
		p.setRDFHandler(new RdfPreprocessor(new RDFHandlerBase() {

			@Override
			public void handleStatement(Statement st) throws RDFHandlerException {
				if (previous != null && StatementComparator.compareStatement(previous, st) > 0) {
					throw new RuntimeException("File not sorted");
				}
				RdfHasher.digest(st, md);
				previous = st;
			}

		}, r.getHash()));
		p.parse(in, "");
		in.close();

		String hash = RdfHasher.getHash(md);
		return hash.equals(r.getHash());
	}

	public String getHash() {
		return r.getHash();
	}

}