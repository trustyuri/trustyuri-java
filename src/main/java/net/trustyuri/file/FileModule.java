package net.trustyuri.file;

import net.trustyuri.AbstractTrustyUriModule;
import net.trustyuri.TrustyUriResource;

public class FileModule extends AbstractTrustyUriModule {

	public static final String MODULE_ID = "FA";

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
	public boolean hasCorrectHash(TrustyUriResource r) throws Exception {
		FileHasher hasher = new FileHasher();
		String ac = hasher.makeArtifactCode(r.getInputStream());
		return r.getArtifactCode().equals(ac);
	}

}
