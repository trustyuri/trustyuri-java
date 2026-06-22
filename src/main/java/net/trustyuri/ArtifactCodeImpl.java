package net.trustyuri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * An implementation of the ArtifactCode interface.
 */
public class ArtifactCodeImpl implements ArtifactCode {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactCodeImpl.class);

    private final String dataHash;
    private final TrustyUriModule module;

    /**
     * Constructs an ArtifactCodeImpl with the given code.
     *
     * @param artifactCode the artifact code
     * @throws IllegalArgumentException if the code is not a valid artifact code
     */
    ArtifactCodeImpl(String artifactCode) {
        if (TrustyUriUtils.isPotentialArtifactCode(artifactCode)) {
            this.dataHash = TrustyUriUtils.getDataPart(artifactCode);
            this.module = ModuleDirectory.getModule(TrustyUriUtils.getModuleId(artifactCode));
            logger.debug("Created ArtifactCodeImpl from code '{}': module='{}', dataHash='{}'", artifactCode, module.getModuleId(), dataHash);
        } else {
            logger.error("Invalid artifact code (failed format/length check): '{}'", artifactCode);
            throw new IllegalArgumentException("Invalid artifact code: " + artifactCode);
        }
    }

    /**
     * Constructs an ArtifactCodeImpl with the given module and data hash.
     *
     * @param module   the module associated with the artifact code
     * @param dataHash the data hash part of the artifact code
     */
    ArtifactCodeImpl(TrustyUriModule module, String dataHash) {
        if (module == null) {
            logger.error("Cannot create artifact code: module is null (dataHash='{}')", dataHash);
            throw new IllegalArgumentException("Module cannot be null");
        }
        if (dataHash == null || dataHash.isEmpty()) {
            logger.error("Cannot create artifact code: data hash is null or empty (module='{}')", module.getModuleId());
            throw new IllegalArgumentException("Data hash cannot be null or empty");
        }
        if (module.getDataPartLength() != dataHash.length()) {
            logger.error("Cannot create artifact code for module '{}': data hash '{}' has length {}, expected {}", module.getModuleId(), dataHash, dataHash.length(), module.getDataPartLength());
            throw new IllegalArgumentException("Data hash length does not match module requirements: expected " + module.getDataPartLength() + ", got " + dataHash.length());
        }
        this.module = module;
        this.dataHash = dataHash;
        logger.debug("Created ArtifactCodeImpl: module='{}', dataHash='{}'", module.getModuleId(), dataHash);
    }

    @Override
    public TrustyUriModule getModule() {
        return this.module;
    }

    @Override
    public String toString() {
        return this.module.getModuleId() + this.dataHash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArtifactCodeImpl that = (ArtifactCodeImpl) o;
        return Objects.equals(dataHash, that.dataHash) && Objects.equals(module, that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dataHash) + Objects.hashCode(module);
    }

}
