package ch.tkuhn.hashrdf;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.trig.TriGParser;

public class Test {

	public static void main(String[] args) throws Exception {
		InputStream in = new FileInputStream(args[0]);
		TriGParser p = new TriGParser();
		List<Statement> statements = new ArrayList<>();
		p.setRDFHandler(new StatementCollector(statements));
		p.parse(in, "");
		for (Statement st : statements) {
			System.err.println(st.getContext());
			System.err.println(st.getSubject());
			System.err.println(st.getPredicate());
			System.err.println(st.getObject());
			System.err.println();
		}
	}

}
