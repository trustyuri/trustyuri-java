#!/bin/sh

mvn -q exec:java -Dexec.mainClass="ch.tkuhn.hashuri.rdf.TransformNanopub" -Dexec.args="$*"
