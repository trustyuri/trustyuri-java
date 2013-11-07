package ch.tkuhn.hashuri.rdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;

import javax.xml.bind.DatatypeConverter;

import org.openrdf.model.Statement;

public class SerStatementComparator implements Comparator<String> {

	public SerStatementComparator() {
	}

	@Override
	public int compare(String s1, String s2) {
		return StatementComparator.compareStatement(fromString(s1), fromString(s2));
	}

	public static Statement fromString(String string) {
		Statement st = null;
		try {
			byte[] data = DatatypeConverter.parseBase64Binary(string);
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
			st = (Statement) in.readObject();
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return st;
	}

	public static String toString(Statement st) {
		String s = null;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(st);
			out.close();
			s = DatatypeConverter.printBase64Binary(bout.toByteArray());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return s;
	}

}
