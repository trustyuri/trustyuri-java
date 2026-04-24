package net.trustyuri.rdf;

import net.trustyuri.ArtifactCode;
import net.trustyuri.ModuleDirectory;
import net.trustyuri.TrustyUriException;
import net.trustyuri.TrustyUriUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.XSD;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class for creating artifact codes for RDF statements.
 */
public class RdfHasher {

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
        return getArtifactCode(digest(statements));
    }

    /**
     * Creates an artifact code for the given RDF statements, where the artifact code is based on the statements in a specific graph.
     *
     * @param statements the RDF statements to create the artifact code for
     * @return the artifact code for the given RDF statements
     * @throws TrustyUriException if the graph is null, a blank node, or if multiple graphs are found
     */
    public static ArtifactCode makeGraphArtifactCode(List<Statement> statements) throws TrustyUriException {
        IRI graphUri = null;
        List<Statement> graph = new ArrayList<>();
        for (Statement st : statements) {
            Resource c = st.getContext();
            if (c == null) {
                throw new TrustyUriException("Graph is null");
            } else if (c instanceof BNode) {
                throw new TrustyUriException("Graph is blank node");
            } else if (graphUri != null && !c.equals(graphUri)) {
                throw new TrustyUriException("Multiple graphs");
            }
            graphUri = (IRI) c;
            graph.add(st);
        }
        if (graph.isEmpty()) {
            throw new TrustyUriException("Graph not found");
        }
        return getGraphArtifactCode(digest(graph));
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
        IRI graphUri = RdfUtils.getTrustyUri(baseUri, " ", setting);
        List<Statement> graph = new ArrayList<>();
        for (Statement st : statements) {
            Resource c = st.getContext();
            if (c != null && c.equals(graphUri)) {
                graph.add(st);
            }
        }
        if (graph.isEmpty()) {
            throw new TrustyUriException("Graph not found");
        }
        return getGraphArtifactCode(digest(graph));
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
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        return md;
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
            throw new RuntimeException("Unexpected blank node encountered");
        } else if (v == null) {
            return "\n";
        } else {
            throw new RuntimeException("Unknown element");
        }
    }

    private static String escapeString(String s) {
        return s.replace("\\", "\\\\").replace("\n", "\\n");
    }

}
