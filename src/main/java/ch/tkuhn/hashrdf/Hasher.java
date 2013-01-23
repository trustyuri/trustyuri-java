package ch.tkuhn.hashrdf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

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
		return bytesToString(md.digest());
	}

	private String bytesToString(byte[] bytes) {
		String h = DatatypeConverter.printBase64Binary(bytes);
		h = h.replaceFirst("=*$", "");
		h = h.replace('+', '-');
		h = h.replace('/', '_');
		return h;
	}

	private String valueToString(Value v) {
		if (v instanceof URI) {
			if (baseURI != null) {
				return HashURIUtils.normalize((URI) v, baseURI) + "\n";
			} else {
				return HashURIUtils.normalize((URI) v, hash) + "\n";
			}
		} else {
			return v.toString() + "\n";
		}
	}

}
