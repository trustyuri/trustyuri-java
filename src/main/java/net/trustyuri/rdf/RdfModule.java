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
 * Module for RDF content.
 */
public class RdfModule extends AbstractTrustyUriModule {

    private static final Logger logger = LoggerFactory.getLogger(RdfModule.class);

    /**
     * The module identifier for RDF content. The identifier is "RA".
     */
    public static final String MODULE_ID = "RA";

    /**
     * Creates a new instance of the RDF module. The constructor does not take any parameters and initializes the module with the predefined module identifier "RA".
     */
    public RdfModule() {
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
        logger.debug("Verifying hash for RDF resource: '{}'", r);
        RdfFileContent content = RdfUtils.load(r);
        content = RdfPreprocessor.run(content, r.getArtifactCode());
        ArtifactCode ac = RdfHasher.makeArtifactCode(content.getStatements());
        boolean matches = r.getArtifactCode().equals(ac.toString());
        if (matches) {
            logger.debug("Hash verification passed for RDF resource: '{}'", r);
        } else {
            logger.warn("Hash verification FAILED for RDF resource: '{}' — expected '{}', computed '{}'", r, r.getArtifactCode(), ac);
        }
        return matches;
    }

    @Override
    public void fixTrustyFile(File file) throws IOException, TrustyUriException {
        logger.info("Fixing trusty RDF file: '{}'", file.getAbsolutePath());
        RdfUtils.fixTrustyRdf(file);
    }

}
