Examples
========

Transform preliminary nanopub into final version:

    $ scripts/TransformNanopub.sh src/main/resources/examples/nanopub1-pre.trig

Check integrity of resulting file:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.RAcbjcRIQozo2wBMq4WcCYkFAjRz0AX-Ux3PquZZrC68s.trig
    Correct hash: RAcbjcRIQozo2wBMq4WcCYkFAjRz0AX-Ux3PquZZrC68s

Change creation time of generated file to "14:33:22" (line 29) and check again:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.RAcbjcRIQozo2wBMq4WcCYkFAjRz0AX-Ux3PquZZrC68s.trig
    *** INCORRECT HASH ***

Fetch and check nanopub via its URI (might be temporarily unavailable):

    $ scripts/CheckFile.sh http://purl.org/hashuri/examples/nanopub1.RAcbjcRIQozo2wBMq4WcCYkFAjRz0AX-Ux3PquZZrC68s.trig
    Correct hash: RAcbjcRIQozo2wBMq4WcCYkFAjRz0AX-Ux3PquZZrC68s

Load unchanged file `nanopub1.RAcbjc...` into local Virtuoso triple store and check via SPARQL:

    $ scripts/CheckNanopubViaSparql.sh http://localhost:8890/sparql http://purl.org/hashuri/examples/nanopub1.RAcbjcRIQozo2wBMq4WcCYkFAjRz0AX-Ux3PquZZrC68s
    Correct hash: RAcbjcRIQozo2wBMq4WcCYkFAjRz0AX-Ux3PquZZrC68s

`nanopub2` is a second example that cites the first one. `nanopub3` shows how blank nodes are
transformed.
