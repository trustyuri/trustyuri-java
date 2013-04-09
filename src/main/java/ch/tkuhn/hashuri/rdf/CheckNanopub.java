package ch.tkuhn.hashuri.rdf;

import java.util.List;

import org.openrdf.model.Statement;

import ch.tkuhn.hashuri.HashUriUtils;
import ch.tkuhn.nanopub.Nanopub;
import ch.tkuhn.nanopub.NanopubUtils;

public class CheckNanopub {
	
	public static boolean isValid(Nanopub nanopub) {
		String hash = HashUriUtils.getHashUriDataPart(nanopub.getUri().toString());
		if (hash == null) return false;
		List<Statement> statements = NanopubUtils.getStatements(nanopub);
		RdfHasher hasher = new RdfHasher(hash);
		String h = hasher.makeHash(statements);
		return h.equals(hash);
	}

}
