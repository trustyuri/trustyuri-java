package net.trustyuri.rdf;

import net.trustyuri.CheckFile;
import net.trustyuri.TrustyUriUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class TransformRdfTest {

    @TempDir
    Path testDir;

    @Test
    public void runTest() throws Exception {
        File testSuiteDir = new File("src/main/resources/testsuite/RA/valid/");
        if (testSuiteDir.isDirectory()) {
            for (File testFile : testSuiteDir.listFiles()) {
                String name = testFile.getName();
                if (!TrustyUriUtils.isPotentialTrustyUri(name)) {
                    continue;
                }
                String preNameStem = name.replaceFirst("^(.*)\\.(RA[A-Za-z0-9\\-_]{43})(\\.[a-z]+)", "$1");
                String preName = name.replaceFirst("^(.*)\\.(RA[A-Za-z0-9\\-_]{43})(\\.[a-z]+)", "$1$3");
                test(preName, "http://trustyuri.net/testsuite/" + preNameStem, name);
            }
        }
    }

    public void test(String preName, String baseUri, String name) throws Exception {
        File preFile = testDir.resolve(preName).toFile();
        FileUtils.copyFile(new File("src/main/resources/testsuite/RA/pre/" + preName), preFile);
        TransformRdf.main(new String[]{preFile.getAbsolutePath(), baseUri});
        File file = testDir.resolve(name).toFile();
        assertTrue(file.exists(), "Expected output file to exist: " + name);
        CheckFile.main(new String[]{file.getAbsolutePath()});
    }

}
