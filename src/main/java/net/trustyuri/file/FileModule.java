package net.trustyuri.file;

import net.trustyuri.AbstractTrustyUriModule;
import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Module for trusty URIs that are based on the hash of a file. The file is hashed using SHA-256, and the resulting hash is encoded in Base64 and used as the artifact code in the trusty URI.
 */
public class FileModule extends AbstractTrustyUriModule {

    private static final Logger logger = LoggerFactory.getLogger(FileModule.class);

    /**
     * The module identifier for this module, which is "FA". This is used in the trusty URI to indicate that the artifact code is based on a file hash.
     */
    public static final String MODULE_ID = "FA";

    /**
     * Constructor for the FileModule class. It calls the constructor of the superclass AbstractTrustyUriModule with the module identifier "FA".
     */
    public FileModule() {
        super(MODULE_ID);
    }

    @Override
    public String getAlgorithmId() {
        return "sha-256";
    }

    @Override
    public int getDataPartLength() {
        return 43;
    }

    @Override
    public boolean hasCorrectHash(TrustyUriResource r) throws IOException {
        logger.debug("Verifying hash for resource: {}", r);
        FileHasher hasher = new FileHasher();
        ArtifactCode ac = hasher.makeArtifactCode(new BufferedInputStream(r.getInputStream()));
        boolean matches = r.getArtifactCode().equals(ac.toString());
        if (matches) {
            logger.debug("Hash verification passed for resource: {}", r);
        } else {
            logger.warn("Hash verification FAILED for resource: {} — expected {}, got {}",
                    r, r.getArtifactCode(), ac);
        }
        return matches;
    }

    @Override
    public void fixTrustyFile(File file) throws IOException {
        logger.info("Fixing trusty URI filename for: {}", file.getAbsolutePath());
        TrustyUriResource r = new TrustyUriResource(file);
        File renamedFile = new File(r.getFilename().replaceAll(r.getArtifactCode(), ""));
        logger.debug("Stripping artifact code '{}' — renaming to: {}", r.getArtifactCode(), renamedFile.getName());
        boolean renamed = file.renameTo(renamedFile);
        if (!renamed) {
            logger.error("Failed to rename '{}' to '{}' before reprocessing", file.getName(), renamedFile.getName());
            throw new IOException("Could not rename file: " + file.getAbsolutePath());
        }
        logger.debug("Renamed successfully, reprocessing: {}", renamedFile.getName());
        ProcessFile.process(renamedFile);
    }

}
