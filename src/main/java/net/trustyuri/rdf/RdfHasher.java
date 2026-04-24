package net.trustyuri.rdf;

import net.trustyuri.ArtifactCode;
import net.trustyuri.ModuleDirectory;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class for creating artifact codes for RDF statements.
 */
public class RdfHasher {

    private static final Logger logger = LoggerFactory.getLogger(RdfHasher.class);

    // TODO Make this a command line argument:
    private static final boolean DEBUG = false;

    private RdfHasher() {
    }  // no instances allowed

    /**
     * Creates an artifact code for the given RDF statements.
     *
     * @param statements the RDF statements to create the artifact code for
     * @return the artifact code for the given RDF statements
     */
    public static ArtifactCode makeArtifactCode(List<Statement> statements) {
        logger.debug("Making artifact code for {} statements", statements.size());
        ArtifactCode ac = getArtifactCode(digest(statements));
        logger.debug("Produced artifact code: '{}'", ac);
        return ac;
    }

    /**
     * Creates an artifact code for the given RDF statements, where the artifact code is based on the statements in a specific graph.
     *
     * @param statements the RDF statements to create the artifact code for
     * @return the artifact code for the given RDF statements
     * @throws TrustyUriException if the graph is null, a blank node, or if multiple graphs are found
     */
    public static ArtifactCode makeGraphArtifactCode(List<Statement> statements) throws TrustyUriException {
        logger.debug("Making graph artifact code for {} statements", statements.size());
        IRI graphUri = null;
        List<Statement> graph = new ArrayList<>();
        for (Statement st : statements) {
            Resource c = st.getContext();
            if (c == null) {
                logger.error("Encountered statement with null graph context");
                throw new TrustyUriException("Graph is null");
            } else if (c instanceof BNode) {
                logger.error("Encountered statement with blank node graph context: '{}'", c);
                throw new TrustyUriException("Graph is blank node");
            } else if (graphUri != null && !c.equals(graphUri)) {
                logger.error("Multiple graphs encountered: expected '{}', found '{}'", graphUri, c);
                throw new TrustyUriException("Multiple graphs");
            }
            graphUri = (IRI) c;
            graph.add(st);
        }
        if (graph.isEmpty()) {
            logger.error("No statements found for graph artifact code");
            throw new TrustyUriException("Graph not found");
        }
        logger.debug("Computing graph artifact code over graph '{}' ({} statements)", graphUri, graph.size());
        ArtifactCode ac = getGraphArtifactCode(digest(graph));
        logger.debug("Produced graph artifact code: '{}'", ac);
        return ac;
    }

    /**
     * Creates an artifact code for the given RDF statements, where the artifact code is based on the statements in a specific graph
     *
     * @param statements the RDF statements to create the artifact code for
     * @param baseUri    the base URI to use
     * @param setting    the setting to use for creating the artifact code
     * @return the artifact code for the given RDF statements
     * @throws TrustyUriException if the graph is null, a blank node, or if multiple graphs are found
     */
    public static ArtifactCode makeGraphArtifactCode(List<Statement> statements, IRI baseUri, TransformRdfSetting setting) throws TrustyUriException {
        logger.debug("Making graph artifact code for {} statements with base URI '{}'", statements.size(), baseUri);
        IRI graphUri = RdfUtils.getTrustyUri(baseUri, " ", setting);
        List<Statement> graph = new ArrayList<>();
        for (Statement st : statements) {
            Resource c = st.getContext();
            if (c != null && c.equals(graphUri)) {
                graph.add(st);
            }
        }
        if (graph.isEmpty()) {
            logger.error("No statements found for graph URI '{}'", graphUri);
            throw new TrustyUriException("Graph not found");
        }
        logger.debug("Found {} statements in graph '{}'", graph.size(), graphUri);
        ArtifactCode ac = getGraphArtifactCode(digest(graph));
        logger.debug("Produced graph artifact code: '{}'", ac);
        return ac;
    }

    /**
     * Creates a message digest for the given RDF statements.
     *
     * @param statements the RDF statements to create the message digest for
     * @return the message digest for the given RDF statements
     */
    public static MessageDigest digest(List<Statement> statements) {
        MessageDigest md = getDigest();
        Collections.sort(statements, new StatementComparator());
        if (DEBUG) {
            System.err.println("----------");
        }
        Statement previous = null;
        for (Statement st : statements) {
            if (!st.equals(previous)) {
                logger.trace("Digesting statement: {}", getDigestString(st).trim());
                digest(st, md);
            }
            previous = st;
        }
        if (DEBUG) {
            System.err.println("----------");
        }
        return md;
    }

    /**
     * Creates a string representation of the given RDF statements for use in the message digest.
     *
     * @param statements the RDF statements to create the string representation for
     * @return the string representation of the given RDF statements for use in the message digest
     */
    public static String getDigestString(List<Statement> statements) {
        StringBuilder sb = new StringBuilder();
        Collections.sort(statements, new StatementComparator());
        Statement previous = null;
        for (Statement st : statements) {
            if (!st.equals(previous)) {
                sb.append(getDigestString(st));
            }
            previous = st;
        }
        return sb.toString();
    }

    /**
     * Creates a message digest.
     *
     * @return the message digest
     */
    public static MessageDigest getDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            logger.error("SHA-256 algorithm not available on this JVM");
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }

    /**
     * Creates an artifact code for the given message digest.
     *
     * @param md the message digest to create the artifact code for
     * @return the artifact code for the given message digest
     */
    public static ArtifactCode getArtifactCode(MessageDigest md) {
        return ArtifactCode.of(ModuleDirectory.getModule(RdfModule.MODULE_ID), TrustyUriUtils.getBase64(md.digest()));
    }

    /**
     * Creates an artifact code for the given message digest
     *
     * @param md the message digest to create the artifact code for
     * @return the artifact code for the given message digest
     */
    public static ArtifactCode getGraphArtifactCode(MessageDigest md) {
        return ArtifactCode.of(ModuleDirectory.getModule(RdfGraphModule.MODULE_ID), TrustyUriUtils.getBase64(md.digest()));
    }

    /**
     * Updates the given message digest with the given RDF statement.
     *
     * @param st the RDF statement to update the message digest with
     * @param md the message digest to update
     */
    public static void digest(Statement st, MessageDigest md) {
        if (DEBUG) {
            System.err.print(valueToString(st.getContext()));
        }
        md.update(valueToString(st.getContext()).getBytes());
        if (DEBUG) {
            System.err.print(valueToString(st.getSubject()));
        }
        md.update(valueToString(st.getSubject()).getBytes());
        if (DEBUG) {
            System.err.print(valueToString(st.getPredicate()));
        }
        md.update(valueToString(st.getPredicate()).getBytes());
        if (DEBUG) {
            System.err.print(valueToString(st.getObject()));
        }
        md.update(valueToString(st.getObject()).getBytes());
    }

    /**
     * Creates a string representation of the given RDF statement for use in the message digest.
     *
     * @param st the RDF statement to create the string representation for
     * @return the string representation of the given RDF statement for use in the message digest
     */
    public static String getDigestString(Statement st) {
        String s = "";
        s += valueToString(st.getContext());
        s += valueToString(st.getSubject());
        s += valueToString(st.getPredicate());
        s += valueToString(st.getObject());
        return s;
    }

    private static String valueToString(Value v) {
        if (v instanceof IRI) {
            return ((IRI) v).toString() + "\n";
        } else if (v instanceof Literal) {
            Literal l = (Literal) v;
            if (l.getLanguage().isPresent()) {
                return "@" + l.getLanguage().get().toLowerCase() + " " + escapeString(l.stringValue()) + "\n";
            } else {
                IRI dataType = l.getDatatype();
                if (dataType == null) {
                    dataType = XSD.STRING;
                }
                return "^" + dataType.stringValue() + " " + escapeString(l.stringValue()) + "\n";
            }
        } else if (v instanceof BNode) {
            logger.error("Blank node encountered during digest — this should have been skolemized earlier: '{}'", v);
            throw new RuntimeException("Unexpected blank node encountered");
        } else if (v == null) {
            return "\n";
        } else {
            logger.error("Unknown RDF value type during digest: '{}'", v.getClass().getName());
            throw new RuntimeException("Unknown element");
        }
    }

    private static String escapeString(String s) {
        return s.replace("\\", "\\\\").replace("\n", "\\n");
    }

}
