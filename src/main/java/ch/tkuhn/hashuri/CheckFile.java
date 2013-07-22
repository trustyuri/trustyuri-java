package ch.tkuhn.hashuri;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class CheckFile {
	
	public static void main(String[] args) throws Exception {
		String fileName = args[0];
		HashUriResource r;
		try {
			URL url = new URL(fileName);
			r = new HashUriResource(url);
		} catch (MalformedURLException ex) {
			r = new HashUriResource(new File(fileName));
		}
		
		String algorithmID = r.getHash().substring(0, 2);
		HashUriModule module = ModuleDirectory.getModule(algorithmID);
		if (module == null) {
			System.out.println("ERROR: Not a hash-URI or unknown algorithm");
			System.exit(1);
		}
		if (module.hasCorrectHash(r)) {
			System.out.println("Correct hash: " + r.getHash());
			//System.out.println("ni URI: " + HashUriUtils.getNiUri(fileName));
		} else {
			System.out.println("*** INCORRECT HASH ***");
		}
	}

}
