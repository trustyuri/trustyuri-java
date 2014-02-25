package net.trustyuri.rdf;

import net.trustyuri.AbstractTrustyUriModule;
import net.trustyuri.TrustyUriResource;

public class RdfModule extends AbstractTrustyUriModule {

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
	public int getHashLength() {
		return 43;
	}

	@Override
	public boolean hasCorrectHash(TrustyUriResource r) throws Exception {
		RdfFileContent content = RdfUtils.load(r);
		content = RdfPreprocessor.run(content, r.getHash());
		String h = RdfHasher.makeHash(content.getStatements());
		return r.getHash().equals(h);
	}

}
