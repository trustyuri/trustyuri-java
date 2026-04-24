package net.trustyuri.rdf;

import net.trustyuri.AbstractTrustyUriModule;
import net.trustyuri.ArtifactCode;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Module for RDF graphs.
 */
public class RdfGraphModule extends AbstractTrustyUriModule {

    private static final Logger logger = LoggerFactory.getLogger(RdfGraphModule.class);

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
        logger.debug("Verifying hash for RDF graph resource: '{}'", r);
        RdfFileContent content = RdfUtils.load(r);
        content = RdfPreprocessor.run(content, r.getArtifactCode());
        ArtifactCode ac = RdfHasher.makeGraphArtifactCode(content.getStatements());
        boolean matches = r.getArtifactCode().equals(ac.toString());
        if (matches) {
            logger.debug("Hash verification passed for RDF graph resource: '{}'", r);
        } else {
            logger.warn("Hash verification FAILED for RDF graph resource: '{}' — expected '{}', computed '{}'", r, r.getArtifactCode(), ac);
        }
        return matches;
    }

    @Override
    public void fixTrustyFile(File file) throws IOException, TrustyUriException {
        logger.info("Fixing trusty RDF graph file: '{}'", file.getAbsolutePath());
        RdfUtils.fixTrustyRdf(file);
    }

}
