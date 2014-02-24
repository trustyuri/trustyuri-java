package net.trustyuri.file;

import net.trustyuri.CheckFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class CheckFileTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		test("file1.FA47DEQpj8HBSa-_TImW-5JCeuQeRkm5NMpJWZG3hSuFU.txt");
		test("file2.FAT-NmyX72cnFIf9aCx-TvoSIzgnLBLfZgA638PsbAZK8.txt");
	}

	public void test(String filename) throws Exception {
		CheckFile.main(new String[] {"src/main/resources/examples/" + filename});
	}

}
