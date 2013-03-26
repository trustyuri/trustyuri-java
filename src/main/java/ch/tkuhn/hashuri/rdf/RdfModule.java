package ch.tkuhn.hashuri.rdf;

import java.io.InputStream;

import ch.tkuhn.hashuri.HashUriModule;

public class RdfModule implements HashUriModule {

	public static final String ALGORITHM_ID = "RA";

	@Override
	public String getAlgorithmID() {
		return ALGORITHM_ID;
	}

	@Override
	public boolean isCorrectHash(InputStream in, String hash) throws Exception {
		RdfFileContent content = RdfUtils.load(in);
		RdfHasher hasher = new RdfHasher(hash);
		String h = hasher.makeHash(content.getStatements());
		return hash.equals(h);
	}

}
