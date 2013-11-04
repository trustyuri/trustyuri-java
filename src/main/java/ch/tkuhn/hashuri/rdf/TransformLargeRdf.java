package ch.tkuhn.hashuri.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;

import ch.tkuhn.hashuri.HashUriResource;

public class TransformLargeRdf {

	private static final int DEFAULT_TRIPLES_PER_PART = 10000000;

	public static void main(String[] args) throws Exception {
		File inputFile = new File(args[0]);
		String baseName = "";
		if (args.length > 1) {
			baseName = args[1];
		} else {
			baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
		}
		int triplesPerPart;
		if (args.length > 2) {
			triplesPerPart = new Integer(args[2]);
		} else {
			triplesPerPart = DEFAULT_TRIPLES_PER_PART;
		}
		TransformLargeRdf t = new TransformLargeRdf(inputFile, baseName, triplesPerPart);
		t.transform();
	}

	private int triplesPerPart;
	private File inputFile;
	private String inputDir;
	private String baseName;
	private RdfFilter filter;
	private MessageDigest md;
	private URI baseUri;
	private String fileName, ext;
	private int step = 0;
	private Map<String,Integer> blankNodeMap;

	private ResourceComparator rcomp = new ResourceComparator();
	private StatementComparator stcomp = new StatementComparator();

	public TransformLargeRdf(File inputFile, String baseName, int triplesPerPart) {
		this.inputFile = inputFile;
		this.baseName = baseName;
		this.triplesPerPart = triplesPerPart;
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

		blankNodeMap = new HashMap<>();
		RdfSummary summary = RdfUtils.loadSummary(new HashUriResource(inputFile), baseUri, blankNodeMap);
		List<Resource> contexts = summary.getContextList();
		for (int cc = 0; cc < contexts.size(); cc++) {
			URI uri = RdfPreprocessor.transformResource(contexts.get(cc), baseUri, blankNodeMap);
			contexts.set(cc, uri);
		}
		Collections.sort(contexts, rcomp);
		int i = 0;
		blankNodeMap = new HashMap<>();
		filter = new RdfFilter(baseUri, blankNodeMap);
		for (Resource c : contexts) {
			filter.addContext(c);
			List<Resource> subjects = summary.getSubjectList(c);
			for (int sc = 0; sc < subjects.size(); sc++) {
				URI uri = RdfPreprocessor.transformResource(subjects.get(sc), baseUri, blankNodeMap);
				subjects.set(sc, uri);
			}
			Collections.sort(subjects, rcomp);
			for (Resource s : subjects) {
				filter.addSubject(c, s);
				i += summary.getCount(c, s);
				if (i > triplesPerPart) {
					transformPart();
					step++;
					i = 0;
					blankNodeMap = new HashMap<>();
					filter = new RdfFilter(baseUri, blankNodeMap);
				}
			}
		}
		transformPart();
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
		int lastStep = step;
		for (step = 0; step <= lastStep; step++) {
			String f = fileName + ".temp" + step + ".nq";
			File tempFile = new File(inputDir, f);
			InputStream in = new FileInputStream(tempFile);
			RDFParser p = Rio.createParser(RDFFormat.NQUADS);
			p.setRDFHandler(new RDFHandlerBase() {

				@Override
				public void handleStatement(Statement st) throws RDFHandlerException {
					replacer.handleStatement(st);
				}

			});
			p.parse(in, "");
			in.close();
			tempFile.delete();
		}
		replacer.endRDF();

		return RdfUtils.getHashURI(baseUri, baseUri, hash, null);
	}

	private void transformPart() throws Exception {
		RdfFileContent content = RdfUtils.load(new HashUriResource(inputFile), filter);
		content = RdfPreprocessor.run(content, baseUri, blankNodeMap);
		List<Statement> statements = content.getStatements();
		Collections.sort(statements, stcomp);
		for (Statement st : statements) {
			RdfHasher.digest(st, md);
		}
		String f = fileName + ".temp" + step + ".nq";
		OutputStream out = new FileOutputStream(new File(inputDir, f));
		RDFWriter writer = Rio.createWriter(RDFFormat.NQUADS, out);
		content.propagate(writer);
		out.close();
	}

}
