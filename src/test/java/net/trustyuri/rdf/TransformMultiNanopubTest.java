package net.trustyuri.rdf;

import java.io.File;

import net.trustyuri.TrustyUriUtils;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nanopub.Nanopub;
import org.nanopub.NanopubImpl;


public class TransformMultiNanopubTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		test("nanopub1-pre.trig",
			"RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg");
		test("nanopub1-pre.nq",
			"RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg");
		test("nanopub1-pre.xml",
			"RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg");
		test("nanopub2-pre.trig",
			"RAAtRSyHmwuoQnfhi22Y-hdsH00tPei3CAkVgiuuisAi8");
		test("nanopub3-pre.trig",
			"RAZgXBFqKI45x1DHmP40hoNj6dneLocMsfhZJJdjOZQSE");
		test("nanopub4-pre.trig",
			"RAGjerlLWh3KiFiYTAQcKycXekeEVZeGq1JSr26KVntSw");
	}

	public void test(String preName, String artifactCode) throws Exception {
		File preFile = new File(testDir.getRoot(), preName);
		FileUtils.copyFile(new File("src/main/resources/examples/" + preName), preFile);
		TransformMultiNanopub.main(new String[] {preFile.getAbsolutePath()});
		File file = new File(preFile.getParent() + "/trusty." + preFile.getName());
		Nanopub np = new NanopubImpl(file);
		assert TrustyUriUtils.getArtifactCode(np.getUri().toString()).equals(artifactCode);
		assert CheckNanopub.isValid(np);
	}

}
