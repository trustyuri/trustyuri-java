package net.trustyuri.rdf;

import java.util.List;

import net.trustyuri.TrustyUriUtils;

import org.nanopub.Nanopub;
import org.nanopub.NanopubUtils;
import org.openrdf.model.Statement;


public class CheckNanopub {
	
	public static boolean isValid(Nanopub nanopub) {
		String hash = TrustyUriUtils.getTrustyUriTail(nanopub.getUri().toString());
		if (hash == null) return false;
		List<Statement> statements = NanopubUtils.getStatements(nanopub);
		statements = RdfPreprocessor.run(statements, hash);
		String h = RdfHasher.makeHash(statements);
		return h.equals(hash);
	}

}
