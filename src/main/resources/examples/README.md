Examples
========

Transform preliminary nanopub into final version:

    $ scripts/TransformNanopub.sh src/main/resources/examples/nanopub1-pre.trig

Check integrity of resulting file:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.trig
    Correct hash: RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk

Change content of generated file and check again:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.trig
    *** INCORRECT HASH ***

Fetch and check nanopub via its URI (might be temporarily unavailable):

    $ scripts/CheckFile.sh http://purl.org/hashuri/examples/nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk.trig
    Correct hash: RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk

Load unchanged file `nanopub1.RA1Suh...` into local Virtuoso triple store and check via SPARQL:

    $ scripts/CheckNanopubViaSparql.sh http://localhost:8890/sparql http://purl.org/hashuri/examples/nanopub1.RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk
    Correct hash: RA1SuhYyX9qlNnWWIIp9dqjGEuyOi5D384gjVShN6SuXk

`nanopub2` is a second example that cites the first one. `nanopub3` shows how blank nodes are
transformed.
