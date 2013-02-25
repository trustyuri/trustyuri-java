Examples
========

Transform preliminary nanopub into final version:

    $ scripts/TransformNanopub.sh src/main/resources/examples/nanopub1-pre.trig

Check integrity of resulting file:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.ALxsBP-3kmRikSxw19MPYbG6ssK3mH826KDLIOLpfUa0 
    Correct hash: ALxsBP-3kmRikSxw19MPYbG6ssK3mH826KDLIOLpfUa0

Change creation time of generated file to "14:33:22" (line 27) and check again:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.ALxsBP-3kmRikSxw19MPYbG6ssK3mH826KDLIOLpfUa0 
    *** INCORRECT HASH ***: AuOUH51T1GgEAgAP7kXB3aBOvG97jBIDM6B8qT2D-Aqk

Fetch and check nanopub via its URI:

    $ scripts/CheckFile.sh http://www.tkuhn.ch/hashrdf/examples/nanopub1.ALxsBP-3kmRikSxw19MPYbG6ssK3mH826KDLIOLpfUa0
    Correct hash: ALxsBP-3kmRikSxw19MPYbG6ssK3mH826KDLIOLpfUa0

Load unchanged file `nanopub1.ALxsB...` into local Virtuoso triple store and check via SPARQL:

    $ scripts/CheckNanopubViaSPARQL.sh http://localhost:8890/sparql http://www.tkuhn.ch/hashrdf/examples/nanopub1.ALxsBP-3kmRikSxw19MPYbG6ssK3mH826KDLIOLpfUa0
    Correct hash: ALxsBP-3kmRikSxw19MPYbG6ssK3mH826KDLIOLpfUa0
