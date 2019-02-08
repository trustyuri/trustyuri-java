package net.trustyuri.file;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.trustyuri.CheckFile;
import net.trustyuri.TrustyUriUtils;


public class ProcessFileTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		File testSuiteDir = new File("src/main/resources/testsuite/FA/valid/");
		if (testSuiteDir.isDirectory()) {
			for (File testFile : testSuiteDir.listFiles()) {
				String name = testFile.getName();
				if (!TrustyUriUtils.isPotentialTrustyUri(name)) continue;
				String preName = name.replaceFirst("^(.*)\\.(FA[A-Za-z0-9\\-_]{43})(\\.[a-z]+)", "$1$3");
				test(name, preName);
			}
		}
	}

	public void test(String name, String preName) throws Exception {
		File preFile = new File(testDir.getRoot(), preName);
		FileUtils.copyFile(new File("src/main/resources/testsuite/FA/valid/" + name), preFile);
		ProcessFile.main(new String[] {preFile.getAbsolutePath()});
		File file = new File(testDir.getRoot(), name);
		assert !preFile.exists();
		assert file.exists();
		CheckFile.main(new String[] {file.getAbsolutePath()});
	}

}
