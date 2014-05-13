package net.trustyuri.rdf;

import java.io.File;

import net.trustyuri.CheckFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class CheckRdfTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		test("rdfxml1.RAjNuWSfaFfTkXEetIbnV8a7Xlmai-IwsEs9gybU0Dgbs.rdf");
		test("ntriples1.RATJSTFCAt4eL5YZtNk7kAj6IvTxyfC5g76Tu2rpA1Ln0.nt");
		test("trix1.RA038sqfhk5MltUES5tDnTZl4wjMawLppCnFzx9kBEQWo.xml");
		test("turtle1.RA4XTpFboYhYGbz2HvVJYcUqztH_x-03_qUUrRHUS4J9w.ttl");
		test("turtle2.RAUAsavvNK_70jxeCm0OuCgo50R6m4SCYdzU0qjRGmmgI.ttl");
		test("nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.trig");
		test("nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.nq");
		test("nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.xml");
		test("nanopub2.RAAtRSyHmwuoQnfhi22Y-hdsH00tPei3CAkVgiuuisAi8.trig");
		test("nanopub3.RAZgXBFqKI45x1DHmP40hoNj6dneLocMsfhZJJdjOZQSE.trig");
		test("nanopub4.RAGjerlLWh3KiFiYTAQcKycXekeEVZeGq1JSr26KVntSw.trig");
	}

	public void test(String filename) throws Exception {
		File file = new File("src/main/resources/examples/" + filename);
		CheckFile c = new CheckFile(file);
		boolean valid = c.check();
		assert valid;
	}

}
