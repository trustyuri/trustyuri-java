#!/bin/sh

mvn -e exec:java -Dexec.mainClass="ch.tkuhn.hashrdf.Test" -Dexec.args="$*"
