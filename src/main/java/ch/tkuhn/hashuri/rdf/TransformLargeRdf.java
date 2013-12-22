package ch.tkuhn.hashuri.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.helpers.RDFaParserSettings;

import ch.tkuhn.hashuri.HashUriResource;

import com.google.code.externalsorting.ExternalSort;

public class TransformLargeRdf {

	public static void main(String[] args) throws Exception {
		File inputFile = new File(args[0]);
		String baseName = "";
		if (args.length > 1) {
			baseName = args[1];
		} else {
			baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
		}
		TransformLargeRdf t = new TransformLargeRdf(inputFile, baseName);
		t.transform();
	}

	private File inputFile;
	private String inputDir;
	private String baseName;
	private MessageDigest md;
	private URI baseUri;
	private String fileName, ext;

	public TransformLargeRdf(File inputFile, String baseName) {
		this.inputFile = inputFile;
		this.baseName = baseName;
	}

	public URI transform() throws Exception {
		baseUri = TransformRdf.getBaseURI(baseName);
		md = RdfHasher.getDigest();
		inputDir = inputFile.getParent();
		HashUriResource r = new HashUriResource(inputFile);
		RDFFormat format = r.getFormat(RDFFormat.TURTLE);

		String name = baseName;
		if (baseName.indexOf("/") > 0) {
			name = baseName.replaceFirst("^.*[^A-Za-z0-9.\\-_]([A-Za-z0-9.\\-_]*)$", "$1");
		}
		fileName = name;
		ext = "";
		if (!format.getFileExtensions().isEmpty()) {
			ext = "." + format.getFileExtensions().get(0);
		}

		RDFParser p = Rio.createParser(format);
		p.getParserConfig().set(RDFaParserSettings.FAIL_ON_RDFA_UNDEFINED_PREFIXES, true);
		File sortInFile = new File(inputDir, fileName + ".temp.sort-in");
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

		}, baseUri));
		BufferedReader reader = new BufferedReader(new InputStreamReader(r.getInputStream()), 64*1024);
		p.parse(reader, "");
		reader.close();
		preOut.close();

		File sortOutFile = new File(inputDir, fileName + ".temp.sort-out");
		File sortTempDir = new File(inputDir, fileName + ".temp");
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

		String hash = RdfHasher.getHash(md);
		String hashFileName = fileName;
		if (hashFileName.length() == 0) {
			hashFileName = hash + ext;
		} else {
			hashFileName += "." + hash + ext;
		}
		OutputStream out = new FileOutputStream(new File(inputDir, hashFileName));
		RDFWriter writer = Rio.createWriter(format, out);
		final HashAdder replacer = new HashAdder(baseUri, hash, writer, null);

		replacer.startRDF();
		br = new BufferedReader(new FileReader(sortOutFile));
		while ((line = br.readLine()) != null) {
			Statement st = SerStatementComparator.fromString(line);
			replacer.handleStatement(st);
		}
		br.close();
		replacer.endRDF();
		sortOutFile.delete();

		return RdfUtils.getHashURI(baseUri, baseUri, hash, null);
	}

}
