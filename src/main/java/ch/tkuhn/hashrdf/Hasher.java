package ch.tkuhn.hashrdf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class Hasher {

	private URI baseURI = null;
	private String hash = null;

	public Hasher(URI baseURI) {
		this.baseURI = baseURI;
	}

	public Hasher(String hash) {
		this.hash = hash;
	}

	public String makeHash(List<Statement> statements) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ex) {}
		Collections.sort(statements, new StatementComparator(baseURI, hash));
		for (Statement st : statements) {
			md.update(valueToString(st.getContext()).getBytes());
			md.update(valueToString(st.getSubject()).getBytes());
			md.update(valueToString(st.getPredicate()).getBytes());
			md.update(valueToString(st.getObject()).getBytes());
		}
		// A = Version 0
		return "A" + bytesToString(md.digest());
	}

	private String bytesToString(byte[] bytes) {
		String h = DatatypeConverter.printBase64Binary(bytes);
		h = h.replaceFirst("=*$", "");
		h = h.replace('+', '-');
		h = h.replace('/', '_');
		return h;
	}

	private String valueToString(Value v) {
		if (v instanceof BNode) {
			throw new RuntimeException("Blank nodes are not allowed");
		} else if (v instanceof URI) {
			if (baseURI != null) {
				return HashURIUtils.normalize((URI) v, baseURI) + "\n";
			} else if (hash != null) {
				return HashURIUtils.normalize((URI) v, hash) + "\n";
			} else {
				return v.toString() + "\n";
			}
		} else if (v instanceof Literal) {
			Literal l = (Literal) v;
			if (l.getDatatype() != null) {
				return "^" + l.getDatatype().stringValue() + " " + escapeString(l.stringValue()) + "\n";
			} else if (l.getLanguage() != null) {
				return "@" + l.getLanguage() + " " + escapeString(l.stringValue()) + "\n";
			} else {
				return "#" + escapeString(l.stringValue()) + "\n";
			}
		} else {
			throw new RuntimeException("Unknown element");
		}
	}

	private static final String escapeString(String s) {
		return s.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\n", "\\\\n");
	}

}
