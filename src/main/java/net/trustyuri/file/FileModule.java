package net.trustyuri.file;

import net.trustyuri.AbstractTrustyUriModule;
import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriResource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

public class FileModule extends AbstractTrustyUriModule {

    public static final String MODULE_ID = "FA";

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
