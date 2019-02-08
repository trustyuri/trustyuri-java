package net.trustyuri.rdf;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.trustyuri.CheckFile;
import net.trustyuri.TrustyUriUtils;


public class TransformRdfTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		File testSuiteDir = new File("src/main/resources/testsuite/RA/valid/");
		if (testSuiteDir.isDirectory()) {
			for (File testFile : testSuiteDir.listFiles()) {
				String name = testFile.getName();
				if (!TrustyUriUtils.isPotentialTrustyUri(name)) continue;
				String preNameStem = name.replaceFirst("^(.*)\\.(RA[A-Za-z0-9\\-_]{43})(\\.[a-z]+)", "$1");
				String preName = name.replaceFirst("^(.*)\\.(RA[A-Za-z0-9\\-_]{43})(\\.[a-z]+)", "$1$3");
				test(preName, "http://trustyuri.net/testsuite/" + preNameStem, name);
			}
		}
	}

	public void test(String preName, String baseUri, String name) throws Exception {
		File preFile = new File(testDir.getRoot(), preName);
		FileUtils.copyFile(new File("src/main/resources/testsuite/RA/pre/" + preName), preFile);
		TransformRdf.main(new String[] {preFile.getAbsolutePath(), baseUri});
		File file = new File(testDir.getRoot(), name);
		assert file.exists();
		CheckFile.main(new String[] {file.getAbsolutePath()});
	}

}
