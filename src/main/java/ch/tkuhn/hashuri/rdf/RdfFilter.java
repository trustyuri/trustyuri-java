package ch.tkuhn.hashuri.rdf;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class RdfFilter {

	private URI baseUri;
	private Map<String,Integer> blankNodeMap;

	private Map<Resource,Map<Resource,Void>> map = new HashMap<>();

	public RdfFilter(URI baseUri, Map<String,Integer> blankNodeMap) {
		this.baseUri = baseUri;
		this.blankNodeMap = blankNodeMap;
	}

	public void addContext(Resource context) {
		map.put(context, new HashMap<Resource,Void>());
	}

	public void addSubject(Resource context, Resource subject) {
		if (!map.containsKey(context)) {
			addContext(context);
		}
		map.get(context).put(subject, null);
	}

	public boolean containsContext(Resource context) {
		return map.containsKey(context);
	}

	public boolean containsSubject(Resource context, Resource subject) {
		if (!containsContext(context)) return false;
		return map.get(context).containsKey(subject);
	}

	public boolean matches(Resource context, Resource subj, URI pred, Value obj) {
		// Transform all to update the blank node map:
		Resource c = RdfPreprocessor.transformResource(context, baseUri, blankNodeMap);
		Resource s = RdfPreprocessor.transformResource(subj, baseUri, blankNodeMap);
		RdfPreprocessor.transformResource(pred, baseUri, blankNodeMap);
		if (obj instanceof Resource) {
			RdfPreprocessor.transformResource((Resource) obj, baseUri, blankNodeMap);
		}
		return containsSubject(c, s);
	}

}
