package net.trustyuri;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TrustyUriUtilsTest {

    @Test
    public void getModuleId() {
        String artifactCode = "FA12345678901234567890123";
        assertEquals("FA", TrustyUriUtils.getModuleId(artifactCode));
    }

    @Test
    public void getModuleIdWithTooShortArtifactCode() {
        String artifactCode = "F";
        assertThrows(StringIndexOutOfBoundsException.class, () -> TrustyUriUtils.getModuleId(artifactCode));
    }

    @Test
    public void getModuleIdWithEmptyArtifactCode() {
        String artifactCode = "";
        assertThrows(StringIndexOutOfBoundsException.class, () -> TrustyUriUtils.getModuleId(artifactCode));
    }

    @Test
    public void getModuleIdWithNullArtifactCode() {
        assertThrows(NullPointerException.class, () -> TrustyUriUtils.getModuleId(null));
    }

    @Test
    public void getDataPart() {
        String artifactCode = "FA12345678901234567890123";
        assertEquals("12345678901234567890123", TrustyUriUtils.getDataPart(artifactCode));
    }

    @Test
    public void getDataPartWithTooShortArtifactCode() {
        String artifactCode = "F";
        assertThrows(StringIndexOutOfBoundsException.class, () -> TrustyUriUtils.getDataPart(artifactCode));
    }

    @Test
    public void getDataPartWithEmptyArtifactCode() {
        String artifactCode = "";
        assertThrows(StringIndexOutOfBoundsException.class, () -> TrustyUriUtils.getDataPart(artifactCode));
    }

    @Test
    public void getDataPartWithNullArtifactCode() {
        assertThrows(NullPointerException.class, () -> TrustyUriUtils.getDataPart(null));
    }

    @Test
    public void getArtifactCodeWithValidArtifactAtEnd() {
        String s = "http://example.org/resource/FA12345678901234567890123";
        assertEquals("FA12345678901234567890123", TrustyUriUtils.getArtifactCode(s));
    }

    @Test
    public void getArtifactCodeWithExtension() {
        String s = "urn:example:FA12345678901234567890123.txt";
        assertEquals("FA12345678901234567890123", TrustyUriUtils.getArtifactCode(s));
    }

    @Test
    public void getArtifactCodeWhenOnlyCode() {
        String s = "FA12345678901234567890123";
        assertEquals("FA12345678901234567890123", TrustyUriUtils.getArtifactCode(s));
    }

    @Test
    public void getArtifactCodeWithInvalidCharactersReturnsNull() {
        String s = "http://example.org/FA12345$678901234567890123";
        assertNull(TrustyUriUtils.getArtifactCode(s));
    }

    @Test
    public void getArtifactCodeWithTooShortArtifactReturnsNull() {
        String s = "http://example.org/FA123";
        assertNull(TrustyUriUtils.getArtifactCode(s));
    }

    @Test
    public void getArtifactCodeWithTooLongExtensionReturnsNull() {
        String ext = "aaaaaaaaaaaaaaaaaaaaa"; // 21 chars
        String s = "FA12345678901234567890123." + ext;
        assertNull(TrustyUriUtils.getArtifactCode(s));
    }

    @Test
    public void getArtifactCodeWithNullInputThrows() {
        assertThrows(NullPointerException.class, () -> TrustyUriUtils.getArtifactCode(null));
    }

}