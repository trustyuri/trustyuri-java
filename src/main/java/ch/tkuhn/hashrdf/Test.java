package ch.tkuhn.hashrdf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.trig.TriGParser;
import org.openrdf.rio.trig.TriGWriter;

public class Test {

	public static void main(String[] args) throws Exception {
		InputStream in = new FileInputStream(args[0]);
		TriGParser p = new TriGParser();
		OutputStream out = new FileOutputStream(args[1]);
		TriGWriter w = new TriGWriter(out);
		List<Statement> statements = new ArrayList<>();
		p.setRDFHandler(new StatementCollector(statements));
		p.parse(in, "");
		in.close();
		w.startRDF();
		for (Statement st : statements) {
			w.handleStatement(st);
		}
		w.endRDF();
		out.close();
	}

}
