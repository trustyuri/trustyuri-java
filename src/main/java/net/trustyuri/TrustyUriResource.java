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
	private String artifactCode;

	public TrustyUriResource(String mimetype, File file, String hash) throws FileNotFoundException {
		init(file.toString(), mimetype, new FileInputStream(file), hash);
	}

	public TrustyUriResource(String mimetype, File file) throws FileNotFoundException {
		String n = file.toString();
		String ac = TrustyUriUtils.getArtifactCode(n);
		init(n, mimetype, new FileInputStream(file), ac);
	}

	public TrustyUriResource(File file, String artifactCode) throws FileNotFoundException {
		String n = file.toString();
		init(n, TrustyUriUtils.getMimetype(n), new FileInputStream(file), artifactCode);
	}

	public TrustyUriResource(File file) throws FileNotFoundException {
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

	private void init(String filename, String mimetype, InputStream in, String artifactCode) {
		this.filename = filename;
		this.mimetype = mimetype;
		this.in = in;
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

	public String getArtifactCode() {
		return artifactCode;
	}

	public String getModuleId() {
		return TrustyUriUtils.getModuleId(artifactCode);
	}

	public RDFFormat getFormat(RDFFormat defaultFormat) {
		RDFFormat format = RDFFormat.forMIMEType(getMimetype());
		if (format == null) {
			format = RDFFormat.forFileName(getFilename(), defaultFormat);
		}
		return format;
	}

}
