package ch.tkuhn.hashuri.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class RdfSummary implements RDFHandler {

	private RDFFormat format = null;
	private URI baseUri;
	private Map<String,Integer> blankNodeMap;
	private List<Pair<String,String>> namespaces;
	private Map<Resource,Map<Resource,Integer>> map;

	public RdfSummary(RDFFormat format, URI baseUri, Map<String,Integer> blankNodeMap) {
		this.format = format;
		this.baseUri = baseUri;
		this.blankNodeMap = blankNodeMap;
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		namespaces = new ArrayList<>();
		map = new HashMap<>();
	}

	@Override
	public void endRDF() throws RDFHandlerException {
	}

	@Override
	public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
		namespaces.add(Pair.of(prefix, uri));
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		Resource c = RdfPreprocessor.transformResource(st.getContext(), baseUri, blankNodeMap);
		Resource context = (Resource) RdfFileContent.rdfEntityMap.get(c);
		if (!map.containsKey(context)) {
			map.put(context, new HashMap<Resource,Integer>());
		}
		Resource s = RdfPreprocessor.transformResource(st.getSubject(), baseUri, blankNodeMap);
		RdfPreprocessor.transformResource(st.getPredicate(), baseUri, blankNodeMap);
		if (st.getObject() instanceof Resource) {
			RdfPreprocessor.transformResource((Resource) st.getObject(), baseUri, blankNodeMap);
		}
		Resource subj = (Resource) RdfFileContent.rdfEntityMap.get(s);
		if (subj == null) {
			subj = s;
			RdfFileContent.rdfEntityMap.put(subj, subj);
		}
		Map<Resource,Integer> subjMap = map.get(context);
		if (!subjMap.containsKey(subj)) {
			subjMap.put(subj, 1);
		} else {
			subjMap.put(subj, subjMap.get(subj) + 1);
		}
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		// Ignore comments
	}

	public List<Resource> getContextList() {
		return new ArrayList<>(map.keySet());
	}

	public List<Resource> getSubjectList(Resource context) {
		return new ArrayList<>(map.get(context).keySet());
	}

	public int getCount(Resource context, Resource subject) {
		return map.get(context).get(subject);
	}

	public RDFFormat getFormat() {
		return format;
	}

}
