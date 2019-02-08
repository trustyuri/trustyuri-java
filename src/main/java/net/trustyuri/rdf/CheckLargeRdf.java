package net.trustyuri.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import com.google.code.externalsorting.ExternalSort;

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;

public class CheckLargeRdf {

	public static void main(String[] args) throws IOException, TrustyUriException {
		File file = new File(args[0]);
		CheckLargeRdf t = new CheckLargeRdf(file);
		boolean valid = t.check();
		if (valid) {
			System.out.println("Correct hash: " + t.ac);
		} else {
			System.out.println("*** INCORRECT HASH ***");
		}
	}

	private File file;
	private MessageDigest md;
	private String ac;

	public CheckLargeRdf(File file) {
		this.file = file;
	}

	public boolean check() throws IOException, TrustyUriException {
		TrustyUriResource r = new TrustyUriResource(file);
		File dir = file.getParentFile();
		String fileName = file.getName();
		md = RdfHasher.getDigest();
		RDFFormat format = r.getFormat(RDFFormat.TURTLE);

		RDFParser p = RdfUtils.getParser(format);
		File sortInFile = new File(dir, fileName + ".temp.sort-in");
		final FileOutputStream preOut = new FileOutputStream(sortInFile);
		p.setRDFHandler(new RdfPreprocessor(new AbstractRDFHandler() {

			@Override
			public void handleStatement(Statement st) throws RDFHandlerException {
				String s = SerStatementComparator.toString(st) + "\n";
				try {
					preOut.write(s.getBytes(Charset.forName("UTF-8")));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

		}, r.getArtifactCode()));
		BufferedReader reader = new BufferedReader(r.getInputStreamReader(), 64*1024);
		try {
			p.parse(reader, "");
		} catch (RDF4JException ex) {
			throw new TrustyUriException(ex);
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
		Statement previous = null;
		while ((line = br.readLine()) != null) {
			Statement st = SerStatementComparator.fromString(line);
			if (!st.equals(previous)) {
				RdfHasher.digest(st, md);
			}
			previous = st;
		}
		br.close();
		sortOutFile.delete();

		ac = RdfHasher.getArtifactCode(md);
		return ac.equals(r.getArtifactCode());
	}

}
