package net.trustyuri.file;

import net.trustyuri.ArtifactCode;
import net.trustyuri.ModuleDirectory;
import net.trustyuri.TrustyUriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class for hashing files. The hash is calculated using the SHA-256 algorithm and encoded in Base64.
 */
public class FileHasher {

    private static final Logger logger = LoggerFactory.getLogger(FileHasher.class);

    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Creates a new FileHasher instance.
     */
    public FileHasher() {
    }

    /**
     * Calculates the artifact code for the given input stream. The input stream is read and hashed using the SHA-256 algorithm, and the resulting hash is encoded in Base64 to create the artifact code.
     *
     * @param in the input stream to be hashed
     * @return the artifact code representing the hash of the input stream
     * @throws IOException if an I/O error occurs while reading the input stream
     */
    public ArtifactCode makeArtifactCode(InputStream in) throws IOException {
        logger.debug("Computing {} artifact code from input stream", HASH_ALGORITHM);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(HASH_ALGORITHM + " algorithm not available on this JVM", ex);
        }
        DigestInputStream d = null;
        try {
            d = new DigestInputStream(in, md);
            while (d.read() != -1) {
            }
        } finally {
            d.close();
        }
        ArtifactCode code = ArtifactCode.of(ModuleDirectory.getModule(FileModule.MODULE_ID), TrustyUriUtils.getBase64(md.digest()));
        logger.debug("Computed artifact code: {}", code);
        return code;
    }

}
