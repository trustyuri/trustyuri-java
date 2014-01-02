package ch.tkuhn.hashuri;

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
			System.out.println("Correct hash: " + c.r.getHash());
			//System.out.println("ni URI: " + HashUriUtils.getNiUri(fileName));
		} else {
			System.out.println("*** INCORRECT HASH ***");
		}
	}

	private HashUriResource r;

	public CheckFile(URL url) throws Exception {
		r = new HashUriResource(url);
	}

	public CheckFile(File file) throws Exception {
		r = new HashUriResource(file);
	}

	public boolean check() throws Exception {
		String algorithmID = r.getHash().substring(0, 2);
		HashUriModule module = ModuleDirectory.getModule(algorithmID);
		if (module == null) {
			throw new RuntimeException("ERROR: Not a hash-URI or unknown algorithm");
		}
		return module.hasCorrectHash(r);
	}

}
