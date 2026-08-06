package net.trustyuri.file;

import net.trustyuri.CheckFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class CheckFileTest {

    @TempDir
    Path testDir;

    @Test
    public void runTest() throws Exception {
        File testSuiteDir = new File("src/main/resources/testsuite/FA/valid/");
        if (testSuiteDir.isDirectory()) {
            for (File testFile : testSuiteDir.listFiles()) {
                test(testFile.getName());
            }
        }
    }

    public void test(String filename) throws Exception {
        File file = new File("src/main/resources/testsuite/FA/valid/" + filename);
        assertTrue(new CheckFile(file).check(), "Expected correct hash for: " + filename);
    }

}
