package ch.tkuhn.hashuri.file;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ch.tkuhn.hashuri.HashUriUtils;

public class FileHasher {

	public FileHasher() {
	}

	public String makeHash(InputStream in) throws Exception {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ex) {}
		DigestInputStream d = null;
		try {
			d = new DigestInputStream(in, md);
			while (d.read() != -1) {}
		} finally {
			d.close();
		}
		return FileModule.ALGORITHM_ID + HashUriUtils.getBase64(md.digest());
	}

}
