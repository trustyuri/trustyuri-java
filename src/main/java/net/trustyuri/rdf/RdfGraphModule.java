package net.trustyuri.rdf;

import net.trustyuri.AbstractTrustyUriModule;
import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;

import java.io.File;
import java.io.IOException;

/**
 * Module for RDF graphs.
 */
public class RdfGraphModule extends AbstractTrustyUriModule {

    /**
     * Module identifier for RDF graphs.
     */
    public static final String MODULE_ID = "RB";

    /**
     * Constructor that initializes the module with its identifier.
     */
    public RdfGraphModule() {
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
    public boolean hasCorrectHash(TrustyUriResource r) throws IOException, TrustyUriException {
        RdfFileContent content = RdfUtils.load(r);
        content = RdfPreprocessor.run(content, r.getArtifactCode());
        ArtifactCode ac = RdfHasher.makeGraphArtifactCode(content.getStatements());
        return r.getArtifactCode().equals(ac.toString());
    }

    @Override
    public void fixTrustyFile(File file) throws IOException, TrustyUriException {
        RdfUtils.fixTrustyRdf(file);
    }

}
