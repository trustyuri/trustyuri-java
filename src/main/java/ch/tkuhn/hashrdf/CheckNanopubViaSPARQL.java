package ch.tkuhn.hashrdf;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

public class CheckNanopubViaSPARQL {
	
	public static void main(String[] args) throws Exception {
		String endpointURL = args[0];
		String uri = args[1];
		String hash = FileUtils.getHashPart(uri);
		if (hash == null) {
			System.out.println("ERROR: No hash in URI");
			System.exit(1);
		}
		
		List<Statement> statements = getNanopubViaSPARQL(endpointURL, uri);
		if (statements == null || statements.size() == 0) {
			System.out.println("Nanopub not found");
			System.exit(1);
		}
		Hasher hasher = new Hasher(hash);
		String h = hasher.makeHash(statements);
		if (hash.equals(h)) {
			System.out.println("Correct hash: " + h);
		} else {
			System.out.println("*** INCORRECT HASH ***: " + h);
		}
		System.exit(0);
	}

	private static final String nanopubViaSPARQLQuery =
			"prefix np: <http://www.nanopub.org/nschema#> " +
			"prefix this: <@> " +
			"select ?G ?S ?P ?O where { " +
			"  { " +
			"    graph ?G { this: np:hasAssertion ?A } " +
			"  } union { " +
			"    graph ?H { { this: np:hasAssertion ?G } union { this: np:hasProvenance ?R " +
			"        { { ?R np:hasAttribution ?G } union { ?R np:hasSupporting ?G } } } } " +
			"  } " +
			"  graph ?G { ?S ?P ?O } " +
			"}";

	public static List<Statement> getNanopubViaSPARQL(String endpointURL, String nanopubURI) {
		List<Statement> statements = new ArrayList<Statement>();
		try {
			SPARQLRepository repo = new SPARQLRepository(endpointURL);
			repo.initialize();
			RepositoryConnection connection = repo.getConnection();
			try {
				String q = nanopubViaSPARQLQuery.replaceAll("@", nanopubURI);
				TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, q);
				TupleQueryResult result = tupleQuery.evaluate();
				try {
					while (result.hasNext()) {
						BindingSet bs = result.next();
						Resource g = (Resource) bs.getBinding("G").getValue();
						Resource s = (Resource) bs.getBinding("S").getValue();
						URI p = (URI) bs.getBinding("P").getValue();
						Value o = bs.getBinding("O").getValue();
						Statement st = new ContextStatementImpl(s, p, o, g);
						statements.add(st);
					}
				} finally {
					result.close();
				}
			} finally {
				connection.close();
			}
		} catch (OpenRDFException ex) {
			ex.printStackTrace();
		}
		return statements;
	}

}
