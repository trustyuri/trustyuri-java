package ch.tkuhn.hashuri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.openrdf.rio.RDFFormat;

public class HashUriResource {

	private String filename;
	private String mimetype;
	private InputStream in;
	private String hash;

	public HashUriResource(String mimetype, File file, String hash) throws FileNotFoundException {
		init(file.toString(), mimetype, new FileInputStream(file), hash);
	}

	public HashUriResource(String mimetype, File file) throws FileNotFoundException {
		String n = file.toString();
		String h = HashUriUtils.getHashUriDataPart(n);
		init(n, mimetype, new FileInputStream(file), h);
	}

	public HashUriResource(File file, String hash) throws FileNotFoundException {
		String n = file.toString();
		init(n, HashUriUtils.getMimetype(n), new FileInputStream(file), hash);
	}

	public HashUriResource(File file) throws FileNotFoundException {
		String n = file.toString();
		String h = HashUriUtils.getHashUriDataPart(n);
		init(n, HashUriUtils.getMimetype(n), new FileInputStream(file), h);
	}

	public HashUriResource(String mimetype, URL url, String hash) throws IOException {
		URLConnection conn = url.openConnection();
		init(url.toString(), mimetype, conn.getInputStream(), hash);
	}

	public HashUriResource(String mimetype, URL url) throws IOException {
		String n = url.toString();
		URLConnection conn = url.openConnection();
		String h = HashUriUtils.getHashUriDataPart(n);
		init(n, mimetype, conn.getInputStream(), h);
	}

	public HashUriResource(URL url, String hash) throws IOException {
		URLConnection conn = url.openConnection();
		init(url.toString(), conn.getContentType(), conn.getInputStream(), hash);
	}

	public HashUriResource(URL url) throws IOException {
		String n = url.toString();
		URLConnection conn = url.openConnection();
		String h = HashUriUtils.getHashUriDataPart(n);
		init(n, conn.getContentType(), conn.getInputStream(), h);
	}

	private void init(String filename, String mimetype, InputStream in, String hash) {
		this.filename = filename;
		this.mimetype = mimetype;
		this.in = in;
		this.hash = hash;
	}

	public String getFilename() {
		return filename;
	}

	public String getMimetype() {
		return mimetype;
	}

	public InputStream getInputStream() {
		return in;
	}

	public String getHash() {
		return hash;
	}

	public RDFFormat getFormat(RDFFormat defaultFormat) {
		RDFFormat format = RDFFormat.forMIMEType(getMimetype());
		if (format == null) {
			format = RDFFormat.forFileName(getFilename(), defaultFormat);
		}
		return format;
	}

}
