package net.trustyuri;

import net.trustyuri.rdf.RdfModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArtifactCodeImplTest {

    private final String validArtifactCode = "RAQkRgam5soAC8p2audYEK88QTJjhxLqrDWP6siwwkr5c";

    @Test
    public void constructWithValidModuleAndInvalidCode() {
        assertThrows(IllegalArgumentException.class, () -> ArtifactCode.of(new RdfModule(), "this_is_an_invalid_code_because_it_is_too_long"));
        assertThrows(IllegalArgumentException.class, () -> ArtifactCode.of(new RdfModule(), "invalid_code_because_it_is_too_short"));
    }

    @Test
    public void constructWithInvalidCode() {
        assertThrows(IllegalArgumentException.class, () -> ArtifactCode.of("INVALID_CODE"));
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
    public void testGetModule() {
        ArtifactCode artifactCode = ArtifactCode.of(validArtifactCode);
        assertInstanceOf(RdfModule.class, artifactCode.getModule());
        assertEquals(RdfModule.MODULE_ID, artifactCode.getModule().getModuleId());
    }

    @Test
    public void testToString() {
        ArtifactCode artifactCode = ArtifactCode.of(validArtifactCode);
        assertEquals(validArtifactCode, artifactCode.toString());
    }

    @Test
    public void testToStringWithModuleAndDataHash() {
        ArtifactCode artifactCode = ArtifactCode.of(new RdfModule(), validArtifactCode.substring(2));
        assertEquals(validArtifactCode, artifactCode.toString());
    }

}