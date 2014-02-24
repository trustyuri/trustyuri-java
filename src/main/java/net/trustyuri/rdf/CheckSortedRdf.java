package net.trustyuri.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.security.MessageDigest;

import net.trustyuri.TrustyUriResource;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.helpers.RDFaParserSettings;


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
	private TrustyUriResource r;

	public CheckSortedRdf(File file) {
		this.file = file;
	}

	public boolean check() throws Exception {
		md = RdfHasher.getDigest();
		r = new TrustyUriResource(file);
		if (r.getHash() == null) {
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(r.getInputStream()), 64*1024);
		try {
			p.parse(reader, "");
		} finally {
			reader.close();
		}

		String hash = RdfHasher.getHash(md);
		return hash.equals(r.getHash());
	}

	public String getHash() {
		return r.getHash();
	}

}