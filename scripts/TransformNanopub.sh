#!/bin/bash

cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..

mvn -q -e exec:java -Dexec.mainClass="ch.tkuhn.hashuri.rdf.TransformNanopub" -Dexec.args="$*"
