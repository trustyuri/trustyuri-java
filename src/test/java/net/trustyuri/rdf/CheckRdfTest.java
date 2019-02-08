package net.trustyuri.rdf;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.trustyuri.CheckFile;


public class CheckRdfTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
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
		boolean valid = c.check();
		assert valid;
	}

}
