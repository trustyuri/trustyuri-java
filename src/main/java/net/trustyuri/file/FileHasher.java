package net.trustyuri.file;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.trustyuri.TrustyUriUtils;

public class FileHasher {

	public FileHasher() {
	}

	public String makeArtifactCode(InputStream in) throws IOException {
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
		return FileModule.MODULE_ID + TrustyUriUtils.getBase64(md.digest());
	}

}
