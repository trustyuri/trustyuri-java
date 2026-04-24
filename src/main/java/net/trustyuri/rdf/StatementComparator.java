package net.trustyuri.rdf;

import org.eclipse.rdf4j.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Optional;

/**
 * Comparator for RDF statements, used for sorting statements in a canonical way.
 */
public class StatementComparator implements Comparator<Statement> {

    private static final Logger logger = LoggerFactory.getLogger(StatementComparator.class);

    /**
     * Creates a new StatementComparator.
     */
    public StatementComparator() {
    }

    @Override
    public int compare(Statement st1, Statement st2) {
        return compareStatement(st1, st2);
    }

    /**
     * Compares two RDF statements in a canonical way, first by context, then by subject, then by predicate, and finally by object.
     *
     * @param st1 the first statement
     * @param st2 the second statement
     * @return a negative integer, zero, or a positive integer as the first statement is less than, equal to, or greater than the second statement
     */
    public static int compareStatement(Statement st1, Statement st2) {
        logger.debug("Comparing statements:\n  st1={}\n  st2={}", st1, st2);
        int c;
        c = compareContext(st1, st2);
        if (c != 0) {
            logger.debug("Different context: result={}", c);
            return c;
        }
        c = compareSubject(st1, st2);
        if (c != 0) {
            logger.debug("Different subject: result={}", c);
            return c;
        }
        c = comparePredicate(st1, st2);
        if (c != 0) {
            logger.debug("Different predicate: result={}", c);
            return c;
        }
        c = compareObject(st1, st2);
        logger.debug("Object comparison result={}", c);
        return c;
    }

    private static int compareContext(Statement st1, Statement st2) {
        Resource r1 = st1.getContext();
        Resource r2 = st2.getContext();
        if (r1 == null && r2 == null) {
            return 0;
        } else if (r1 == null && r2 != null) {
            return -1;
        } else if (r1 != null && r2 == null) {
            return 1;
        }
        return compareResource(r1, r2);
    }

    private static int compareSubject(Statement st1, Statement st2) {
        return compareResource(st1.getSubject(), st2.getSubject());
    }

    private static int comparePredicate(Statement st1, Statement st2) {
        return compareURIs(st1.getPredicate(), st2.getPredicate());
    }

    private static int compareObject(Statement st1, Statement st2) {
        Value v1 = st1.getObject();
        Value v2 = st2.getObject();
        if (v1 instanceof Literal && !(v2 instanceof Literal)) {
            return 1;
        } else if (!(v1 instanceof Literal) && v2 instanceof Literal) {
            return -1;
        } else if (v1 instanceof Literal) {
            return compareLiteral((Literal) v1, (Literal) v2);
        } else {
            return compareResource((Resource) v1, (Resource) v2);
        }
    }

    private static int compareResource(Resource r1, Resource r2) {
        if (r1 instanceof BNode) {
            logger.error("Blank node encountered in resource comparison: r1={}, r2={}", r1, r2);
            throw new IllegalArgumentException("Blank nodes are not supported in StatementComparator: " + r1);
        } else {
            return compareURIs((IRI) r1, (IRI) r2);
        }
    }

    private static int compareLiteral(Literal l1, Literal l2) {
        logger.trace("Comparing literals: l1={}, l2={}", l1, l2);
        String s1 = l1.stringValue();
        String s2 = l2.stringValue();
        if (!s1.equals(s2)) {
            logger.debug("Literal value differs: '{}' vs '{}'", s1, s2);
            return s1.compareTo(s2);
        }
        s1 = null;
        s2 = null;
        if (l1.getDatatype() != null) {
            s1 = l1.getDatatype().toString();
        }
        if (l1.getLanguage().isPresent()) {
            logger.trace("Ignoring datatype because language is present for l1={}", l1);
            s1 = null;
        }
        if (l2.getDatatype() != null) {
            s2 = l2.getDatatype().toString();
        }
        if (l2.getLanguage().isPresent()) {
            logger.trace("Ignoring datatype because language is present for l2={}", l2);
            s2 = null;
        }
        if (s1 == null && s2 != null) {
            return -1;
        } else if (s1 != null && s2 == null) {
            return 1;
        } else if (s1 != null && !s1.equals(s2)) {
            logger.debug("Datatype differs: '{}' vs '{}'", s1, s2);
            return s1.compareTo(s2);
        }
        Optional<String> lang1 = l1.getLanguage();
        Optional<String> lang2 = l2.getLanguage();
        if (!lang1.isPresent() && lang2.isPresent()) {
            return -1;
        } else if (lang1.isPresent() && !lang2.isPresent()) {
            return 1;
        } else if (lang1.isPresent() && !lang1.get().equalsIgnoreCase(lang2.get())) {
            logger.debug("Language differs: '{}' vs '{}'", lang1.get(), lang2.get());
            return lang1.get().toLowerCase().compareTo(lang2.get().toLowerCase());
        }
        return 0;
    }

    private static int compareURIs(IRI uri1, IRI uri2) {
        return uri1.toString().compareTo(uri2.toString());
    }

}
