#!/bin/sh

mvn -q exec:java -Dexec.mainClass="ch.tkuhn.hashrdf.CheckNanopubViaSPARQL" -Dexec.args="$*"
