package net.trustyuri.file;

import java.io.File;

import net.trustyuri.CheckFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class CheckFileTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
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
		CheckFile.main(new String[] {"src/main/resources/testsuite/FA/valid/" + filename});
	}

}
