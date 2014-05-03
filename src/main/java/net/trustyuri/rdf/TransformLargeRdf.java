package net.trustyuri.rdf;

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

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.helpers.RDFaParserSettings;


import com.google.code.externalsorting.ExternalSort;

public class TransformLargeRdf {

	public static void main(String[] args) throws IOException, TrustyUriException {
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

	public URI transform() throws IOException, TrustyUriException {
		baseUri = TransformRdf.getBaseURI(baseName);
		md = RdfHasher.getDigest();
		inputDir = inputFile.getParent();
		TrustyUriResource r = new TrustyUriResource(inputFile);
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
		try {
			p.parse(reader, "");
		} catch (OpenRDFException ex) {
			throw new TrustyUriException(ex);
		} finally {
			reader.close();
			preOut.close();
		}

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

		String artifactCode = RdfHasher.getArtifactCode(md);
		String acFileName = fileName;
		if (acFileName.length() == 0) {
			acFileName = artifactCode + ext;
		} else {
			acFileName += "." + artifactCode + ext;
		}
		OutputStream out = new FileOutputStream(new File(inputDir, acFileName));
		RDFWriter writer = Rio.createWriter(format, out);
		final HashAdder replacer = new HashAdder(baseUri, artifactCode, writer, null);

		br = new BufferedReader(new FileReader(sortOutFile));
		try {
			replacer.startRDF();
			while ((line = br.readLine()) != null) {
				Statement st = SerStatementComparator.fromString(line);
				replacer.handleStatement(st);
			}
			replacer.endRDF();
		} catch (RDFHandlerException ex) {
			throw new TrustyUriException(ex);
		} finally {
			br.close();
			sortOutFile.delete();
		}

		return RdfUtils.getTrustyUri(baseUri, artifactCode);
	}

}
