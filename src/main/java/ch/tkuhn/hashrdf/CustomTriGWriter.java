package ch.tkuhn.hashrdf;

import java.io.IOException;
import java.io.OutputStream;

import org.openrdf.model.URI;
import org.openrdf.rio.trig.TriGWriter;

public class CustomTriGWriter extends TriGWriter {

	public CustomTriGWriter(OutputStream out) {
		super(out);
	}

	protected void writeURI(URI uri) throws IOException {
		String prefix = namespaceTable.get(uri.toString());
		if (prefix != null) {
			writer.write(prefix);
			writer.write(":");
		} else {
			super.writeURI(uri);
		}
	}

}
