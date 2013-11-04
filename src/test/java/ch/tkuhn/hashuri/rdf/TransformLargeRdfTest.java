package ch.tkuhn.hashuri.rdf;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.tkuhn.hashuri.CheckFile;

public class TransformLargeRdfTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		test("rdfxml1-pre.rdf",
			"http://purl.org/hashuri/examples/rdfxml1",
			"rdfxml1.RAJase0GSn-6UT-7ve_fYWLb0iHqX7z6KmopkwbUepEJw.rdf");
		test("ntriples1-pre.nt",
			"http://purl.org/hashuri/examples/ntriples1",
			"ntriples1.RAbeMUqngGrBcvpQd2HxIrkXFOSVMK-5pfnMNpEfEIlD8.nt");
		test("trix1-pre.xml",
			"http://purl.org/hashuri/examples/trix1",
			"trix1.RAcpyR-XR9cOpqQFvJ43SLhouV8uL6N_AXFNWfhjxiFGY.xml");
		test("turtle1-pre.ttl",
			"http://purl.org/hashuri/examples/turtle1",
			"turtle1.RANkrDU-BLg8eNRHLYtgf-VngD8QHPRYGWKZbX48HKvw0.ttl");
		test("turtle2-pre.ttl",
			"http://purl.org/hashuri/examples/turtle2",
			"turtle2.RAiYxMrJ7r0zUPEp3IvA07bcyE3v-I26uzw6GWpFtD36o.ttl");
		test("nanopub1-pre.trig",
			"http://purl.org/hashuri/examples/nanopub1",
			"nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.trig");
		test("nanopub1-pre.nq",
			"http://purl.org/hashuri/examples/nanopub1",
			"nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.nq");
		test("nanopub1-pre.xml",
			"http://purl.org/hashuri/examples/nanopub1",
			"nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.xml");
	}

	public void test(String preName, String baseUri, String name) throws Exception {
		File preFile = new File(testDir.getRoot(), preName);
		FileUtils.copyFile(new File("src/main/resources/examples/" + preName), preFile);
		TransformLargeRdf.main(new String[] {preFile.getAbsolutePath(), baseUri, "1"});
		File file = new File(testDir.getRoot(), name);
		assert file.exists();
		CheckFile.main(new String[] {file.getAbsolutePath()});
	}

}
