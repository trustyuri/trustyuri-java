package net.trustyuri.file;

import java.io.File;
import java.io.IOException;

import net.trustyuri.AbstractTrustyUriModule;
import net.trustyuri.TrustyUriResource;

import org.openrdf.model.URI;

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
	public boolean hasCorrectHash(TrustyUriResource r) throws IOException {
		FileHasher hasher = new FileHasher();
		String ac = hasher.makeArtifactCode(r.getInputStream());
		return r.getArtifactCode().equals(ac);
	}

	@Override
	public URI fixTrustyUri(TrustyUriResource r) throws IOException {
		File file = new File(r.getFilename());
		File renamedFile = new File(r.getFilename().replaceAll(r.getArtifactCode(), ""));
		file.renameTo(renamedFile);
		ProcessFile.process(renamedFile);
		return null;
	}

}
