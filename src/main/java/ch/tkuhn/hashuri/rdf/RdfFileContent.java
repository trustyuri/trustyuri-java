package ch.tkuhn.hashuri.rdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class RdfFileContent implements RDFHandler {
	
	private static Map<Value,Value> rdfEntityMap = new HashMap<>();
	
	private RDFFormat originalFormat = null;
	private List<Object> objects;
	private List<Statement> statements;

	public RdfFileContent(RDFFormat originalFormat) {
		this.originalFormat = originalFormat;
	}
	
	@Override
	public void startRDF() throws RDFHandlerException {
		objects = new ArrayList<>();
		statements = new ArrayList<>();
	}

	@Override
	public void endRDF() throws RDFHandlerException {
	}

	@Override
	public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
		objects.add(Pair.of(prefix, uri));
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		Resource subj = (Resource) rdfEntityMap.get(st.getSubject());
		if (subj == null) {
			subj = st.getSubject();
			rdfEntityMap.put(subj, subj);
		}
		URI pred = (URI) rdfEntityMap.get(st.getPredicate());
		if (pred == null) {
			pred = st.getPredicate();
			rdfEntityMap.put(pred, pred);
		}
		Value obj = (Value) rdfEntityMap.get(st.getObject());
		if (obj == null) {
			obj = st.getObject();
			rdfEntityMap.put(obj, obj);
		}
		if (st.getContext() == null) {
			st = new StatementImpl(subj, pred, obj);
		} else {
			Resource context = (Resource) rdfEntityMap.get(st.getContext());
			if (context == null) {
				context = st.getContext();
				rdfEntityMap.put(context, context);
			}
			st = new ContextStatementImpl(subj, pred, obj, context);
		}
		objects.add(st);
		statements.add(st);
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		objects.add(comment);
	}
	
	public List<Statement> getStatements() {
		return new ArrayList<>(statements);
	}

	public void propagate(RDFHandler handler) throws RDFHandlerException {
		handler.startRDF();
		for (Object obj : objects) {
			if (obj instanceof Pair) {
				@SuppressWarnings("unchecked")
				Pair<String,String> ns = (Pair<String, String>) obj;
				handler.handleNamespace(ns.getLeft(), ns.getRight());
			} else if (obj instanceof Statement) {
				handler.handleStatement((Statement) obj);
			} else {
				handler.handleComment((String) obj);
			}
		}
		handler.endRDF();
	}

	public RDFFormat getOriginalFormat() {
		return originalFormat;
	}

}
