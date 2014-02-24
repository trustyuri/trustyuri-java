package net.trustyuri.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;

import net.trustyuri.TrustyUriResource;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.helpers.RDFaParserSettings;


import com.google.code.externalsorting.ExternalSort;

public class CheckLargeRdf {

	public static void main(String[] args) throws Exception {
		File file = new File(args[0]);
		CheckLargeRdf t = new CheckLargeRdf(file);
		boolean valid = t.check();
		if (valid) {
			System.out.println("Correct hash: " + t.hash);
		} else {
			System.out.println("*** INCORRECT HASH ***");
		}
	}

	private File file;
	private MessageDigest md;
	private String hash;

	public CheckLargeRdf(File file) {
		this.file = file;
	}

	public boolean check() throws Exception {
		TrustyUriResource r = new TrustyUriResource(file);
		File dir = file.getParentFile();
		String fileName = file.getName();
		md = RdfHasher.getDigest();
		RDFFormat format = r.getFormat(RDFFormat.TURTLE);

		RDFParser p = Rio.createParser(format);
		p.getParserConfig().set(RDFaParserSettings.FAIL_ON_RDFA_UNDEFINED_PREFIXES, true);
		File sortInFile = new File(dir, fileName + ".temp.sort-in");
		final FileOutputStream preOut = new FileOutputStream(sortInFile);
		p.setRDFHandler(new RdfPreprocessor(new RDFHandlerBase() {
			
			@Override
			public void handleStatement(Statement st) throws RDFHandlerException {
				String s = SerStatementComparator.toString(st) + "\n";
				try {
					preOut.write(s.getBytes());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

		}, r.getHash()));
		BufferedReader reader = new BufferedReader(new InputStreamReader(r.getInputStream()), 64*1024);
		try {
			p.parse(reader, "");
		} finally {
			reader.close();
			preOut.close();
		}

		File sortOutFile = new File(dir, fileName + ".temp.sort-out");
		File sortTempDir = new File(dir, fileName + ".temp");
		sortTempDir.mkdir();
		Comparator<String> cmp = new SerStatementComparator();
		Charset cs = Charset.defaultCharset();
		System.gc();
		List<File> tempFiles = ExternalSort.sortInBatch(sortInFile, cmp, 1024, cs, sortTempDir, false);
		ExternalSort.mergeSortedFiles(tempFiles, sortOutFile, cmp, cs);
		sortInFile.delete();
		sortTempDir.delete();

		BufferedReader br = new BufferedReader(new FileReader(sortOutFile));
		String line;
		while ((line = br.readLine()) != null) {
			Statement st = SerStatementComparator.fromString(line);
			RdfHasher.digest(st, md);
		}
		br.close();
		sortOutFile.delete();

		hash = RdfHasher.getHash(md);
		return hash.equals(r.getHash());
	}

}
