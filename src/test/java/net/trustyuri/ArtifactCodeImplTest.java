package net.trustyuri;

import net.trustyuri.rdf.RdfModule;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArtifactCodeImplTest {

    private final String validArtifactCode = "RAQkRgam5soAC8p2audYEK88QTJjhxLqrDWP6siwwkr5c";

    @Test
    public void constructWithValidCode() {
        ArtifactCode artifactCode = ArtifactCode.of(validArtifactCode);
        assertEquals(validArtifactCode, artifactCode.getCode());
    }

    @Test
    public void constructWithInvalidCode() {
        assertNull(ArtifactCode.of("INVALID_CODE"));
    }

    @Test
    public void testEquals() {
        ArtifactCode code1 = ArtifactCode.of(validArtifactCode);
        ArtifactCode code2 = ArtifactCode.of("RAoYy0bW33mUXlf6MOe3Q09AMualJr4D99z9vtEHFVsgE");
        ArtifactCode code3 = ArtifactCode.of(validArtifactCode);
        assertEquals(code1, code3);
        assertNotEquals(code1, code2);
    }

    @Test
    public void testHashCode() {
        ArtifactCode code1 = ArtifactCode.of(validArtifactCode);
        ArtifactCode code2 = ArtifactCode.of(validArtifactCode);
        assertEquals(code1.hashCode(), code2.hashCode());
    }

    @Test
    public void testToString() {
        ArtifactCode artifactCode = ArtifactCode.of(validArtifactCode);
        assertEquals("ArtifactCode{code='" + validArtifactCode + "'}", artifactCode.toString());
    }

    @Test
    public void testGetModule() {
        ArtifactCode artifactCode = ArtifactCode.of(validArtifactCode);
        assertTrue(artifactCode.getModule() instanceof RdfModule);
        assertEquals(RdfModule.MODULE_ID, artifactCode.getModule().getModuleId());
    }

}