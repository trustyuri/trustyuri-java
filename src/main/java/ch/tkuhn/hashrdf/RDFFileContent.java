package ch.tkuhn.hashrdf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class RDFFileContent implements RDFHandler {
	
	private List<Object> objects;
	private List<Statement> statements;

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

}
