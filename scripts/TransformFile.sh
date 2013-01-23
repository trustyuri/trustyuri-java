#!/bin/sh

mvn -q exec:java -Dexec.mainClass="ch.tkuhn.hashrdf.TransformFile" -Dexec.args="$*"
