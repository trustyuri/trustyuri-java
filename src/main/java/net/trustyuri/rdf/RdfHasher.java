package net.trustyuri.rdf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.trustyuri.TrustyUriUtils;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class RdfHasher {

	private static final boolean DEBUG = false;

	private RdfHasher() {}  // no instances allowed

	public static String makeHash(List<Statement> statements) {
		return getHash(digest(statements));
	}

	public static String makeGraphHash(List<Statement> statements) throws Exception {
		URI graphUri = null;
		List<Statement> graph = new ArrayList<Statement>();
		for (Statement st : statements) {
			Resource c = st.getContext();
			if (c == null) {
				throw new Exception("Graph is null");
			} else if (c instanceof BNode) {
				throw new Exception("Graph is blank node");
			} else if (graphUri != null && !c.equals(graphUri)) {
				throw new Exception("Multiple graphs");
			}
			graphUri = (URI) c;
			graph.add(st);
		}
		if (graph.size() == 0) {
			throw new Exception("Graph not found");
		}
		return getGraphHash(digest(graph));
	}

	public static String makeGraphHash(List<Statement> statements, URI baseUri) throws Exception {
		URI graphUri = RdfUtils.getTrustyUri(baseUri, baseUri, " ", null);
		List<Statement> graph = new ArrayList<Statement>();
		for (Statement st : statements) {
			Resource c = st.getContext();
			if (c != null && c.equals(graphUri)) graph.add(st);
		}
		if (graph.size() == 0) {
			throw new Exception("Graph not found");
		}
		return getGraphHash(digest(graph));
	}

	private static MessageDigest digest(List<Statement> statements) {
		MessageDigest md = getDigest();
		Collections.sort(statements, new StatementComparator());
		if (DEBUG) System.err.println("----------");
		for (Statement st : statements) {
			digest(st, md);
		}
		if (DEBUG) System.err.println("----------");
		return md;
	}

	public static MessageDigest getDigest() {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ex) {}
		return md;
	}

	public static String getHash(MessageDigest md) {
		return RdfModule.MODULE_ID + TrustyUriUtils.getBase64(md.digest());
	}

	public static String getGraphHash(MessageDigest md) {
		return RdfGraphModule.MODULE_ID + TrustyUriUtils.getBase64(md.digest());
	}

	public static void digest(Statement st, MessageDigest md) {
		if (DEBUG) System.err.print(valueToString(st.getContext()));
		md.update(valueToString(st.getContext()).getBytes());
		if (DEBUG) System.err.print(valueToString(st.getSubject()));
		md.update(valueToString(st.getSubject()).getBytes());
		if (DEBUG) System.err.print(valueToString(st.getPredicate()));
		md.update(valueToString(st.getPredicate()).getBytes());
		if (DEBUG) System.err.print(valueToString(st.getObject()));
		md.update(valueToString(st.getObject()).getBytes());
	}

	private static String valueToString(Value v) {
		if (v instanceof URI) {
			return ((URI) v).toString() + "\n";
		} else if (v instanceof Literal) {
			Literal l = (Literal) v;
			if (l.getDatatype() != null) {
				return "^" + l.getDatatype().stringValue() + " " + escapeString(l.stringValue()) + "\n";
			} else if (l.getLanguage() != null) {
				return "@" + l.getLanguage() + " " + escapeString(l.stringValue()) + "\n";
			} else {
				return "#" + escapeString(l.stringValue()) + "\n";
			}
		} else if (v instanceof BNode) {
			throw new RuntimeException("Unexpected blank node encountered");
		} else if (v == null) {
			return "\n";
		} else {
			throw new RuntimeException("Unknown element");
		}
	}

	private static final String escapeString(String s) {
		return s.replace("\\", "\\\\").replace("\n", "\\n");
	}

}
