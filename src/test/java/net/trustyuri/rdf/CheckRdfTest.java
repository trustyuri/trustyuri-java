package net.trustyuri.rdf;

import net.trustyuri.CheckFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class CheckRdfTest {

    @TempDir
    Path testDir;

    @Test
    public void runTest() throws Exception {
        File testSuiteDir = new File("src/main/resources/testsuite/RA/valid/");
        if (testSuiteDir.isDirectory()) {
            for (File testFile : testSuiteDir.listFiles()) {
                test(testFile.getName());
            }
        }
    }

    public void test(String filename) throws Exception {
        File file = new File("src/main/resources/testsuite/RA/valid/" + filename);
        CheckFile c = new CheckFile(file);
        assertTrue(c.check());
    }

}
