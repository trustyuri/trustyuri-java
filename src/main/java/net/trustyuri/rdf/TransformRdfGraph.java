package net.trustyuri.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;

public class TransformRdfGraph {

	// TODO only transform blank nodes that appear within the given graph

	public static void main(String[] args) throws IOException, TrustyUriException {
		if (args.length < 2) {
			throw new RuntimeException("Not enough arguments: <file> <graph-uri1> (<graph-uri2> ...)");
		}
		File inputFile = new File(args[0]);
		List<IRI> baseUris = new ArrayList<IRI>();
		for (int i = 1 ; i < args.length ; i++) {
			String arg = args[i];
			if (arg.contains("://")) {
				baseUris.add(SimpleValueFactory.getInstance().createIRI(arg));
			} else {
				BufferedReader reader = new BufferedReader(new FileReader(arg));
				String line;
				while ((line = reader.readLine()) != null) {
				   baseUris.add(SimpleValueFactory.getInstance().createIRI(line));
				}
				reader.close();
			}
		}
		RdfFileContent content = RdfUtils.load(new TrustyUriResource(inputFile));
		String outputFilePath = inputFile.getPath().replaceFirst("[.][^.]+$", "") + ".t";
		RDFFormat format = content.getOriginalFormat();
		if (!format.getFileExtensions().isEmpty()) {
			outputFilePath += "." + format.getFileExtensions().get(0);
		}
		transform(content, new File(outputFilePath), baseUris.toArray(new IRI[baseUris.size()]));
	}

	public static void transform(RdfFileContent content, File outputFile, IRI... baseUris)
			throws IOException, TrustyUriException {
		try {
			OutputStream out = new FileOutputStream(outputFile);
			processBaseUris(content, Rio.createWriter(content.getOriginalFormat(), new OutputStreamWriter(out, Charset.forName("UTF-8"))), baseUris);
			out.close();
		} catch (RDFHandlerException ex) {
			throw new TrustyUriException(ex);
		}
	}

	public static void transform(RdfFileContent content, RDFHandler handler, IRI... baseUris)
			throws IOException, TrustyUriException {
		try {
			processBaseUris(content, handler, baseUris);
		} catch (RDFHandlerException ex) {
			throw new TrustyUriException(ex);
		}
	}

	public static void transform(InputStream in, RDFFormat format, OutputStream out, IRI... baseUris)
			throws IOException, TrustyUriException {
		RdfFileContent content = RdfUtils.load(in, format);
		try {
			processBaseUris(content, Rio.createWriter(format, new OutputStreamWriter(out, Charset.forName("UTF-8"))), baseUris);
		} catch (RDFHandlerException ex) {
			throw new TrustyUriException(ex);
		}
		out.close();
	}

	private static void processBaseUris(RdfFileContent content, RDFHandler handler, IRI... baseUris)
			throws RDFHandlerException, TrustyUriException {
		for (IRI baseUri : baseUris) {
			RdfFileContent newContent = new RdfFileContent(content.getOriginalFormat());
			RdfPreprocessor preprocessor = new RdfPreprocessor(newContent, baseUri);
			content.propagate(preprocessor);
			content = newContent;
			String artifactCode = RdfHasher.makeGraphArtifactCode(content.getStatements(), baseUri);
			newContent = new RdfFileContent(content.getOriginalFormat());
			HashAdder hashAdder = new HashAdder(baseUri, artifactCode, newContent, null);
			content.propagate(hashAdder);
			content = newContent;
		}
		content.propagate(handler);
	}

}
