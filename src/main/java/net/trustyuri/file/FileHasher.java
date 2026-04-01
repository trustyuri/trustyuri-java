package net.trustyuri.file;

import net.trustyuri.ArtifactCode;
import net.trustyuri.ModuleDirectory;
import net.trustyuri.TrustyUriUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileHasher {

    public FileHasher() {
    }

    public ArtifactCode makeArtifactCode(InputStream in) throws IOException {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
        }
        DigestInputStream d = null;
        try {
            d = new DigestInputStream(in, md);
            while (d.read() != -1) {
            }
        } finally {
            d.close();
        }
        return ArtifactCode.of(ModuleDirectory.getModule(FileModule.MODULE_ID), TrustyUriUtils.getBase64(md.digest()));
    }

}
