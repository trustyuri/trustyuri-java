package net.trustyuri.rdf;

import java.io.File;
import java.io.IOException;

import net.trustyuri.AbstractTrustyUriModule;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;

import org.openrdf.model.URI;

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
	public int getDataPartLength() {
		return 43;
	}

	@Override
	public boolean hasCorrectHash(TrustyUriResource r) throws IOException, TrustyUriException {
		RdfFileContent content = RdfUtils.load(r);
		content = RdfPreprocessor.run(content, r.getArtifactCode());
		String ac = RdfHasher.makeArtifactCode(content.getStatements());
		return r.getArtifactCode().equals(ac);
	}

	@Override
	public URI fixTrustyFile(File file) throws IOException, TrustyUriException {
		return RdfUtils.fixTrustyFile(file, false);
	}

}
