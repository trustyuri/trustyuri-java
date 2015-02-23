package net.trustyuri;

import java.io.File;
import java.io.IOException;

/**
 * @author Tobias Kuhn
 */
public class FixTrustyFile {

	public static void main(String[] args) throws IOException, TrustyUriException {
		for (String arg : args) {
			fix(new File(arg));
		}
	}

	public static void fix(File file) throws IOException, TrustyUriException {
		FixTrustyFile c = new FixTrustyFile(file);
		c.fix();
	}

	private File file;

	public FixTrustyFile(File file) throws IOException {
		this.file = file;
	}

	public void fix() throws IOException, TrustyUriException {
		TrustyUriResource r = new TrustyUriResource(file);
		TrustyUriModule module = ModuleDirectory.getModule(r.getModuleId());
		if (module == null) {
			throw new TrustyUriException("ERROR: Not a trusty URI or unknown module");
		}
		module.fixTrustyFile(file);
	}

}
