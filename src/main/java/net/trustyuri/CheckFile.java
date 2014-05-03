package net.trustyuri;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Reads and checks content from local files or URLs.
 *
 * @author Tobias Kuhn
 */
public class CheckFile {

	/**
	 * Reads and checks the content of one or several URLs (in the form of trusty URIs) or local
	 * files (in the form of trusty file names).
	 *
	 * @param args a list of URLs or file names
	 */
	public static void main(String[] args) throws IOException, TrustyUriException {
		for (String arg : args) {
			check(arg);
		}
	}

	/**
	 * Check the content of a trusty URI (fetched from the web) or a trusty file (read from the
	 * file system).
	 *
	 * @param fileOrUrl the file name or URL
	 */
	public static void check(String fileOrUrl) throws IOException, TrustyUriException {
		CheckFile c;
		try {
			URL url = new URL(fileOrUrl);
			c = new CheckFile(url);
		} catch (MalformedURLException ex) {
			c = new CheckFile(new File(fileOrUrl));
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

	/**
	 * Creates a new object to check the content to be fetch from a URL.
	 *
	 * @param url the URL
	 */
	public CheckFile(URL url) throws IOException {
		r = new TrustyUriResource(url);
	}

	/**
	 * Creates a new object to check the content to be read from a local file.
	 *
	 * @param file
	 */
	public CheckFile(File file) throws IOException {
		r = new TrustyUriResource(file);
	}

	/**
	 * Checks whether the content matches the hash of the trusty URI.
	 *
	 * @return true if the content matches the hash
	 */
	public boolean check() throws IOException, TrustyUriException {
		TrustyUriModule module = ModuleDirectory.getModule(r.getModuleId());
		if (module == null) {
			throw new TrustyUriException("ERROR: Not a trusty URI or unknown module");
		}
		return module.hasCorrectHash(r);
	}

}
