package ch.tkuhn.hashuri.rdf;

import ch.tkuhn.hashuri.HashUriModule;
import ch.tkuhn.hashuri.HashUriResource;

public class RdfModule implements HashUriModule {

	public static final String ALGORITHM_ID = "RA";

	@Override
	public String getAlgorithmID() {
		return ALGORITHM_ID;
	}

	@Override
	public boolean hasCorrectHash(HashUriResource r) throws Exception {
		RdfFileContent content = RdfUtils.load(r);
		RdfHasher hasher = new RdfHasher(r.getHash());
		String h = hasher.makeHash(content.getStatements());
		return r.getHash().equals(h);
	}

}
