package ch.tkuhn.hashuri.rdf;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import ch.tkuhn.hashuri.HashUriUtils;
import ch.tkuhn.nanopub.MalformedNanopubException;
import ch.tkuhn.nanopub.Nanopub;
import ch.tkuhn.nanopub.NanopubImpl;

public class CheckNanopubViaSparql {
	
	public static void main(String[] args) throws Exception {
		String endpointURL = args[0];
		String uriString = args[1];
		URI uri = new URIImpl(uriString);
		Nanopub nanopub = null;
		try {
			nanopub = new NanopubImpl(endpointURL, uri);
		} catch (MalformedNanopubException ex) {
			System.out.println("Nanopub not found");
			System.exit(1);
		}
		if (CheckNanopub.isValid(nanopub)) {
			System.out.println("Correct hash: " + HashUriUtils.getHashUriDataPart(uriString));
		} else {
			System.out.println("*** INCORRECT HASH ***");
		}
		System.exit(0);
	}

}
