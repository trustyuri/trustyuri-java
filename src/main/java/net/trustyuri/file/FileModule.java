package net.trustyuri.file;

import net.trustyuri.AbstractTrustyUriModule;
import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriResource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Module for trusty URIs that are based on the hash of a file. The file is hashed using SHA-256, and the resulting hash is encoded in Base64 and used as the artifact code in the trusty URI.
 */
public class FileModule extends AbstractTrustyUriModule {

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
        FileHasher hasher = new FileHasher();
        ArtifactCode ac = hasher.makeArtifactCode(new BufferedInputStream(r.getInputStream()));
        return r.getArtifactCode().equals(ac.toString());
    }

    @Override
    public void fixTrustyFile(File file) throws IOException {
        TrustyUriResource r = new TrustyUriResource(file);
        File renamedFile = new File(r.getFilename().replaceAll(r.getArtifactCode(), ""));
        file.renameTo(renamedFile);
        ProcessFile.process(renamedFile);
    }

}
