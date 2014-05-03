package net.trustyuri;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class CheckFile {
	
	public static void main(String[] args) throws IOException, TrustyUriException {
		String fileName = args[0];
		CheckFile c;
		try {
			URL url = new URL(fileName);
			c = new CheckFile(url);
		} catch (MalformedURLException ex) {
			c = new CheckFile(new File(fileName));
		}

		boolean valid = c.check();
		if (valid) {
			System.out.println("Correct hash: " + c.r.getArtifactCode());
			//System.out.println("ni URI: " + TrustyUriUtils.getNiUri(fileName));
		} else {
			System.out.println("*** INCORRECT HASH ***");
		}
	}

	private TrustyUriResource r;

	public CheckFile(URL url) throws IOException {
		r = new TrustyUriResource(url);
	}

	public CheckFile(File file) throws IOException {
		r = new TrustyUriResource(file);
	}

	public boolean check() throws IOException, TrustyUriException {
		TrustyUriModule module = ModuleDirectory.getModule(r.getModuleId());
		if (module == null) {
			throw new TrustyUriException("ERROR: Not a trusty URI or unknown module");
		}
		return module.hasCorrectHash(r);
	}

}
