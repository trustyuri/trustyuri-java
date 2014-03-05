package net.trustyuri.rdf;

import java.util.List;

import net.trustyuri.TrustyUriUtils;

import org.nanopub.Nanopub;
import org.nanopub.NanopubUtils;
import org.openrdf.model.Statement;

public class CheckNanopub {
	
	public static boolean isValid(Nanopub nanopub) {
		String artifactCode = TrustyUriUtils.getArtifactCode(nanopub.getUri().toString());
		if (artifactCode == null) return false;
		List<Statement> statements = NanopubUtils.getStatements(nanopub);
		statements = RdfPreprocessor.run(statements, artifactCode);
		String ac = RdfHasher.makeArtifactCode(statements);
		return ac.equals(artifactCode);
	}

}
