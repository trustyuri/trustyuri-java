package ch.tkuhn.hashuri.rdf;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Statement;

public class TransformNanopub {

	private static final String hasAssertion = "http://www.nanopub.org/nschema#hasAssertion";
	private static final String type = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	private static final String nanopub = "http://www.nanopub.org/nschema#Nanopublication";

	public static void main(String[] args) throws Exception {
		File inputFile = new File(args[0]);
		RdfFileContent content = RdfUtils.load(inputFile);
		Set<String> baseURICandidates = new HashSet<>();
		for (Statement st : content.getStatements()) {
			String s = st.getSubject().toString();
			String p = st.getPredicate().toString();
			String o = st.getObject().toString();
			if (p.equals(hasAssertion)) {
				baseURICandidates.add(s);
			} else if (p.equals(type) && o.equals(nanopub)) {
				baseURICandidates.add(s);
			}
		}
		if (baseURICandidates.size() == 0) {
			System.out.println("ERROR: No candidate found for nanopub URI");
			System.exit(1);
		} else if (baseURICandidates.size() > 1) {
			System.out.println("ERROR: More than one candidate found for nanopub URI:");
			for (String s : baseURICandidates) {
				System.out.println(s);
			}
			System.exit(1);
		}
		String baseName = baseURICandidates.iterator().next();
		TransformRdfFile.transform(content, inputFile.getParent(), baseName);
	}

}
