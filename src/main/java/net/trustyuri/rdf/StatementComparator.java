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
        logger.debug("Comparing statements: <{}> vs <{}>", st1, st2);
        int c;
        c = compareContext(st1, st2);
        if (c != 0) {
            logger.debug("Statements differ at context: <{}> vs <{}> => {}", st1.getContext(), st2.getContext(), c);
            return c;
        }
        c = compareSubject(st1, st2);
        if (c != 0) {
            logger.debug("Statements differ at subject: <{}> vs <{}> => {}", st1.getSubject(), st2.getSubject(), c);
            return c;
        }
        c = comparePredicate(st1, st2);
        if (c != 0) {
            logger.debug("Statements differ at predicate: <{}> vs <{}> => {}", st1.getPredicate(), st2.getPredicate(), c);
            return c;
        }
        c = compareObject(st1, st2);
        logger.debug("Statements differ at object: <{}> vs <{}> => {}", st1.getObject(), st2.getObject(), c);
        return c;
    }

    private static int compareContext(Statement st1, Statement st2) {
        Resource r1 = st1.getContext();
        Resource r2 = st2.getContext();
        if (r1 == null && r2 == null) {
            return 0;
        } else if (r1 == null) {
            logger.trace("Context: st1 has no graph (default graph), st2 has <{}>", r2);
            return -1;
        } else if (r2 == null) {
            logger.trace("Context: st1 has <{}>, st2 has no graph (default graph)", r1);
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
            logger.trace("Object type mismatch: v1 is Literal <{}>, v2 is Resource <{}>; literals sort after resources", v1, v2);
            return 1;
        } else if (!(v1 instanceof Literal) && v2 instanceof Literal) {
            logger.trace("Object type mismatch: v1 is Resource <{}>, v2 is Literal <{}>; literals sort after resources", v1, v2);
            return -1;
        } else if (v1 instanceof Literal) {
            return compareLiteral((Literal) v1, (Literal) v2);
        } else {
            return compareResource((Resource) v1, (Resource) v2);
        }
    }

    private static int compareResource(Resource r1, Resource r2) {
        if (r1 instanceof BNode) {
            logger.error("Cannot compare blank nodes — blank nodes have no stable identity for canonical sorting: r1={}, r2={}", r1, r2);
            throw new IllegalArgumentException("Blank nodes are not supported in StatementComparator: " + r1);
        } else {
            return compareURIs((IRI) r1, (IRI) r2);
        }
    }

    private static int compareLiteral(Literal l1, Literal l2) {
        logger.trace("Comparing literals: \"{}\" vs \"{}\"", l1, l2);
        String s1 = l1.stringValue();
        String s2 = l2.stringValue();
        if (!s1.equals(s2)) {
            logger.debug("Literal lexical values differ: \"{}\" vs \"{}\"", s1, s2);
            return s1.compareTo(s2);
        }
        logger.trace("Literal lexical values are equal: \"{}\"; comparing datatypes", s1);
        String dt1 = null;
        String dt2 = null;
        if (l1.getDatatype() != null && !l1.getLanguage().isPresent()) {
            dt1 = l1.getDatatype().toString();
        } else if (l1.getLanguage().isPresent()) {
            logger.trace("Skipping datatype for l1 — language tag takes precedence: lang={}", l1.getLanguage().get());
        }
        if (l2.getDatatype() != null && !l2.getLanguage().isPresent()) {
            dt2 = l2.getDatatype().toString();
        } else if (l2.getLanguage().isPresent()) {
            logger.trace("Skipping datatype for l2 — language tag takes precedence: lang={}", l2.getLanguage().get());
        }
        if (dt1 == null && dt2 != null) {
            logger.debug("Datatype differs: l1 has no effective datatype, l2 has <{}>", dt2);
            return -1;
        } else if (dt1 != null && dt2 == null) {
            logger.debug("Datatype differs: l1 has <{}>, l2 has no effective datatype", dt1);
            return 1;
        } else if (dt1 != null && !dt1.equals(dt2)) {
            logger.debug("Datatype differs: <{}> vs <{}>", dt1, dt2);
            return dt1.compareTo(dt2);
        }
        Optional<String> lang1 = l1.getLanguage();
        Optional<String> lang2 = l2.getLanguage();
        if (!lang1.isPresent() && lang2.isPresent()) {
            logger.debug("Language tag differs: l1 has none, l2 has \"{}\"", lang2.get());
            return -1;
        } else if (lang1.isPresent() && !lang2.isPresent()) {
            logger.debug("Language tag differs: l1 has \"{}\", l2 has none", lang1.get());
            return 1;
        } else if (lang1.isPresent() && !lang1.get().equalsIgnoreCase(lang2.get())) {
            logger.debug("Language tag differs (case-insensitive): \"{}\" vs \"{}\"", lang1.get(), lang2.get());
            return lang1.get().toLowerCase().compareTo(lang2.get().toLowerCase());
        }
        logger.trace("Literals are fully equal: \"{}\"", l1);
        return 0;
    }

    private static int compareURIs(IRI uri1, IRI uri2) {
        return uri1.toString().compareTo(uri2.toString());
    }

}
