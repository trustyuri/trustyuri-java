package ch.tkuhn.hashuri.rdf;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.tkuhn.hashuri.CheckFile;

public class TransformNanopubTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		test("nanopub1-pre.trig",
			"nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.trig");
		test("nanopub1-pre.nq",
			"nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.nq");
		test("nanopub1-pre.xml",
			"nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.xml");
		test("nanopub2-pre.trig",
			"nanopub2.RACnUvlm10Umx49xlVUfV4McO4XBscCGIovFKesbSVpVM.trig");
		test("nanopub3-pre.trig",
			"nanopub3.RAGEgUJRjRd0guvKXkejgV3t1Cg6nySVeU27zl8aYLa68.trig");
		test("nanopub4-pre.trig",
			"nanopub4.RAMfwspGKTaoqJR9HDgGvQ6iEpFGfsKKS_QhvLZT52lvw.trig");
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
