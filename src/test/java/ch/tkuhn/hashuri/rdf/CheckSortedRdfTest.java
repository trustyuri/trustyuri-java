package ch.tkuhn.hashuri.rdf;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CheckSortedRdfTest {

	@Rule
	public TemporaryFolder testDir = new TemporaryFolder();
 
	@Test
	public void runTest() throws Exception {
		test("rdfxml1.RAJase0GSn-6UT-7ve_fYWLb0iHqX7z6KmopkwbUepEJw.rdf");
		test("ntriples1.RAbeMUqngGrBcvpQd2HxIrkXFOSVMK-5pfnMNpEfEIlD8.nt");
		test("trix1.RAcpyR-XR9cOpqQFvJ43SLhouV8uL6N_AXFNWfhjxiFGY.xml");
		test("turtle1.RANkrDU-BLg8eNRHLYtgf-VngD8QHPRYGWKZbX48HKvw0.ttl");
		test("turtle2.RAiYxMrJ7r0zUPEp3IvA07bcyE3v-I26uzw6GWpFtD36o.ttl");
		test("nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.trig");
		test("nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.nq");
		test("nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.xml");
		test("nanopub2.RACnUvlm10Umx49xlVUfV4McO4XBscCGIovFKesbSVpVM.trig");
		test("nanopub3.RAGEgUJRjRd0guvKXkejgV3t1Cg6nySVeU27zl8aYLa68.trig");
		test("nanopub4.RAMfwspGKTaoqJR9HDgGvQ6iEpFGfsKKS_QhvLZT52lvw.trig");
	}

	public void test(String filename) throws Exception {
		CheckSortedRdf.main(new String[] {"src/main/resources/examples/" + filename});
	}

}
