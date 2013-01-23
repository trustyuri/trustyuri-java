package ch.tkuhn.hashrdf;

import java.util.Comparator;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class StatementComparator implements Comparator<Statement> {

	private URI baseURI = null;
	private String hash = null;

	public StatementComparator(URI baseURI, String hash) {
		this.baseURI = baseURI;
		this.hash = hash;
	}

	@Override
	public int compare(Statement st1, Statement st2) {
		int c;
		c = compareContext(st1, st2);
		if (c != 0) return c;
		c = compareSubject(st1, st2);
		if (c != 0) return c;
		c = comparePredicate(st1, st2);
		if (c != 0) return c;
		return compareObject(st1, st2);
	}

	private int compareContext(Statement st1, Statement st2) {
		Resource r1 = st1.getContext();
		Resource r2 = st2.getContext();
		if (r1 == null || r2 == null) {
			throw new RuntimeException("Context of statement is null");
		}
		return compareResource(r1, r2);
	}

	private int compareSubject(Statement st1, Statement st2) {
		return compareResource(st1.getSubject(),  st2.getSubject());
	}

	private int comparePredicate(Statement st1, Statement st2) {
		return compareURIs(st1.getPredicate(), st2.getPredicate());
	}

	private int compareObject(Statement st1, Statement st2) {
		Value v1 = st1.getObject();
		Value v2 = st2.getObject();
		if (v1 instanceof Literal && !(v2 instanceof Literal)) {
			return 1;
		} else if (!(v1 instanceof Literal) && v2 instanceof Literal) {
			return -1;
		} else if (v1 instanceof Literal) {
			return compareLiteral((Literal) v1, (Literal) v2);
		} else {
			return compareResource((Resource) v1, (Resource) v2);
		}
	}

	private int compareResource(Resource r1, Resource r2) {
		if (r1 instanceof BNode || r2 instanceof BNode) {
			throw new RuntimeException("Blank nodes are not allowed");
		}
		return compareURIs((URI) r1, (URI) r2);
	}

	private int compareLiteral(Literal l1, Literal l2) {
		return 0;
	}

	private int compareURIs(URI uri1, URI uri2) {
		return uriToString(uri1).compareTo(uriToString(uri2));
	}

	private String uriToString(URI uri) {
		if (baseURI != null) {
			return HashURIUtils.normalize(uri, baseURI);
		} else if (hash != null) {
			return HashURIUtils.normalize(uri, hash);
		} else {
			return uri.toString();
		}
	}

}
