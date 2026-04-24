package net.trustyuri;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

/**
 * A resource that can be represented by a trusty URI.
 */
public class TrustyUriResource {

    private String filename;
    private String mimetype;
    private InputStream in;
    private String artifactCode;
    private boolean compressed;

    /**
     * Creates a new resource with the given filename, mimetype, input stream and artifact code.
     *
     * @param mimetype the mimetype of the resource
     * @param file     the file containing the resource
     * @param hash     the artifact code of the resource
     * @throws IOException if the file cannot be read
     */
    public TrustyUriResource(String mimetype, File file, String hash) throws IOException {
        init(file.toString(), mimetype, new FileInputStream(file), hash);
    }

    /**
     * Creates a new resource with the given filename, mimetype, input stream and artifact code.
     *
     * @param mimetype the mimetype of the resource
     * @param file     the file containing the resource
     * @throws IOException if the file cannot be read
     */
    public TrustyUriResource(String mimetype, File file) throws IOException {
        String n = file.toString();
        String ac = TrustyUriUtils.getArtifactCode(n);
        init(n, mimetype, new FileInputStream(file), ac);
    }

    /**
     * Creates a new resource with the given filename, mimetype, input stream and artifact code.
     *
     * @param file         the file containing the resource
     * @param artifactCode the artifact code of the resource
     * @throws IOException if the file cannot be read
     */
    public TrustyUriResource(File file, String artifactCode) throws IOException {
        String n = file.toString();
        init(n, TrustyUriUtils.getMimetype(n), new FileInputStream(file), artifactCode);
    }

    /**
     * Creates a new resource with the given filename, mimetype, input stream and artifact code.
     *
     * @param file the file containing the resource
     * @throws IOException if the file cannot be read
     */
    public TrustyUriResource(File file) throws IOException {
        String n = file.toString();
        String ac = TrustyUriUtils.getArtifactCode(n);
        init(n, TrustyUriUtils.getMimetype(n), new FileInputStream(file), ac);
    }

    /**
     * Creates a new resource with the given filename, mimetype, input stream and artifact code.
     *
     * @param mimetype     the mimetype of the resource
     * @param url          the URL of the resource
     * @param artifactCode the artifact code of the resource
     * @throws IOException if the file cannot be read
     */
    public TrustyUriResource(String mimetype, URL url, String artifactCode) throws IOException {
        URLConnection conn = url.openConnection();
        init(url.toString(), mimetype, conn.getInputStream(), artifactCode);
    }

    /**
     * Creates a new resource with the given filename, mimetype, input stream and artifact code.
     *
     * @param mimetype the mimetype of the resource
     * @param url      the URL of the resource
     * @throws IOException if the file cannot be read
     */
    public TrustyUriResource(String mimetype, URL url) throws IOException {
        String n = url.toString();
        URLConnection conn = url.openConnection();
        String ac = TrustyUriUtils.getArtifactCode(n);
        init(n, mimetype, conn.getInputStream(), ac);
    }

    /**
     * Creates a new resource with the given filename, mimetype, input stream and artifact code.
     *
     * @param url          the URL of the resource
     * @param artifactCode the artifact code of the resource
     * @throws IOException if the file cannot be read
     */
    public TrustyUriResource(URL url, String artifactCode) throws IOException {
        URLConnection conn = url.openConnection();
        init(url.toString(), conn.getContentType(), conn.getInputStream(), artifactCode);
    }

    /**
     * Creates a new resource with the given filename, mimetype, input stream and artifact code.
     *
     * @param url the URL of the resource
     * @throws IOException if the file cannot be read
     */
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

    /**
     * Returns the filename of the resource, which may be a URL or a file path.
     *
     * @return the filename of the resource, which may be a URL or a file path
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Returns the mimetype of the resource, which may be null if it cannot be determined from the filename or URL.
     *
     * @return the mimetype of the resource, which may be null if it cannot be determined from the filename or URL
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * Returns an InputStream for the resource, which may be a GZIPInputStream if the filename ends with .gz or .gzip.
     *
     * @return an InputStream for the resource, which may be a GZIPInputStream if the filename ends with .gz or .gzip
     */
    public InputStream getInputStream() {
        return in;
    }

    /**
     * Returns an InputStreamReader for the resource, using UTF-8 encoding.
     *
     * @return an InputStreamReader for the resource, using UTF-8 encoding
     */
    public InputStreamReader getInputStreamReader() {
        return new InputStreamReader(in, StandardCharsets.UTF_8);
    }

    /**
     * Returns the artifact code of the resource, which is derived from the filename or URL.
     *
     * @return the artifact code of the resource, which is derived from the filename or URL
     */
    public String getArtifactCode() {
        return artifactCode;
    }

    /**
     * Returns true if the resource is compressed, i.e. if the filename ends with .gz or .gzip.
     *
     * @return true if the resource is compressed, i.e. if the filename ends with .gz or .gzip
     */
    public boolean isCompressed() {
        return compressed;
    }

    /**
     * Returns the module identifier of the resource, which is derived from the artifact code.
     *
     * @return the module identifier of the resource, which is derived from the artifact code
     */
    public String getModuleId() {
        return TrustyUriUtils.getModuleId(artifactCode);
    }

    /**
     * Returns the RDF format of the resource, or the given default format if it cannot be determined from the mimetype or filename.
     *
     * @param defaultFormat the default format to return if the format cannot be determined from the mimetype or filename
     * @return the RDF format of the resource, or the given default format if it cannot be determined from the mimetype or filename
     */
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
