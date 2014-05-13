Examples
========

Transform preliminary nanopub into final version:

    $ scripts/TransformNanopub.sh src/main/resources/examples/nanopub1-pre.trig

Check integrity of resulting file:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.trig
    Correct hash: RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg

Change content of generated file and check again:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg.trig
    *** INCORRECT HASH ***

Load unchanged file `nanopub1.RAq2P3...` into local Virtuoso triple store and check via SPARQL:

    $ scripts/CheckNanopubViaSparql.sh http://localhost:8890/sparql http://trustyuri.net/examples/nanopub1.RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg
    Correct hash: RAq2P3suae730r_PPkfdmBhBIpqNO6763sJ0yMQWm6xVg

`nanopub2` is a second example that cites the first one. `nanopub3` shows how blank nodes are
transformed.
