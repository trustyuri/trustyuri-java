package net.trustyuri;

import java.io.File;
import java.io.IOException;

import org.openrdf.model.URI;

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

		URI uri = c.fix();
		if (uri != null) {
			System.out.println("Fixed URI: " + uri);
		}
	}

	private File file;

	public FixTrustyFile(File file) throws IOException {
		this.file = file;
	}

	public URI fix() throws IOException, TrustyUriException {
		TrustyUriResource r = new TrustyUriResource(file);
		TrustyUriModule module = ModuleDirectory.getModule(r.getModuleId());
		if (module == null) {
			throw new TrustyUriException("ERROR: Not a trusty URI or unknown module");
		}
		return module.fixTrustyFile(file);
	}

}
