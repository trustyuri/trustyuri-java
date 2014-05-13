package net.trustyuri.rdf;

import java.io.File;

import net.trustyuri.CheckFile;
import net.trustyuri.rdf.TransformNanopub;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class TransformNanopubTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		test("nanopub1-pre.trig",
			"nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.trig");
		test("nanopub1-pre.nq",
			"nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.nq");
		test("nanopub1-pre.xml",
			"nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.xml");
		test("nanopub2-pre.trig",
			"nanopub2.RAAtRSyHmwuoQnfhi22Y-hdsH00tPei3CAkVgiuuisAi8.trig");
		test("nanopub3-pre.trig",
			"nanopub3.RAZgXBFqKI45x1DHmP40hoNj6dneLocMsfhZJJdjOZQSE.trig");
		test("nanopub4-pre.trig",
			"nanopub4.RAGjerlLWh3KiFiYTAQcKycXekeEVZeGq1JSr26KVntSw.trig");
	}

	public void test(String preName, String name) throws Exception {
		File preFile = new File(testDir.getRoot(), preName);
		FileUtils.copyFile(new File("src/main/resources/examples/" + preName), preFile);
		TransformNanopub.main(new String[] {preFile.getAbsolutePath()});
		File file = new File(testDir.getRoot(), name);
		assert file.exists();
		CheckFile.main(new String[] {file.getAbsolutePath()});
	}

}
