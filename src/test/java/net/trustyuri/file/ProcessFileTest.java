package net.trustyuri.file;

import java.io.File;

import net.trustyuri.CheckFile;
import net.trustyuri.file.ProcessFile;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class ProcessFileTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		test("file1.FA47DEQpj8HBSa-_TImW-5JCeuQeRkm5NMpJWZG3hSuFU.txt",
			"file1.txt");
		test("file2.FAT-NmyX72cnFIf9aCx-TvoSIzgnLBLfZgA638PsbAZK8.txt",
			"file2.txt");
	}

	public void test(String name, String preName) throws Exception {
		File preFile = new File(testDir.getRoot(), preName);
		FileUtils.copyFile(new File("src/main/resources/examples/" + name), preFile);
		ProcessFile.main(new String[] {preFile.getAbsolutePath()});
		File file = new File(testDir.getRoot(), name);
		assert !preFile.exists();
		assert file.exists();
		CheckFile.main(new String[] {file.getAbsolutePath()});
	}

}
