package net.trustyuri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.openrdf.rio.RDFFormat;

public class TrustyUriResource {

	private String filename;
	private String mimetype;
	private InputStream in;
	private String hash;

	public TrustyUriResource(String mimetype, File file, String hash) throws FileNotFoundException {
		init(file.toString(), mimetype, new FileInputStream(file), hash);
	}

	public TrustyUriResource(String mimetype, File file) throws FileNotFoundException {
		String n = file.toString();
		String t = TrustyUriUtils.getTrustyUriTail(n);
		init(n, mimetype, new FileInputStream(file), t);
	}

	public TrustyUriResource(File file, String hash) throws FileNotFoundException {
		String n = file.toString();
		init(n, TrustyUriUtils.getMimetype(n), new FileInputStream(file), hash);
	}

	public TrustyUriResource(File file) throws FileNotFoundException {
		String n = file.toString();
		String t = TrustyUriUtils.getTrustyUriTail(n);
		init(n, TrustyUriUtils.getMimetype(n), new FileInputStream(file), t);
	}

	public TrustyUriResource(String mimetype, URL url, String hash) throws IOException {
		URLConnection conn = url.openConnection();
		init(url.toString(), mimetype, conn.getInputStream(), hash);
	}

	public TrustyUriResource(String mimetype, URL url) throws IOException {
		String n = url.toString();
		URLConnection conn = url.openConnection();
		String t = TrustyUriUtils.getTrustyUriTail(n);
		init(n, mimetype, conn.getInputStream(), t);
	}

	public TrustyUriResource(URL url, String hash) throws IOException {
		URLConnection conn = url.openConnection();
		init(url.toString(), conn.getContentType(), conn.getInputStream(), hash);
	}

	public TrustyUriResource(URL url) throws IOException {
		String n = url.toString();
		URLConnection conn = url.openConnection();
		String t = TrustyUriUtils.getTrustyUriTail(n);
		init(n, conn.getContentType(), conn.getInputStream(), t);
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

	public String getModuleId() {
		return TrustyUriUtils.getModuleId(hash);
	}

	public RDFFormat getFormat(RDFFormat defaultFormat) {
		RDFFormat format = RDFFormat.forMIMEType(getMimetype());
		if (format == null) {
			format = RDFFormat.forFileName(getFilename(), defaultFormat);
		}
		return format;
	}

}
