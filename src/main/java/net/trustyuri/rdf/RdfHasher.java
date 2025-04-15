package net.trustyuri.rdf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriUtils;

public class RdfHasher {

	// TODO Make this a command line argument:
	private static final boolean DEBUG = false;

	private RdfHasher() {}  // no instances allowed

	public static String makeArtifactCode(List<Statement> statements) {
		return getArtifactCode(digest(statements));
	}

	public static String makeGraphArtifactCode(List<Statement> statements) throws TrustyUriException {
		IRI graphUri = null;
		List<Statement> graph = new ArrayList<Statement>();
		for (Statement st : statements) {
			Resource c = st.getContext();
			if (c == null) {
				throw new TrustyUriException("Graph is null");
			} else if (c instanceof BNode) {
				throw new TrustyUriException("Graph is blank node");
			} else if (graphUri != null && !c.equals(graphUri)) {
				throw new TrustyUriException("Multiple graphs");
			}
			graphUri = (IRI) c;
			graph.add(st);
		}
		if (graph.size() == 0) {
			throw new TrustyUriException("Graph not found");
		}
		return getGraphArtifactCode(digest(graph));
	}

	public static String makeGraphArtifactCode(List<Statement> statements, IRI baseUri, TransformRdfSetting setting) throws TrustyUriException {
		IRI graphUri = RdfUtils.getTrustyUri(baseUri, " ", setting);
		List<Statement> graph = new ArrayList<Statement>();
		for (Statement st : statements) {
			Resource c = st.getContext();
			if (c != null && c.equals(graphUri)) graph.add(st);
		}
		if (graph.size() == 0) {
			throw new TrustyUriException("Graph not found");
		}
		return getGraphArtifactCode(digest(graph));
	}

	public static MessageDigest digest(List<Statement> statements) {
		MessageDigest md = getDigest();
		Collections.sort(statements, new StatementComparator());
		if (DEBUG) System.err.println("----------");
		Statement previous = null;
		for (Statement st : statements) {
			if (!st.equals(previous)) {
				digest(st, md);
			}
			previous = st;
		}
		if (DEBUG) System.err.println("----------");
		return md;
	}

	public static String getDigestString(List<Statement> statements) {
		StringBuilder sb = new StringBuilder();
		Collections.sort(statements, new StatementComparator());
		Statement previous = null;
		for (Statement st : statements) {
			if (!st.equals(previous)) {
				sb.append(getDigestString(st));
			}
			previous = st;
		}
		return sb.toString();
	}

	public static MessageDigest getDigest() {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
		return md;
	}

	public static String getArtifactCode(MessageDigest md) {
		return RdfModule.MODULE_ID + TrustyUriUtils.getBase64(md.digest());
	}

	public static String getGraphArtifactCode(MessageDigest md) {
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

	public static String getDigestString(Statement st) {
		String s = "";
		s += valueToString(st.getContext());
		s += valueToString(st.getSubject());
		s += valueToString(st.getPredicate());
		s += valueToString(st.getObject());
		return s;
	}

	private static String valueToString(Value v) {
		if (v instanceof IRI) {
			return ((IRI) v).toString() + "\n";
		} else if (v instanceof Literal) {
			Literal l = (Literal) v;
			if (l.getLanguage().isPresent()) {
				return "@" + l.getLanguage().get().toLowerCase() + " " + escapeString(l.stringValue()) + "\n";
			} else {
				IRI dataType = l.getDatatype();
				if (dataType == null) dataType = XMLSchema.STRING;
				return "^" + dataType.stringValue() + " " + escapeString(l.stringValue()) + "\n";
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
