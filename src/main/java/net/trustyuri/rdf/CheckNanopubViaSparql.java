package net.trustyuri.rdf;

import net.trustyuri.TrustyUriUtils;

import org.nanopub.MalformedNanopubException;
import org.nanopub.Nanopub;
import org.nanopub.NanopubImpl;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.sparql.SPARQLRepository;


public class CheckNanopubViaSparql {
	
	public static void main(String[] args) throws Exception {
		String endpointURL = args[0];
		String uriString = args[1];
		URI uri = new URIImpl(uriString);
		Nanopub nanopub = null;
		try {
			SPARQLRepository repo = new SPARQLRepository(endpointURL);
			repo.initialize();
			nanopub = new NanopubImpl(repo, uri);
		} catch (MalformedNanopubException ex) {
			System.out.println("Nanopub not found");
			System.exit(1);
		}
		if (CheckNanopub.isValid(nanopub)) {
			System.out.println("Correct hash: " + TrustyUriUtils.getTrustyUriTail(uriString));
		} else {
			System.out.println("*** INCORRECT HASH ***");
		}
		System.exit(0);
	}

}
