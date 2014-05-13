package net.trustyuri.rdf;

import java.io.File;

import net.trustyuri.CheckFile;
import net.trustyuri.rdf.TransformLargeRdf;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TransformLargeRdfTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		test("rdfxml1-pre.rdf",
			"http://trustyuri.net/examples/rdfxml1",
			"rdfxml1.RAjNuWSfaFfTkXEetIbnV8a7Xlmai-IwsEs9gybU0Dgbs.rdf");
		test("ntriples1-pre.nt",
			"http://trustyuri.net/examples/ntriples1",
			"ntriples1.RATJSTFCAt4eL5YZtNk7kAj6IvTxyfC5g76Tu2rpA1Ln0.nt");
		test("trix1-pre.xml",
			"http://trustyuri.net/examples/trix1",
			"trix1.RA038sqfhk5MltUES5tDnTZl4wjMawLppCnFzx9kBEQWo.xml");
		test("turtle1-pre.ttl",
			"http://trustyuri.net/examples/turtle1",
			"turtle1.RA4XTpFboYhYGbz2HvVJYcUqztH_x-03_qUUrRHUS4J9w.ttl");
		test("turtle2-pre.ttl",
			"http://trustyuri.net/examples/turtle2",
			"turtle2.RAUAsavvNK_70jxeCm0OuCgo50R6m4SCYdzU0qjRGmmgI.ttl");
		test("nanopub1-pre.trig",
			"http://trustyuri.net/examples/nanopub1",
			"nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.trig");
		test("nanopub1-pre.nq",
			"http://trustyuri.net/examples/nanopub1",
			"nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.nq");
		test("nanopub1-pre.xml",
			"http://trustyuri.net/examples/nanopub1",
			"nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.xml");
	}

	public void test(String preName, String baseUri, String name) throws Exception {
		File preFile = new File(testDir.getRoot(), preName);
		FileUtils.copyFile(new File("src/main/resources/examples/" + preName), preFile);
		TransformLargeRdf.main(new String[] {preFile.getAbsolutePath(), baseUri});
		File file = new File(testDir.getRoot(), name);
		assert file.exists();
		CheckFile.main(new String[] {file.getAbsolutePath()});
	}

}
