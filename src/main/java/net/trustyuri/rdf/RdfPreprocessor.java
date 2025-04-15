package net.trustyuri.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import net.trustyuri.ModuleDirectory;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriModule;

public class RdfPreprocessor implements RDFHandler {

	// TODO check that already hashed graphs are not changed

	private RDFHandler nestedHandler;
	private IRI baseUri;
	private String artifactCode;
	private Map<String,Integer> blankNodeMap;
	private TrustyUriModule moduleRB;
	private Map<Resource,IRI> transformMap;
	private TransformRdfSetting setting;

	public static RdfFileContent run(RdfFileContent content, IRI baseUri, TransformRdfSetting setting) throws TrustyUriException {
		RdfFileContent p = new RdfFileContent(content.getOriginalFormat());
		try {
			content.propagate(new RdfPreprocessor(p, baseUri, setting));
		} catch (RDFHandlerException ex) {
			throw new TrustyUriException(ex);
		}
		return p;
	}

	public static RdfFileContent run(RdfFileContent content, String artifactCode) throws TrustyUriException {
		RdfFileContent p = new RdfFileContent(content.getOriginalFormat());
		try {
			content.propagate(new RdfPreprocessor(p, artifactCode));
		} catch (RDFHandlerException ex) {
			throw new TrustyUriException(ex);
		}
		return p;
	}

	public static List<Statement> run(List<Statement> statements, IRI baseUri, TransformRdfSetting setting) {
		return run(statements, baseUri, null, setting);
	}

	public static List<Statement> run(List<Statement> statements, String artifactCode) {
		return run(statements, null, artifactCode, null);
	}

	private static List<Statement> run(List<Statement> statements, IRI baseUri, String artifactCode, TransformRdfSetting setting) {
		List<Statement> r = new ArrayList<Statement>();
		RdfPreprocessor obj = new RdfPreprocessor(baseUri, artifactCode, setting);
		for (Statement st : statements) {
			r.add(obj.preprocess(st));
		}
		return r;
	}

	private RdfPreprocessor(IRI baseUri, String artifactCode, TransformRdfSetting setting) {
		this.baseUri = baseUri;
		this.artifactCode = artifactCode;
		this.setting = setting;
		init();
	}

	public RdfPreprocessor(RDFHandler nestedHandler, IRI baseUri, TransformRdfSetting setting) {
		this(nestedHandler, baseUri, null, setting);
	}

	public RdfPreprocessor(RDFHandler nestedHandler, String artifactCode) {
		this(nestedHandler, artifactCode, null);
	}

	public RdfPreprocessor(RDFHandler nestedHandler, IRI baseUri, Map<String,Integer> blankNodeMap, TransformRdfSetting setting) {
		this.nestedHandler = nestedHandler;
		this.baseUri = baseUri;
		this.artifactCode = null;
		this.blankNodeMap = blankNodeMap;
		this.setting = setting;
		init();
	}

	public RdfPreprocessor(RDFHandler nestedHandler, String artifactCode, Map<String,Integer> blankNodeMap) {
		this.nestedHandler = nestedHandler;
		this.baseUri = null;
		this.artifactCode = artifactCode;
		this.blankNodeMap = blankNodeMap;
		this.setting = null;
		init();
	}

	private void init() {
		if (blankNodeMap == null) {
			this.blankNodeMap = new HashMap<String,Integer>();
		}
		moduleRB = ModuleDirectory.getModule(RdfGraphModule.MODULE_ID);
		transformMap = new HashMap<Resource,IRI>();
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		nestedHandler.startRDF();
	}

	@Override
	public void endRDF() throws RDFHandlerException {
		nestedHandler.endRDF();
	}

	@Override
	public void handleNamespace(String prefix, String uri)
			throws RDFHandlerException {
		nestedHandler.handleNamespace(prefix, uri);
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		nestedHandler.handleStatement(preprocess(st));
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		nestedHandler.handleComment(comment);
	}

	public Map<Resource,IRI> getTransformMap() {
		return transformMap;
	}

	private Statement preprocess(Statement st) {
		Resource context = st.getContext();
		IRI trustyGraph = null;
		if (context != null) {
			if (context instanceof IRI && moduleRB.matches((IRI) context)) {
				trustyGraph = (IRI) context;
			}
			context = transform(context, trustyGraph);
		}
		Resource subject = transform(st.getSubject(), trustyGraph);
		IRI predicate = transform(st.getPredicate(), trustyGraph);
		Value object = st.getObject();
		if (object instanceof Resource) {
			object = transform((Resource) object, trustyGraph);
		}
		return SimpleValueFactory.getInstance().createStatement(subject, predicate, object, context);
	}

	private IRI transform(Resource r, IRI trustyGraph) {
		if (baseUri == null) {
			RdfUtils.checkUri((IRI) r);
			return SimpleValueFactory.getInstance().createIRI(RdfUtils.normalize((IRI) r, artifactCode));
		}
		IRI uri = RdfUtils.getPreUri(r, baseUri, blankNodeMap, trustyGraph != null, setting);
		if (uri == null) {
			// TODO Allow for 'force' option; URI might only look like a trusty URI...
			throw new RuntimeException("Transformation would break existing trusty URI graph: " +
					trustyGraph);
		} else if (!r.toString().equals(uri.toString())) {
			transformMap.put(r, uri);
		}
		return uri;
	}

}
