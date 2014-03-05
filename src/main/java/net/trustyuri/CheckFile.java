package net.trustyuri;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class CheckFile {
	
	public static void main(String[] args) throws Exception {
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

	public CheckFile(URL url) throws Exception {
		r = new TrustyUriResource(url);
	}

	public CheckFile(File file) throws Exception {
		r = new TrustyUriResource(file);
	}

	public boolean check() throws Exception {
		TrustyUriModule module = ModuleDirectory.getModule(r.getModuleId());
		if (module == null) {
			throw new RuntimeException("ERROR: Not a trusty URI or unknown module");
		}
		return module.hasCorrectHash(r);
	}

}
