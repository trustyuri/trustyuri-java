package net.trustyuri.file;

import net.trustyuri.CheckFile;
import net.trustyuri.TrustyUriUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ProcessFileTest {

    @TempDir
    Path testDir;

    @Test
    public void runTest() throws Exception {
        File testSuiteDir = new File("src/main/resources/testsuite/FA/valid/");
        if (testSuiteDir.isDirectory()) {
            for (File testFile : testSuiteDir.listFiles()) {
                String name = testFile.getName();
                if (!TrustyUriUtils.isPotentialTrustyUri(name)) {
                    continue;
                }
                String preName = name.replaceFirst("^(.*)\\.(FA[A-Za-z0-9\\-_]{43})(\\.[a-z]+)", "$1$3");
                test(name, preName);
            }
        }
    }

    public void test(String name, String preName) throws Exception {
        File preFile = testDir.resolve(preName).toFile();
        FileUtils.copyFile(new File("src/main/resources/testsuite/FA/valid/" + name), preFile);
        ProcessFile.main(new String[]{preFile.getAbsolutePath()});
        File file = testDir.resolve(name).toFile();
        assertFalse(preFile.exists());
        assertTrue(file.exists());
        CheckFile.main(new String[]{file.getAbsolutePath()});
    }

}
