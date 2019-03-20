package net.trustyuri;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class TrustyUriResource {

	private String filename;
	private String mimetype;
	private InputStream in;
	private String artifactCode;
	private boolean compressed;

	public TrustyUriResource(String mimetype, File file, String hash) throws IOException {
		init(file.toString(), mimetype, new FileInputStream(file), hash);
	}

	public TrustyUriResource(String mimetype, File file) throws IOException {
		String n = file.toString();
		String ac = TrustyUriUtils.getArtifactCode(n);
		init(n, mimetype, new FileInputStream(file), ac);
	}

	public TrustyUriResource(File file, String artifactCode) throws IOException {
		String n = file.toString();
		init(n, TrustyUriUtils.getMimetype(n), new FileInputStream(file), artifactCode);
	}

	public TrustyUriResource(File file) throws IOException {
		String n = file.toString();
		String ac = TrustyUriUtils.getArtifactCode(n);
		init(n, TrustyUriUtils.getMimetype(n), new FileInputStream(file), ac);
	}

	public TrustyUriResource(String mimetype, URL url, String artifactCode) throws IOException {
		URLConnection conn = url.openConnection();
		init(url.toString(), mimetype, conn.getInputStream(), artifactCode);
	}

	public TrustyUriResource(String mimetype, URL url) throws IOException {
		String n = url.toString();
		URLConnection conn = url.openConnection();
		String ac = TrustyUriUtils.getArtifactCode(n);
		init(n, mimetype, conn.getInputStream(), ac);
	}

	public TrustyUriResource(URL url, String artifactCode) throws IOException {
		URLConnection conn = url.openConnection();
		init(url.toString(), conn.getContentType(), conn.getInputStream(), artifactCode);
	}

	public TrustyUriResource(URL url) throws IOException {
		String n = url.toString();
		URLConnection conn = url.openConnection();
		String ac = TrustyUriUtils.getArtifactCode(n);
		init(n, conn.getContentType(), conn.getInputStream(), ac);
	}

	private void init(String filename, String mimetype, InputStream in, String artifactCode) throws IOException {
		if (filename.matches(".*\\.(gz|gzip)")) {
			this.in = new GZIPInputStream(in);
		} else {
			this.in = in;
		}
		this.filename = filename;
		this.mimetype = mimetype;
		this.artifactCode = artifactCode;
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

	public InputStreamReader getInputStreamReader() {
		return new InputStreamReader(in, Charset.forName("UTF-8"));
	}

	public String getArtifactCode() {
		return artifactCode;
	}

	public boolean isCompressed() {
		return compressed;
	}

	public String getModuleId() {
		return TrustyUriUtils.getModuleId(artifactCode);
	}

	public RDFFormat getFormat(RDFFormat defaultFormat) {
		Optional<RDFFormat> format = Rio.getParserFormatForMIMEType(getMimetype());
		if (!format.isPresent()) {
			format = Rio.getParserFormatForFileName(getFilename());
		}
		if (format.isPresent()) {
			return format.get();
		}
		return defaultFormat;
	}

}
