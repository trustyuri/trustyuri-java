Examples
========

Transform preliminary nanopub into final version:

    $ scripts/TransformNanopub.sh src/main/resources/examples/nanopub1-pre.trig

Check integrity of resulting file:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.4jWMi-TQ9EQG2iLkBpM9QDJxjcw7rHJWhD-Sx1mibpY
    Correct hash: 4jWMi-TQ9EQG2iLkBpM9QDJxjcw7rHJWhD-Sx1mibpY

Change creation time of generated file to "14:33:22" (line 25) and check again:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.4jWMi-TQ9EQG2iLkBpM9QDJxjcw7rHJWhD-Sx1mibpY
    *** INCORRECT HASH ***: X43jqRnRCpb5_EcCxMiV-kr6QgWLUUKmwQp36ITMeLo

Fetch and check nanopub via its URI:

    $ scripts/CheckFile.sh src/main/resources/examples/nanopub1.4jWMi-TQ9EQG2iLkBpM9QDJxjcw7rHJWhD-Sx1mibpY
    Correct hash: 4jWMi-TQ9EQG2iLkBpM9QDJxjcw7rHJWhD-Sx1mibpY

Load unchanged file `nanopub1.4jWMi...` into local Virtuoso triple store and check via SPARQL:

    $ scripts/CheckNanopubViaSPARQL.sh http://localhost:8890/sparql http://www.tkuhn.ch/hashrdf/examples/nanopub1.4jWMi-TQ9EQG2iLkBpM9QDJxjcw7rHJWhD-Sx1mibpY
    Correct hash: 4jWMi-TQ9EQG2iLkBpM9QDJxjcw7rHJWhD-Sx1mibpY
