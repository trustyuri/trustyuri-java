package ch.tkuhn.hashuri.rdf;

import ch.tkuhn.hashuri.HashUriModule;
import ch.tkuhn.hashuri.HashUriResource;

public class RdfModule implements HashUriModule {

	public static final String MODULE_ID = "RA";

	@Override
	public String getModuleId() {
		return MODULE_ID;
	}

	@Override
	public String getAlgorithmId() {
		return "sha-256";
	}

	@Override
	public boolean hasCorrectHash(HashUriResource r) throws Exception {
		RdfFileContent content = RdfUtils.load(r);
		content = RdfPreprocessor.run(content, r.getHash());
		String h = RdfHasher.makeHash(content.getStatements());
		return r.getHash().equals(h);
	}

}
